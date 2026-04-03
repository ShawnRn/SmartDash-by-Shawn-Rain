import CoreBluetooth
import Foundation

struct SimulatorSettings: Codable {
    var busCurrentLimit: Int = 80
    var phaseCurrentLimit: Int = 300
    var motorDirection: Bool = false
    var sensorType: Int = 0
    var hallSequence: Bool = false
    var phaseShiftAngle: Int = 0
    var polePairs: Int = 50
    var underVoltage: Int = 39
    var overVoltage: Int = 67
    var weakMagCurrent: Int = 0
    var regenCurrent: Int = 15
    var bluetoothPassword: Int = 8888
}

struct SimulatorStep: Codable {
    var name: String
    var durationSeconds: Double
    var voltage: Double
    var busCurrent: Double
    var phaseCurrent: Double
    var speedKmh: Double
    var controllerTemp: Double
    var faultCode: Int = 0
    var braking: Bool = false
    var cruise: Bool = false
    var reverse: Bool = false
}

struct SimulatorScenario: Codable {
    var name: String
    var updateHz: Double = 5
    var settings: SimulatorSettings = .init()
    var steps: [SimulatorStep]
}

private extension Data {
    func hexString() -> String {
        map { String(format: "%02X", $0) }.joined()
    }

    func chunks(of size: Int) -> [Data] {
        guard size > 0 else { return [self] }
        var result: [Data] = []
        var start = startIndex
        while start < endIndex {
            let end = index(start, offsetBy: size, limitedBy: endIndex) ?? endIndex
            result.append(self[start..<end])
            start = end
        }
        return result
    }
}

final class MockZhikeController: NSObject, CBPeripheralManagerDelegate {
    private let deviceName: String
    private let scenario: SimulatorScenario
    private var peripheralManager: CBPeripheralManager!
    private var realtimeCharacteristic: CBMutableCharacteristic?
    private var auxCharacteristic: CBMutableCharacteristic?
    private var handshakeCharacteristic: CBMutableCharacteristic?
    private var handshakeCharacteristic2: CBMutableCharacteristic?
    private var subscribedCentrals: [UUID: CBCentral] = [:]
    private var timer: DispatchSourceTimer?
    private var currentStepIndex = 0
    private var currentStepStartedAt = Date()

    init(deviceName: String, scenario: SimulatorScenario) {
        self.deviceName = deviceName
        self.scenario = scenario
        super.init()
        self.peripheralManager = CBPeripheralManager(delegate: self, queue: nil)
    }

    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .poweredOn:
            print("[mock-zhike] Bluetooth powered on")
            setupServices()
            startAdvertising()
            startRealtimeLoop()
        case .poweredOff:
            print("[mock-zhike] Bluetooth powered off")
        case .unsupported:
            print("[mock-zhike] Peripheral mode unsupported on this Mac")
        case .unauthorized:
            print("[mock-zhike] Bluetooth unauthorized; grant permission in System Settings")
        default:
            print("[mock-zhike] Bluetooth state changed: \(peripheral.state.rawValue)")
        }
    }

    func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral, didSubscribeTo characteristic: CBCharacteristic) {
        subscribedCentrals[central.identifier] = central
        print("[mock-zhike] central subscribed: \(central.identifier.uuidString) -> \(characteristic.uuid.uuidString)")
        sendRealtimeFrame(reason: "subscribe")
    }

    func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral, didUnsubscribeFrom characteristic: CBCharacteristic) {
        subscribedCentrals.removeValue(forKey: central.identifier)
        print("[mock-zhike] central unsubscribed: \(central.identifier.uuidString)")
    }

    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveRead request: CBATTRequest) {
        let payload = Data([0x01, 0x02])
        request.value = payload
        peripheral.respond(to: request, withResult: .success)
    }

    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveWrite requests: [CBATTRequest]) {
        for request in requests {
            let payload = request.value ?? Data()
            let hex = payload.hexString()
            print("[mock-zhike] write -> \(request.characteristic.uuid.uuidString) hex=\(hex)")

            if hex.contains("AA110001") {
                sendSettingsFrame()
            }
            if hex.contains("AA13FF01") || hex.contains("AA130001") {
                sendRealtimeFrame(reason: "poll")
            }
            if hex.contains("AA16004C58B3A7") {
                print("[mock-zhike] unlock handshake received")
            }

            peripheral.respond(to: request, withResult: .success)
        }
    }

    private func setupServices() {
        peripheralManager.removeAllServices()

        let ffe1 = CBMutableCharacteristic(
            type: CBUUID(string: "FFE1"),
            properties: [.notify, .write, .writeWithoutResponse],
            value: nil,
            permissions: [.readable, .writeable]
        )
        let ffe2 = CBMutableCharacteristic(
            type: CBUUID(string: "FFE2"),
            properties: [.write, .writeWithoutResponse],
            value: nil,
            permissions: [.writeable]
        )
        let mainService = CBMutableService(type: CBUUID(string: "FFE0"), primary: true)
        mainService.characteristics = [ffe1, ffe2]

        let fff1 = CBMutableCharacteristic(
            type: CBUUID(string: "FFF1"),
            properties: [.read],
            value: nil,
            permissions: [.readable]
        )
        let fff2 = CBMutableCharacteristic(
            type: CBUUID(string: "FFF2"),
            properties: [.read],
            value: nil,
            permissions: [.readable]
        )
        let handshakeService = CBMutableService(type: CBUUID(string: "FFF0"), primary: false)
        handshakeService.characteristics = [fff1, fff2]

        realtimeCharacteristic = ffe1
        auxCharacteristic = ffe2
        handshakeCharacteristic = fff1
        handshakeCharacteristic2 = fff2

        peripheralManager.add(mainService)
        peripheralManager.add(handshakeService)
    }

    private func startAdvertising() {
        peripheralManager.startAdvertising([
            CBAdvertisementDataLocalNameKey: deviceName,
            CBAdvertisementDataServiceUUIDsKey: [CBUUID(string: "FFE0")]
        ])
        print("[mock-zhike] advertising as \(deviceName)")
        print("[mock-zhike] scenario=\(scenario.name), updateHz=\(scenario.updateHz)")
    }

    private func startRealtimeLoop() {
        let timer = DispatchSource.makeTimerSource(queue: .main)
        let interval = max(0.05, 1.0 / max(1.0, scenario.updateHz))
        timer.schedule(deadline: .now() + interval, repeating: interval)
        timer.setEventHandler { [weak self] in
            self?.sendRealtimeFrame(reason: "timer")
        }
        timer.resume()
        self.timer = timer
    }

    private func currentStep() -> SimulatorStep {
        guard !scenario.steps.isEmpty else {
            return SimulatorStep(
                name: "idle",
                durationSeconds: 3,
                voltage: 54.2,
                busCurrent: 0,
                phaseCurrent: 0,
                speedKmh: 0,
                controllerTemp: 32
            )
        }

        var step = scenario.steps[currentStepIndex]
        let elapsed = Date().timeIntervalSince(currentStepStartedAt)
        if elapsed >= max(0.2, step.durationSeconds) {
            currentStepIndex = (currentStepIndex + 1) % scenario.steps.count
            currentStepStartedAt = Date()
            step = scenario.steps[currentStepIndex]
            print("[mock-zhike] step -> \(step.name)")
        }
        return step
    }

    private func sendSettingsFrame() {
        guard let characteristic = realtimeCharacteristic else { return }
        let payload = buildSettingsFrame()
        send(data: payload, through: characteristic, label: "settings")
    }

    private func sendRealtimeFrame(reason: String) {
        guard let characteristic = realtimeCharacteristic else { return }
        guard !subscribedCentrals.isEmpty else { return }
        let step = currentStep()
        let payload = buildRealtimeFrame(for: step)
        send(data: payload, through: characteristic, label: "realtime/\(reason)")
    }

    private func send(data: Data, through characteristic: CBMutableCharacteristic, label: String) {
        for chunk in data.chunks(of: 20) {
            let ok = peripheralManager.updateValue(chunk, for: characteristic, onSubscribedCentrals: nil)
            if !ok {
                print("[mock-zhike] updateValue back-pressure while sending \(label)")
                break
            }
        }
    }

    private func buildSettingsFrame() -> Data {
        var words = Array(repeating: UInt16(0), count: 64)
        let settings = scenario.settings
        words[0] = clampWord(settings.busCurrentLimit)
        words[1] = clampWord(settings.phaseCurrentLimit)
        words[2] = settings.motorDirection ? 1 : 0
        words[9] = clampWord(settings.underVoltage)
        words[10] = clampWord(settings.overVoltage)
        words[21] = clampWord(settings.weakMagCurrent)
        words[23] = clampWord(settings.regenCurrent)
        words[30] = clampWord(settings.sensorType)
        words[31] = settings.hallSequence ? 1 : 0
        words[32] = clampWord(Int(Double(settings.phaseShiftAngle) * 182.0444))
        words[34] = clampWord(settings.polePairs)
        words[60] = clampWord(settings.bluetoothPassword)

        let checksum = checksumWord(for: words.dropLast())
        words[63] = checksum

        var data = Data([0xAA, 0x11, 0x00])
        for word in words {
            data.append(UInt8(word & 0xFF))
            data.append(UInt8((word >> 8) & 0xFF))
        }
        return data
    }

    private func buildRealtimeFrame(for step: SimulatorStep) -> Data {
        var words = Array(repeating: UInt16(0), count: 32)
        words[6] = signedWord(Int((step.speedKmh * 10.0).rounded()))
        words[8] = clampWord(Int((step.voltage * 273.0666667).rounded()))
        words[9] = signedWord(Int(step.busCurrent.rounded()))
        words[17] = clampWord(Int((step.phaseCurrent * 146.0249554).rounded()))
        words[18] = signedWord(Int((step.controllerTemp * 100.0).rounded()))
        words[21] = ioStatusWord(for: step)
        words[22] = clampWord(step.faultCode)
        words[25] = 0
        words[31] = checksumWord(for: words.dropLast())

        var data = Data([0xAA, 0x13, 0x00])
        for word in words {
            data.append(UInt8(word & 0xFF))
            data.append(UInt8((word >> 8) & 0xFF))
        }
        return data
    }

    private func ioStatusWord(for step: SimulatorStep) -> UInt16 {
        var word = 0
        if step.reverse { word |= 0x04 }
        if step.braking { word |= 0x08 }
        if step.cruise { word |= 0x40 }
        return clampWord(word)
    }

    private func clampWord(_ value: Int) -> UInt16 {
        UInt16(max(0, min(0xFFFF, value)))
    }

    private func signedWord(_ value: Int) -> UInt16 {
        let clamped = max(-32768, min(32767, value))
        return UInt16(bitPattern: Int16(clamped))
    }

    private func checksumWord<S: Sequence>(for words: S) -> UInt16 where S.Element == UInt16 {
        let sum = words.reduce(0) { partial, next in (partial + Int(next)) & 0xFFFF }
        return UInt16((0xFFFF - sum) & 0xFFFF)
    }
}

private func loadScenario(from path: String?) throws -> SimulatorScenario {
    if let path, !path.isEmpty {
        let url = URL(fileURLWithPath: path)
        let data = try Data(contentsOf: url)
        return try JSONDecoder().decode(SimulatorScenario.self, from: data)
    }

    return SimulatorScenario(
        name: "default-sweep",
        updateHz: 6,
        settings: SimulatorSettings(),
        steps: [
            SimulatorStep(name: "idle", durationSeconds: 4, voltage: 54.4, busCurrent: 0, phaseCurrent: 0, speedKmh: 0, controllerTemp: 33),
            SimulatorStep(name: "walk", durationSeconds: 4, voltage: 53.9, busCurrent: 4, phaseCurrent: 18, speedKmh: 6, controllerTemp: 33.5),
            SimulatorStep(name: "cruise", durationSeconds: 6, voltage: 52.8, busCurrent: 18, phaseCurrent: 72, speedKmh: 35, controllerTemp: 36, cruise: true),
            SimulatorStep(name: "sprint", durationSeconds: 5, voltage: 50.7, busCurrent: 48, phaseCurrent: 190, speedKmh: 68, controllerTemp: 41),
            SimulatorStep(name: "regen", durationSeconds: 4, voltage: 54.8, busCurrent: -10, phaseCurrent: 12, speedKmh: 27, controllerTemp: 39, braking: true),
            SimulatorStep(name: "stop", durationSeconds: 4, voltage: 54.2, busCurrent: -1, phaseCurrent: 0, speedKmh: 0, controllerTemp: 37)
        ]
    )
}

private func argumentValue(_ flag: String) -> String? {
    let args = CommandLine.arguments
    guard let index = args.firstIndex(of: flag), index + 1 < args.count else { return nil }
    return args[index + 1]
}

let deviceName = argumentValue("--device-name") ?? "ZK-MOCK"
let scenarioPath = argumentValue("--scenario")

do {
    let scenario = try loadScenario(from: scenarioPath)
    _ = MockZhikeController(deviceName: deviceName, scenario: scenario)
    RunLoop.main.run()
} catch {
    fputs("[mock-zhike] failed: \(error)\n", stderr)
    exit(1)
}
