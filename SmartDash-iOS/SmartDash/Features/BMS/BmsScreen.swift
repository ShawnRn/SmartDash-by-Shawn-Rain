import SwiftUI

struct BmsScreen: View {
    @EnvironmentObject private var store: SmartDashStore

    var body: some View {
        NavigationStack {
            List {
                Section("电池") {
                    LabeledContent("SoC", value: "\(store.batteryState.socPercent.formatted(0))%")
                    LabeledContent("滤波电压", value: "\(store.batteryState.filteredVoltage.formatted(1)) V")
                    LabeledContent("滤波电流", value: "\(store.batteryState.filteredCurrent.formatted(1)) A")
                    LabeledContent("剩余能量", value: "\(store.rangeEstimate.remainingEnergyWh.formatted(0)) Wh")
                    LabeledContent("置信度", value: store.rangeEstimate.confidence.rawValue)
                }
                Section("协议") {
                    Label("ANT BMS parser placeholder", systemImage: "checkmark.circle")
                    Label("JK BMS parser placeholder", systemImage: "checkmark.circle")
                }
            }
            .navigationTitle("BMS")
        }
    }
}
