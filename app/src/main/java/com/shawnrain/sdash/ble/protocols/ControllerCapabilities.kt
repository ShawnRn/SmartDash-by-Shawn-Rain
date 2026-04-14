package com.shawnrain.sdash.ble.protocols

data class ControllerCapabilities(
    val protocol: String,
    val firmwareVersion: Int? = null,
    val firmwareVersionLabel: String? = null,
    val hardwareVersion: String? = null,
    val featureFlags: Set<String> = emptySet()
)

data class ParameterLockState(
    val locked: Boolean,
    val reason: String? = null
)

fun ZhikeParamDefinition.resolveLockState(capabilities: ControllerCapabilities?): ParameterLockState {
    val requiredVersion = minFirmwareVersion ?: return ParameterLockState(locked = false)

    val currentCapabilities = capabilities
        ?: return ParameterLockState(
            locked = true,
            reason = "未识别到控制器能力，按保守策略锁定（需要固件版本 >= $requiredVersion）"
        )

    if (currentCapabilities.protocol != "zhike") {
        return ParameterLockState(
            locked = true,
            reason = "当前连接的不是智科控制器（需要固件版本 >= $requiredVersion）"
        )
    }

    val currentFirmwareVersion = currentCapabilities.firmwareVersion
        ?: return ParameterLockState(
            locked = true,
            reason = "当前控制器未返回固件版本，按保守策略锁定（需要固件版本 >= $requiredVersion）"
        )

    return if (currentFirmwareVersion < requiredVersion) {
        ParameterLockState(
            locked = true,
            reason = "当前固件版本 $currentFirmwareVersion，需升级到 >= $requiredVersion"
        )
    } else {
        ParameterLockState(locked = false)
    }
}
