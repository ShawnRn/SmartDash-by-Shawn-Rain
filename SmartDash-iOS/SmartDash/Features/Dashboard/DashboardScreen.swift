import SwiftUI

struct DashboardScreen: View {
    @EnvironmentObject private var store: SmartDashStore
    @State private var showingConnect = false
    @State private var showingCardEditor = false

    var body: some View {
        NavigationStack {
            GeometryReader { proxy in
                let landscape = proxy.size.width > proxy.size.height
                ScrollView {
                    VStack(spacing: 16) {
                        header
                        if landscape {
                            landscapeMetrics
                        } else {
                            portraitMetrics
                        }
                        debugControls
                    }
                    .padding()
                }
                .background(.background)
            }
            .navigationTitle("SmartDash")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        store.isRideActive ? store.stopRide() : store.startRide()
                    } label: {
                        Label(
                            store.isRideActive ? "结束记录" : "开始记录",
                            systemImage: store.isRideActive ? "stop.fill" : "record.circle"
                        )
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.small)
                    .tint(store.isRideActive ? .red : .accentColor)
                }
                ToolbarItem(placement: .topBarTrailing) {
                    StatusPill(title: store.rideDirectionLabel, systemImage: "location.north.line", tint: .teal)
                }
            }
            .sheet(isPresented: $showingConnect) {
                ConnectSheet()
                    .presentationDetents([.medium, .large])
            }
            .sheet(isPresented: $showingCardEditor) {
                DashboardCardEditor()
                    .presentationDetents([.medium, .large])
            }
        }
    }

    private var header: some View {
        GlassPanel {
            HStack(spacing: 12) {
                StatusPill(title: "\(store.batteryState.socPercent.formatted(0))%", systemImage: "battery.75percent", tint: .green)
                Spacer()
                Button {
                    showingConnect = true
                } label: {
                    StatusPill(title: connectionTitle, systemImage: "antenna.radiowaves.left.and.right", tint: .blue)
                }
                .buttonStyle(.plain)
                Spacer()
                StatusPill(title: "\(store.rangeEstimate.estimatedRangeKm.formatted(0)) km", systemImage: "point.topleft.down.curvedto.point.bottomright.up", tint: .orange)
            }
        }
    }

    private var portraitMetrics: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 150), spacing: 12)], spacing: 12) {
            ForEach(visibleMetrics) { metric in
                metricTile(metric)
                    .contextMenu {
                        Button {
                            showingCardEditor = true
                        } label: {
                            Label("编辑卡片", systemImage: "slider.horizontal.3")
                        }
                    }
                    .onLongPressGesture {
                        showingCardEditor = true
                    }
            }
        }
    }

    private var landscapeMetrics: some View {
        HStack(spacing: 12) {
            metricTile(.speed)
            metricTile(.power)
            metricTile(.efficiency)
        }
    }

    private var debugControls: some View {
        GlassPanel {
            HStack {
                Button {
                    store.simulateFrame()
                } label: {
                    Label("模拟遥测", systemImage: "waveform.path")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
                .accessibilityLabel("模拟遥测帧")
            }
        }
    }

    private var connectionTitle: String {
        switch store.ble.state {
        case .connected(let name): name
        case .connecting(let name): "连接 \(name)"
        case .scanning: "扫描中"
        case .failed(let text): text
        case .disconnected: "已断开"
        case .idle: "未连接"
        }
    }

    private var visibleMetrics: [DashboardMetric] {
        let hidden = Set(store.persisted.settings.hiddenDashboardItems)
        return store.persisted.settings.dashboardItems
            .compactMap(DashboardMetric.init(id:))
            .filter { !hidden.contains($0.id) }
    }

    @ViewBuilder
    private func metricTile(_ metric: DashboardMetric) -> some View {
        switch metric {
        case .speed:
            MetricTile(title: "速度", value: store.latestMetrics.speedKmh.formatted(1), unit: "km/h", symbol: "speedometer", tint: .cyan)
        case .power:
            MetricTile(title: "功率", value: store.latestMetrics.powerKw.formatted(2), unit: "kW", symbol: "bolt.fill", tint: .yellow)
        case .efficiency:
            MetricTile(title: "平均能耗", value: store.accumulatorState.avgNetEfficiencyWhKm.formatted(1), unit: "Wh/km", symbol: "leaf.fill", tint: .green)
        case .voltage:
            MetricTile(title: "电压", value: store.latestMetrics.voltage.formatted(1), unit: "V", symbol: "waveform.path.ecg", tint: .purple)
        case .current:
            MetricTile(title: "母线电流", value: store.latestMetrics.busCurrent.formatted(1), unit: "A", symbol: "alternatingcurrent", tint: .indigo)
        case .soc:
            MetricTile(title: "电量", value: store.batteryState.socPercent.formatted(0), unit: "%", symbol: "battery.75percent", tint: .green)
        case .distance:
            MetricTile(title: "里程", value: (store.accumulatorState.totalDistanceMeters / 1000).formatted(2), unit: "km", symbol: "road.lanes", tint: .orange)
        case .temperature:
            MetricTile(title: "控制器温度", value: store.latestMetrics.controllerTemp.formatted(0), unit: "°C", symbol: "thermometer.medium", tint: .red)
        case .regen:
            MetricTile(title: "回收能量", value: store.accumulatorState.regenEnergyWh.formatted(1), unit: "Wh", symbol: "arrow.triangle.2.circlepath", tint: .mint)
        }
    }
}

enum DashboardMetric: String, Identifiable, CaseIterable {
    case speed
    case power
    case efficiency
    case voltage
    case current
    case soc
    case distance
    case temperature
    case regen

    var id: String { rawValue }

    nonisolated init?(id: String) {
        self.init(rawValue: id)
    }

    nonisolated var title: String {
        switch self {
        case .speed: "速度"
        case .power: "功率"
        case .efficiency: "平均能耗"
        case .voltage: "电压"
        case .current: "母线电流"
        case .soc: "电量"
        case .distance: "里程"
        case .temperature: "控制器温度"
        case .regen: "回收能量"
        }
    }
}

private struct DashboardCardEditor: View {
    @EnvironmentObject private var store: SmartDashStore
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section("长按仪表卡片可进入这里，拖动排序，开关控制显示") {
                    ForEach(orderedMetrics) { metric in
                        Toggle(metric.title, isOn: binding(for: metric))
                    }
                    .onMove(perform: move)
                }
            }
            .environment(\.editMode, .constant(.active))
            .navigationTitle("编辑卡片")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("重置") {
                        store.persisted.settings.dashboardItems = DashboardMetric.allCases.map(\.id)
                        store.persisted.settings.hiddenDashboardItems = []
                        store.save()
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("完成") { dismiss() }
                }
            }
            .onAppear {
                store.ensureDashboardMetricDefaults()
            }
        }
    }

    private var orderedMetrics: [DashboardMetric] {
        store.persisted.settings.dashboardItems.compactMap(DashboardMetric.init(id:))
    }

    private func binding(for metric: DashboardMetric) -> Binding<Bool> {
        Binding {
            !store.persisted.settings.hiddenDashboardItems.contains(metric.id)
        } set: { isOn in
            if isOn {
                store.persisted.settings.hiddenDashboardItems.removeAll { $0 == metric.id }
            } else if !store.persisted.settings.hiddenDashboardItems.contains(metric.id) {
                store.persisted.settings.hiddenDashboardItems.append(metric.id)
            }
            store.save()
        }
    }

    private func move(from source: IndexSet, to destination: Int) {
        store.persisted.settings.dashboardItems.move(fromOffsets: source, toOffset: destination)
        store.save()
    }
}
