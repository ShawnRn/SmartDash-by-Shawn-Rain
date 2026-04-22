package com.shawnrain.sdash.data.sync

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncVehicleProfileSnapshotTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun syncVehicleProfileSnapshotRoundTripsExtendedVehicleFields() {
        val snapshot = SyncVehicleProfileSnapshot(
            id = "vehicle-1",
            name = "City Bike",
            macAddress = "AA:BB:CC:DD:EE:FF",
            batteryCapacityAh = 42.5f,
            batterySeries = 16,
            wheelCircumferenceMm = 1960f,
            wheelRimSize = "12寸",
            tireSpecLabel = "90/90-12",
            polePairs = 30,
            totalMileageKm = 3210.5f,
            learnedEfficiencyWhKm = 38.4f,
            learnedUsableEnergyRatio = 0.82f,
            learnedInternalResistanceOhm = 0.041f,
            updatedAt = 123456789L,
            updatedByDeviceId = "device-a"
        )

        val encoded = json.encodeToString(SyncVehicleProfileSnapshot.serializer(), snapshot)
        val decoded = json.decodeFromString(SyncVehicleProfileSnapshot.serializer(), encoded)

        assertEquals(snapshot.macAddress, decoded.macAddress)
        assertEquals(snapshot.wheelRimSize, decoded.wheelRimSize)
        assertEquals(snapshot.tireSpecLabel, decoded.tireSpecLabel)
        assertEquals(snapshot.totalMileageKm, decoded.totalMileageKm, 0.001f)
        assertEquals(snapshot.learnedInternalResistanceOhm, decoded.learnedInternalResistanceOhm, 0.000001f)
        assertEquals(snapshot.wheelCircumferenceMm, decoded.wheelCircumferenceMm, 0.001f)
    }

    @Test
    fun syncVehicleProfileSnapshotDefaultsExtendedFieldsForOlderPayloads() {
        val decoded = json.decodeFromString(
            SyncVehicleProfileSnapshot.serializer(),
            """
            {
              "id": "vehicle-1",
              "name": "Legacy Bike",
              "batteryCapacityAh": 50.0,
              "batterySeries": 13,
              "wheelCircumferenceMm": 1800.0,
              "polePairs": 50,
              "learnedEfficiencyWhKm": 0.0,
              "learnedUsableEnergyRatio": 0.9,
              "learnedInternalResistanceOhm": 0.0,
              "updatedAt": 1,
              "updatedByDeviceId": "device"
            }
            """.trimIndent()
        )

        assertEquals("", decoded.macAddress)
        assertEquals("10寸", decoded.wheelRimSize)
        assertEquals("", decoded.tireSpecLabel)
        assertEquals(0f, decoded.totalMileageKm, 0.001f)
    }

    @Test
    fun syncVehicleSettingsSnapshotRoundTripsControllerBindingFields() {
        val snapshot = SyncVehicleSettingsSnapshot(
            vehicleProfileId = "vehicle-1",
            speedSource = "CONTROLLER",
            battDataSource = "BMS",
            wheelCircumferenceMm = 1980f,
            polePairs = 28,
            controllerBrand = "zhike",
            lastControllerDeviceAddress = "11:22:33:44:55:66",
            lastControllerDeviceName = "GEKOO-BLE",
            lastControllerProtocolId = "zhike",
            dashboardItems = listOf("SOC", "RANGE"),
            rideOverviewItems = listOf("TRIP_DISTANCE"),
            updatedAt = 123L,
            updatedByDeviceId = "device-a"
        )

        val encoded = json.encodeToString(SyncVehicleSettingsSnapshot.serializer(), snapshot)
        val decoded = json.decodeFromString(SyncVehicleSettingsSnapshot.serializer(), encoded)

        assertEquals(snapshot.lastControllerDeviceAddress, decoded.lastControllerDeviceAddress)
        assertEquals(snapshot.lastControllerDeviceName, decoded.lastControllerDeviceName)
        assertEquals(snapshot.lastControllerProtocolId, decoded.lastControllerProtocolId)
    }
}
