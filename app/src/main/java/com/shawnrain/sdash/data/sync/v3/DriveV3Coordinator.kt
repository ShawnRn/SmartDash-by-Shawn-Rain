package com.shawnrain.sdash.data.sync.v3

import android.content.Context
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.history.RideHistoryRecord
import com.shawnrain.sdash.data.history.RideHistoryRepository
import com.shawnrain.sdash.data.sync.EncryptedBackup
import com.shawnrain.sdash.data.sync.EncryptionService
import com.shawnrain.sdash.data.sync.GoogleDriveSyncManager
import com.shawnrain.sdash.data.sync.SyncMetadataRepository
import com.shawnrain.sdash.data.sync.PendingMutationRepository
import com.shawnrain.sdash.debug.AppLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Base64
import java.security.MessageDigest

/**
 * Drive V3 is entity-addressed: small manifests point at per-entity encrypted
 * chunks. This coordinator is the Android entrypoint for the new format while
 * the existing V2 coordinator remains as the migration fallback.
 */
class DriveV3Coordinator(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val rideHistoryRepository: RideHistoryRepository,
    private val driveSyncManager: GoogleDriveSyncManager,
    private val metadataRepository: SyncMetadataRepository,
    private val mutationRepository: PendingMutationRepository,
    private val entityStore: DriveEntityStore
) {
    companion object {
        private const val TAG = "DriveV3Coordinator"
        private val publishMutex = Mutex()
        private const val SETTINGS_ENTRY = "v3/settings/current.json.enc"
        private const val ENTITY_VEHICLE_PROFILE = "vehicle_profile"
        private const val ENTITY_CONTROLLER_BINDING = "controller_binding"
        private const val ENTITY_RIDE = "ride"
        private const val ENTITY_SPEED_TEST = "speed_test"
    }

    suspend fun buildLocalManifest(): DriveV3Manifest = withContext(Dispatchers.IO) {
        val metadata = metadataRepository.getMetadata(context)
        val rideRefs = rideHistoryRepository.listRideHistorySummaries().map { summary ->
            DriveV3EntityRef(
                type = "ride",
                id = summary.id,
                vehicleId = summary.vehicleId,
                entryName = "v3/rides/${safeToken(summary.vehicleId)}/${safeToken(summary.id)}.json.enc",
                updatedAt = summary.updatedAt,
                fingerprint = summary.detailFingerprint
            )
        }
        val vehicleProfiles = settingsRepository.vehicleProfilesSnapshot()
        val controllerBindings = settingsRepository.controllerBindingsSnapshot()
        val profileRefs = vehicleProfiles.map { profile ->
            DriveV3EntityRef(
                type = ENTITY_VEHICLE_PROFILE,
                id = profile.id,
                vehicleId = profile.id,
                entryName = vehicleProfileEntry(profile.id),
                updatedAt = profile.lastModified
            )
        }
        val bindingRefs = controllerBindings.map { binding ->
            DriveV3EntityRef(
                type = ENTITY_CONTROLLER_BINDING,
                id = binding.vehicleId,
                vehicleId = binding.vehicleId,
                entryName = controllerBindingEntry(binding.vehicleId),
                updatedAt = maxOf(binding.verifiedAt, binding.lastConnectedAt)
            )
        }
        DriveV3Manifest(
            updatedAt = System.currentTimeMillis(),
            updatedByDeviceId = metadata.deviceId,
            updatedByDeviceName = metadata.deviceName,
            entities = profileRefs + bindingRefs + rideRefs,
            counters = DriveV3Counters(
                vehicleProfileCount = vehicleProfiles.size,
                controllerBindingCount = controllerBindings.size,
                rideCount = rideRefs.size,
                speedTestCount = 0
            )
        )
    }

    suspend fun syncHealth(): SyncHealthSnapshot = withContext(Dispatchers.IO) {
        val remote = entityStore.fetchManifest()
        SyncHealthSnapshot(
            schemaVersion = remote?.schemaVersion ?: 3,
            localPendingCount = mutationRepository.getCoalescedPending().size,
            remoteRideCount = remote?.counters?.rideCount ?: 0,
            remoteVehicleCount = remote?.counters?.vehicleProfileCount ?: 0,
            remoteControllerBindingCount = remote?.counters?.controllerBindingCount ?: 0,
            lastRemoteUpdatedAt = remote?.updatedAt ?: 0L,
            lastError = metadataRepository.getMetadata(context).lastSyncError
        )
    }

    suspend fun publishManifestOnly() {
        entityStore.uploadManifest(buildLocalManifest())
    }

    suspend fun pullAndMergeRemoteSnapshot(): DriveV3PullResult? = withContext(Dispatchers.IO) {
        val manifest = entityStore.fetchManifest() ?: return@withContext null
        val password = deriveEncryptionPassword()
        var settingsMerged = 0
        var ridesMerged = 0
        var ridesSkipped = 0

        runCatching {
            val settingsPayload = downloadEncrypted(manifest.settingsEntry, password).toString(Charsets.UTF_8)
            settingsMerged = settingsRepository.mergeBackupJson(settingsPayload, rideHistoryRepository)
        }.onFailure { error ->
            AppLogger.w(TAG, "V3 settings merge skipped: ${error.message}")
        }

        val localRideFingerprints = rideHistoryRepository
            .listRideHistorySummaries()
            .associate { summary -> summary.id to summary.detailFingerprint }
        manifest.entities
            .asSequence()
            .filter { it.type == ENTITY_RIDE && it.deletedAt == 0L }
            .forEach { ref ->
                val localFingerprint = localRideFingerprints[ref.id]
                if (localFingerprint != null && ref.fingerprint.isNotBlank() && localFingerprint == ref.fingerprint) {
                    ridesSkipped += 1
                    return@forEach
                }
                runCatching {
                    val payload = downloadEncrypted(ref.entryName, password).toString(Charsets.UTF_8)
                    val json = JSONObject(payload)
                    val vehicleId = ref.vehicleId.ifBlank { json.optString("vehicleId") }
                    val record = RideHistoryRecord.fromJson(json)
                    if (vehicleId.isNotBlank() && record.id.isNotBlank()) {
                        rideHistoryRepository.upsertRide(vehicleId, record)
                        ridesMerged += 1
                    }
                }.onFailure { error ->
                    AppLogger.w(TAG, "V3 ride merge skipped: id=${ref.id} error=${error.message}")
                }
            }

        metadataRepository.recordPullSuccess(context, manifest.updatedAt)
        DriveV3PullResult(
            manifest = manifest,
            settingsMerged = settingsMerged,
            ridesMerged = ridesMerged,
            ridesSkipped = ridesSkipped
        )
    }

    suspend fun publishFullSnapshot(): DriveV3PublishResult = withContext(Dispatchers.IO) {
        publishMutex.withLock {
            publishFullSnapshotLocked()
        }
    }

    private suspend fun publishFullSnapshotLocked(): DriveV3PublishResult {
        if (!driveSyncManager.isSignedIn()) {
            throw IllegalStateException("尚未登录 Google Drive")
        }

        val metadata = metadataRepository.getMetadata(context)
        val stateVersion = maxOf(metadata.localStateVersion, metadata.lastPushedLocalVersion) + 1
        val now = System.currentTimeMillis()
        val password = deriveEncryptionPassword()
        val remoteManifest = entityStore.fetchManifest()
        val remoteRefs = remoteManifest
            ?.entities
            .orEmpty()
            .associateBy(::entityKey)
        val refs = mutableListOf<DriveV3EntityRef>()
        var uploadedEntities = 0

        suspend fun uploadEncrypted(
            entryName: String,
            payload: ByteArray,
            existingRef: DriveV3EntityRef? = null
        ): String {
            val encrypted = EncryptionService.encryptWithPassword(
                plainText = payload,
                password = password,
                salt = EncryptionService.generateSalt(),
                version = EncryptionService.VERSION_PASSWORD_RANDOM_SALT_GZIP
            )
            val uploadedFileId = entityStore.uploadEntity(
                entryName = entryName,
                payload = encrypted.toJson().toByteArray(Charsets.UTF_8),
                existingFileId = existingRef?.fileId?.takeIf { it.isNotBlank() },
                skipLookup = existingRef == null
            )
            uploadedEntities += 1
            return uploadedFileId
        }

        val settingsPayload = stableSettingsPayloadBytes()
        val settingsFingerprint = sha256(settingsPayload)
        val settingsFileId = if (remoteManifest?.settingsFingerprint != settingsFingerprint) {
            entityStore.uploadEntity(
                entryName = SETTINGS_ENTRY,
                payload = EncryptionService.encryptWithPassword(
                    plainText = settingsPayload,
                    password = password,
                    salt = EncryptionService.generateSalt(),
                    version = EncryptionService.VERSION_PASSWORD_RANDOM_SALT_GZIP
                ).toJson().toByteArray(Charsets.UTF_8),
                existingFileId = remoteManifest?.settingsFileId?.takeIf { it.isNotBlank() },
                skipLookup = remoteManifest == null
            ).also {
                uploadedEntities += 1
            }
        } else {
            remoteManifest.settingsFileId
        }

        val profiles = settingsRepository.vehicleProfilesSnapshot()
        profiles.forEach { profile ->
            val payload = profile.toJson().toString().toByteArray(Charsets.UTF_8)
            val fingerprint = sha256(payload)
            val key = entityKey(ENTITY_VEHICLE_PROFILE, profile.id)
            val remoteRef = remoteRefs[key]
            val entry = DriveV3EntityRef(
                type = ENTITY_VEHICLE_PROFILE,
                id = profile.id,
                vehicleId = profile.id,
                entryName = vehicleProfileEntry(profile.id),
                updatedAt = profile.lastModified.takeIf { it > 0L } ?: now,
                fingerprint = fingerprint
            )
            refs += if (!remoteRefMatches(remoteRefs, entry)) {
                entry.copy(fileId = uploadEncrypted(entry.entryName, payload, remoteRef))
            } else {
                entry.copy(fileId = remoteRef?.fileId.orEmpty())
            }
        }

        val bindings = settingsRepository.controllerBindingsSnapshot()
        bindings.forEach { binding ->
            val payload = binding.toJson().toString().toByteArray(Charsets.UTF_8)
            val fingerprint = sha256(payload)
            val key = entityKey(ENTITY_CONTROLLER_BINDING, binding.vehicleId)
            val remoteRef = remoteRefs[key]
            val entry = DriveV3EntityRef(
                type = ENTITY_CONTROLLER_BINDING,
                id = binding.vehicleId,
                vehicleId = binding.vehicleId,
                entryName = controllerBindingEntry(binding.vehicleId),
                updatedAt = maxOf(binding.verifiedAt, binding.lastConnectedAt),
                fingerprint = fingerprint
            )
            refs += if (!remoteRefMatches(remoteRefs, entry)) {
                entry.copy(fileId = uploadEncrypted(entry.entryName, payload, remoteRef))
            } else {
                entry.copy(fileId = remoteRef?.fileId.orEmpty())
            }
        }

        val activeVehicleId = settingsRepository.currentVehicleId.first()
        val speedTests = settingsRepository.speedTestHistory.first()
        speedTests.forEach { record ->
            val payloadJson = record.toJson()
                .put("vehicleId", activeVehicleId)
                .put("updatedAt", record.timestampMs)
            val payload = payloadJson.toString().toByteArray(Charsets.UTF_8)
            val key = entityKey(ENTITY_SPEED_TEST, record.id)
            val remoteRef = remoteRefs[key]
            val entry = DriveV3EntityRef(
                type = ENTITY_SPEED_TEST,
                id = record.id,
                vehicleId = activeVehicleId,
                entryName = speedTestEntry(record.id),
                updatedAt = record.timestampMs,
                fingerprint = sha256(payload)
            )
            refs += if (!remoteRefMatches(remoteRefs, entry)) {
                entry.copy(fileId = uploadEncrypted(entry.entryName, payload, remoteRef))
            } else {
                entry.copy(fileId = remoteRef?.fileId.orEmpty())
            }
        }

        val rideSummaries = rideHistoryRepository.listRideHistorySummaries()
        rideSummaries.forEach { summary ->
            val key = entityKey(ENTITY_RIDE, summary.id)
            val remoteRef = remoteRefs[key]
            val entry = DriveV3EntityRef(
                type = ENTITY_RIDE,
                id = summary.id,
                vehicleId = summary.vehicleId,
                entryName = rideEntry(summary.vehicleId, summary.id),
                updatedAt = summary.updatedAt,
                fingerprint = summary.detailFingerprint
            )
            refs += if (!remoteRefMatches(remoteRefs, entry)) {
                val record = rideHistoryRepository.loadRideRecord(summary.id) ?: return@forEach
                val payloadJson = record.toJson()
                    .put("vehicleId", summary.vehicleId)
                    .put("updatedAt", summary.updatedAt)
                val payload = payloadJson.toString().toByteArray(Charsets.UTF_8)
                val resolvedFingerprint = entry.fingerprint.ifBlank { sha256(payload) }
                entry.copy(
                    fingerprint = resolvedFingerprint,
                    fileId = uploadEncrypted(entry.entryName, payload, remoteRef)
                )
            } else {
                entry.copy(fileId = remoteRef?.fileId.orEmpty())
            }
        }

        val manifest = DriveV3Manifest(
            schemaVersion = 3,
            updatedAt = now,
            updatedByDeviceId = metadata.deviceId,
            updatedByDeviceName = metadata.deviceName,
            settingsEntry = SETTINGS_ENTRY,
            settingsFileId = settingsFileId,
            settingsFingerprint = settingsFingerprint,
            entities = refs,
            counters = DriveV3Counters(
                vehicleProfileCount = profiles.size,
                controllerBindingCount = bindings.size,
                rideCount = rideSummaries.size,
                speedTestCount = speedTests.size
            )
        )
        entityStore.uploadManifest(manifest)

        val pending = mutationRepository.getCoalescedPending()
        if (pending.isNotEmpty()) {
            mutationRepository.markSynced(pending.map { it.id })
        }
        metadataRepository.recordPushSuccess(context, stateVersion)
        metadataRepository.updateMigrationVersion(context, 3)

        return DriveV3PublishResult(
            manifest = manifest,
            uploadedEntityCount = uploadedEntities,
            rideCount = rideSummaries.size,
            vehicleProfileCount = profiles.size,
            controllerBindingCount = bindings.size,
            speedTestCount = speedTests.size
        )
    }

    private suspend fun stableSettingsPayloadBytes(): ByteArray {
        val raw = settingsRepository.exportSettingsBackupJson(excludeLegacyRideHistory = true)
        val root = JSONObject(raw)
        root.put("exportedAt", 0L)
        return root.toString().toByteArray(Charsets.UTF_8)
    }

    private fun remoteRefMatches(
        remoteRefs: Map<String, DriveV3EntityRef>,
        local: DriveV3EntityRef
    ): Boolean {
        val remote = remoteRefs[entityKey(local)] ?: return false
        return remote.deletedAt == 0L &&
            remote.entryName == local.entryName &&
            remote.fingerprint.isNotBlank() &&
            remote.fingerprint == local.fingerprint
    }

    private fun entityKey(type: String, id: String): String = "$type:$id"

    private fun entityKey(ref: DriveV3EntityRef): String = entityKey(ref.type, ref.id)

    private fun safeToken(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))

    private fun rideEntry(vehicleId: String, rideId: String): String =
        "v3/rides/${safeToken(vehicleId)}/${safeToken(rideId)}.json.enc"

    private fun vehicleProfileEntry(vehicleId: String): String =
        "v3/vehicle-profiles/${safeToken(vehicleId)}.json.enc"

    private fun controllerBindingEntry(vehicleId: String): String =
        "v3/controller-bindings/${safeToken(vehicleId)}.json.enc"

    private fun speedTestEntry(testId: String): String =
        "v3/speed-tests/${safeToken(testId)}.json.enc"

    private suspend fun downloadEncrypted(entryName: String, password: String): ByteArray {
        val encryptedJson = entityStore.downloadEntity(entryName).toString(Charsets.UTF_8)
        val encrypted = EncryptedBackup.fromJson(encryptedJson)
        return if (encrypted.version >= EncryptionService.VERSION_PASSWORD_FIXED_SALT_LEGACY) {
            EncryptionService.decryptWithPassword(encrypted, password)
        } else {
            EncryptionService.decrypt(encrypted)
        }
    }

    private fun deriveEncryptionPassword(): String {
        val email = driveSyncManager.getCurrentAccount()?.email
            ?: throw IllegalStateException("尚未登录 Google Drive")
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(email.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hash)
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

}
