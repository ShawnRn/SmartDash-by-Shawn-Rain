import Combine
import Foundation
import UIKit

#if canImport(GoogleSignIn)
import GoogleSignIn
#endif

enum CloudSyncStatus: Equatable {
    case signedOut
    case ready(String)
    case syncing(String)
    case success(String)
    case failed(String)
    case configurationRequired(String)
}

struct DriveRemoteSummary: Equatable {
    var stateVersion: Int64
    var updatedAt: Int64
    var updatedByDeviceName: String
    var rideCount: Int
    var speedTestCount: Int
    var vehicleProfileCount: Int
}

protocol DriveTokenProvider {
    var signedInEmail: String? { get }
    func accessToken() async throws -> String
}

struct GoogleDriveConfiguration {
    static let driveAppDataScope = "https://www.googleapis.com/auth/drive.appdata"
    static let apiBase = URL(string: "https://www.googleapis.com")!
    static let uploadBase = URL(string: "https://www.googleapis.com/upload/drive/v3")!
    static let currentStateFileName = "current_state.json.enc"
    static let manifestFileName = "habe_metadata.json"
    static let backupPrefix = "habe_backup_"
}

@MainActor
final class GoogleDriveAccountService: ObservableObject, DriveTokenProvider {
    @Published private(set) var status: CloudSyncStatus = .configurationRequired("需要配置 iOS OAuth Client ID 与 URL Scheme")

    var signedInEmail: String? {
        #if canImport(GoogleSignIn)
        GIDSignIn.sharedInstance.currentUser?.profile?.email
        #else
        nil
        #endif
    }

    func restore() {
        #if canImport(GoogleSignIn)
        guard hasGoogleClientConfiguration else {
            status = .configurationRequired("请先配置 iOS OAuth Client ID / URL Scheme")
            return
        }
        GIDSignIn.sharedInstance.restorePreviousSignIn { [weak self] user, error in
            let email = user?.profile?.email
            let errorMessage = error?.localizedDescription
            Task { @MainActor [weak self] in
                guard let self else { return }
                if let email {
                    self.status = .ready(email)
                } else {
                    self.status = .signedOut
                    if let errorMessage { self.status = .failed(errorMessage) }
                }
            }
        }
        #else
        status = .configurationRequired("GoogleSignIn-iOS SPM 尚未加入工程")
        #endif
    }

    func signIn() async {
        #if canImport(GoogleSignIn)
        guard hasGoogleClientConfiguration else {
            status = .configurationRequired("请先配置 GIDClientID 与 URL Scheme")
            return
        }
        guard let presenter = UIApplication.shared.smartDashTopViewController else {
            status = .failed("无法打开 Google 登录窗口")
            return
        }
        status = .syncing("正在登录 Google")
        do {
            let result = try await GIDSignIn.sharedInstance.signIn(
                withPresenting: presenter,
                hint: nil,
                additionalScopes: [GoogleDriveConfiguration.driveAppDataScope]
            )
            status = .ready(result.user.profile?.email ?? "Google 已登录")
        } catch {
            status = .failed(error.localizedDescription)
        }
        #else
        status = .configurationRequired("GoogleSignIn-iOS SPM 尚未加入工程")
        #endif
    }

    func signOut() {
        #if canImport(GoogleSignIn)
        GIDSignIn.sharedInstance.signOut()
        #endif
        status = .signedOut
    }

    func handle(url: URL) -> Bool {
        #if canImport(GoogleSignIn)
        GIDSignIn.sharedInstance.handle(url)
        #else
        false
        #endif
    }

    func accessToken() async throws -> String {
        #if canImport(GoogleSignIn)
        guard let user = GIDSignIn.sharedInstance.currentUser else {
            throw URLError(.userAuthenticationRequired)
        }
        return try await withCheckedThrowingContinuation { continuation in
            user.refreshTokensIfNeeded { user, error in
                if let error {
                    continuation.resume(throwing: error)
                } else if let token = user?.accessToken.tokenString {
                    continuation.resume(returning: token)
                } else {
                    continuation.resume(throwing: URLError(.userAuthenticationRequired))
                }
            }
        }
        #else
        throw URLError(.userAuthenticationRequired)
        #endif
    }

    private var hasGoogleClientConfiguration: Bool {
        Bundle.main.object(forInfoDictionaryKey: "GIDClientID") as? String != nil
    }
}

struct GoogleDriveSyncService {
    var tokenProvider: DriveTokenProvider
    var session: URLSession = .shared

    func downloadCurrentState(passwordEmail: String) async throws -> DriveCurrentState? {
        guard let fileId = try await findFile(named: GoogleDriveConfiguration.currentStateFileName) else {
            return nil
        }
        let encryptedData = try await download(fileId: fileId)
        let backup = try JSONDecoder().decode(EncryptedBackup.self, from: encryptedData)
        let password = DriveEncryptionService().password(fromGoogleEmail: passwordEmail)
        let plain = try DriveEncryptionService().decrypt(backup, password: password)
        return try DriveStateSerializer().deserialize(plain)
    }

    func remoteSummary(passwordEmail: String) async throws -> DriveRemoteSummary? {
        guard let state = try await downloadCurrentState(passwordEmail: passwordEmail) else { return nil }
        return DriveRemoteSummary(
            stateVersion: state.stateVersion,
            updatedAt: state.updatedAt,
            updatedByDeviceName: state.updatedByDeviceName,
            rideCount: state.rides.filter { !$0.isDeleted }.count,
            speedTestCount: state.speedTests.filter { !$0.isDeleted }.count,
            vehicleProfileCount: state.vehicleProfiles.filter { !$0.isDeleted }.count
        )
    }

    func uploadCurrentState(_ state: DriveCurrentState, passwordEmail: String) async throws {
        let plain = try DriveStateSerializer().serialize(state)
        let password = DriveEncryptionService().password(fromGoogleEmail: passwordEmail)
        let encrypted = try DriveEncryptionService().encrypt(plainText: plain, password: password)
        let data = try JSONEncoder().encode(encrypted)
        if let fileId = try await findFile(named: GoogleDriveConfiguration.currentStateFileName) {
            try await update(fileId: fileId, content: data)
        } else {
            _ = try await create(name: GoogleDriveConfiguration.currentStateFileName, content: data, mimeType: "application/octet-stream")
        }
        try await uploadManifest(for: state, plaintext: plain)
    }

    private func uploadManifest(for state: DriveCurrentState, plaintext: Data) async throws {
        let manifest = DriveChangeManifest(
            stateVersion: state.stateVersion,
            updatedAt: state.updatedAt,
            updatedByDeviceId: state.updatedByDeviceId,
            updatedByDeviceName: state.updatedByDeviceName,
            checksum: DriveStateSerializer().checksum(plaintext),
            latestRideId: state.rides.max(by: { $0.endedAtMs < $1.endedAtMs })?.id,
            latestRideEndedAt: state.rides.map(\.endedAtMs).max(),
            latestSpeedTestId: state.speedTests.max(by: { $0.endedAtMs < $1.endedAtMs })?.id,
            entityCounters: EntityCounters(
                rideCount: state.rides.filter { !$0.isDeleted }.count,
                speedTestCount: state.speedTests.filter { !$0.isDeleted }.count,
                vehicleProfileCount: state.vehicleProfiles.filter { !$0.isDeleted }.count
            )
        )
        let data = try JSONEncoder().encode(manifest)
        if let fileId = try await findFile(named: GoogleDriveConfiguration.manifestFileName) {
            try await update(fileId: fileId, content: data, mimeType: "application/json")
        } else {
            _ = try await create(name: GoogleDriveConfiguration.manifestFileName, content: data, mimeType: "application/json")
        }
    }

    private func findFile(named name: String) async throws -> String? {
        var components = URLComponents(url: GoogleDriveConfiguration.apiBase.appendingPathComponent("drive/v3/files"), resolvingAgainstBaseURL: false)!
        components.queryItems = [
            URLQueryItem(name: "q", value: "name = '\(name)' and trashed = false"),
            URLQueryItem(name: "spaces", value: "appDataFolder"),
            URLQueryItem(name: "fields", value: "files(id,name,modifiedTime)")
        ]
        let data = try await request(url: components.url!, method: "GET")
        let list = try JSONDecoder().decode(DriveFileList.self, from: data)
        return list.files.first?.id
    }

    private func download(fileId: String) async throws -> Data {
        var components = URLComponents(url: GoogleDriveConfiguration.apiBase.appendingPathComponent("drive/v3/files/\(fileId)"), resolvingAgainstBaseURL: false)!
        components.queryItems = [URLQueryItem(name: "alt", value: "media")]
        return try await request(url: components.url!, method: "GET")
    }

    private func create(name: String, content: Data, mimeType: String) async throws -> String {
        let boundary = "SmartDashBoundary\(UUID().uuidString)"
        var body = Data()
        body.append("--\(boundary)\r\n")
        body.append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
        body.append("{\"name\":\"\(name)\",\"parents\":[\"appDataFolder\"]}\r\n")
        body.append("--\(boundary)\r\n")
        body.append("Content-Type: \(mimeType)\r\n\r\n")
        body.append(content)
        body.append("\r\n--\(boundary)--\r\n")

        var url = GoogleDriveConfiguration.uploadBase.appendingPathComponent("files")
        url.append(queryItems: [URLQueryItem(name: "uploadType", value: "multipart")])
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("multipart/related; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        request.httpBody = body
        let response = try await authorized(request)
        let created = try JSONDecoder().decode(DriveFile.self, from: response)
        return created.id
    }

    private func update(fileId: String, content: Data, mimeType: String = "application/octet-stream") async throws {
        var request = URLRequest(url: GoogleDriveConfiguration.uploadBase.appendingPathComponent("files/\(fileId)").appending(queryItems: [URLQueryItem(name: "uploadType", value: "media")]))
        request.httpMethod = "PATCH"
        request.httpBody = content
        request.setValue(mimeType, forHTTPHeaderField: "Content-Type")
        _ = try await authorized(request)
    }

    private func request(url: URL, method: String) async throws -> Data {
        var request = URLRequest(url: url)
        request.httpMethod = method
        return try await authorized(request)
    }

    private func authorized(_ request: URLRequest) async throws -> Data {
        var request = request
        let token = try await tokenProvider.accessToken()
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) else {
            throw URLError(.badServerResponse)
        }
        return data
    }
}

private extension UIApplication {
    var smartDashTopViewController: UIViewController? {
        connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first { $0.isKeyWindow }?
            .rootViewController?
            .topMost
    }
}

private extension UIViewController {
    var topMost: UIViewController {
        if let presentedViewController {
            return presentedViewController.topMost
        }
        if let navigationController = self as? UINavigationController {
            return navigationController.visibleViewController?.topMost ?? navigationController
        }
        if let tabBarController = self as? UITabBarController {
            return tabBarController.selectedViewController?.topMost ?? tabBarController
        }
        return self
    }
}

private struct DriveFileList: Codable {
    var files: [DriveFile]
}

private struct DriveFile: Codable {
    var id: String
    var name: String?
}

private extension Data {
    mutating func append(_ string: String) {
        append(Data(string.utf8))
    }
}
