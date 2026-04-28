import SwiftUI

struct HistoryScreen: View {
    @EnvironmentObject private var store: SmartDashStore

    var body: some View {
        NavigationStack {
            List {
                ForEach(store.persisted.rides) { ride in
                    NavigationLink {
                        RideDetailScreen(ride: ride)
                    } label: {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(ride.title)
                                .font(.headline)
                            Text("\((ride.distanceMeters / 1000).formatted(2)) km · \(ride.avgNetEfficiencyWhKm.formatted(1)) Wh/km · \(ride.totalEnergyWh.formatted(1)) Wh")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            }
            .overlay {
                if store.persisted.rides.isEmpty {
                    ContentUnavailableView("暂无行程", systemImage: "map", description: Text("开始骑行后会在这里生成记录"))
                }
            }
            .navigationTitle("行程")
        }
    }
}

struct RideDetailScreen: View {
    var ride: RideHistoryRecord

    var body: some View {
        List {
            Section("统计概览") {
                LabeledContent("距离", value: "\((ride.distanceMeters / 1000).formatted(2)) km")
                LabeledContent("均速", value: "\(ride.avgSpeedKmh.formatted(1)) km/h")
                LabeledContent("极速", value: "\(ride.maxSpeedKmh.formatted(1)) km/h")
                LabeledContent("净能耗", value: "\(ride.avgNetEfficiencyWhKm.formatted(1)) Wh/km")
                LabeledContent("牵引能量", value: "\(ride.tractionEnergyWh.formatted(1)) Wh")
                LabeledContent("回收能量", value: "\(ride.regenEnergyWh.formatted(1)) Wh")
                LabeledContent("峰值功率", value: "\(ride.peakPowerKw.formatted(2)) kW")
            }
            Section("导出") {
                ShareLink(item: csvText, preview: SharePreview("SmartDash Ride CSV"))
            }
        }
        .navigationTitle(ride.title)
    }

    private var csvText: String {
        var lines = ["timestampMs,speedKmH,powerKw,voltage,busCurrent,soc,estimatedRangeKm,distanceMeters,totalEnergyWh,tractionEnergyWh,regenEnergyWh"]
        for sample in ride.samples {
            lines.append("\(sample.timestampMs),\(sample.speedKmH),\(sample.powerKw),\(sample.voltage),\(sample.busCurrent),\(sample.soc),\(sample.estimatedRangeKm),\(sample.distanceMeters),\(sample.totalEnergyWh),\(sample.tractionEnergyWh),\(sample.regenEnergyWh)")
        }
        return lines.joined(separator: "\n")
    }
}
