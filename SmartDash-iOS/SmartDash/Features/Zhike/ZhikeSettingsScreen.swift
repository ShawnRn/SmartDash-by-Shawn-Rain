import SwiftUI

struct ZhikeSettingsScreen: View {
    @EnvironmentObject private var store: SmartDashStore
    @State private var values: [String: Double] = Dictionary(uniqueKeysWithValues: ZhikeParameterCatalog.parameters.map { ($0.id, $0.defaultValue) })

    var body: some View {
        List {
            Section("设备") {
                Button {
                    store.ble.readZhikeSettings()
                } label: {
                    Label("读取参数 AA 11 00 01", systemImage: "arrow.down.doc")
                }
                Button {
                    store.ble.sendZhikeRealtimePoll()
                } label: {
                    Label("发送实时轮询", systemImage: "waveform.path.ecg")
                }
            }

            Section("参数") {
                ForEach(ZhikeParameterCatalog.parameters) { definition in
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(definition.title)
                            Spacer()
                            Text("\((values[definition.id] ?? definition.defaultValue).formatted(0))\(definition.unit)")
                                .foregroundStyle(.secondary)
                        }
                        Slider(
                            value: Binding(
                                get: { values[definition.id] ?? definition.defaultValue },
                                set: { values[definition.id] = $0 }
                            ),
                            in: definition.minimum...definition.maximum,
                            step: 1
                        )
                    }
                    .padding(.vertical, 4)
                }
            }

            Section("写入") {
                Button {
                    store.log.info("智科写入流程已准备：解锁 -> AA 12 payload -> 写后校验")
                } label: {
                    Label("写入并校验", systemImage: "checkmark.seal")
                }
                Text("iOS 版保留 Android 同一参数 catalog 与写入路径；实车写入需连接 FFE1 写特征。")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
        }
        .navigationTitle("智科调参")
    }
}
