@preconcurrency import CoreBluetooth
import Combine
import Foundation

enum BLEConnectionState: Equatable {
    case idle
    case scanning
    case connecting(String)
    case connected(String)
    case disconnected
    case failed(String)
}

struct DiscoveredBLEDevice: Identifiable, Equatable {
    var id: UUID
    var name: String
    var rssi: Int
    var advertisedServices: [String] = []
    var isLikelyController: Bool = false
}

@MainActor
final class BLEManager: NSObject, ObservableObject {
    @Published private(set) var state: BLEConnectionState = .idle
    @Published private(set) var devices: [DiscoveredBLEDevice] = []
    @Published private(set) var latestMetrics = VehicleMetrics()
    @Published private(set) var writeLog: [String] = []
    @Published private(set) var scanLog: [String] = []

    private var central: CBCentralManager!
    private var peripheral: CBPeripheral?
    private var discoveredPeripherals: [UUID: CBPeripheral] = [:]
    private var writeCharacteristic: CBCharacteristic?
    private var notifyCharacteristic: CBCharacteristic?
    private var parser = ProtocolParser()

    private let serviceUUID = CBUUID(string: "FFE0")
    private let mainCharacteristicUUID = CBUUID(string: "FFE1")
    private let auxCharacteristicUUID = CBUUID(string: "FFE2")

    override init() {
        super.init()
        central = CBCentralManager(delegate: self, queue: nil)
    }

    func startScan() {
        guard central.state == .poweredOn else {
            state = .failed(centralStateText(central.state))
            return
        }
        central.stopScan()
        devices.removeAll()
        scanLog.removeAll()
        discoveredPeripherals.removeAll()
        state = .scanning
        appendScanLog("开始扫描：不过滤 Service UUID，兼容不广播 FFE0 的控制器")
        central.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
        Task { [weak self] in
            try? await Task.sleep(nanoseconds: 10_000_000_000)
            self?.stopScan()
        }
    }

    func stopScan() {
        central.stopScan()
        if case .scanning = state {
            state = devices.isEmpty ? .failed("未发现 BLE 设备，请确认控制器已上电且靠近手机") : .idle
        }
    }

    func connect(_ device: DiscoveredBLEDevice) {
        guard let peripheral = discoveredPeripherals[device.id] ?? central.retrievePeripherals(withIdentifiers: [device.id]).first else { return }
        central.stopScan()
        self.peripheral = peripheral
        writeCharacteristic = nil
        notifyCharacteristic = nil
        peripheral.delegate = self
        state = .connecting(device.name)
        appendScanLog("连接 \(device.name)")
        central.connect(peripheral)
    }

    func disconnect() {
        if let peripheral {
            central.cancelPeripheralConnection(peripheral)
        }
        writeCharacteristic = nil
        notifyCharacteristic = nil
        state = .disconnected
    }

    func sendZhikeRealtimePoll() {
        write(Data([0xAA, 0x13, 0xFF, 0x01, 0xAA, 0x13, 0x00, 0x01]), label: "实时轮询")
    }

    func readZhikeSettings() {
        write(Data([0xAA, 0x11, 0x00, 0x01]), label: "读取参数")
    }

    private func write(_ data: Data, label: String) {
        guard let peripheral, let writeCharacteristic else {
            writeLog.insert("\(label): 写特征未就绪", at: 0)
            return
        }
        let type: CBCharacteristicWriteType = writeCharacteristic.properties.contains(.write) ? .withResponse : .withoutResponse
        peripheral.writeValue(data, for: writeCharacteristic, type: type)
        writeLog.insert("\(label): \(data.hexString)", at: 0)
        writeLog = Array(writeLog.prefix(50))
    }

    private func appendScanLog(_ line: String) {
        scanLog.insert(line, at: 0)
        scanLog = Array(scanLog.prefix(80))
    }

    private func centralStateText(_ state: CBManagerState) -> String {
        switch state {
        case .unknown: "蓝牙状态未知"
        case .resetting: "蓝牙正在重置"
        case .unsupported: "此设备不支持 BLE"
        case .unauthorized: "没有蓝牙权限，请在系统设置中允许 SmartDash 使用蓝牙"
        case .poweredOff: "蓝牙已关闭"
        case .poweredOn: "蓝牙可用"
        @unknown default: "蓝牙不可用"
        }
    }
}

extension BLEManager: CBCentralManagerDelegate {
    nonisolated func centralManagerDidUpdateState(_ central: CBCentralManager) {
        Task { @MainActor in
            if central.state == .poweredOn {
                if case .failed = state { state = .idle }
                appendScanLog("蓝牙已就绪")
            } else {
                state = .failed(centralStateText(central.state))
                appendScanLog(centralStateText(central.state))
            }
        }
    }

    nonisolated func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi RSSI: NSNumber) {
        Task { @MainActor in
            let name = peripheral.name ?? advertisementData[CBAdvertisementDataLocalNameKey] as? String ?? "Unknown"
            let serviceUUIDs = (advertisementData[CBAdvertisementDataServiceUUIDsKey] as? [CBUUID]) ?? []
            let serviceStrings = serviceUUIDs.map(\.uuidString)
            let likely = serviceUUIDs.contains(serviceUUID) ||
                name.localizedCaseInsensitiveContains("zhike") ||
                name.localizedCaseInsensitiveContains("zk") ||
                name.localizedCaseInsensitiveContains("ble") ||
                name.localizedCaseInsensitiveContains("controller")
            let device = DiscoveredBLEDevice(
                id: peripheral.identifier,
                name: name,
                rssi: RSSI.intValue,
                advertisedServices: serviceStrings,
                isLikelyController: likely
            )
            discoveredPeripherals[peripheral.identifier] = peripheral
            if let index = devices.firstIndex(where: { $0.id == device.id }) {
                devices[index] = device
            } else {
                devices.append(device)
                appendScanLog("发现 \(name) RSSI \(RSSI.intValue)\(serviceStrings.isEmpty ? "" : " services \(serviceStrings.joined(separator: ","))")")
            }
            devices.sort { lhs, rhs in
                if lhs.isLikelyController != rhs.isLikelyController { return lhs.isLikelyController && !rhs.isLikelyController }
                return lhs.rssi > rhs.rssi
            }
        }
    }

    nonisolated func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        Task { @MainActor in
            state = .connected(peripheral.name ?? "Controller")
            appendScanLog("已连接，发现服务")
            peripheral.discoverServices(nil)
        }
    }

    nonisolated func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        Task { @MainActor in state = .failed(error?.localizedDescription ?? "连接失败") }
    }

    nonisolated func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        Task { @MainActor in state = .disconnected }
    }
}

extension BLEManager: CBPeripheralDelegate {
    nonisolated func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        Task { @MainActor in
            if let error {
                state = .failed(error.localizedDescription)
                return
            }
            let services = peripheral.services ?? []
            appendScanLog("服务 \(services.map { $0.uuid.uuidString }.joined(separator: ","))")
            services.forEach { service in
                peripheral.discoverCharacteristics(nil, for: service)
            }
        }
    }

    nonisolated func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        Task { @MainActor in
            for characteristic in service.characteristics ?? [] {
                appendScanLog("特征 \(characteristic.uuid.uuidString) props \(characteristic.properties.rawValue)")
                let isZhikeCharacteristic = characteristic.uuid == mainCharacteristicUUID || characteristic.uuid == auxCharacteristicUUID
                if characteristic.properties.contains(.notify) && (isZhikeCharacteristic || notifyCharacteristic == nil) {
                    notifyCharacteristic = characteristic
                    peripheral.setNotifyValue(true, for: characteristic)
                    appendScanLog("订阅通知 \(characteristic.uuid.uuidString)")
                }
                if characteristic.properties.contains(.write) || characteristic.properties.contains(.writeWithoutResponse) {
                    if isZhikeCharacteristic || writeCharacteristic == nil {
                        writeCharacteristic = characteristic
                        appendScanLog("选择写特征 \(characteristic.uuid.uuidString)")
                    }
                }
            }
            if writeCharacteristic != nil {
                sendZhikeRealtimePoll()
            }
        }
    }

    nonisolated func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        guard let data = characteristic.value else { return }
        Task { @MainActor in
            if let metrics = parser.parse(data) {
                latestMetrics = metrics
            }
        }
    }
}

private extension Data {
    var hexString: String {
        map { String(format: "%02X", $0) }.joined(separator: " ")
    }
}
