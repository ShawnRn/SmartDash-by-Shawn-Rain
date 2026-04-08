package com.shawnrain.habe.ble.protocols

import com.shawnrain.habe.debug.AppLogger

/**
 * 智科参数写入前的本地校验器
 * 输出 errors（阻止写入）和 warnings（用户确认后可继续）
 */
object ZhikeSettingsValidator {
    private const val TAG = "ZhikeValidator"

    enum class Severity {
        ERROR,    // 阻止写入
        WARNING   // 风险提示，用户确认后可继续
    }

    enum class IssueCategory(val label: String) {
        THROTTLE("转把"),
        VOLTAGE("电压保护"),
        CURRENT("电流关系"),
        PERFORMANCE("性能"),
        TCS("牵引力控制"),
        FIRMWARE("固件兼容")
    }

    data class ValidationIssue(
        val code: String,
        val title: String,
        val message: String,
        val severity: Severity,
        val category: IssueCategory,
        val relatedKeys: List<String>,
        /** 自动修复建议（可选） */
        val suggestion: String? = null
    ) {
        val displayText: String
            get() = buildString {
                append("• $title: $message")
                suggestion?.let { append("\n  → $it") }
            }
    }

    data class ValidationResult(
        val errors: List<ValidationIssue>,
        val warnings: List<ValidationIssue>
    ) {
        val hasBlockingIssues: Boolean get() = errors.isNotEmpty()
        val hasWarnings: Boolean get() = warnings.isNotEmpty()
        val allIssues: List<ValidationIssue> get() = errors + warnings

        /** 按类别分组 */
        val issuesByCategory: Map<IssueCategory, List<ValidationIssue>>
            get() = allIssues.groupBy { it.category }
    }

    /**
     * 执行完整校验
     */
    fun validate(settings: ZhikeSettings, firmwareVersion: Int? = null): ValidationResult {
        val errors = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<ValidationIssue>()

        // D1. 转把电压校验
        validateThrottleVoltage(settings, errors, warnings)

        // D2. 电压保护相关
        validateVoltageProtection(settings, errors, warnings)

        // D3. 电流相关
        validateCurrents(settings, errors, warnings)

        // D4. TCS / 高级参数
        validateAdvancedParams(settings, errors, warnings)

        // D5. 固件版本门控
        if (firmwareVersion != null) {
            validateFirmwareCompatibility(settings, firmwareVersion, errors)
        }

        return ValidationResult(errors.toList(), warnings.toList())
    }

    private fun validateThrottleVoltage(
        settings: ZhikeSettings,
        errors: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationIssue>
    ) {
        val fullThrottle = ZhikeParameterCatalog.readNumeric(settings, requireDef("full_throttle_voltage"))
        val idleThrottle = ZhikeParameterCatalog.readNumeric(settings, requireDef("idle_throttle_voltage"))

        // 规则 1: 满把电压必须大于空把电压
        if (fullThrottle <= idleThrottle) {
            errors.add(
                ValidationIssue(
                    code = "THROTTLE_VOLTAGE_INVERTED",
                    title = "转把电压异常",
                    message = "满把电压 (${formatV(fullThrottle)}) 必须大于空把电压 (${formatV(idleThrottle)})",
                    severity = Severity.ERROR,
                    category = IssueCategory.THROTTLE,
                    relatedKeys = listOf("full_throttle_voltage", "idle_throttle_voltage"),
                    suggestion = "修正为：满把电压 > 空把电压（典型值 4.2V > 0.8V）"
                )
            )
        }

        // 规则 2: 差值裕量
        val delta = fullThrottle - idleThrottle
        if (delta > 0 && delta < 0.3) {
            warnings.add(
                ValidationIssue(
                    code = "THROTTLE_MARGIN_LOW",
                    title = "转把裕量不足",
                    message = "满把与空把电压差仅 ${"%.2f".format(delta)}V",
                    severity = Severity.WARNING,
                    category = IssueCategory.THROTTLE,
                    relatedKeys = listOf("full_throttle_voltage", "idle_throttle_voltage"),
                    suggestion = "建议保留 ≥ 0.3V 裕量，防止误触"
                )
            )
        }
    }

    private fun validateVoltageProtection(
        settings: ZhikeSettings,
        errors: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationIssue>
    ) {
        val underVoltage = ZhikeParameterCatalog.readNumeric(settings, requireDef("under_voltage"))
        val overVoltage = ZhikeParameterCatalog.readNumeric(settings, requireDef("over_voltage"))
        val powerReductionStart = ZhikeParameterCatalog.readNumeric(settings, requireDef("power_reduction_start_voltage"))
        val powerReductionEnd = ZhikeParameterCatalog.readNumeric(settings, requireDef("power_reduction_end_voltage"))
        val regenVoltageLimit = ZhikeParameterCatalog.readNumeric(settings, requireDef("regenerative_voltage_limit"))

        // 规则 3: 降载起始 ≥ 降载截止
        if (powerReductionStart < powerReductionEnd) {
            errors.add(
                ValidationIssue(
                    code = "POWER_REDUCTION_INVERTED",
                    title = "降载区间异常",
                    message = "降载起始电压 (${formatV(powerReductionStart)}) 必须 ≥ 降载截止电压 (${formatV(powerReductionEnd)})",
                    severity = Severity.ERROR,
                    category = IssueCategory.VOLTAGE,
                    relatedKeys = listOf("power_reduction_start_voltage", "power_reduction_end_voltage"),
                    suggestion = "修正为：起始电压 ≥ 截止电压"
                )
            )
        }

        // 规则 4: 欠压 ≤ 降载截止
        if (underVoltage > powerReductionEnd) {
            errors.add(
                ValidationIssue(
                    code = "UNDER_VOLTAGE_TOO_HIGH",
                    title = "欠压点过高",
                    message = "欠压点 (${formatV(underVoltage)}) 不应高于降载截止电压 (${formatV(powerReductionEnd)})",
                    severity = Severity.ERROR,
                    category = IssueCategory.VOLTAGE,
                    relatedKeys = listOf("under_voltage", "power_reduction_end_voltage"),
                    suggestion = "欠压点应低于降载截止电压"
                )
            )
        }

        // 规则 5: 过压 ≥ 降载起始
        if (overVoltage < powerReductionStart) {
            errors.add(
                ValidationIssue(
                    code = "OVER_VOLTAGE_TOO_LOW",
                    title = "过压点过低",
                    message = "过压点 (${formatV(overVoltage)}) 必须 ≥ 降载起始电压 (${formatV(powerReductionStart)})",
                    severity = Severity.ERROR,
                    category = IssueCategory.VOLTAGE,
                    relatedKeys = listOf("over_voltage", "power_reduction_start_voltage"),
                    suggestion = "过压点应高于或等于降载起始电压"
                )
            )
        }

        // 规则 6: 回充电压上限 ≤ 过压
        if (regenVoltageLimit > overVoltage) {
            errors.add(
                ValidationIssue(
                    code = "REGEN_VOLTAGE_EXCEEDS_OVER",
                    title = "回充电压异常",
                    message = "回充电压上限 (${formatV(regenVoltageLimit)}) 不得高于过压点 (${formatV(overVoltage)})",
                    severity = Severity.ERROR,
                    category = IssueCategory.VOLTAGE,
                    relatedKeys = listOf("regenerative_voltage_limit", "over_voltage"),
                    suggestion = "回充电压上限 ≤ 过压点"
                )
            )
        }

        // 规则 4 (warning): 欠压接近降载截止
        if (underVoltage <= powerReductionEnd && (powerReductionEnd - underVoltage) < 2.0) {
            warnings.add(
                ValidationIssue(
                    code = "UNDER_VOLTAGE_CLOSE_TO_CUTOFF",
                    title = "欠压接近降载截止",
                    message = "欠压点与降载截止电压差距过小",
                    severity = Severity.WARNING,
                    category = IssueCategory.VOLTAGE,
                    relatedKeys = listOf("under_voltage", "power_reduction_end_voltage"),
                    suggestion = "建议保留 ≥ 2V 差值，防止掉入欠压前没有正常降载区"
                )
            )
        }
    }

    private fun validateCurrents(
        settings: ZhikeSettings,
        errors: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationIssue>
    ) {
        val busCurrent = settings.busCurrent.toDouble()
        val phaseCurrent = settings.phaseCurrent.toDouble()
        val weakMagCurrent = settings.weakMagCurrent.toDouble()

        // 规则 8: 母线电流 > 0
        if (busCurrent <= 0.0) {
            errors.add(
                ValidationIssue(
                    code = "BUS_CURRENT_INVALID",
                    title = "母线电流无效",
                    message = "母线电流必须大于 0（当前: ${"%.0f".format(busCurrent)}A）",
                    severity = Severity.ERROR,
                    category = IssueCategory.CURRENT,
                    relatedKeys = listOf("bus_current"),
                    suggestion = "典型值：25~45A"
                )
            )
        }

        // 规则 9: 相线电流 > 0
        if (phaseCurrent <= 0.0) {
            errors.add(
                ValidationIssue(
                    code = "PHASE_CURRENT_INVALID",
                    title = "相线电流无效",
                    message = "相线电流必须大于 0（当前: ${"%.0f".format(phaseCurrent)}A）",
                    severity = Severity.ERROR,
                    category = IssueCategory.CURRENT,
                    relatedKeys = listOf("phase_current"),
                    suggestion = "典型值：80~200A"
                )
            )
        }

        // 规则 10: 相线电流 ≥ 母线电流
        if (phaseCurrent > 0 && busCurrent > 0 && phaseCurrent < busCurrent) {
            warnings.add(
                ValidationIssue(
                    code = "PHASE_LESS_THAN_BUS",
                    title = "相线电流低于母线电流",
                    message = "相线电流 (${phaseCurrent.toInt()}A) 通常应 ≥ 母线电流 (${busCurrent.toInt()}A)",
                    severity = Severity.WARNING,
                    category = IssueCategory.CURRENT,
                    relatedKeys = listOf("phase_current", "bus_current"),
                    suggestion = "相线电流一般 ≥ 母线电流 × 1.5"
                )
            )
        }

        // 规则 11: 相线电流显著高于母线经验倍数
        if (busCurrent > 0 && phaseCurrent > busCurrent * 4.0) {
            warnings.add(
                ValidationIssue(
                    code = "PHASE_EXCESSIVELY_HIGH",
                    title = "相线电流异常高",
                    message = "相线电流是母线电流的 ${"%.1f".format(phaseCurrent / busCurrent)} 倍（通常 ≤ 4 倍）",
                    severity = Severity.WARNING,
                    category = IssueCategory.CURRENT,
                    relatedKeys = listOf("phase_current", "bus_current"),
                    suggestion = "建议降低相线电流至母线电流的 2~3.5 倍"
                )
            )
        }

        // 规则 12: 弱磁电流偏高
        if (phaseCurrent > 0 && weakMagCurrent > phaseCurrent / 3.0) {
            warnings.add(
                ValidationIssue(
                    code = "WEAK_MAGNET_HIGH",
                    title = "弱磁电流偏高",
                    message = "弱磁电流超过相线电流的 1/3",
                    severity = Severity.WARNING,
                    category = IssueCategory.PERFORMANCE,
                    relatedKeys = listOf("weak_magnet_current", "phase_current"),
                    suggestion = "建议降低至相线电流的 20~30%，否则可能导致高速异响、发热或不稳"
                )
            )
        }
    }

    private fun validateAdvancedParams(
        settings: ZhikeSettings,
        errors: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationIssue>
    ) {
        // TCS 灵敏度
        ZhikeParameterCatalog.findDefinition("tcs_sensitivity")?.let { def ->
            val tcsSensitivity = ZhikeParameterCatalog.readNumeric(settings, def).toInt()
            // 规则 13: TCS 灵敏度过高提醒
            if (tcsSensitivity in 1..9) {
                warnings.add(
                    ValidationIssue(
                        code = "TCS_SENSITIVITY_HIGH",
                        title = "TCS 灵敏度过高",
                        message = "TCS 灵敏度设为 $tcsSensitivity",
                        severity = Severity.WARNING,
                        category = IssueCategory.TCS,
                        relatedKeys = listOf("tcs_sensitivity"),
                        suggestion = "坑洼路面、翘头或轻微打滑时可能误触发，建议 ≥ 10"
                    )
                )
            }
        }
    }

    private fun validateFirmwareCompatibility(
        settings: ZhikeSettings,
        firmwareVersion: Int,
        errors: MutableList<ValidationIssue>
    ) {
        for (paramDef in ZhikeParameterCatalog.groups.flatMap { it.parameters }) {
            val minVersion = paramDef.minFirmwareVersion ?: continue
            if (firmwareVersion < minVersion) {
                AppLogger.w(
                    TAG,
                    "参数 ${paramDef.key} 需要固件版本 ≥ $minVersion，当前版本 $firmwareVersion"
                )
            }
        }
    }

    private fun requireDef(key: String) =
        requireNotNull(ZhikeParameterCatalog.findDefinition(key)) { "Missing parameter definition for $key" }

    private fun formatV(voltage: Double) = "%.1fV".format(voltage)
}
