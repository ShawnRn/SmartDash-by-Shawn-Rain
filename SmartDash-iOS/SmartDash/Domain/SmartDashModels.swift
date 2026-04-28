import Foundation

typealias EnergyWh = Double
typealias EnergyAh = Double
typealias EfficiencyWhKm = Double

enum SpeedSource: String, Codable, CaseIterable, Identifiable {
    case controller = "CONTROLLER"
    case gps = "GPS"

    var id: String { rawValue }
}

enum BatteryDataSource: String, Codable, CaseIterable, Identifiable {
    case controller = "CONTROLLER"
    case bms = "BMS"

    var id: String { rawValue }
}

struct VehicleProfile: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var name: String = "SmartDash"
    var macAddress: String = ""
    var batteryCapacityAh: Double = 35
    var batterySeries: Int = 20
    var wheelCircumferenceMm: Double = 1800
    var wheelRimSize: String = "10寸"
    var tireSpecLabel: String = ""
    var polePairs: Int = 50
    var totalMileageKm: Double = 0
    var learnedEfficiencyWhKm: Double = 35
    var learnedUsableEnergyRatio: Double = 0.85
    var learnedInternalResistanceOhm: Double = 0.0
    var lastModified: Int64 = Int64(Date().timeIntervalSince1970 * 1000)

    mutating func sanitize() {
        batteryCapacityAh = batteryCapacityAh.finiteOr(35).clamped(to: 1...500)
        wheelCircumferenceMm = wheelCircumferenceMm.finiteOr(1800).clamped(to: 500...4000)
        totalMileageKm = totalMileageKm.finiteOr(0).clamped(to: 0...1_000_000)
        learnedEfficiencyWhKm = learnedEfficiencyWhKm.finiteOr(35).clamped(to: 1...500)
        learnedUsableEnergyRatio = learnedUsableEnergyRatio.finiteOr(0.85).clamped(to: 0.2...1.0)
        learnedInternalResistanceOhm = learnedInternalResistanceOhm.finiteOr(0).clamped(to: 0...1)
        batterySeries = max(1, min(40, batterySeries))
        polePairs = max(1, min(200, polePairs))
    }
}

struct VehicleMetrics: Codable, Equatable {
    var voltage: Double = 0
    var busCurrent: Double = 0
    var phaseCurrent: Double = 0
    var rpm: Double = 0
    var speedKmh: Double = 0
    var motorTemp: Double = 0
    var controllerTemp: Double = 0
    var faultCode: Int = 0
    var braking: Bool = false
    var cruise: Bool = false
    var reverse: Bool = false
    var protocolId: String = "mock"
    var sourceFrameId: Int64?
    var timestampMs: Int64 = Date.nowMs

    var powerKw: Double { voltage * busCurrent / 1000.0 }
}

struct RideMetricSample: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var elapsedMs: Int64
    var timestampMs: Int64
    var speedKmH: Double
    var powerKw: Double
    var voltage: Double
    var voltageSag: Double
    var busCurrent: Double
    var phaseCurrent: Double
    var controllerTemp: Double
    var soc: Double
    var estimatedRangeKm: Double
    var rpm: Double
    var efficiencyWhKm: Double
    var avgEfficiencyWhKm: Double
    var avgNetEfficiencyWhKm: Double
    var avgTractionEfficiencyWhKm: Double
    var distanceMeters: Double
    var totalEnergyWh: Double
    var tractionEnergyWh: Double
    var regenEnergyWh: Double
    var recoveredEnergyWh: Double
    var maxControllerTemp: Double
    var gradePercent: Double?
    var altitudeMeters: Double?
    var latitude: Double?
    var longitude: Double?
}

struct RideHistoryRecord: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var vehicleProfileId: String
    var title: String
    var startedAtMs: Int64
    var endedAtMs: Int64
    var durationMs: Int64
    var distanceMeters: Double
    var maxSpeedKmh: Double
    var avgSpeedKmh: Double
    var peakPowerKw: Double
    var totalEnergyWh: Double
    var tractionEnergyWh: Double
    var regenEnergyWh: Double
    var avgEfficiencyWhKm: Double
    var avgNetEfficiencyWhKm: Double
    var avgTractionEfficiencyWhKm: Double
    var maxControllerTemp: Double
    var samples: [RideMetricSample] = []
    var updatedAt: Int64 = Date.nowMs
}

struct SpeedTestRecord: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var vehicleProfileId: String
    var label: String = "0-50 km/h"
    var startedAtMs: Int64 = Date.nowMs
    var endedAtMs: Int64 = Date.nowMs
    var targetSpeedKmh: Double = 50
    var achievedSpeedKmh: Double = 0
    var timeToTargetMs: Int64 = 0
    var distanceMeters: Double = 0
    var peakPowerKw: Double = 0
    var peakBusCurrentA: Double = 0
    var minVoltageV: Double = 0
    var updatedAt: Int64 = Date.nowMs
}

struct SmartDashSettings: Codable, Equatable {
    var speedSource: SpeedSource = .controller
    var battDataSource: BatteryDataSource = .controller
    var controllerBrand: String = "auto"
    var logLevel: String = "DEBUG"
    var dashboardItems: [String] = ["speed", "power", "efficiency", "voltage", "current", "soc"]
    var hiddenDashboardItems: [String] = []
    var rideOverviewItems: [String] = ["distance", "energy", "avgSpeed", "range"]
    var posterTemplateId: String = "performance_dark"
    var posterAspectRatio: String = "STORY_9_16"
    var posterShowTrack: Bool = true
    var posterShowWatermark: Bool = true
}

extension Date {
    static var nowMs: Int64 { Int64(Date().timeIntervalSince1970 * 1000) }
}

extension Double {
    func finiteOr(_ fallback: Double) -> Double {
        isFinite ? self : fallback
    }

    func clamped(to range: ClosedRange<Double>) -> Double {
        min(max(self, range.lowerBound), range.upperBound)
    }
}
