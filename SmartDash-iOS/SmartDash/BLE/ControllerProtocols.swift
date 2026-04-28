import Foundation

protocol ControllerProtocol {
    var id: String { get }
    var displayName: String { get }
    mutating func parse(_ data: Data, frameId: Int64) -> VehicleMetrics?
    func realtimePollCommand() -> Data?
    func readSettingsCommand() -> Data?
}

struct ProtocolParser {
    private var zhike = ZhikeControllerProtocol()
    private var apt = AptAusiControllerProtocol()
    private var yuanqu = YuanquControllerProtocol()
    private(set) var latestMetrics = VehicleMetrics()
    private var frameId: Int64 = 0

    mutating func parse(_ data: Data) -> VehicleMetrics? {
        frameId += 1
        if let metrics = zhike.parse(data, frameId: frameId) {
            latestMetrics = metrics
            return metrics
        }
        if let metrics = apt.parse(data, frameId: frameId) {
            latestMetrics = metrics
            return metrics
        }
        if let metrics = yuanqu.parse(data, frameId: frameId) {
            latestMetrics = metrics
            return metrics
        }
        return nil
    }
}

struct ZhikeControllerProtocol: ControllerProtocol {
    var id: String { "zhike" }
    var displayName: String { "智科" }
    private var zeroFrameCount = 0
    private var lastMetrics: VehicleMetrics?

    func realtimePollCommand() -> Data? {
        Data([0xAA, 0x13, 0xFF, 0x01, 0xAA, 0x13, 0x00, 0x01])
    }

    func readSettingsCommand() -> Data? {
        Data([0xAA, 0x11, 0x00, 0x01])
    }

    mutating func parse(_ data: Data, frameId: Int64) -> VehicleMetrics? {
        let bytes = [UInt8](data)
        guard bytes.count >= 67, bytes.first == 0xAA, bytes.dropFirst().contains(0x13) else {
            return nil
        }

        func word(_ index: Int) -> UInt16 {
            let offset = 3 + index * 2
            guard offset + 1 < bytes.count else { return 0 }
            return UInt16(bytes[offset]) << 8 | UInt16(bytes[offset + 1])
        }

        let rpm = Double(word(6)) / 5.46
        let voltage = Double(word(8)) / 273.0666667
        let busCurrent = Double(Int16(bitPattern: word(9)))
        let phaseCurrent = Double(Int16(bitPattern: word(10)))
        let motorTemp = Double(Int16(bitPattern: word(12))) / 100.0
        let controllerTemp = Double(Int16(bitPattern: word(18))) / 100.0
        let faultCode = Int(word(22))
        let ioStatus = word(23)
        let speed = max(0, rpm / 50.0 * 1.8 * 60.0 / 1000.0)

        let zeroLike = voltage < 2 && abs(busCurrent) < 0.1 && rpm < 1 && speed < 0.1
        if zeroLike {
            zeroFrameCount += 1
            return nil
        }
        zeroFrameCount = 0

        let metrics = VehicleMetrics(
            voltage: voltage,
            busCurrent: busCurrent,
            phaseCurrent: phaseCurrent,
            rpm: rpm,
            speedKmh: speed,
            motorTemp: motorTemp,
            controllerTemp: controllerTemp,
            faultCode: faultCode,
            braking: (ioStatus & 0x01) != 0,
            cruise: (ioStatus & 0x02) != 0,
            reverse: (ioStatus & 0x04) != 0,
            protocolId: id,
            sourceFrameId: frameId,
            timestampMs: Date.nowMs
        )
        lastMetrics = metrics
        return metrics
    }
}

struct AptAusiControllerProtocol: ControllerProtocol {
    var id: String { "apt-ausi" }
    var displayName: String { "安能特" }
    mutating func parse(_ data: Data, frameId: Int64) -> VehicleMetrics? { nil }
    func realtimePollCommand() -> Data? { nil }
    func readSettingsCommand() -> Data? { nil }
}

struct YuanquControllerProtocol: ControllerProtocol {
    var id: String { "yuanqu" }
    var displayName: String { "远驱" }
    mutating func parse(_ data: Data, frameId: Int64) -> VehicleMetrics? { nil }
    func realtimePollCommand() -> Data? { nil }
    func readSettingsCommand() -> Data? { nil }
}

struct ZhikeParameterDefinition: Identifiable, Equatable {
    var id: String
    var title: String
    var wordIndex: Int
    var unit: String
    var minimum: Double
    var maximum: Double
    var defaultValue: Double
}

enum ZhikeParameterCatalog {
    static let parameters: [ZhikeParameterDefinition] = [
        .init(id: "bus_current", title: "母线电流", wordIndex: 0, unit: "A", minimum: 1, maximum: 300, defaultValue: 25),
        .init(id: "phase_current", title: "相线电流", wordIndex: 1, unit: "A", minimum: 1, maximum: 600, defaultValue: 80),
        .init(id: "motor_direction", title: "电机方向", wordIndex: 2, unit: "", minimum: 0, maximum: 1, defaultValue: 0),
        .init(id: "under_voltage", title: "欠压保护", wordIndex: 9, unit: "V", minimum: 20, maximum: 120, defaultValue: 39),
        .init(id: "over_voltage", title: "过压保护", wordIndex: 10, unit: "V", minimum: 20, maximum: 120, defaultValue: 84),
        .init(id: "weak_magnet_current", title: "弱磁电流", wordIndex: 21, unit: "A", minimum: 0, maximum: 200, defaultValue: 15),
        .init(id: "regen_current", title: "反峰制动电流", wordIndex: 23, unit: "A", minimum: 0, maximum: 200, defaultValue: 20),
        .init(id: "sensor_type", title: "传感器类型", wordIndex: 30, unit: "", minimum: 0, maximum: 3, defaultValue: 0),
        .init(id: "hall_sequence", title: "霍尔序", wordIndex: 31, unit: "", minimum: 0, maximum: 1, defaultValue: 0),
        .init(id: "phase_shift_angle", title: "相位角", wordIndex: 32, unit: "°", minimum: 0, maximum: 360, defaultValue: 120),
        .init(id: "pole_pairs", title: "极对数", wordIndex: 34, unit: "", minimum: 1, maximum: 200, defaultValue: 50),
        .init(id: "bluetooth_password", title: "蓝牙密码", wordIndex: 60, unit: "", minimum: 0, maximum: 999999, defaultValue: 123456)
    ]
}
