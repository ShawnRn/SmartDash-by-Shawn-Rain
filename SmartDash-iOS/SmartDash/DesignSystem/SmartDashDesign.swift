import SwiftUI

struct GlassPanel<Content: View>: View {
    var content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        content
            .padding(16)
            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
    }
}

struct MetricTile: View {
    var title: String
    var value: String
    var unit: String
    var symbol: String
    var tint: Color = .accentColor

    var body: some View {
        GlassPanel {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Image(systemName: symbol)
                        .foregroundStyle(tint)
                    Spacer()
                    Text(unit)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Text(value)
                    .font(.system(size: 32, weight: .semibold, design: .rounded))
                    .monospacedDigit()
                    .lineLimit(1)
                    .minimumScaleFactor(0.55)
                Text(title)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

struct StatusPill: View {
    var title: String
    var systemImage: String
    var tint: Color = .accentColor

    var body: some View {
        Label(title, systemImage: systemImage)
            .font(.caption.weight(.medium))
            .lineLimit(1)
            .padding(.horizontal, 10)
            .padding(.vertical, 7)
            .background(tint.opacity(0.14), in: Capsule())
            .foregroundStyle(tint)
    }
}

extension Double {
    func formatted(_ fraction: Int = 1) -> String {
        self.formatted(.number.precision(.fractionLength(fraction)))
    }
}
