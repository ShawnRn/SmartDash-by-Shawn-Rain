package com.shawnrain.habe.ble.protocols

/**
 * 智科参数写入后的回读核对器
 * 比对写入前后的参数差异，确认控制器真实应用了变更
 */
object ZhikePostWriteVerifier {
    private const val TAG = "ZhikeVerifier"

    data class VerificationResult(
        val success: Boolean,
        val mismatchedKeys: List<String>,
        val message: String
    )

    /**
     * 比对写入前后的参数差异
     * @param before 写入前的参数快照
     * @param after 写入后的回读参数
     * @param modifiedKeys 本次修改的参数 key 列表
     */
    fun verify(
        before: ZhikeSettings,
        after: ZhikeSettings,
        modifiedKeys: List<String>
    ): VerificationResult {
        if (!after.loadedFromController) {
            return VerificationResult(
                success = false,
                mismatchedKeys = modifiedKeys,
                message = "控制器未返回有效参数，写入结果未知"
            )
        }

        val mismatched = mutableListOf<String>()

        for (key in modifiedKeys) {
            val def = ZhikeParameterCatalog.findDefinition(key) ?: continue
            val beforeVal = readValue(before, def)
            val afterVal = readValue(after, def)
            if (beforeVal != afterVal) {
                mismatched.add(key)
            }
        }

        // 额外检查核心风险参数
        val criticalKeys = listOf(
            "bus_current", "phase_current", "weak_magnet_current",
            "under_voltage", "over_voltage", "motor_direction"
        )
        for (key in criticalKeys) {
            if (key in modifiedKeys) continue
            val def = ZhikeParameterCatalog.findDefinition(key) ?: continue
            val beforeVal = readValue(before, def)
            val afterVal = readValue(after, def)
            if (beforeVal != afterVal) {
                mismatched.add("$key (unexpected)")
            }
        }

        return if (mismatched.isEmpty()) {
            VerificationResult(
                success = true,
                mismatchedKeys = emptyList(),
                message = "写入成功，所有参数已确认生效"
            )
        } else {
            val modifiedApplied = modifiedKeys.filter { it !in mismatched }
            val modifiedNotApplied = modifiedKeys.filter { it in mismatched }

            val message = buildString {
                if (modifiedNotApplied.isNotEmpty()) {
                    append("控制器返回成功，但以下参数未完全生效：")
                    append(modifiedNotApplied.joinToString(", "))
                }
                if (modifiedApplied.isEmpty() && modifiedKeys.isNotEmpty()) {
                    append("所有修改均未生效。")
                }
                val unexpectedChanges = mismatched.filter { it.contains("(unexpected)") }
                if (unexpectedChanges.isNotEmpty()) {
                    if (isNotEmpty()) append("\n")
                    append("非预期变更：${unexpectedChanges.joinToString(", ")}")
                }
            }

            VerificationResult(
                success = modifiedNotApplied.isEmpty(),
                mismatchedKeys = mismatched,
                message = message.ifBlank { "写入结果部分确认" }
            )
        }
    }

    private fun readValue(settings: ZhikeSettings, def: ZhikeParamDefinition): Any {
        return when (def.kind) {
            ZhikeParamKind.BOOL -> ZhikeParameterCatalog.readBool(settings, def)
            ZhikeParamKind.LIST -> ZhikeParameterCatalog.readListIndex(settings, def)
            ZhikeParamKind.TEXT -> ZhikeParameterCatalog.readText(settings, def)
            else -> ZhikeParameterCatalog.readNumeric(settings, def)
        }
    }
}
