import SwiftUI

struct SettingsScreen: View {
    @EnvironmentObject private var store: SmartDashStore
    @State private var selectedVehicleId: String = ""

    var body: some View {
        NavigationStack {
            List {
                Section("车辆") {
                    Picker("当前车辆", selection: $store.persisted.activeVehicleProfileId) {
                        ForEach(store.persisted.vehicleProfiles) { profile in
                            Text(profile.name).tag(profile.id)
                        }
                    }
                    NavigationLink("车辆档案") { VehicleProfilesScreen() }
                    NavigationLink("GPS 轮径校准") { CalibrationScreen() }
                }

                Section("调参与能力") {
                    NavigationLink("智科调参") { ZhikeSettingsScreen() }
                    NavigationLink("骑行海报") { PosterScreen() }
                    NavigationLink("诊断日志") { DiagnosticsScreen() }
                }

                Section("同步") {
                    HStack {
                        Text("Google Drive")
                        Spacer()
                        Text(syncText(store.syncStatus))
                            .foregroundStyle(.secondary)
                    }
                    if let summary = store.remoteDriveSummary {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("云端 v\(summary.stateVersion) · \(summary.updatedByDeviceName)")
                            Text("\(summary.vehicleProfileCount) 车辆 · \(summary.rideCount) 行程 · \(summary.speedTestCount) 测速")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                    HStack {
                        Button {
                            store.signInGoogleDrive()
                        } label: {
                            Label("登录", systemImage: "person.crop.circle.badge.checkmark")
                        }
                        Button {
                            store.signOutGoogleDrive()
                        } label: {
                            Label("退出", systemImage: "person.crop.circle.badge.xmark")
                        }
                    }
                    Button {
                        store.checkGoogleDriveUpdate()
                    } label: {
                        Label("检测云端更新", systemImage: "icloud.and.arrow.down")
                    }
                    Button {
                        store.syncNow()
                    } label: {
                        Label("立即合并同步", systemImage: "arrow.triangle.2.circlepath.icloud")
                    }
                    Button {
                        store.uploadGoogleDriveState()
                    } label: {
                        Label("上传本机状态", systemImage: "icloud.and.arrow.up")
                    }
                    HStack {
                        Text("iCloud")
                        Spacer()
                        Text(syncText(store.persistence.iCloudStatus))
                            .foregroundStyle(.secondary)
                    }
                }

                Section("显示") {
                    Picker("速度来源", selection: $store.persisted.settings.speedSource) {
                        ForEach(SpeedSource.allCases) { source in
                            Text(source.rawValue).tag(source)
                        }
                    }
                    Picker("电池来源", selection: $store.persisted.settings.battDataSource) {
                        ForEach(BatteryDataSource.allCases) { source in
                            Text(source.rawValue).tag(source)
                        }
                    }
                    Toggle("海报显示轨迹", isOn: $store.persisted.settings.posterShowTrack)
                    Toggle("海报水印", isOn: $store.persisted.settings.posterShowWatermark)
                }

                Section("关于") {
                    LabeledContent("Bundle ID", value: "com.shawnrain.SmartDash")
                    LabeledContent("同步 Schema", value: "Drive V2")
                    LabeledContent("能量口径", value: "Net Wh")
                }
            }
            .navigationTitle("设置")
            .onChange(of: store.persisted) { _, _ in store.save() }
        }
    }

    private func syncText(_ status: CloudSyncStatus) -> String {
        switch status {
        case .signedOut: "未登录"
        case .ready(let text), .syncing(let text), .success(let text), .failed(let text), .configurationRequired(let text): text
        }
    }
}

struct VehicleProfilesScreen: View {
    @EnvironmentObject private var store: SmartDashStore

    var body: some View {
        List {
            ForEach($store.persisted.vehicleProfiles) { $profile in
                Section(profile.name) {
                    TextField("名称", text: $profile.name)
                    TextField("MAC", text: $profile.macAddress)
                    Stepper("电池串数 \(profile.batterySeries)", value: $profile.batterySeries, in: 1...40)
                    Stepper("极对数 \(profile.polePairs)", value: $profile.polePairs, in: 1...200)
                    LabeledContent("轮径", value: "\(profile.wheelCircumferenceMm.formatted(0)) mm")
                    Slider(value: $profile.wheelCircumferenceMm, in: 500...4000, step: 1)
                    LabeledContent("容量", value: "\(profile.batteryCapacityAh.formatted(1)) Ah")
                    Slider(value: $profile.batteryCapacityAh, in: 1...200, step: 0.5)
                }
            }
            Button {
                store.persisted.vehicleProfiles.append(VehicleProfile(name: "新车辆"))
                store.persisted.normalize()
            } label: {
                Label("新增车辆", systemImage: "plus")
            }
        }
        .navigationTitle("车辆档案")
    }
}

struct CalibrationScreen: View {
    @EnvironmentObject private var store: SmartDashStore

    var body: some View {
        List {
            Section("校准") {
                Text("以手机 GPS 距离与控制器 RPM 推算距离对比，保存后直接写入当前车辆档案。")
                    .foregroundStyle(.secondary)
                Button("应用模拟建议 1800 mm") {
                    if let index = store.persisted.vehicleProfiles.firstIndex(where: { $0.id == store.persisted.activeVehicleProfileId }) {
                        store.persisted.vehicleProfiles[index].wheelCircumferenceMm = 1800
                        store.save()
                    }
                }
            }
        }
        .navigationTitle("GPS 校准")
    }
}

struct DiagnosticsScreen: View {
    @EnvironmentObject private var store: SmartDashStore

    var body: some View {
        List(store.log.entries, id: \.self) { line in
            Text(line)
                .font(.caption.monospaced())
        }
        .navigationTitle("诊断日志")
    }
}
