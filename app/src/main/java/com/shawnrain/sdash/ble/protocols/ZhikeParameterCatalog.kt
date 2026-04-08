package com.shawnrain.sdash.ble.protocols

import kotlin.math.roundToInt

enum class ZhikeParamKind {
    UINT,
    INT,
    FLOAT,
    BOOL,
    LIST,
    TEXT
}

data class ZhikePresetDefinition(
    val id: String,
    val label: String
)

data class ZhikeParamDefinition(
    val key: String,
    val label: String,
    val kind: ZhikeParamKind,
    val wordIndex: Int,
    val unit: String = "",
    val min: Double = 0.0,
    val max: Double = 0.0,
    val bitStart: Int = 0,
    val bitEnd: Int = 15,
    val scale: Double = 1.0,
    val optionLabels: List<String> = emptyList(),
    val mapping: Map<Int, Int> = emptyMap(),
    val bitIndices: List<Int> = emptyList(),
    val useContinuousBits: Boolean = false,
    val minFirmwareVersion: Int? = null
)

data class ZhikeParamGroup(
    val id: Int,
    val title: String,
    val parameters: List<ZhikeParamDefinition>,
    val presets: List<ZhikePresetDefinition> = emptyList()
)

object ZhikeParameterCatalog {
    val groups: List<ZhikeParamGroup> = listOf(
        ZhikeParamGroup(
            id = 0,
            title = "电流参数",
            parameters = listOf(
                uintParam("bus_current", "母线电流", 0, unit = "A", min = 0.0, max = 800.0),
                uintParam("phase_current", "相线电流", 1, unit = "A", min = 0.0, max = 2000.0)
            ),
            presets = listOf(
                ZhikePresetDefinition("current_swap_common", "通用换电")
            )
        ),
        ZhikeParamGroup(
            id = 1,
            title = "电机参数",
            parameters = listOf(
                boolParam("motor_direction", "电机方向", 2, 0, onText = "反转", offText = "正转"),
                listParam(
                    "sensor_type",
                    "位置传感器类型",
                    30,
                    optionLabels = listOf("120° Hall", "60° Hall", "编码器 / Encoder"),
                    bitIndices = listOf(0, 1)
                ),
                boolParam("hall_sequence", "霍尔顺序", 31, 0, onText = "反序", offText = "正序"),
                boolParam("encoder_sequence", "编码器顺序", 31, 1, onText = "反序", offText = "正序"),
                intParam("phase_shift_angle", "相移角度", 32, unit = "°", min = -180.0, max = 180.0, scale = 182.0444444444444),
                uintParam("pole_pairs", "极对数", 34, min = 0.0, max = 50.0),
                uintParam("carrier_frequency", "载波频率", 48, min = 0.0, max = 20.0, minFirmwareVersion = 20),
                uintParam("reference_current", "学习电流", 49, unit = "A", min = 0.0, max = 4095.0),
                uintParam("weak_magnet_current", "高速挡弱磁电流", 21, unit = "A", min = 0.0, max = 500.0),
                uintParam("encoder_accuracy", "编码器精度", 51, unit = "线", min = 0.0, max = 8192.0),
                uintParam("temperature_sensor_type", "温度传感器类型", 30, min = 0.0, max = 255.0, bitStart = 8, bitEnd = 15, minFirmwareVersion = 27),
                uintParam("derating_temperature", "降载温度", 33, unit = "°C", min = 0.0, max = 255.0, bitStart = 0, bitEnd = 7, minFirmwareVersion = 27),
                uintParam("over_temperature", "过温温度", 33, unit = "°C", min = 0.0, max = 255.0, bitStart = 8, bitEnd = 15, minFirmwareVersion = 27)
            )
        ),
        ZhikeParamGroup(
            id = 2,
            title = "转把参数",
            parameters = listOf(
                floatParam("full_throttle_voltage", "满把电压", 3, unit = "V", min = 0.0, max = 5.0, scale = 2450.0),
                floatParam("idle_throttle_voltage", "空把电压", 4, unit = "V", min = 0.0, max = 5.0, scale = 2450.0),
                floatParam("forward_soft_start", "前进挡软启时间", 5, unit = "Sec", min = 0.0, max = 20.0, scale = 273.0666667),
                listParam(
                    "riding_mode",
                    "骑行模式",
                    29,
                    optionLabels = listOf("线性", "柔和", "运动", "暴力"),
                    bitIndices = listOf(0, 14)
                ),
                boolParam("brake_throttle_reset", "刹车油门复位", 8, 4, onText = "开启", offText = "关闭", minFirmwareVersion = 27),
                boolParam("brake_fault_detection", "刹把故障检测", 8, 5, onText = "开启", offText = "关闭", minFirmwareVersion = 27)
            ),
            presets = listOf(
                ZhikePresetDefinition("throttle_punchy", "强劲起步"),
                ZhikePresetDefinition("throttle_daily", "日常起步")
            )
        ),
        ZhikeParamGroup(
            id = 3,
            title = "起步参数",
            parameters = listOf(
                boolParam("anti_theft_lock_motor", "防盗锁电机", 29, 1, onText = "关闭", offText = "开启"),
                boolParam("lock_motor_mode", "锁电机模式", 29, 11, onText = "强制", offText = "节能"),
                boolParam("auto_parking", "自动驻车", 29, 10, onText = "开启", offText = "关闭"),
                boolParam("parking_signal", "P 挡信号类型", 29, 12, onText = "低刹", offText = "高刹"),
                boolParam("brake_driver", "刹车不断电", 29, 15, onText = "开启", offText = "关闭", minFirmwareVersion = 26),
                boolParam("side_stand_effective", "边撑有效", 29, 7, onText = "开启", offText = "关闭"),
                boolParam("cruise_to_reverse", "长按巡航切倒挡", 29, 6, onText = "开启", offText = "关闭"),
                listParam(
                    "warning_function",
                    "预警功能",
                    29,
                    optionLabels = listOf("无效", "关闭", "开启"),
                    mapping = mapOf(0 to 0, 1 to 1, 3 to 2),
                    bitIndices = listOf(8, 9)
                ),
                uintParam("tcs_sensitivity", "TCS 灵敏度", 58, min = 0.0, max = 255.0, bitStart = 0, bitEnd = 7),
                uintParam("tcs_recovery_time", "TCS 恢复时间", 58, min = 0.0, max = 15.0, bitStart = 8, bitEnd = 12, minFirmwareVersion = 26)
            )
        ),
        ZhikeParamGroup(
            id = 4,
            title = "P 挡参数",
            parameters = listOf(
                listParam(
                    "signal_port_selection",
                    "信号口功能选择",
                    29,
                    optionLabels = listOf("默认", "低刹", "高刹", "巡航"),
                    mapping = mapOf(0 to 0, 1 to 1, 2 to 2, 4 to 3),
                    bitIndices = listOf(2, 3, 4)
                ),
                boolParam("trigger_mode", "触发模式", 29, 5, onText = "拨动", offText = "点动"),
                boolParam("normal_brake_release_p", "常规刹车解 P 挡", 29, 13, onText = "开启", offText = "关闭"),
                uintParam("auto_return_p_time", "自动回 P 时间", 28, unit = "秒", min = 0.0, max = 255.0, bitStart = 8, bitEnd = 15, minFirmwareVersion = 27)
            )
        ),
        ZhikeParamGroup(
            id = 5,
            title = "仪表参数",
            parameters = listOf(
                listParam(
                    "vehicle_model",
                    "车型选择",
                    20,
                    optionLabels = listOf("普通", "110 P", "200 P", "MZ", "485 车型", "一线通", "AE", "NXT", "FX", "PD 485", "GD 485"),
                    mapping = mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3, 4 to 0, 5 to 1, 6 to 3, 7 to 4, 8 to 5, 9 to 0, 10 to 1),
                    useContinuousBits = true,
                    bitStart = 8,
                    bitEnd = 15
                ),
                uintParam("one_line_coefficient", "一线通系数", 7, min = 0.0, max = 255.0, bitStart = 0, bitEnd = 7),
                uintParam("one_line_protocol", "一线通协议", 7, min = 0.0, max = 50.0, bitStart = 8, bitEnd = 15, minFirmwareVersion = 27),
                uintParam("hall_meter_pulse_count", "霍尔仪表脉冲数", 8, min = 0.0, max = 500.0, minFirmwareVersion = 16),
                boolParam("bms_protocol", "BMS 协议", 8, 0, onText = "开启", offText = "关闭", minFirmwareVersion = 27),
                listParam(
                    "port_function",
                    "端口功能",
                    8,
                    optionLabels = listOf("一线通输出", "霍尔脉冲", "保留 1", "保留 2", "灯控"),
                    mapping = mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3, 4 to 4),
                    bitIndices = listOf(1, 2, 3),
                    minFirmwareVersion = 27
                )
            )
        ),
        ZhikeParamGroup(
            id = 6,
            title = "三挡参数",
            parameters = listOf(
                uintParam("high_speed_max_speed", "高速挡最高转速", 11, unit = "rpm", min = 0.0, max = 6000.0),
                uintParam("high_speed_phase_percent", "高速挡相线电流", 14, unit = "%", min = 0.0, max = 100.0),
                uintParam("high_speed_bus_percent", "高速挡母线电流", 17, unit = "%", min = 0.0, max = 100.0),
                uintParam("mid_speed_max_speed", "中速挡最高转速", 12, unit = "rpm", min = 0.0, max = 6000.0),
                uintParam("mid_speed_phase_percent", "中速挡相线电流", 15, unit = "%", min = 0.0, max = 100.0),
                uintParam("mid_speed_bus_percent", "中速挡母线电流", 18, unit = "%", min = 0.0, max = 100.0),
                uintParam("low_speed_max_speed", "低速挡最高转速", 13, unit = "rpm", min = 0.0, max = 6000.0),
                uintParam("low_speed_phase_percent", "低速挡相线电流", 16, unit = "%", min = 0.0, max = 100.0),
                uintParam("low_speed_bus_percent", "低速挡母线电流", 19, unit = "%", min = 0.0, max = 100.0),
                listParam(
                    "shift_method",
                    "换挡方式",
                    20,
                    optionLabels = listOf("拨动", "点动", "加减挡"),
                    bitIndices = listOf(0, 1)
                ),
                listParam(
                    "momentary_default_gear",
                    "点动默认挡位",
                    20,
                    optionLabels = listOf("低速挡", "中速挡", "高速挡"),
                    bitIndices = listOf(4, 5)
                )
            )
        ),
        ZhikeParamGroup(
            id = 7,
            title = "倒挡参数",
            parameters = listOf(
                floatParam("reverse_soft_start", "倒挡软启时间", 6, unit = "Sec", min = 0.0, max = 20.0, scale = 273.0666667),
                uintParam("reverse_speed", "倒挡速度", 25, unit = "rpm", min = 0.0, max = 1000.0),
                uintParam("reverse_torque", "倒挡扭矩", 28, unit = "N·m", min = 0.0, max = 100.0, bitStart = 0, bitEnd = 7)
            )
        ),
        ZhikeParamGroup(
            id = 8,
            title = "电压参数",
            parameters = listOf(
                uintParam("under_voltage", "欠压点", 9, unit = "V", min = 0.0, max = 130.0),
                uintParam("over_voltage", "过压点", 10, unit = "V", min = 0.0, max = 130.0),
                uintParam("power_reduction_start_voltage", "降载起始电压", 27, unit = "V", min = 0.0, max = 100.0),
                uintParam("power_reduction_end_voltage", "降载截止电压", 26, unit = "V", min = 0.0, max = 100.0)
            ),
            presets = listOf(
                ZhikePresetDefinition("voltage_48v", "48 V"),
                ZhikePresetDefinition("voltage_60v", "60 V"),
                ZhikePresetDefinition("voltage_64v", "64 V"),
                ZhikePresetDefinition("voltage_72v", "72 V"),
                ZhikePresetDefinition("voltage_76v", "76 V")
            )
        ),
        ZhikeParamGroup(
            id = 9,
            title = "SOC 参数",
            parameters = listOf(
                uintParam("battery_series", "电池串数", 46, unit = "S", min = 0.0, max = 63.0, bitStart = 0, bitEnd = 5),
                uintParam("battery_capacity", "电池容量", 46, unit = "Ah", min = 0.0, max = 1023.0, bitStart = 6, bitEnd = 15),
                uintParam("discharge_coefficient", "放电系数", 59, min = 0.0, max = 255.0, bitStart = 0, bitEnd = 7),
                listParam(
                    "battery_type",
                    "电池类型",
                    59,
                    optionLabels = listOf("三元锂", "磷酸铁锂", "铅酸", "锰酸锂"),
                    bitIndices = listOf(8, 9)
                )
            )
        ),
        ZhikeParamGroup(
            id = 10,
            title = "EBS 参数",
            parameters = listOf(
                uintParam("brake_feedback_torque", "刹车反馈扭矩", 22, unit = "N·m", min = 0.0, max = 100.0),
                uintParam("release_throttle_feedback_torque", "松把反馈扭矩", 45, unit = "N·m", min = 0.0, max = 100.0),
                uintParam("regen_current", "最大回充电流", 23, unit = "A", min = 0.0, max = 100.0),
                uintParam("minimum_feedback_speed", "最小反馈速度", 24, unit = "rpm", min = 0.0, max = 3000.0),
                uintParam("regenerative_voltage_limit", "回充电压上限", 57, unit = "V", min = 0.0, max = 100.0),
                ZhikeParamDefinition(
                    key = "steep_slope_descent",
                    label = "陡坡缓降",
                    kind = ZhikeParamKind.TEXT,
                    wordIndex = 23,
                    minFirmwareVersion = 26
                )
            ),
            presets = listOf(
                ZhikePresetDefinition("ebs_standard", "通用 EBS"),
                ZhikePresetDefinition("ebs_medium", "中度 EBS"),
                ZhikePresetDefinition("ebs_off", "关闭 EBS")
            )
        ),
        ZhikeParamGroup(
            id = 11,
            title = "高级参数",
            parameters = listOf(
                uintParam("c_kp", "C_Kp", 35, min = 0.0, max = 4095.0),
                uintParam("c_ki", "C_Ki", 36, min = 0.0, max = 4095.0),
                uintParam("s_kp", "S_Kp", 43, min = 0.0, max = 4095.0),
                uintParam("s_ki", "S_Ki", 44, min = 0.0, max = 4095.0),
                uintParam("lf_parameter", "LF 参数", 53, min = 0.0, max = 4095.0),
                uintParam("sf_parameter", "SF 参数", 54, min = 0.0, max = 4095.0),
                uintParam("utilization", "利用率", 37, min = 0.0, max = 4095.0),
                uintParam("margin", "Margin", 38, min = 0.0, max = 4095.0),
                uintParam("adjustment_coefficient", "调整系数", 39, min = 0.0, max = 4095.0),
                uintParam("back_emf", "反电动势", 40, min = 0.0, max = 4095.0),
                uintParam("rated_speed", "额定转速", 41, min = 0.0, max = 4095.0),
                uintParam("reference_1", "参考值 1", 55, min = 0.0, max = 4095.0),
                uintParam("reference_2", "参考值 2", 56, min = 0.0, max = 4095.0),
                uintParam("bluetooth_password", "控制器蓝牙密码", 60, min = 0.0, max = 9999.0)
            )
        )
    )

    private val definitions = groups.flatMap { it.parameters }
    private val definitionsByKey = definitions.associateBy { it.key }

    fun findDefinition(key: String): ZhikeParamDefinition? = definitionsByKey[key]

    fun applyPreset(settings: ZhikeSettings, presetId: String): ZhikeSettings {
        val words = settings.rawWords.copyOf()
        when (presetId) {
            "current_swap_common" -> {
                setNumeric(words, requireDef("bus_current"), 45.0)
                setNumeric(words, requireDef("phase_current"), 200.0)
                setNumeric(words, requireDef("weak_magnet_current"), 45.0)
                setNumeric(words, requireDef("one_line_coefficient"), 150.0)
            }
            "throttle_punchy" -> {
                setNumeric(words, requireDef("forward_soft_start"), 0.3)
                setList(words, requireDef("riding_mode"), 2)
            }
            "throttle_daily" -> {
                setNumeric(words, requireDef("forward_soft_start"), 5.0)
                setList(words, requireDef("riding_mode"), 1)
            }
            "voltage_48v" -> applyVoltagePreset(words, 34.0, 95.0, 42.0, 35.0)
            "voltage_60v" -> applyVoltagePreset(words, 44.0, 95.0, 52.0, 45.0)
            "voltage_64v" -> applyVoltagePreset(words, 50.0, 95.0, 56.0, 51.0)
            "voltage_72v" -> applyVoltagePreset(words, 57.0, 95.0, 65.0, 58.0)
            "voltage_76v" -> applyVoltagePreset(words, 63.0, 95.0, 69.0, 64.0)
            "ebs_standard" -> {
                setNumeric(words, requireDef("brake_feedback_torque"), 5.0)
                setNumeric(words, requireDef("release_throttle_feedback_torque"), 5.0)
                setNumeric(words, requireDef("regen_current"), 10.0)
                setNumeric(words, requireDef("minimum_feedback_speed"), 100.0)
                setNumeric(words, requireDef("regenerative_voltage_limit"), 90.0)
            }
            "ebs_medium" -> {
                setNumeric(words, requireDef("brake_feedback_torque"), 10.0)
                setNumeric(words, requireDef("release_throttle_feedback_torque"), 10.0)
                setNumeric(words, requireDef("regen_current"), 20.0)
                setNumeric(words, requireDef("minimum_feedback_speed"), 100.0)
                setNumeric(words, requireDef("regenerative_voltage_limit"), 90.0)
            }
            "ebs_off" -> {
                setNumeric(words, requireDef("brake_feedback_torque"), 0.0)
                setNumeric(words, requireDef("release_throttle_feedback_torque"), 0.0)
                setNumeric(words, requireDef("regen_current"), 0.0)
                setNumeric(words, requireDef("minimum_feedback_speed"), 3000.0)
                setNumeric(words, requireDef("regenerative_voltage_limit"), 0.0)
            }
        }
        return settings.copyWithRawWords(words)
    }

    fun readNumeric(settings: ZhikeSettings, definition: ZhikeParamDefinition): Double {
        val raw = extractRawValue(settings.rawWords, definition)
        val decoded = when (definition.kind) {
            ZhikeParamKind.UINT -> raw / definition.scale
            ZhikeParamKind.INT -> decodeSigned(raw, definition.bitEnd - definition.bitStart + 1) / definition.scale
            ZhikeParamKind.FLOAT -> raw / definition.scale
            ZhikeParamKind.TEXT -> raw.toDouble()
            else -> raw.toDouble()
        }
        return decoded.coerceIn(definition.min, definition.max)
    }

    fun readBool(settings: ZhikeSettings, definition: ZhikeParamDefinition): Boolean {
        return extractBitRange(settings.rawWords[definition.wordIndex], definition.bitStart, definition.bitEnd) != 0
    }

    fun readListIndex(settings: ZhikeSettings, definition: ZhikeParamDefinition): Int {
        val raw = extractRawValue(settings.rawWords, definition)
        return definition.mapping[raw] ?: raw
    }

    fun readText(settings: ZhikeSettings, definition: ZhikeParamDefinition): String {
        return when (definition.key) {
            "steep_slope_descent" -> {
                val current = readNumeric(settings, requireDef("regen_current")).roundToInt()
                if (current % 2 != 0) "打开" else "关闭"
            }
            else -> formatNumericValue(readNumeric(settings, definition), definition)
        }
    }

    fun optionLabel(settings: ZhikeSettings, definition: ZhikeParamDefinition): String {
        val index = readListIndex(settings, definition).coerceIn(0, definition.optionLabels.lastIndex.coerceAtLeast(0))
        return definition.optionLabels.getOrElse(index) { index.toString() }
    }

    fun updateNumeric(settings: ZhikeSettings, definition: ZhikeParamDefinition, value: Double): ZhikeSettings {
        val words = settings.rawWords.copyOf()
        setNumeric(words, definition, value)
        return settings.copyWithRawWords(words)
    }

    fun updateBool(settings: ZhikeSettings, definition: ZhikeParamDefinition, value: Boolean): ZhikeSettings {
        val words = settings.rawWords.copyOf()
        setBitRange(words, definition.wordIndex, definition.bitStart, definition.bitEnd, if (value) 1 else 0)
        return settings.copyWithRawWords(words)
    }

    fun updateList(settings: ZhikeSettings, definition: ZhikeParamDefinition, selectedIndex: Int): ZhikeSettings {
        val words = settings.rawWords.copyOf()
        setList(words, definition, selectedIndex)
        return settings.copyWithRawWords(words)
    }

    fun formatNumericValue(value: Double, definition: ZhikeParamDefinition): String {
        return when (definition.kind) {
            ZhikeParamKind.FLOAT -> {
                if (value == value.roundToInt().toDouble()) {
                    value.roundToInt().toString()
                } else {
                    "%.2f".format(value).trimEnd('0').trimEnd('.')
                }
            }
            else -> {
                if (value == value.roundToInt().toDouble()) {
                    value.roundToInt().toString()
                } else {
                    "%.2f".format(value).trimEnd('0').trimEnd('.')
                }
            }
        }
    }

    private fun applyVoltagePreset(words: IntArray, underVoltage: Double, overVoltage: Double, startVoltage: Double, endVoltage: Double) {
        setNumeric(words, requireDef("under_voltage"), underVoltage)
        setNumeric(words, requireDef("over_voltage"), overVoltage)
        setNumeric(words, requireDef("power_reduction_start_voltage"), startVoltage)
        setNumeric(words, requireDef("power_reduction_end_voltage"), endVoltage)
    }

    private fun setNumeric(words: IntArray, definition: ZhikeParamDefinition, value: Double) {
        val clamped = value.coerceIn(definition.min, definition.max)
        val encoded = when (definition.kind) {
            ZhikeParamKind.UINT, ZhikeParamKind.FLOAT, ZhikeParamKind.TEXT -> (clamped * definition.scale).roundToInt()
            ZhikeParamKind.INT -> encodeSigned((clamped * definition.scale).roundToInt(), definition.bitEnd - definition.bitStart + 1)
            else -> clamped.roundToInt()
        }
        setBitRange(words, definition.wordIndex, definition.bitStart, definition.bitEnd, encoded)
    }

    private fun setList(words: IntArray, definition: ZhikeParamDefinition, selectedIndex: Int) {
        val encoded = definition.mapping.entries.firstOrNull { it.value == selectedIndex }?.key ?: selectedIndex
        if (definition.useContinuousBits) {
            setBitRange(words, definition.wordIndex, definition.bitStart, definition.bitEnd, encoded)
            return
        }
        var word = words[definition.wordIndex]
        definition.bitIndices.forEachIndexed { optionBitIndex, bitPosition ->
            if (bitPosition == -1) return@forEachIndexed
            val bitValue = if ((encoded and (1 shl optionBitIndex)) != 0) 1 else 0
            word = applyBitRange(word, bitPosition, bitPosition, bitValue)
        }
        words[definition.wordIndex] = word and 0xFFFF
    }

    private fun extractRawValue(words: IntArray, definition: ZhikeParamDefinition): Int {
        val word = words.getOrElse(definition.wordIndex) { 0 }
        return when (definition.kind) {
            ZhikeParamKind.LIST -> {
                if (definition.useContinuousBits) {
                    extractBitRange(word, definition.bitStart, definition.bitEnd)
                } else {
                    definition.bitIndices.withIndex().fold(0) { acc, indexedValue ->
                        val sourceBit = indexedValue.value
                        if (sourceBit == -1) {
                            acc
                        } else {
                            acc or (((word shr sourceBit) and 0x01) shl indexedValue.index)
                        }
                    }
                }
            }
            else -> extractBitRange(word, definition.bitStart, definition.bitEnd)
        }
    }

    private fun requireDef(key: String): ZhikeParamDefinition {
        return requireNotNull(findDefinition(key)) { "Missing Zhike parameter definition for $key" }
    }
}

fun ZhikeSettings.copyWithRawWords(words: IntArray): ZhikeSettings {
    val copy = copy(rawWords = words.copyOf())
    copy.syncLegacyFieldsFromWords()
    return copy
}

fun ZhikeSettings.syncLegacyFieldsFromWords() {
    busCurrent = ZhikeParameterCatalog.readNumeric(this, requireZhikeDef("bus_current")).roundToInt()
    phaseCurrent = ZhikeParameterCatalog.readNumeric(this, requireZhikeDef("phase_current")).roundToInt()
    motorDirection = ZhikeParameterCatalog.readBool(this, requireZhikeDef("motor_direction"))
    sensorType = ZhikeParameterCatalog.readListIndex(this, requireZhikeDef("sensor_type"))
    hallSequence = ZhikeParameterCatalog.readBool(this, requireZhikeDef("hall_sequence"))
    phaseShiftAngle = ZhikeParameterCatalog.readNumeric(this, requireZhikeDef("phase_shift_angle")).roundToInt()
    polePairs = ZhikeParameterCatalog.readNumeric(this, requireZhikeDef("pole_pairs")).roundToInt()
    underVoltage = ZhikeParameterCatalog.readNumeric(this, requireZhikeDef("under_voltage")).roundToInt()
    overVoltage = ZhikeParameterCatalog.readNumeric(this, requireZhikeDef("over_voltage")).roundToInt()
    weakMagCurrent = ZhikeParameterCatalog.readNumeric(this, requireZhikeDef("weak_magnet_current")).roundToInt()
    regenCurrent = ZhikeParameterCatalog.readNumeric(this, requireZhikeDef("regen_current")).roundToInt()
    bluetoothPassword = ZhikeParameterCatalog.readNumeric(this, requireZhikeDef("bluetooth_password")).roundToInt()
}

private fun requireZhikeDef(key: String): ZhikeParamDefinition {
    return requireNotNull(ZhikeParameterCatalog.findDefinition(key)) { "Missing Zhike parameter definition for $key" }
}

private fun uintParam(
    key: String,
    label: String,
    wordIndex: Int,
    unit: String = "",
    min: Double,
    max: Double,
    bitStart: Int = 0,
    bitEnd: Int = 15,
    scale: Double = 1.0,
    minFirmwareVersion: Int? = null
): ZhikeParamDefinition {
    return ZhikeParamDefinition(
        key = key,
        label = label,
        kind = ZhikeParamKind.UINT,
        wordIndex = wordIndex,
        unit = unit,
        min = min,
        max = max,
        bitStart = bitStart,
        bitEnd = bitEnd,
        scale = scale,
        minFirmwareVersion = minFirmwareVersion
    )
}

private fun intParam(
    key: String,
    label: String,
    wordIndex: Int,
    unit: String = "",
    min: Double,
    max: Double,
    bitStart: Int = 0,
    bitEnd: Int = 15,
    scale: Double = 1.0,
    minFirmwareVersion: Int? = null
): ZhikeParamDefinition {
    return ZhikeParamDefinition(
        key = key,
        label = label,
        kind = ZhikeParamKind.INT,
        wordIndex = wordIndex,
        unit = unit,
        min = min,
        max = max,
        bitStart = bitStart,
        bitEnd = bitEnd,
        scale = scale,
        minFirmwareVersion = minFirmwareVersion
    )
}

private fun floatParam(
    key: String,
    label: String,
    wordIndex: Int,
    unit: String = "",
    min: Double,
    max: Double,
    bitStart: Int = 0,
    bitEnd: Int = 15,
    scale: Double = 1.0,
    minFirmwareVersion: Int? = null
): ZhikeParamDefinition {
    return ZhikeParamDefinition(
        key = key,
        label = label,
        kind = ZhikeParamKind.FLOAT,
        wordIndex = wordIndex,
        unit = unit,
        min = min,
        max = max,
        bitStart = bitStart,
        bitEnd = bitEnd,
        scale = scale,
        minFirmwareVersion = minFirmwareVersion
    )
}

private fun boolParam(
    key: String,
    label: String,
    wordIndex: Int,
    bitIndex: Int,
    onText: String,
    offText: String,
    minFirmwareVersion: Int? = null
): ZhikeParamDefinition {
    return ZhikeParamDefinition(
        key = key,
        label = label,
        kind = ZhikeParamKind.BOOL,
        wordIndex = wordIndex,
        optionLabels = listOf(offText, onText),
        min = 0.0,
        max = 1.0,
        bitStart = bitIndex,
        bitEnd = bitIndex,
        minFirmwareVersion = minFirmwareVersion
    )
}

private fun listParam(
    key: String,
    label: String,
    wordIndex: Int,
    optionLabels: List<String>,
    mapping: Map<Int, Int> = emptyMap(),
    bitIndices: List<Int> = emptyList(),
    useContinuousBits: Boolean = false,
    bitStart: Int = 0,
    bitEnd: Int = 15,
    minFirmwareVersion: Int? = null
): ZhikeParamDefinition {
    return ZhikeParamDefinition(
        key = key,
        label = label,
        kind = ZhikeParamKind.LIST,
        wordIndex = wordIndex,
        optionLabels = optionLabels,
        min = 0.0,
        max = optionLabels.lastIndex.coerceAtLeast(0).toDouble(),
        bitStart = bitStart,
        bitEnd = bitEnd,
        mapping = mapping,
        bitIndices = bitIndices,
        useContinuousBits = useContinuousBits,
        minFirmwareVersion = minFirmwareVersion
    )
}

private fun extractBitRange(word: Int, start: Int, end: Int): Int {
    val width = end - start + 1
    val mask = if (width >= 16) 0xFFFF else (1 shl width) - 1
    return (word ushr start) and mask
}

private fun setBitRange(words: IntArray, wordIndex: Int, start: Int, end: Int, value: Int) {
    words[wordIndex] = applyBitRange(words[wordIndex], start, end, value)
}

private fun applyBitRange(word: Int, start: Int, end: Int, value: Int): Int {
    val width = end - start + 1
    val mask = if (width >= 16) 0xFFFF else (1 shl width) - 1
    val shiftedMask = (mask shl start) and 0xFFFF
    return ((word and shiftedMask.inv()) or ((value and mask) shl start)) and 0xFFFF
}

private fun decodeSigned(raw: Int, width: Int): Double {
    if (width <= 0) return raw.toDouble()
    val signBit = 1 shl (width - 1)
    return if ((raw and signBit) != 0) {
        (raw - (1 shl width)).toDouble()
    } else {
        raw.toDouble()
    }
}

private fun encodeSigned(value: Int, width: Int): Int {
    return if (value < 0) {
        (value + (1 shl width)) and ((1 shl width) - 1)
    } else {
        value
    }
}
