import SwiftUI

struct ConnectSheet: View {
    @EnvironmentObject private var store: SmartDashStore

    var body: some View {
        NavigationStack {
            List {
                Section("状态") {
                    Text(statusText)
                    Button {
                        store.ble.startScan()
                    } label: {
                        Label("扫描控制器", systemImage: "dot.radiowaves.left.and.right")
                    }
                    Button {
                        store.ble.stopScan()
                    } label: {
                        Label("停止扫描", systemImage: "stop.circle")
                    }
                    Button {
                        store.ble.readZhikeSettings()
                    } label: {
                        Label("读取智科参数", systemImage: "slider.horizontal.3")
                    }
                }

                Section("设备") {
                    ForEach(store.ble.devices) { device in
                        Button {
                            store.ble.connect(device)
                        } label: {
                            HStack {
                                VStack(alignment: .leading) {
                                    HStack {
                                        Text(device.name)
                                        if device.isLikelyController {
                                            Image(systemName: "checkmark.seal.fill")
                                                .foregroundStyle(.green)
                                        }
                                    }
                                    Text(device.id.uuidString)
                                        .font(.caption2)
                                        .foregroundStyle(.secondary)
                                    if !device.advertisedServices.isEmpty {
                                        Text(device.advertisedServices.joined(separator: ", "))
                                            .font(.caption2)
                                            .foregroundStyle(.secondary)
                                    }
                                }
                                Spacer()
                                Text("\(device.rssi)")
                                    .font(.caption.monospacedDigit())
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                }

                Section("写入日志") {
                    ForEach(store.ble.writeLog, id: \.self) { line in
                        Text(line)
                            .font(.caption.monospaced())
                    }
                }

                Section("扫描日志") {
                    ForEach(store.ble.scanLog, id: \.self) { line in
                        Text(line)
                            .font(.caption.monospaced())
                    }
                }
            }
            .navigationTitle("连接")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        store.ble.disconnect()
                    } label: {
                        Image(systemName: "xmark.circle")
                    }
                    .accessibilityLabel("断开")
                }
            }
        }
    }

    private var statusText: String {
        switch store.ble.state {
        case .idle: "未连接"
        case .scanning: "正在扫描附近 BLE 设备"
        case .connecting(let name): "正在连接 \(name)"
        case .connected(let name): "已连接 \(name)"
        case .disconnected: "已断开"
        case .failed(let message): message
        }
    }
}
