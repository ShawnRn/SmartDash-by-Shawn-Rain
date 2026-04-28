import Foundation

struct DriveStateMerger {
    func merge(local: DriveCurrentState, remote: DriveCurrentState, deviceId: String, deviceName: String) -> DriveCurrentState {
        let now = Date.nowMs
        let settings = remote.settings.updatedAt >= local.settings.updatedAt ? remote.settings : local.settings

        return DriveCurrentState(
            schemaVersion: max(local.schemaVersion, remote.schemaVersion),
            stateVersion: max(local.stateVersion, remote.stateVersion) + 1,
            updatedAt: now,
            updatedByDeviceId: deviceId,
            updatedByDeviceName: deviceName,
            activeVehicleProfileId: newestActiveVehicle(local: local, remote: remote),
            settings: settings,
            vehicleSettings: mergeById(local.vehicleSettings, remote.vehicleSettings, id: \.vehicleProfileId, updatedAt: \.updatedAt),
            vehicleProfiles: mergeVehicleProfiles(local.vehicleProfiles, remote.vehicleProfiles),
            rides: mergeById(local.rides, remote.rides, id: \.id, updatedAt: \.updatedAt),
            speedTests: mergeById(local.speedTests, remote.speedTests, id: \.id, updatedAt: \.updatedAt)
        )
    }

    private func newestActiveVehicle(local: DriveCurrentState, remote: DriveCurrentState) -> String {
        remote.updatedAt >= local.updatedAt ? remote.activeVehicleProfileId : local.activeVehicleProfileId
    }

    private func mergeById<T, ID: Hashable>(_ local: [T], _ remote: [T], id: KeyPath<T, ID>, updatedAt: KeyPath<T, Int64>) -> [T] {
        var merged: [ID: T] = [:]
        for item in local { merged[item[keyPath: id]] = item }
        for item in remote {
            let key = item[keyPath: id]
            if let existing = merged[key] {
                merged[key] = item[keyPath: updatedAt] >= existing[keyPath: updatedAt] ? item : existing
            } else {
                merged[key] = item
            }
        }
        return Array(merged.values)
    }

    private func mergeVehicleProfiles(_ local: [SyncVehicleProfileSnapshot], _ remote: [SyncVehicleProfileSnapshot]) -> [SyncVehicleProfileSnapshot] {
        var merged: [String: SyncVehicleProfileSnapshot] = [:]
        for profile in local { merged[profile.id] = profile }
        for profile in remote {
            guard var existing = merged[profile.id] else {
                merged[profile.id] = profile
                continue
            }
            let newest = profile.updatedAt >= existing.updatedAt ? profile : existing
            let oldest = profile.updatedAt >= existing.updatedAt ? existing : profile
            existing = newest
            existing.totalMileageKm = max(newest.totalMileageKm, oldest.totalMileageKm)
            existing.learnedEfficiencyWhKm = newest.learnedEfficiencyWhKm.finiteOr(oldest.learnedEfficiencyWhKm)
            existing.learnedUsableEnergyRatio = newest.learnedUsableEnergyRatio.finiteOr(oldest.learnedUsableEnergyRatio)
            existing.learnedInternalResistanceOhm = max(newest.learnedInternalResistanceOhm.finiteOr(0), oldest.learnedInternalResistanceOhm.finiteOr(0))
            merged[profile.id] = existing
        }
        return Array(merged.values)
    }
}
