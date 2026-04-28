import Combine
import Foundation

@MainActor
final class AppLog: ObservableObject {
    static let shared = AppLog()
    @Published private(set) var entries: [String] = []

    func info(_ message: String) {
        append("INFO", message)
    }

    func warning(_ message: String) {
        append("WARN", message)
    }

    func error(_ message: String) {
        append("ERROR", message)
    }

    private func append(_ level: String, _ message: String) {
        let line = "[\(level)] \(Date()) \(message)"
        entries.insert(line, at: 0)
        entries = Array(entries.prefix(300))
    }
}
