import SwiftUI

struct RootView: View {
    @StateObject private var store = SmartDashStore()
    @State private var selectedTab: SmartDashTab = .dashboard

    var body: some View {
        TabView(selection: $selectedTab) {
            SpeedtestScreen()
                .tabItem { Label("加速", systemImage: "timer") }
                .tag(SmartDashTab.acceleration)

            DashboardScreen()
                .tabItem { Label("仪表", systemImage: "gauge.with.dots.needle.67percent") }
                .tag(SmartDashTab.dashboard)

            SettingsScreen()
                .tabItem { Label("设置", systemImage: "gearshape") }
                .tag(SmartDashTab.settings)
        }
        .environmentObject(store)
        .onOpenURL { url in
            _ = store.handleOpenURL(url)
        }
    }
}

private enum SmartDashTab: Hashable {
    case acceleration
    case dashboard
    case settings
}

#Preview {
    RootView()
}
