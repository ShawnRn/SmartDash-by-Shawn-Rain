import CloudKit
import Combine
import CoreData
import Foundation
import UIKit

@MainActor
final class SmartDashPersistence: ObservableObject {
    static let shared = SmartDashPersistence()

    private let localURL: URL
    @Published private(set) var iCloudStatus: CloudSyncStatus = .configurationRequired("需要开启 iCloud/CloudKit capability")

    init() {
        let base = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
        localURL = base.appendingPathComponent("SmartDashState.json")
        try? FileManager.default.createDirectory(at: base, withIntermediateDirectories: true)
    }

    func load() -> PersistedSmartDashState {
        guard let data = try? Data(contentsOf: localURL),
              let state = try? JSONDecoder().decode(PersistedSmartDashState.self, from: data) else {
            return PersistedSmartDashState()
        }
        return state
    }

    func save(_ state: PersistedSmartDashState) {
        do {
            let data = try JSONEncoder().encode(state)
            try data.write(to: localURL, options: [.atomic])
        } catch {
            AppLog.shared.error("Local save failed: \(error.localizedDescription)")
        }
    }

    func refreshICloudStatus() async {
        guard isCloudKitRuntimeEnabled else {
            iCloudStatus = .configurationRequired("未开启 iCloud/CloudKit capability")
            return
        }
        do {
            let status = try await CKContainer.default().accountStatus()
            switch status {
            case .available:
                iCloudStatus = .ready("iCloud 可用")
            case .noAccount:
                iCloudStatus = .failed("未登录 iCloud")
            case .restricted:
                iCloudStatus = .failed("iCloud 受限")
            case .couldNotDetermine:
                iCloudStatus = .failed("无法确认 iCloud 状态")
            case .temporarilyUnavailable:
                iCloudStatus = .failed("iCloud 暂时不可用")
            @unknown default:
                iCloudStatus = .failed("未知 iCloud 状态")
            }
        } catch {
            iCloudStatus = .failed(error.localizedDescription)
        }
    }

    private var isCloudKitRuntimeEnabled: Bool {
        Bundle.main.object(forInfoDictionaryKey: "SmartDashEnableCloudKit") as? Bool == true
    }
}

struct PersistedSmartDashState: Codable, Equatable {
    var deviceId: String = UUID().uuidString
    var deviceName: String = UIDevice.current.name
    var localStateVersion: Int64 = 1
    var settings: SmartDashSettings = .init()
    var activeVehicleProfileId: String = ""
    var vehicleProfiles: [VehicleProfile] = [VehicleProfile()]
    var rides: [RideHistoryRecord] = []
    var speedTests: [SpeedTestRecord] = []

    mutating func normalize() {
        if activeVehicleProfileId.isEmpty || !vehicleProfiles.contains(where: { $0.id == activeVehicleProfileId }) {
            activeVehicleProfileId = vehicleProfiles.first?.id ?? ""
        }
        for index in vehicleProfiles.indices {
            vehicleProfiles[index].sanitize()
        }
    }
}

extension PersistedSmartDashState {
    func toDriveState() -> DriveCurrentState {
        let now = Date.nowMs
        let active = vehicleProfiles.first(where: { $0.id == activeVehicleProfileId }) ?? vehicleProfiles.first ?? VehicleProfile()
        return DriveCurrentState(
            stateVersion: localStateVersion,
            updatedAt: now,
            updatedByDeviceId: deviceId,
            updatedByDeviceName: deviceName,
            activeVehicleProfileId: active.id,
            settings: SyncSettingsSnapshot(
                speedSource: settings.speedSource.rawValue,
                battDataSource: settings.battDataSource.rawValue,
                wheelCircumferenceMm: active.wheelCircumferenceMm,
                polePairs: active.polePairs,
                controllerBrand: settings.controllerBrand,
                logLevel: settings.logLevel,
                dashboardItems: settings.dashboardItems,
                rideOverviewItems: settings.rideOverviewItems,
                posterTemplateId: settings.posterTemplateId,
                posterAspectRatio: settings.posterAspectRatio,
                posterShowTrack: settings.posterShowTrack,
                posterShowWatermark: settings.posterShowWatermark,
                learnedEfficiencyWhKm: active.learnedEfficiencyWhKm,
                learnedUsableEnergyRatio: active.learnedUsableEnergyRatio,
                updatedAt: now,
                updatedByDeviceId: deviceId
            ),
            vehicleSettings: vehicleProfiles.map {
                SyncVehicleSettingsSnapshot(
                    vehicleProfileId: $0.id,
                    speedSource: settings.speedSource.rawValue,
                    battDataSource: settings.battDataSource.rawValue,
                    wheelCircumferenceMm: $0.wheelCircumferenceMm,
                    polePairs: $0.polePairs,
                    controllerBrand: settings.controllerBrand,
                    dashboardItems: settings.dashboardItems,
                    rideOverviewItems: settings.rideOverviewItems,
                    updatedAt: $0.lastModified,
                    updatedByDeviceId: deviceId
                )
            },
            vehicleProfiles: vehicleProfiles.map {
                SyncVehicleProfileSnapshot(
                    id: $0.id,
                    name: $0.name,
                    macAddress: $0.macAddress,
                    batteryCapacityAh: $0.batteryCapacityAh,
                    batterySeries: $0.batterySeries,
                    wheelCircumferenceMm: $0.wheelCircumferenceMm,
                    wheelRimSize: $0.wheelRimSize,
                    tireSpecLabel: $0.tireSpecLabel,
                    polePairs: $0.polePairs,
                    totalMileageKm: $0.totalMileageKm,
                    learnedEfficiencyWhKm: $0.learnedEfficiencyWhKm,
                    learnedUsableEnergyRatio: $0.learnedUsableEnergyRatio,
                    learnedInternalResistanceOhm: $0.learnedInternalResistanceOhm,
                    updatedAt: $0.lastModified,
                    updatedByDeviceId: deviceId
                )
            },
            rides: rides.map(\.syncSnapshot),
            speedTests: speedTests.map(\.syncSnapshot)
        )
    }

    mutating func applyDriveState(_ drive: DriveCurrentState) {
        activeVehicleProfileId = drive.activeVehicleProfileId
        settings.speedSource = SpeedSource(rawValue: drive.settings.speedSource) ?? .controller
        settings.battDataSource = BatteryDataSource(rawValue: drive.settings.battDataSource) ?? .controller
        settings.controllerBrand = drive.settings.controllerBrand
        settings.logLevel = drive.settings.logLevel
        settings.dashboardItems = drive.settings.dashboardItems
        settings.rideOverviewItems = drive.settings.rideOverviewItems
        settings.posterTemplateId = drive.settings.posterTemplateId
        settings.posterAspectRatio = drive.settings.posterAspectRatio
        settings.posterShowTrack = drive.settings.posterShowTrack
        settings.posterShowWatermark = drive.settings.posterShowWatermark
        vehicleProfiles = drive.vehicleProfiles.filter { !$0.isDeleted }.map {
            VehicleProfile(
                id: $0.id,
                name: $0.name,
                macAddress: $0.macAddress,
                batteryCapacityAh: $0.batteryCapacityAh,
                batterySeries: $0.batterySeries,
                wheelCircumferenceMm: $0.wheelCircumferenceMm,
                wheelRimSize: $0.wheelRimSize,
                tireSpecLabel: $0.tireSpecLabel,
                polePairs: $0.polePairs,
                totalMileageKm: $0.totalMileageKm,
                learnedEfficiencyWhKm: $0.learnedEfficiencyWhKm,
                learnedUsableEnergyRatio: $0.learnedUsableEnergyRatio,
                learnedInternalResistanceOhm: $0.learnedInternalResistanceOhm,
                lastModified: $0.updatedAt
            )
        }
        rides = drive.rides.filter { !$0.isDeleted }.map(\.rideRecord)
        speedTests = drive.speedTests.filter { !$0.isDeleted }.map(\.speedTestRecord)
        localStateVersion = max(localStateVersion, drive.stateVersion)
        normalize()
    }
}

private extension SyncRideSnapshot {
    var rideRecord: RideHistoryRecord {
        RideHistoryRecord(
            id: id,
            vehicleProfileId: vehicleProfileId,
            title: title,
            startedAtMs: startedAtMs,
            endedAtMs: endedAtMs,
            durationMs: durationMs,
            distanceMeters: distanceMeters,
            maxSpeedKmh: maxSpeedKmh,
            avgSpeedKmh: avgSpeedKmh,
            peakPowerKw: peakPowerKw,
            totalEnergyWh: totalEnergyWh,
            tractionEnergyWh: tractionEnergyWh,
            regenEnergyWh: regenEnergyWh,
            avgEfficiencyWhKm: avgEfficiencyWhKm,
            avgNetEfficiencyWhKm: avgNetEfficiencyWhKm,
            avgTractionEfficiencyWhKm: avgTractionEfficiencyWhKm,
            maxControllerTemp: maxControllerTemp,
            samples: samples.map(\.rideMetricSample),
            updatedAt: updatedAt
        )
    }
}

private extension SyncRideMetricSample {
    var rideMetricSample: RideMetricSample {
        RideMetricSample(
            elapsedMs: elapsedMs,
            timestampMs: timestampMs,
            speedKmH: speedKmH,
            powerKw: powerKw,
            voltage: voltage,
            voltageSag: voltageSag,
            busCurrent: busCurrent,
            phaseCurrent: phaseCurrent,
            controllerTemp: controllerTemp,
            soc: soc,
            estimatedRangeKm: estimatedRangeKm,
            rpm: rpm,
            efficiencyWhKm: efficiencyWhKm,
            avgEfficiencyWhKm: avgEfficiencyWhKm,
            avgNetEfficiencyWhKm: avgNetEfficiencyWhKm,
            avgTractionEfficiencyWhKm: avgTractionEfficiencyWhKm,
            distanceMeters: distanceMeters,
            totalEnergyWh: totalEnergyWh,
            tractionEnergyWh: tractionEnergyWh,
            regenEnergyWh: regenEnergyWh,
            recoveredEnergyWh: recoveredEnergyWh,
            maxControllerTemp: maxControllerTemp,
            gradePercent: gradePercent,
            altitudeMeters: altitudeMeters,
            latitude: latitude,
            longitude: longitude
        )
    }
}

private extension SyncSpeedTestSnapshot {
    var speedTestRecord: SpeedTestRecord {
        SpeedTestRecord(
            id: id,
            vehicleProfileId: vehicleProfileId,
            label: label,
            startedAtMs: startedAtMs,
            endedAtMs: endedAtMs,
            targetSpeedKmh: targetSpeedKmh,
            achievedSpeedKmh: achievedSpeedKmh,
            timeToTargetMs: timeToTargetMs,
            distanceMeters: distanceMeters,
            peakPowerKw: peakPowerKw,
            peakBusCurrentA: peakBusCurrentA,
            minVoltageV: minVoltageV,
            updatedAt: updatedAt
        )
    }
}

private extension RideHistoryRecord {
    var syncSnapshot: SyncRideSnapshot {
        SyncRideSnapshot(
            id: id,
            vehicleProfileId: vehicleProfileId,
            title: title,
            startedAtMs: startedAtMs,
            endedAtMs: endedAtMs,
            durationMs: durationMs,
            distanceMeters: distanceMeters,
            maxSpeedKmh: maxSpeedKmh,
            avgSpeedKmh: avgSpeedKmh,
            peakPowerKw: peakPowerKw,
            totalEnergyWh: totalEnergyWh,
            tractionEnergyWh: tractionEnergyWh,
            regenEnergyWh: regenEnergyWh,
            avgEfficiencyWhKm: avgEfficiencyWhKm,
            avgNetEfficiencyWhKm: avgNetEfficiencyWhKm,
            avgTractionEfficiencyWhKm: avgTractionEfficiencyWhKm,
            maxControllerTemp: maxControllerTemp,
            updatedAt: updatedAt,
            updatedByDeviceId: "ios",
            sampleCount: samples.count,
            samples: samples.map(\.syncSample)
        )
    }
}

private extension RideMetricSample {
    var syncSample: SyncRideMetricSample {
        SyncRideMetricSample(
            elapsedMs: elapsedMs,
            timestampMs: timestampMs,
            speedKmH: speedKmH,
            powerKw: powerKw,
            voltage: voltage,
            voltageSag: voltageSag,
            busCurrent: busCurrent,
            phaseCurrent: phaseCurrent,
            controllerTemp: controllerTemp,
            soc: soc,
            estimatedRangeKm: estimatedRangeKm,
            rpm: rpm,
            efficiencyWhKm: efficiencyWhKm,
            avgEfficiencyWhKm: avgEfficiencyWhKm,
            avgNetEfficiencyWhKm: avgNetEfficiencyWhKm,
            avgTractionEfficiencyWhKm: avgTractionEfficiencyWhKm,
            distanceMeters: distanceMeters,
            totalEnergyWh: totalEnergyWh,
            tractionEnergyWh: tractionEnergyWh,
            regenEnergyWh: regenEnergyWh,
            recoveredEnergyWh: recoveredEnergyWh,
            maxControllerTemp: maxControllerTemp,
            gradePercent: gradePercent,
            altitudeMeters: altitudeMeters,
            latitude: latitude,
            longitude: longitude
        )
    }
}

private extension SpeedTestRecord {
    var syncSnapshot: SyncSpeedTestSnapshot {
        SyncSpeedTestSnapshot(
            id: id,
            vehicleProfileId: vehicleProfileId,
            label: label,
            startedAtMs: startedAtMs,
            endedAtMs: endedAtMs,
            targetSpeedKmh: targetSpeedKmh,
            achievedSpeedKmh: achievedSpeedKmh,
            timeToTargetMs: timeToTargetMs,
            distanceMeters: distanceMeters,
            peakPowerKw: peakPowerKw,
            peakBusCurrentA: peakBusCurrentA,
            minVoltageV: minVoltageV,
            updatedAt: updatedAt,
            updatedByDeviceId: "ios"
        )
    }
}
