import Foundation

struct DriveCurrentState: Codable, Equatable {
    var schemaVersion: Int = 2
    var stateVersion: Int64
    var updatedAt: Int64
    var updatedByDeviceId: String
    var updatedByDeviceName: String
    var activeVehicleProfileId: String = ""
    var settings: SyncSettingsSnapshot = .init()
    var vehicleSettings: [SyncVehicleSettingsSnapshot] = []
    var vehicleProfiles: [SyncVehicleProfileSnapshot] = []
    var rides: [SyncRideSnapshot] = []
    var speedTests: [SyncSpeedTestSnapshot] = []

    init(
        schemaVersion: Int = 2,
        stateVersion: Int64,
        updatedAt: Int64,
        updatedByDeviceId: String,
        updatedByDeviceName: String,
        activeVehicleProfileId: String = "",
        settings: SyncSettingsSnapshot = .init(),
        vehicleSettings: [SyncVehicleSettingsSnapshot] = [],
        vehicleProfiles: [SyncVehicleProfileSnapshot] = [],
        rides: [SyncRideSnapshot] = [],
        speedTests: [SyncSpeedTestSnapshot] = []
    ) {
        self.schemaVersion = schemaVersion
        self.stateVersion = stateVersion
        self.updatedAt = updatedAt
        self.updatedByDeviceId = updatedByDeviceId
        self.updatedByDeviceName = updatedByDeviceName
        self.activeVehicleProfileId = activeVehicleProfileId
        self.settings = settings
        self.vehicleSettings = vehicleSettings
        self.vehicleProfiles = vehicleProfiles
        self.rides = rides
        self.speedTests = speedTests
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        schemaVersion = c.decodeDefault(Int.self, forKey: .schemaVersion, default: 2)
        stateVersion = c.decodeDefault(Int64.self, forKey: .stateVersion, default: 0)
        updatedAt = c.decodeDefault(Int64.self, forKey: .updatedAt, default: 0)
        updatedByDeviceId = c.decodeDefault(String.self, forKey: .updatedByDeviceId, default: "")
        updatedByDeviceName = c.decodeDefault(String.self, forKey: .updatedByDeviceName, default: "Unknown")
        activeVehicleProfileId = c.decodeDefault(String.self, forKey: .activeVehicleProfileId, default: "")
        settings = c.decodeDefault(SyncSettingsSnapshot.self, forKey: .settings, default: .init())
        vehicleSettings = c.decodeDefault([SyncVehicleSettingsSnapshot].self, forKey: .vehicleSettings, default: [])
        vehicleProfiles = c.decodeDefault([SyncVehicleProfileSnapshot].self, forKey: .vehicleProfiles, default: [])
        rides = c.decodeDefault([SyncRideSnapshot].self, forKey: .rides, default: [])
        speedTests = c.decodeDefault([SyncSpeedTestSnapshot].self, forKey: .speedTests, default: [])
    }
}

struct SyncSettingsSnapshot: Codable, Equatable {
    var speedSource: String = "CONTROLLER"
    var battDataSource: String = "CONTROLLER"
    var wheelCircumferenceMm: Double = 1800
    var polePairs: Int = 50
    var controllerBrand: String = "auto"
    var logLevel: String = "DEBUG"
    var overlayEnabled: Bool = false
    var dashboardItems: [String] = []
    var rideOverviewItems: [String] = []
    var driveBackupRetention: String = "KEEP_ALL"
    var posterTemplateId: String = "performance_dark"
    var posterAspectRatio: String = "STORY_9_16"
    var posterShowTrack: Bool = true
    var posterShowWatermark: Bool = true
    var learnedEfficiencyWhKm: Double = 35
    var learnedUsableEnergyRatio: Double = 0.85
    var updatedAt: Int64 = 0
    var updatedByDeviceId: String = ""

    init() {}

    init(
        speedSource: String = "CONTROLLER",
        battDataSource: String = "CONTROLLER",
        wheelCircumferenceMm: Double = 1800,
        polePairs: Int = 50,
        controllerBrand: String = "auto",
        logLevel: String = "DEBUG",
        overlayEnabled: Bool = false,
        dashboardItems: [String] = [],
        rideOverviewItems: [String] = [],
        driveBackupRetention: String = "KEEP_ALL",
        posterTemplateId: String = "performance_dark",
        posterAspectRatio: String = "STORY_9_16",
        posterShowTrack: Bool = true,
        posterShowWatermark: Bool = true,
        learnedEfficiencyWhKm: Double = 35,
        learnedUsableEnergyRatio: Double = 0.85,
        updatedAt: Int64 = 0,
        updatedByDeviceId: String = ""
    ) {
        self.speedSource = speedSource
        self.battDataSource = battDataSource
        self.wheelCircumferenceMm = wheelCircumferenceMm
        self.polePairs = polePairs
        self.controllerBrand = controllerBrand
        self.logLevel = logLevel
        self.overlayEnabled = overlayEnabled
        self.dashboardItems = dashboardItems
        self.rideOverviewItems = rideOverviewItems
        self.driveBackupRetention = driveBackupRetention
        self.posterTemplateId = posterTemplateId
        self.posterAspectRatio = posterAspectRatio
        self.posterShowTrack = posterShowTrack
        self.posterShowWatermark = posterShowWatermark
        self.learnedEfficiencyWhKm = learnedEfficiencyWhKm
        self.learnedUsableEnergyRatio = learnedUsableEnergyRatio
        self.updatedAt = updatedAt
        self.updatedByDeviceId = updatedByDeviceId
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        speedSource = c.decodeDefault(String.self, forKey: .speedSource, default: "CONTROLLER")
        battDataSource = c.decodeDefault(String.self, forKey: .battDataSource, default: "CONTROLLER")
        wheelCircumferenceMm = c.decodeDefault(Double.self, forKey: .wheelCircumferenceMm, default: 1800)
        polePairs = c.decodeDefault(Int.self, forKey: .polePairs, default: 50)
        controllerBrand = c.decodeDefault(String.self, forKey: .controllerBrand, default: "auto")
        logLevel = c.decodeDefault(String.self, forKey: .logLevel, default: "DEBUG")
        overlayEnabled = c.decodeDefault(Bool.self, forKey: .overlayEnabled, default: false)
        dashboardItems = c.decodeDefault([String].self, forKey: .dashboardItems, default: [])
        rideOverviewItems = c.decodeDefault([String].self, forKey: .rideOverviewItems, default: [])
        driveBackupRetention = c.decodeDefault(String.self, forKey: .driveBackupRetention, default: "KEEP_ALL")
        posterTemplateId = c.decodeDefault(String.self, forKey: .posterTemplateId, default: "performance_dark")
        posterAspectRatio = c.decodeDefault(String.self, forKey: .posterAspectRatio, default: "STORY_9_16")
        posterShowTrack = c.decodeDefault(Bool.self, forKey: .posterShowTrack, default: true)
        posterShowWatermark = c.decodeDefault(Bool.self, forKey: .posterShowWatermark, default: true)
        learnedEfficiencyWhKm = c.decodeDefault(Double.self, forKey: .learnedEfficiencyWhKm, default: 35)
        learnedUsableEnergyRatio = c.decodeDefault(Double.self, forKey: .learnedUsableEnergyRatio, default: 0.85)
        updatedAt = c.decodeDefault(Int64.self, forKey: .updatedAt, default: 0)
        updatedByDeviceId = c.decodeDefault(String.self, forKey: .updatedByDeviceId, default: "")
    }
}

struct SyncVehicleSettingsSnapshot: Codable, Equatable {
    var vehicleProfileId: String
    var speedSource: String = "CONTROLLER"
    var battDataSource: String = "CONTROLLER"
    var wheelCircumferenceMm: Double = 1800
    var polePairs: Int = 50
    var controllerBrand: String = "auto"
    var lastControllerDeviceAddress: String = ""
    var lastControllerDeviceName: String = ""
    var lastControllerProtocolId: String = ""
    var dashboardItems: [String] = []
    var rideOverviewItems: [String] = []
    var updatedAt: Int64 = 0
    var updatedByDeviceId: String = ""

    init(
        vehicleProfileId: String,
        speedSource: String = "CONTROLLER",
        battDataSource: String = "CONTROLLER",
        wheelCircumferenceMm: Double = 1800,
        polePairs: Int = 50,
        controllerBrand: String = "auto",
        lastControllerDeviceAddress: String = "",
        lastControllerDeviceName: String = "",
        lastControllerProtocolId: String = "",
        dashboardItems: [String] = [],
        rideOverviewItems: [String] = [],
        updatedAt: Int64 = 0,
        updatedByDeviceId: String = ""
    ) {
        self.vehicleProfileId = vehicleProfileId
        self.speedSource = speedSource
        self.battDataSource = battDataSource
        self.wheelCircumferenceMm = wheelCircumferenceMm
        self.polePairs = polePairs
        self.controllerBrand = controllerBrand
        self.lastControllerDeviceAddress = lastControllerDeviceAddress
        self.lastControllerDeviceName = lastControllerDeviceName
        self.lastControllerProtocolId = lastControllerProtocolId
        self.dashboardItems = dashboardItems
        self.rideOverviewItems = rideOverviewItems
        self.updatedAt = updatedAt
        self.updatedByDeviceId = updatedByDeviceId
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        vehicleProfileId = c.decodeDefault(String.self, forKey: .vehicleProfileId, default: "")
        speedSource = c.decodeDefault(String.self, forKey: .speedSource, default: "CONTROLLER")
        battDataSource = c.decodeDefault(String.self, forKey: .battDataSource, default: "CONTROLLER")
        wheelCircumferenceMm = c.decodeDefault(Double.self, forKey: .wheelCircumferenceMm, default: 1800)
        polePairs = c.decodeDefault(Int.self, forKey: .polePairs, default: 50)
        controllerBrand = c.decodeDefault(String.self, forKey: .controllerBrand, default: "auto")
        lastControllerDeviceAddress = c.decodeDefault(String.self, forKey: .lastControllerDeviceAddress, default: "")
        lastControllerDeviceName = c.decodeDefault(String.self, forKey: .lastControllerDeviceName, default: "")
        lastControllerProtocolId = c.decodeDefault(String.self, forKey: .lastControllerProtocolId, default: "")
        dashboardItems = c.decodeDefault([String].self, forKey: .dashboardItems, default: [])
        rideOverviewItems = c.decodeDefault([String].self, forKey: .rideOverviewItems, default: [])
        updatedAt = c.decodeDefault(Int64.self, forKey: .updatedAt, default: 0)
        updatedByDeviceId = c.decodeDefault(String.self, forKey: .updatedByDeviceId, default: "")
    }
}

struct SyncVehicleProfileSnapshot: Identifiable, Codable, Equatable {
    var id: String
    var name: String
    var macAddress: String = ""
    var batteryCapacityAh: Double
    var batterySeries: Int
    var wheelCircumferenceMm: Double
    var wheelRimSize: String = "10寸"
    var tireSpecLabel: String = ""
    var polePairs: Int
    var totalMileageKm: Double = 0
    var learnedEfficiencyWhKm: Double
    var learnedUsableEnergyRatio: Double
    var learnedInternalResistanceOhm: Double
    var updatedAt: Int64
    var updatedByDeviceId: String
    var isDeleted: Bool = false

    init(
        id: String,
        name: String,
        macAddress: String = "",
        batteryCapacityAh: Double,
        batterySeries: Int,
        wheelCircumferenceMm: Double,
        wheelRimSize: String = "10寸",
        tireSpecLabel: String = "",
        polePairs: Int,
        totalMileageKm: Double = 0,
        learnedEfficiencyWhKm: Double,
        learnedUsableEnergyRatio: Double,
        learnedInternalResistanceOhm: Double,
        updatedAt: Int64,
        updatedByDeviceId: String,
        isDeleted: Bool = false
    ) {
        self.id = id
        self.name = name
        self.macAddress = macAddress
        self.batteryCapacityAh = batteryCapacityAh
        self.batterySeries = batterySeries
        self.wheelCircumferenceMm = wheelCircumferenceMm
        self.wheelRimSize = wheelRimSize
        self.tireSpecLabel = tireSpecLabel
        self.polePairs = polePairs
        self.totalMileageKm = totalMileageKm
        self.learnedEfficiencyWhKm = learnedEfficiencyWhKm
        self.learnedUsableEnergyRatio = learnedUsableEnergyRatio
        self.learnedInternalResistanceOhm = learnedInternalResistanceOhm
        self.updatedAt = updatedAt
        self.updatedByDeviceId = updatedByDeviceId
        self.isDeleted = isDeleted
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = c.decodeDefault(String.self, forKey: .id, default: UUID().uuidString)
        name = c.decodeDefault(String.self, forKey: .name, default: "SmartDash")
        macAddress = c.decodeDefault(String.self, forKey: .macAddress, default: "")
        batteryCapacityAh = c.decodeDefault(Double.self, forKey: .batteryCapacityAh, default: 35)
        batterySeries = c.decodeDefault(Int.self, forKey: .batterySeries, default: 20)
        wheelCircumferenceMm = c.decodeDefault(Double.self, forKey: .wheelCircumferenceMm, default: 1800)
        wheelRimSize = c.decodeDefault(String.self, forKey: .wheelRimSize, default: "10寸")
        tireSpecLabel = c.decodeDefault(String.self, forKey: .tireSpecLabel, default: "")
        polePairs = c.decodeDefault(Int.self, forKey: .polePairs, default: 50)
        totalMileageKm = c.decodeDefault(Double.self, forKey: .totalMileageKm, default: 0)
        learnedEfficiencyWhKm = c.decodeDefault(Double.self, forKey: .learnedEfficiencyWhKm, default: 35)
        learnedUsableEnergyRatio = c.decodeDefault(Double.self, forKey: .learnedUsableEnergyRatio, default: 0.85)
        learnedInternalResistanceOhm = c.decodeDefault(Double.self, forKey: .learnedInternalResistanceOhm, default: 0)
        updatedAt = c.decodeDefault(Int64.self, forKey: .updatedAt, default: 0)
        updatedByDeviceId = c.decodeDefault(String.self, forKey: .updatedByDeviceId, default: "")
        isDeleted = c.decodeDefault(Bool.self, forKey: .isDeleted, default: false)
    }
}

struct SyncRideSnapshot: Identifiable, Codable, Equatable {
    var id: String
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
    var tractionEnergyWh: Double = 0
    var regenEnergyWh: Double = 0
    var avgEfficiencyWhKm: Double
    var avgNetEfficiencyWhKm: Double
    var avgTractionEfficiencyWhKm: Double = 0
    var maxControllerTemp: Double = 0
    var updatedAt: Int64
    var updatedByDeviceId: String
    var summaryRevision: Int = 1
    var detailSchemaRevision: Int = 1
    var detailFingerprint: String = ""
    var completenessScore: Double = 1
    var sampleCount: Int = 0
    var trackPoints: [SyncRideTrackPoint] = []
    var samples: [SyncRideMetricSample] = []
    var isDeleted: Bool = false

    init(
        id: String,
        vehicleProfileId: String,
        title: String,
        startedAtMs: Int64,
        endedAtMs: Int64,
        durationMs: Int64,
        distanceMeters: Double,
        maxSpeedKmh: Double,
        avgSpeedKmh: Double,
        peakPowerKw: Double,
        totalEnergyWh: Double,
        tractionEnergyWh: Double = 0,
        regenEnergyWh: Double = 0,
        avgEfficiencyWhKm: Double,
        avgNetEfficiencyWhKm: Double,
        avgTractionEfficiencyWhKm: Double = 0,
        maxControllerTemp: Double = 0,
        updatedAt: Int64,
        updatedByDeviceId: String,
        summaryRevision: Int = 1,
        detailSchemaRevision: Int = 1,
        detailFingerprint: String = "",
        completenessScore: Double = 1,
        sampleCount: Int = 0,
        trackPoints: [SyncRideTrackPoint] = [],
        samples: [SyncRideMetricSample] = [],
        isDeleted: Bool = false
    ) {
        self.id = id
        self.vehicleProfileId = vehicleProfileId
        self.title = title
        self.startedAtMs = startedAtMs
        self.endedAtMs = endedAtMs
        self.durationMs = durationMs
        self.distanceMeters = distanceMeters
        self.maxSpeedKmh = maxSpeedKmh
        self.avgSpeedKmh = avgSpeedKmh
        self.peakPowerKw = peakPowerKw
        self.totalEnergyWh = totalEnergyWh
        self.tractionEnergyWh = tractionEnergyWh
        self.regenEnergyWh = regenEnergyWh
        self.avgEfficiencyWhKm = avgEfficiencyWhKm
        self.avgNetEfficiencyWhKm = avgNetEfficiencyWhKm
        self.avgTractionEfficiencyWhKm = avgTractionEfficiencyWhKm
        self.maxControllerTemp = maxControllerTemp
        self.updatedAt = updatedAt
        self.updatedByDeviceId = updatedByDeviceId
        self.summaryRevision = summaryRevision
        self.detailSchemaRevision = detailSchemaRevision
        self.detailFingerprint = detailFingerprint
        self.completenessScore = completenessScore
        self.sampleCount = sampleCount
        self.trackPoints = trackPoints
        self.samples = samples
        self.isDeleted = isDeleted
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = c.decodeDefault(String.self, forKey: .id, default: UUID().uuidString)
        vehicleProfileId = c.decodeDefault(String.self, forKey: .vehicleProfileId, default: "")
        title = c.decodeDefault(String.self, forKey: .title, default: "骑行")
        startedAtMs = c.decodeDefault(Int64.self, forKey: .startedAtMs, default: 0)
        endedAtMs = c.decodeDefault(Int64.self, forKey: .endedAtMs, default: startedAtMs)
        durationMs = c.decodeDefault(Int64.self, forKey: .durationMs, default: max(0, endedAtMs - startedAtMs))
        distanceMeters = c.decodeDefault(Double.self, forKey: .distanceMeters, default: 0)
        maxSpeedKmh = c.decodeDefault(Double.self, forKey: .maxSpeedKmh, default: 0)
        avgSpeedKmh = c.decodeDefault(Double.self, forKey: .avgSpeedKmh, default: 0)
        peakPowerKw = c.decodeDefault(Double.self, forKey: .peakPowerKw, default: 0)
        totalEnergyWh = c.decodeDefault(Double.self, forKey: .totalEnergyWh, default: 0)
        tractionEnergyWh = c.decodeDefault(Double.self, forKey: .tractionEnergyWh, default: max(0, totalEnergyWh))
        regenEnergyWh = c.decodeDefault(Double.self, forKey: .regenEnergyWh, default: 0)
        avgEfficiencyWhKm = c.decodeDefault(Double.self, forKey: .avgEfficiencyWhKm, default: 0)
        avgNetEfficiencyWhKm = c.decodeDefault(Double.self, forKey: .avgNetEfficiencyWhKm, default: avgEfficiencyWhKm)
        avgTractionEfficiencyWhKm = c.decodeDefault(Double.self, forKey: .avgTractionEfficiencyWhKm, default: avgEfficiencyWhKm)
        maxControllerTemp = c.decodeDefault(Double.self, forKey: .maxControllerTemp, default: 0)
        updatedAt = c.decodeDefault(Int64.self, forKey: .updatedAt, default: endedAtMs)
        updatedByDeviceId = c.decodeDefault(String.self, forKey: .updatedByDeviceId, default: "")
        summaryRevision = c.decodeDefault(Int.self, forKey: .summaryRevision, default: 1)
        detailSchemaRevision = c.decodeDefault(Int.self, forKey: .detailSchemaRevision, default: 1)
        detailFingerprint = c.decodeDefault(String.self, forKey: .detailFingerprint, default: "")
        completenessScore = c.decodeDefault(Double.self, forKey: .completenessScore, default: 1)
        sampleCount = c.decodeDefault(Int.self, forKey: .sampleCount, default: 0)
        trackPoints = c.decodeDefault([SyncRideTrackPoint].self, forKey: .trackPoints, default: [])
        samples = c.decodeDefault([SyncRideMetricSample].self, forKey: .samples, default: [])
        isDeleted = c.decodeDefault(Bool.self, forKey: .isDeleted, default: false)
    }
}

struct SyncSpeedTestSnapshot: Identifiable, Codable, Equatable {
    var id: String
    var vehicleProfileId: String
    var label: String
    var startedAtMs: Int64
    var endedAtMs: Int64
    var targetSpeedKmh: Double
    var achievedSpeedKmh: Double
    var timeToTargetMs: Int64
    var distanceMeters: Double
    var peakPowerKw: Double
    var peakBusCurrentA: Double
    var minVoltageV: Double
    var updatedAt: Int64
    var updatedByDeviceId: String
    var trackPoints: [SyncSpeedTestTrackPoint] = []
    var isDeleted: Bool = false

    init(
        id: String,
        vehicleProfileId: String,
        label: String,
        startedAtMs: Int64,
        endedAtMs: Int64,
        targetSpeedKmh: Double,
        achievedSpeedKmh: Double,
        timeToTargetMs: Int64,
        distanceMeters: Double,
        peakPowerKw: Double,
        peakBusCurrentA: Double,
        minVoltageV: Double,
        updatedAt: Int64,
        updatedByDeviceId: String,
        trackPoints: [SyncSpeedTestTrackPoint] = [],
        isDeleted: Bool = false
    ) {
        self.id = id
        self.vehicleProfileId = vehicleProfileId
        self.label = label
        self.startedAtMs = startedAtMs
        self.endedAtMs = endedAtMs
        self.targetSpeedKmh = targetSpeedKmh
        self.achievedSpeedKmh = achievedSpeedKmh
        self.timeToTargetMs = timeToTargetMs
        self.distanceMeters = distanceMeters
        self.peakPowerKw = peakPowerKw
        self.peakBusCurrentA = peakBusCurrentA
        self.minVoltageV = minVoltageV
        self.updatedAt = updatedAt
        self.updatedByDeviceId = updatedByDeviceId
        self.trackPoints = trackPoints
        self.isDeleted = isDeleted
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = c.decodeDefault(String.self, forKey: .id, default: UUID().uuidString)
        vehicleProfileId = c.decodeDefault(String.self, forKey: .vehicleProfileId, default: "")
        label = c.decodeDefault(String.self, forKey: .label, default: "0-50 km/h")
        startedAtMs = c.decodeDefault(Int64.self, forKey: .startedAtMs, default: 0)
        endedAtMs = c.decodeDefault(Int64.self, forKey: .endedAtMs, default: startedAtMs)
        targetSpeedKmh = c.decodeDefault(Double.self, forKey: .targetSpeedKmh, default: 50)
        achievedSpeedKmh = c.decodeDefault(Double.self, forKey: .achievedSpeedKmh, default: 0)
        timeToTargetMs = c.decodeDefault(Int64.self, forKey: .timeToTargetMs, default: 0)
        distanceMeters = c.decodeDefault(Double.self, forKey: .distanceMeters, default: 0)
        peakPowerKw = c.decodeDefault(Double.self, forKey: .peakPowerKw, default: 0)
        peakBusCurrentA = c.decodeDefault(Double.self, forKey: .peakBusCurrentA, default: 0)
        minVoltageV = c.decodeDefault(Double.self, forKey: .minVoltageV, default: 0)
        updatedAt = c.decodeDefault(Int64.self, forKey: .updatedAt, default: endedAtMs)
        updatedByDeviceId = c.decodeDefault(String.self, forKey: .updatedByDeviceId, default: "")
        trackPoints = c.decodeDefault([SyncSpeedTestTrackPoint].self, forKey: .trackPoints, default: [])
        isDeleted = c.decodeDefault(Bool.self, forKey: .isDeleted, default: false)
    }
}

struct SyncRideTrackPoint: Codable, Equatable {
    var latitude: Double
    var longitude: Double
}

struct SyncRideMetricSample: Codable, Equatable {
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
    var estimatedRangeKm: Double = 0
    var rpm: Double
    var efficiencyWhKm: Double
    var avgEfficiencyWhKm: Double = 0
    var avgNetEfficiencyWhKm: Double = 0
    var avgTractionEfficiencyWhKm: Double = 0
    var distanceMeters: Double
    var totalEnergyWh: Double = 0
    var tractionEnergyWh: Double = 0
    var regenEnergyWh: Double = 0
    var recoveredEnergyWh: Double = 0
    var maxControllerTemp: Double = 0
    var gradePercent: Double?
    var altitudeMeters: Double?
    var latitude: Double?
    var longitude: Double?

    init(
        elapsedMs: Int64,
        timestampMs: Int64,
        speedKmH: Double,
        powerKw: Double,
        voltage: Double,
        voltageSag: Double,
        busCurrent: Double,
        phaseCurrent: Double,
        controllerTemp: Double,
        soc: Double,
        estimatedRangeKm: Double = 0,
        rpm: Double,
        efficiencyWhKm: Double,
        avgEfficiencyWhKm: Double = 0,
        avgNetEfficiencyWhKm: Double = 0,
        avgTractionEfficiencyWhKm: Double = 0,
        distanceMeters: Double,
        totalEnergyWh: Double = 0,
        tractionEnergyWh: Double = 0,
        regenEnergyWh: Double = 0,
        recoveredEnergyWh: Double = 0,
        maxControllerTemp: Double = 0,
        gradePercent: Double? = nil,
        altitudeMeters: Double? = nil,
        latitude: Double? = nil,
        longitude: Double? = nil
    ) {
        self.elapsedMs = elapsedMs
        self.timestampMs = timestampMs
        self.speedKmH = speedKmH
        self.powerKw = powerKw
        self.voltage = voltage
        self.voltageSag = voltageSag
        self.busCurrent = busCurrent
        self.phaseCurrent = phaseCurrent
        self.controllerTemp = controllerTemp
        self.soc = soc
        self.estimatedRangeKm = estimatedRangeKm
        self.rpm = rpm
        self.efficiencyWhKm = efficiencyWhKm
        self.avgEfficiencyWhKm = avgEfficiencyWhKm
        self.avgNetEfficiencyWhKm = avgNetEfficiencyWhKm
        self.avgTractionEfficiencyWhKm = avgTractionEfficiencyWhKm
        self.distanceMeters = distanceMeters
        self.totalEnergyWh = totalEnergyWh
        self.tractionEnergyWh = tractionEnergyWh
        self.regenEnergyWh = regenEnergyWh
        self.recoveredEnergyWh = recoveredEnergyWh
        self.maxControllerTemp = maxControllerTemp
        self.gradePercent = gradePercent
        self.altitudeMeters = altitudeMeters
        self.latitude = latitude
        self.longitude = longitude
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        elapsedMs = c.decodeDefault(Int64.self, forKey: .elapsedMs, default: 0)
        timestampMs = c.decodeDefault(Int64.self, forKey: .timestampMs, default: 0)
        speedKmH = c.decodeDefault(Double.self, forKey: .speedKmH, default: 0)
        powerKw = c.decodeDefault(Double.self, forKey: .powerKw, default: 0)
        voltage = c.decodeDefault(Double.self, forKey: .voltage, default: 0)
        voltageSag = c.decodeDefault(Double.self, forKey: .voltageSag, default: 0)
        busCurrent = c.decodeDefault(Double.self, forKey: .busCurrent, default: 0)
        phaseCurrent = c.decodeDefault(Double.self, forKey: .phaseCurrent, default: 0)
        controllerTemp = c.decodeDefault(Double.self, forKey: .controllerTemp, default: 0)
        soc = c.decodeDefault(Double.self, forKey: .soc, default: 0)
        estimatedRangeKm = c.decodeDefault(Double.self, forKey: .estimatedRangeKm, default: 0)
        rpm = c.decodeDefault(Double.self, forKey: .rpm, default: 0)
        efficiencyWhKm = c.decodeDefault(Double.self, forKey: .efficiencyWhKm, default: 0)
        avgEfficiencyWhKm = c.decodeDefault(Double.self, forKey: .avgEfficiencyWhKm, default: 0)
        avgNetEfficiencyWhKm = c.decodeDefault(Double.self, forKey: .avgNetEfficiencyWhKm, default: avgEfficiencyWhKm)
        avgTractionEfficiencyWhKm = c.decodeDefault(Double.self, forKey: .avgTractionEfficiencyWhKm, default: avgEfficiencyWhKm)
        distanceMeters = c.decodeDefault(Double.self, forKey: .distanceMeters, default: 0)
        totalEnergyWh = c.decodeDefault(Double.self, forKey: .totalEnergyWh, default: 0)
        tractionEnergyWh = c.decodeDefault(Double.self, forKey: .tractionEnergyWh, default: max(0, totalEnergyWh))
        regenEnergyWh = c.decodeDefault(Double.self, forKey: .regenEnergyWh, default: 0)
        recoveredEnergyWh = c.decodeDefault(Double.self, forKey: .recoveredEnergyWh, default: regenEnergyWh)
        maxControllerTemp = c.decodeDefault(Double.self, forKey: .maxControllerTemp, default: controllerTemp)
        gradePercent = try? c.decodeIfPresent(Double.self, forKey: .gradePercent)
        altitudeMeters = try? c.decodeIfPresent(Double.self, forKey: .altitudeMeters)
        latitude = try? c.decodeIfPresent(Double.self, forKey: .latitude)
        longitude = try? c.decodeIfPresent(Double.self, forKey: .longitude)
    }
}

struct SyncSpeedTestTrackPoint: Codable, Equatable {
    var latitude: Double
    var longitude: Double
}

struct DriveChangeManifest: Codable, Equatable {
    var schemaVersion: Int = 2
    var stateVersion: Int64
    var updatedAt: Int64
    var updatedByDeviceId: String
    var updatedByDeviceName: String
    var checksum: String = ""
    var latestRideId: String?
    var latestRideEndedAt: Int64?
    var latestSpeedTestId: String?
    var entityCounters: EntityCounters = .init()
    var currentStateFileName: String = "current_state.json.enc"
}

struct EntityCounters: Codable, Equatable {
    var rideCount: Int = 0
    var speedTestCount: Int = 0
    var vehicleProfileCount: Int = 0
}

struct EncryptedBackup: Codable, Equatable {
    var version: Int
    var algorithm: String = "AES-256-GCM"
    var salt: String
    var iv: String
    var cipherText: String
    var tag: String
}
