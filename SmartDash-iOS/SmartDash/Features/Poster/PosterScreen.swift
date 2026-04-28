import SwiftUI

struct PosterScreen: View {
    @EnvironmentObject private var store: SmartDashStore

    var body: some View {
        ScrollView {
            VStack(spacing: 18) {
                poster
                    .frame(maxWidth: 360)
                    .aspectRatio(9.0 / 16.0, contentMode: .fit)
                    .padding()

                ShareLink(item: posterSummary) {
                    Label("分享海报数据", systemImage: "square.and.arrow.up")
                }
                .buttonStyle(.borderedProminent)
            }
            .frame(maxWidth: .infinity)
            .padding()
        }
        .navigationTitle("骑行海报")
    }

    private var poster: some View {
        ZStack {
            LinearGradient(colors: [.black, .blue.opacity(0.8), .teal.opacity(0.8)], startPoint: .topLeading, endPoint: .bottomTrailing)
            VStack(alignment: .leading) {
                Text("SmartDash")
                    .font(.largeTitle.bold())
                Text(store.activeVehicle.name)
                    .foregroundStyle(.secondary)
                Spacer()
                Text("\((store.accumulatorState.totalDistanceMeters / 1000).formatted(2)) km")
                    .font(.system(size: 56, weight: .bold, design: .rounded))
                    .minimumScaleFactor(0.6)
                HStack {
                    Label("\(store.accumulatorState.avgNetEfficiencyWhKm.formatted(1)) Wh/km", systemImage: "leaf")
                    Label("\(store.accumulatorState.peakDrivePowerKw.formatted(2)) kW", systemImage: "bolt")
                }
                .font(.caption)
            }
            .foregroundStyle(.white)
            .padding(26)
        }
        .clipShape(RoundedRectangle(cornerRadius: 28, style: .continuous))
    }

    private var posterSummary: String {
        "SmartDash \(store.activeVehicle.name): \((store.accumulatorState.totalDistanceMeters / 1000).formatted(2)) km, \(store.accumulatorState.avgNetEfficiencyWhKm.formatted(1)) Wh/km"
    }
}
