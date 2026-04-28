import SwiftUI

struct SpeedtestScreen: View {
    @EnvironmentObject private var store: SmartDashStore
    @State private var running = false
    @State private var target: Double = 50
    @State private var startMs: Int64?

    var body: some View {
        NavigationStack {
            List {
                Section("目标") {
                    LabeledContent("目标速度", value: "\(target.formatted(0)) km/h")
                    Slider(value: $target, in: 20...120, step: 5)
                    Button {
                        toggle()
                    } label: {
                        Label(running ? "结束测速" : "开始测速", systemImage: running ? "stop.fill" : "timer")
                    }
                    .buttonStyle(.borderedProminent)
                }

                Section("实时") {
                    LabeledContent("当前速度", value: "\(store.latestMetrics.speedKmh.formatted(1)) km/h")
                    LabeledContent("峰值功率", value: "\(store.accumulatorState.peakDrivePowerKw.formatted(2)) kW")
                    LabeledContent("最小电压", value: "\(store.latestMetrics.voltage.formatted(1)) V")
                }

                Section("历史") {
                    ForEach(store.persisted.speedTests) { test in
                        VStack(alignment: .leading) {
                            Text(test.label)
                            Text("\(test.achievedSpeedKmh.formatted(1)) km/h · \(Double(test.timeToTargetMs) / 1000, specifier: "%.2f") s")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            }
            .navigationTitle("加速")
        }
    }

    private func toggle() {
        if running {
            let now = Date.nowMs
            let record = SpeedTestRecord(
                vehicleProfileId: store.activeVehicle.id,
                label: "0-\(Int(target)) km/h",
                startedAtMs: startMs ?? now,
                endedAtMs: now,
                targetSpeedKmh: target,
                achievedSpeedKmh: store.accumulatorState.maxSpeedKmh,
                timeToTargetMs: max(0, now - (startMs ?? now)),
                distanceMeters: store.accumulatorState.totalDistanceMeters,
                peakPowerKw: store.accumulatorState.peakDrivePowerKw,
                peakBusCurrentA: store.latestMetrics.busCurrent,
                minVoltageV: store.latestMetrics.voltage
            )
            store.persisted.speedTests.insert(record, at: 0)
            store.save()
            running = false
        } else {
            startMs = Date.nowMs
            running = true
        }
    }
}
