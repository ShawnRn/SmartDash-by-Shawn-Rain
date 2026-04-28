import Foundation

enum SampleQuality: String, Codable, Equatable {
    case good = "GOOD"
    case duplicate = "DUPLICATE"
    case tooDense = "TOO_DENSE"
    case gapReset = "GAP_RESET"
    case outlier = "OUTLIER"
    case recovered = "RECOVERED"
    case powerOff = "POWER_OFF"
}

enum SampleDataMode: String, Codable, Equatable {
    case raw = "RAW"
    case recovered = "RECOVERED"
    case powerOff = "POWER_OFF"
}

struct TelemetrySample: Codable, Equatable {
    var timestampMs: Int64
    var sourceFrameId: Int64?
    var voltageV: Double
    var busCurrentA: Double
    var phaseCurrentA: Double
    var rpm: Double
    var controllerSpeedKmH: Double
    var displaySpeedKmH: Double
    var distanceSpeedKmH: Double
    var motorTempC: Double
    var controllerTempC: Double
    var braking: Bool
    var cruise: Bool
    var quality: SampleQuality
    var dataMode: SampleDataMode
    var dtMs: Int64
    var allowIntegration: Bool
    var allowLearning: Bool
}

struct RideAccumulatorState: Codable, Equatable {
    var tractionEnergyWh: EnergyWh = 0
    var regenEnergyWh: EnergyWh = 0
    var netBatteryEnergyWh: EnergyWh = 0
    var tractionAh: EnergyAh = 0
    var netBatteryAh: EnergyAh = 0
    var peakDrivePowerKw: Double = 0
    var peakRegenPowerKw: Double = 0
    var maxControllerTempC: Double = 0
    var maxMotorTempC: Double = 0
    var totalDistanceMeters: Double = 0
    var maxSpeedKmh: Double = 0
    var totalSpeedSum: Double = 0
    var movingTimeMs: Int64 = 0
    var sampleCount: Int = 0
    var integrationCount: Int = 0

    var averageSpeedKmh: Double {
        sampleCount > 0 ? totalSpeedSum / Double(sampleCount) : 0
    }

    var avgNetEfficiencyWhKm: Double {
        let km = totalDistanceMeters / 1000.0
        guard km > 0.02 else { return 0 }
        return max(0, netBatteryEnergyWh / km)
    }

    var avgTractionEfficiencyWhKm: Double {
        let km = totalDistanceMeters / 1000.0
        guard km > 0.02 else { return 0 }
        return max(0, tractionEnergyWh / km)
    }
}

struct BatteryState: Codable, Equatable {
    var socPercent: Double = 0
    var socByAhPercent: Double = 0
    var socByOcvPercent: Double = 0
    var filteredVoltage: Double = 0
    var filteredCurrent: Double = 0
    var learnedInternalResistanceOhm: Double = 0
    var isStationary: Bool = true
    var confidence: Double = 0.5
    var irContext: String = "default"
}

enum RangeConfidence: String, Codable {
    case low = "LOW"
    case medium = "MEDIUM"
    case high = "HIGH"
}

struct RangeEstimate: Codable, Equatable {
    var estimatedRangeKm: Double = 0
    var remainingEnergyWh: EnergyWh = 0
    var averageWindowEfficiencyWhKm: EfficiencyWhKm = 0
    var isWindowFresh: Bool = false
    var confidence: RangeConfidence = .low
}
