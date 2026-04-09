package com.shawnrain.sdash.data.sync

import android.content.Context
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.VehicleProfile
import com.shawnrain.sdash.data.history.RideMetricSampleSchema
import com.shawnrain.sdash.data.history.RideHistoryRecord
import com.shawnrain.sdash.data.speedtest.SpeedTestRecord
import com.shawnrain.sdash.debug.AppLogger

/**
 * Merges local and remote DriveCurrentState with entity-level conflict resolution.
 * Uses Last-Write-Wins (LWW) with additional semantics for rides (completeness-based).
 */
class DriveStateMerger(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    companion object {
        private const val TAG = "DriveStateMerger"

        internal fun selectRideWinner(
            localRide: SyncRideSnapshot,
            remoteRide: SyncRideSnapshot
        ): SyncRideSnapshot = when {
            remoteRide.isDeleted && !localRide.isDeleted -> localRide
            localRide.isDeleted -> remoteRide
            remoteRide.summaryRevision > localRide.summaryRevision -> remoteRide
            localRide.summaryRevision > remoteRide.summaryRevision -> localRide
            shouldPreferCandidateByRideDetail(remoteRide, localRide) -> remoteRide
            shouldPreferCandidateByRideDetail(localRide, remoteRide) -> localRide
            rideDetailScore(remoteRide) > rideDetailScore(localRide) -> remoteRide
            rideDetailScore(localRide) > rideDetailScore(remoteRide) -> localRide
            remoteRide.completenessScore > localRide.completenessScore -> remoteRide
            localRide.completenessScore > remoteRide.completenessScore -> localRide
            remoteRide.updatedAt > localRide.updatedAt -> remoteRide
            else -> localRide
        }

        private fun shouldPreferCandidateByRideDetail(
            candidate: SyncRideSnapshot,
            other: SyncRideSnapshot
        ): Boolean {
            if (!rideDetailMetadataChanged(candidate, other)) return false

            val candidateCoverage = rideParameterCoverageScore(candidate)
            val otherCoverage = rideParameterCoverageScore(other)
            return when {
                candidateCoverage > otherCoverage -> true
                candidateCoverage < otherCoverage -> false
                candidate.detailSchemaRevision > other.detailSchemaRevision -> true
                candidate.detailSchemaRevision < other.detailSchemaRevision -> false
                candidate.updatedAt > other.updatedAt -> true
                else -> false
            }
        }

        private fun rideDetailMetadataChanged(
            first: SyncRideSnapshot,
            second: SyncRideSnapshot
        ): Boolean {
            if (first.detailSchemaRevision != second.detailSchemaRevision) return true
            val firstFingerprint = first.detailFingerprint.takeIf { it.isNotBlank() }
            val secondFingerprint = second.detailFingerprint.takeIf { it.isNotBlank() }
            return firstFingerprint != null && secondFingerprint != null && firstFingerprint != secondFingerprint
        }

        private fun rideParameterCoverageScore(snapshot: SyncRideSnapshot): Long {
            val sampleCoverage = snapshot.samples.sumOf { RideMetricSampleSchema.populatedFieldCount(it).toLong() }
            return (sampleCoverage * 1_000L) + snapshot.trackPoints.size.toLong()
        }

        private fun rideDetailScore(snapshot: SyncRideSnapshot): Long {
            return (snapshot.samples.size.toLong() * 1_000_000L) +
                (snapshot.trackPoints.size.toLong() * 10_000L) +
                snapshot.sampleCount.toLong()
        }

        private fun speedTestDetailScore(snapshot: SyncSpeedTestSnapshot): Int {
            return snapshot.trackPoints.size
        }
    }

    /**
     * Merge remote state into local state.
     * Returns a MergeResult with notes about what changed.
     */
    suspend fun merge(
        localState: DriveCurrentState,
        remoteState: DriveCurrentState,
        localMetadata: SyncMetadata
    ): MergeResult {
        val notes = mutableListOf<String>()
        var conflictCount = 0

        // 1. Merge settings (field-level LWW)
        val mergedSettings = mergeSettings(localState.settings, remoteState.settings, notes)
        val (mergedVehicleSettings, vehicleSettingsUpdated) = mergeVehicleSettings(
            localState.vehicleSettings,
            remoteState.vehicleSettings,
            notes
        )
        val settingsUpdated = mergedSettings != localState.settings || vehicleSettingsUpdated

        // 2. Merge vehicle profiles (by ID, LWW)
        val (mergedProfiles, profileNotes, profileConflicts) = mergeVehicleProfiles(
            localState.vehicleProfiles,
            remoteState.vehicleProfiles
        )
        notes.addAll(profileNotes)
        conflictCount += profileConflicts

        // 3. Merge rides (by ID, completeness-aware)
        val (mergedRides, rideNotes, rideConflicts, ridesAdded) = mergeRides(
            localState.rides,
            remoteState.rides
        )
        notes.addAll(rideNotes)
        conflictCount += rideConflicts

        // 4. Merge speed tests (by ID, LWW)
        val (mergedSpeedTests, speedTestNotes) = mergeSpeedTests(
            localState.speedTests,
            remoteState.speedTests
        )
        notes.addAll(speedTestNotes)

        val mergedState = remoteState.copy(
            settings = mergedSettings,
            vehicleSettings = mergedVehicleSettings,
            vehicleProfiles = mergedProfiles,
            rides = mergedRides,
            speedTests = mergedSpeedTests,
            stateVersion = maxOf(localState.stateVersion, remoteState.stateVersion) + 1,
            updatedAt = System.currentTimeMillis()
        )

        val changedLocally = settingsUpdated || profileConflicts > 0 || rideConflicts > 0
        val changedRemotely = remoteState.stateVersion > localMetadata.lastAppliedRemoteVersion

        if (notes.isEmpty()) {
            notes.add("云端和本地都没有新的资料变更")
        }

        AppLogger.i(TAG, "Merge complete: ${notes.joinToString("; ")}")

        return MergeResult(
            mergedState = mergedState,
            changedLocally = changedLocally,
            changedRemotely = changedRemotely,
            conflictCount = conflictCount,
            notes = notes,
            ridesMerged = ridesAdded,
            profilesMerged = profileConflicts,
            settingsUpdated = settingsUpdated
        )
    }

    private fun mergeSettings(
        local: SyncSettingsSnapshot,
        remote: SyncSettingsSnapshot,
        notes: MutableList<String>
    ): SyncSettingsSnapshot {
        // Simple LWW: use whichever has a later updatedAt
        return if (remote.updatedAt > local.updatedAt) {
            notes.add("已从云端更新设置")
            remote
        } else {
            local
        }
    }

    private fun mergeVehicleProfiles(
        local: List<SyncVehicleProfileSnapshot>,
        remote: List<SyncVehicleProfileSnapshot>
    ): Triple<List<SyncVehicleProfileSnapshot>, List<String>, Int> {
        val notes = mutableListOf<String>()
        var conflicts = 0
        val mergedMap = local.associateBy { it.id }.toMutableMap()

        for (remoteProfile in remote) {
            val localProfile = mergedMap[remoteProfile.id]
            if (localProfile == null) {
                // New profile from remote
                if (!remoteProfile.isDeleted) {
                    mergedMap[remoteProfile.id] = remoteProfile
                    notes.add("已新增车辆档案：${remoteProfile.name}")
                }
            } else {
                // Both exist - LWW
                if (remoteProfile.updatedAt > localProfile.updatedAt) {
                    if (remoteProfile.isDeleted) {
                        mergedMap.remove(remoteProfile.id)
                        notes.add("已删除车辆档案：${remoteProfile.name}")
                    } else {
                        mergedMap[remoteProfile.id] = remoteProfile
                        conflicts++
                        notes.add("已更新车辆档案：${remoteProfile.name}")
                    }
                }
            }
        }

        return Triple(mergedMap.values.toList(), notes, conflicts)
    }

    private fun mergeVehicleSettings(
        local: List<SyncVehicleSettingsSnapshot>,
        remote: List<SyncVehicleSettingsSnapshot>,
        notes: MutableList<String>
    ): Pair<List<SyncVehicleSettingsSnapshot>, Boolean> {
        if (local.isEmpty() && remote.isEmpty()) return emptyList<SyncVehicleSettingsSnapshot>() to false
        if (remote.isEmpty()) return local to false
        if (local.isEmpty()) {
            notes.add("已从云端更新设置")
            return remote to true
        }

        var updated = false
        val mergedMap = local.associateBy { it.vehicleProfileId }.toMutableMap()
        for (remoteSettings in remote) {
            val localSettings = mergedMap[remoteSettings.vehicleProfileId]
            if (localSettings == null || remoteSettings.updatedAt > localSettings.updatedAt) {
                mergedMap[remoteSettings.vehicleProfileId] = remoteSettings
                updated = true
            }
        }
        if (updated && notes.none { it == "已从云端更新设置" }) {
            notes.add("已从云端更新设置")
        }
        return mergedMap.values.sortedBy { it.vehicleProfileId } to updated
    }

    private fun mergeRides(
        local: List<SyncRideSnapshot>,
        remote: List<SyncRideSnapshot>
    ): Quad<List<SyncRideSnapshot>, List<String>, Int, Int> {
        val notes = mutableListOf<String>()
        var conflicts = 0
        var ridesAdded = 0
        val mergedMap = local.associateBy { it.id }.toMutableMap()

        for (remoteRide in remote) {
            val localRide = mergedMap[remoteRide.id]
            if (localRide == null) {
                // New ride from remote
                if (!remoteRide.isDeleted) {
                    mergedMap[remoteRide.id] = remoteRide
                    ridesAdded++
                    notes.add("已新增行程：${remoteRide.title}")
                }
            } else {
                // Both exist - use completeness-aware merge
                val winner = selectRideWinner(localRide, remoteRide)
                if (winner === remoteRide && winner != localRide) {
                    conflicts++
                }
                mergedMap[remoteRide.id] = winner
            }
        }

        // Sort by startedAtMs descending (newest first)
        val merged = mergedMap.values.sortedByDescending { it.startedAtMs }

        return Quad(merged, notes, conflicts, ridesAdded)
    }

    private fun mergeSpeedTests(
        local: List<SyncSpeedTestSnapshot>,
        remote: List<SyncSpeedTestSnapshot>
    ): Pair<List<SyncSpeedTestSnapshot>, List<String>> {
        val notes = mutableListOf<String>()
        val mergedMap = local.associateBy { it.id }.toMutableMap()

        for (remoteTest in remote) {
            val localTest = mergedMap[remoteTest.id]
            if (localTest == null) {
                if (!remoteTest.isDeleted) {
                    mergedMap[remoteTest.id] = remoteTest
                    notes.add("已新增测速记录：${remoteTest.label}")
                }
            } else {
                val winner = when {
                    remoteTest.isDeleted -> null
                    speedTestDetailScore(remoteTest) > speedTestDetailScore(localTest) -> remoteTest
                    speedTestDetailScore(localTest) > speedTestDetailScore(remoteTest) -> localTest
                    remoteTest.updatedAt > localTest.updatedAt -> remoteTest
                    else -> localTest
                }
                if (winner == null) {
                    mergedMap.remove(remoteTest.id)
                } else {
                    mergedMap[remoteTest.id] = winner
                    if (winner === remoteTest) {
                        notes.add("已更新测速记录：${remoteTest.label}")
                    }
                }
            }
        }

        return Pair(mergedMap.values.sortedByDescending { it.startedAtMs }, notes)
    }
}

/**
 * Simple 4-tuple for merge results.
 */
data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
