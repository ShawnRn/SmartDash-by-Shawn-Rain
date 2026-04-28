import CryptoKit
import Foundation
import CommonCrypto
import Security
import zlib

enum DriveCodecError: Error {
    case keyDerivationFailed
    case invalidPayload
    case unsupportedVersion(Int)
    case compressionFailed
}

struct DriveStateSerializer {
    private let encoder: JSONEncoder
    private let decoder: JSONDecoder

    init() {
        encoder = JSONEncoder()
        encoder.outputFormatting = [.withoutEscapingSlashes]
        decoder = JSONDecoder()
    }

    func serialize(_ state: DriveCurrentState) throws -> Data {
        try encoder.encode(state)
    }

    func deserialize(_ data: Data) throws -> DriveCurrentState {
        try decoder.decode(DriveCurrentState.self, from: data)
    }

    func checksum(_ data: Data) -> String {
        SHA256.hash(data: data).map { String(format: "%02x", $0) }.joined()
    }
}

struct DriveEncryptionService {
    static let versionPasswordFixedSaltLegacy = 2
    static let versionPasswordRandomSalt = 3
    static let versionPasswordRandomSaltGzip = 4
    static let iterations = 100_000
    static let keyByteCount = 32

    func password(fromGoogleEmail email: String) -> String {
        let digest = SHA256.hash(data: Data(email.utf8))
        return Data(digest).base64EncodedString()
    }

    func encrypt(plainText: Data, password: String, gzip: Bool = true) throws -> EncryptedBackup {
        let salt = randomData(count: 16)
        let key = try deriveKey(password: password, salt: salt)
        let nonce = AES.GCM.Nonce()
        let payload = gzip ? try gzipData(plainText) : plainText
        let sealed = try AES.GCM.seal(payload, using: key, nonce: nonce)
        let nonceData = nonce.withUnsafeBytes { Data($0) }

        return EncryptedBackup(
            version: gzip ? Self.versionPasswordRandomSaltGzip : Self.versionPasswordRandomSalt,
            salt: salt.base64NoPadding,
            iv: nonceData.base64NoPadding,
            cipherText: sealed.ciphertext.base64NoPadding,
            tag: sealed.tag.base64NoPadding
        )
    }

    func decrypt(_ backup: EncryptedBackup, password: String) throws -> Data {
        guard backup.version >= Self.versionPasswordFixedSaltLegacy else {
            throw DriveCodecError.unsupportedVersion(backup.version)
        }
        let salt = backup.version == Self.versionPasswordFixedSaltLegacy
            ? Data(password.utf8)
            : try Data(base64NoPadding: backup.salt)
        let iv = try Data(base64NoPadding: backup.iv)
        let cipherText = try Data(base64NoPadding: backup.cipherText)
        let tag = try Data(base64NoPadding: backup.tag)
        let key = try deriveKey(password: password, salt: salt)
        let box = try AES.GCM.SealedBox(nonce: AES.GCM.Nonce(data: iv), ciphertext: cipherText, tag: tag)
        let plain = try AES.GCM.open(box, using: key)
        return backup.version == Self.versionPasswordRandomSaltGzip ? try gunzipData(plain) : plain
    }

    private func deriveKey(password: String, salt: Data) throws -> SymmetricKey {
        var key = Data(repeating: 0, count: Self.keyByteCount)
        let result = key.withUnsafeMutableBytes { keyBytes in
            salt.withUnsafeBytes { saltBytes in
                CCKeyDerivationPBKDF(
                    CCPBKDFAlgorithm(kCCPBKDF2),
                    password,
                    password.utf8.count,
                    saltBytes.bindMemory(to: UInt8.self).baseAddress,
                    salt.count,
                    CCPseudoRandomAlgorithm(kCCPRFHmacAlgSHA256),
                    UInt32(Self.iterations),
                    keyBytes.bindMemory(to: UInt8.self).baseAddress,
                    Self.keyByteCount
                )
            }
        }
        guard result == kCCSuccess else { throw DriveCodecError.keyDerivationFailed }
        return SymmetricKey(data: key)
    }

    private func randomData(count: Int) -> Data {
        var bytes = [UInt8](repeating: 0, count: count)
        _ = SecRandomCopyBytes(kSecRandomDefault, count, &bytes)
        return Data(bytes)
    }
}

private func gzipData(_ data: Data) throws -> Data {
    try zlibTransform(data, mode: .encode)
}

private func gunzipData(_ data: Data) throws -> Data {
    try zlibTransform(data, mode: .decode)
}

private enum ZlibMode {
    case encode
    case decode
}

private func zlibTransform(_ data: Data, mode: ZlibMode) throws -> Data {
    if data.isEmpty { return Data() }
    let chunkSize = 64 * 1024
    var stream = z_stream()
    let windowBits: Int32 = 15 + 16
    let initStatus: Int32
    switch mode {
    case .encode:
        initStatus = deflateInit2_(&stream, Int32(Z_DEFAULT_COMPRESSION), Int32(Z_DEFLATED), windowBits, 8, Int32(Z_DEFAULT_STRATEGY), ZLIB_VERSION, Int32(MemoryLayout<z_stream>.size))
    case .decode:
        initStatus = inflateInit2_(&stream, windowBits, ZLIB_VERSION, Int32(MemoryLayout<z_stream>.size))
    }
    guard initStatus == Z_OK else { throw DriveCodecError.compressionFailed }
    defer {
        switch mode {
        case .encode: deflateEnd(&stream)
        case .decode: inflateEnd(&stream)
        }
    }

    return try data.withUnsafeBytes { sourceBuffer in
        guard let source = sourceBuffer.bindMemory(to: Bytef.self).baseAddress else { return Data() }
        stream.next_in = UnsafeMutablePointer<Bytef>(mutating: source)
        stream.avail_in = uInt(data.count)
        var output = Data()
        let chunk = UnsafeMutablePointer<Bytef>.allocate(capacity: chunkSize)
        defer { chunk.deallocate() }

        var status: Int32
        repeat {
            stream.next_out = chunk
            stream.avail_out = uInt(chunkSize)
            switch mode {
            case .encode:
                status = deflate(&stream, Z_FINISH)
            case .decode:
                status = inflate(&stream, Z_NO_FLUSH)
            }
            guard status != Z_STREAM_ERROR && status != Z_DATA_ERROR && status != Z_MEM_ERROR else {
                throw DriveCodecError.compressionFailed
            }
            output.append(chunk, count: chunkSize - Int(stream.avail_out))
        } while status != Z_STREAM_END
        return output
    }
}

extension Data {
    var base64NoPadding: String {
        base64EncodedString().replacingOccurrences(of: "=", with: "")
    }

    init(base64NoPadding value: String) throws {
        var padded = value
        let remainder = padded.count % 4
        if remainder > 0 {
            padded += String(repeating: "=", count: 4 - remainder)
        }
        guard let data = Data(base64Encoded: padded) else {
            throw DriveCodecError.invalidPayload
        }
        self = data
    }
}
