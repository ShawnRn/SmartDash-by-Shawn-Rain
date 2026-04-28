import Foundation

struct DirectionInput {
    var gpsBearingDeg: Double?
    var gpsAgeMs: Int64 = .max
    var gpsAccuracyM: Double = .greatestFiniteMagnitude
    var gpsStepDistanceM: Double = 0
    var sensorHeadingDeg: Double?
    var sensorAgeMs: Int64 = .max
    var speedKmH: Double
    var timestampMs: Int64 = Date.nowMs
}

struct DirectionStabilizer {
    private var stableDirectionDeg: Double?
    private var lastTimestampMs: Int64?

    mutating func update(_ input: DirectionInput) -> Double? {
        guard input.speedKmH >= 3 else { return stableDirectionDeg }

        let candidate = bestCandidate(input)
        guard let candidate else { return stableDirectionDeg }
        guard let current = stableDirectionDeg else {
            stableDirectionDeg = normalized(candidate)
            lastTimestampMs = input.timestampMs
            return stableDirectionDeg
        }

        let delta = shortestDelta(from: current, to: candidate)
        guard abs(delta) >= 5 else { return stableDirectionDeg }

        let dt = max(0.05, Double(input.timestampMs - (lastTimestampMs ?? input.timestampMs)) / 1000.0)
        let alpha = smoothingAlpha(speedKmH: input.speedKmH)
        let maxTurn = maxTurnRate(speedKmH: input.speedKmH) * dt
        let limitedDelta = (delta * alpha).clamped(to: -maxTurn...maxTurn)
        stableDirectionDeg = normalized(current + limitedDelta)
        lastTimestampMs = input.timestampMs
        return stableDirectionDeg
    }

    private func bestCandidate(_ input: DirectionInput) -> Double? {
        if input.speedKmH > 8,
           let gps = input.gpsBearingDeg,
           input.gpsAgeMs <= 2500,
           input.gpsAccuracyM <= 25,
           (input.speedKmH > 12 || input.gpsStepDistanceM >= 3) {
            return gps
        }
        if let sensor = input.sensorHeadingDeg, input.sensorAgeMs <= 1500 {
            return sensor
        }
        return input.gpsBearingDeg
    }

    private func smoothingAlpha(speedKmH: Double) -> Double {
        switch speedKmH {
        case ..<3: 0
        case ..<8: 0.06
        case ..<15: 0.12
        case ..<35: 0.20
        default: 0.30
        }
    }

    private func maxTurnRate(speedKmH: Double) -> Double {
        switch speedKmH {
        case ..<8: 35
        case ..<15: 50
        case ..<35: 70
        default: 90
        }
    }
}

struct DirectionLabelFormatter {
    private let labels = ["北", "东北", "东", "东南", "南", "西南", "西", "西北"]
    private var lastIndex: Int?
    var hysteresisDeg: Double = 12

    mutating func label(for degrees: Double?) -> String {
        guard let degrees else { return "--" }
        let normalized = normalized(degrees)
        let index = Int(((normalized + 22.5) / 45).rounded(.down)) % labels.count
        if let lastIndex, lastIndex != index {
            let center = Double(index) * 45
            if abs(shortestDelta(from: center, to: normalized)) > 22.5 - hysteresisDeg {
                return labels[lastIndex]
            }
        }
        lastIndex = index
        return labels[index]
    }
}

func normalized(_ degrees: Double) -> Double {
    var value = degrees.truncatingRemainder(dividingBy: 360)
    if value < 0 { value += 360 }
    return value
}

func shortestDelta(from: Double, to: Double) -> Double {
    var delta = normalized(to) - normalized(from)
    if delta > 180 { delta -= 360 }
    if delta < -180 { delta += 360 }
    return delta
}
