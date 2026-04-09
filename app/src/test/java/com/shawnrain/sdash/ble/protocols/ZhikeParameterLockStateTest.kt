package com.shawnrain.sdash.ble.protocols

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZhikeParameterLockStateTest {

    private val requiresV27 = ZhikeParamDefinition(
        key = "temperature_sensor_type",
        label = "温度传感器类型",
        kind = ZhikeParamKind.UINT,
        wordIndex = 30,
        minFirmwareVersion = 27
    )

    @Test
    fun olderFirmware_staysLockedWithUpgradeReason() {
        val lockState = requiresV27.resolveLockState(
            ControllerCapabilities(protocol = "zhike", firmwareVersion = 26)
        )

        assertTrue(lockState.locked)
        assertEquals("当前固件版本 26，需升级到 >= 27", lockState.reason)
    }

    @Test
    fun newerFirmware_isUnlocked() {
        val lockState = requiresV27.resolveLockState(
            ControllerCapabilities(protocol = "zhike", firmwareVersion = 28)
        )

        assertFalse(lockState.locked)
        assertEquals(null, lockState.reason)
    }

    @Test
    fun missingFirmware_staysLockedConservatively() {
        val lockState = requiresV27.resolveLockState(
            ControllerCapabilities(protocol = "zhike", firmwareVersion = null)
        )

        assertTrue(lockState.locked)
        assertEquals("当前控制器未返回固件版本，按保守策略锁定（需要固件版本 >= 27）", lockState.reason)
    }

    @Test
    fun missingCapabilities_staysLockedConservatively() {
        val lockState = requiresV27.resolveLockState(null)

        assertTrue(lockState.locked)
        assertEquals("未识别到控制器能力，按保守策略锁定（需要固件版本 >= 27）", lockState.reason)
    }
}
