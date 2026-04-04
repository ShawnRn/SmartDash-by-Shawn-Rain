package com.shawnrain.habe.data

import com.shawnrain.habe.ui.text.withDisplaySpacing
import kotlin.math.PI

data class WheelPreset(
    val label: String,
    val rimSize: String,
    val circumference: Float,
    val group: String,
    val recommended: Boolean = false
)

object WheelPresets {
    private fun normalizeKey(value: String): String {
        return value
            .withDisplaySpacing()
            .filterNot(Char::isWhitespace)
            .lowercase()
    }

    private fun preset(label: String, rimSize: String, circumference: Float, group: String, recommended: Boolean = false): WheelPreset {
        return WheelPreset(
            label = label.withDisplaySpacing(),
            rimSize = rimSize.withDisplaySpacing(),
            circumference = circumference,
            group = group.withDisplaySpacing(),
            recommended = recommended
        )
    }

    private fun imperial(rimInch: Float, sectionInch: Float): Float {
        val diameterMm = (rimInch + sectionInch * 2f) * 25.4f
        return (diameterMm * PI).toFloat()
    }

    private fun metric(rimInch: Float, widthMm: Float, aspectRatio: Float): Float {
        val diameterMm = rimInch * 25.4f + widthMm * (aspectRatio / 100f) * 2f
        return (diameterMm * PI).toFloat()
    }

    val common = listOf(
        preset("8.5寸 50/75-6.1", "8.5寸", metric(6.1f, 50f, 75f), "小轮径通勤"),
        preset("8.5寸 60/70-6.5", "8.5寸", metric(6.5f, 60f, 70f), "小轮径通勤"),
        preset("9寸 3.00-4", "9寸", imperial(4f, 3f), "小轮径通勤"),
        preset("10寸 2.125", "10寸", imperial(10f, 2.125f), "10寸轮毂电机"),
        preset("10寸 2.50", "10寸", imperial(10f, 2.5f), "10寸轮毂电机"),
        preset("10寸 3.00-10", "10寸", imperial(10f, 3f), "10寸轮毂电机"),
        preset("10寸 3.50-10", "10寸", imperial(10f, 3.5f), "10寸轮毂电机", recommended = true),
        preset("10寸 90/90-10", "10寸", metric(10f, 90f, 90f), "10寸轮毂电机"),
        preset("10寸 100/80-10", "10寸", metric(10f, 100f, 80f), "10寸轮毂电机"),
        preset("10寸 100/90-10", "10寸", metric(10f, 100f, 90f), "10寸轮毂电机"),
        preset("11寸 90/90-11", "11寸", metric(11f, 90f, 90f), "11寸轮毂电机"),
        preset("11寸 100/80-11", "11寸", metric(11f, 100f, 80f), "11寸轮毂电机"),
        preset("11寸 3.50-11", "11寸", imperial(11f, 3.5f), "11寸轮毂电机"),
        preset("12寸 2.125", "12寸", imperial(12f, 2.125f), "12寸轮毂电机"),
        preset("12寸 90/90-12", "12寸", metric(12f, 90f, 90f), "12寸轮毂电机"),
        preset("12寸 100/70-12", "12寸", metric(12f, 100f, 70f), "12寸轮毂电机"),
        preset("12寸 100/80-12", "12寸", metric(12f, 100f, 80f), "12寸轮毂电机"),
        preset("12寸 100/90-12", "12寸", metric(12f, 100f, 90f), "12寸轮毂电机"),
        preset("12寸 110/70-12", "12寸", metric(12f, 110f, 70f), "12寸轮毂电机"),
        preset("12寸 120/70-12", "12寸", metric(12f, 120f, 70f), "12寸轮毂电机"),
        preset("12寸 130/70-12", "12寸", metric(12f, 130f, 70f), "12寸轮毂电机"),
        preset("13寸 110/70-13", "13寸", metric(13f, 110f, 70f), "大踏板"),
        preset("14寸 80/90-14", "14寸", metric(14f, 80f, 90f), "大踏板"),
        preset("16寸 2.125", "16寸", imperial(16f, 2.125f), "自行车/跨骑"),
        preset("17寸 70/90-17", "17寸", metric(17f, 70f, 90f), "跨骑/街车"),
        preset("17寸 130/70-17", "17寸", metric(17f, 130f, 70f), "跨骑/街车")
    ).sortedWith(compareBy<WheelPreset> { it.group }.thenBy { it.circumference })

    val recommended: List<WheelPreset> = common.filter { it.recommended }
    val rimOptions: List<String> = common.map { it.rimSize }.distinct()

    fun canonicalRimSize(rimSize: String): String {
        if (rimSize.isBlank()) return rimOptions.firstOrNull().orEmpty()
        val normalized = normalizeKey(rimSize)
        return rimOptions.firstOrNull { normalizeKey(it) == normalized }
            ?: rimSize.withDisplaySpacing()
    }

    fun presetsForRim(rimSize: String): List<WheelPreset> {
        val normalized = normalizeKey(rimSize)
        return common.filter { normalizeKey(it.rimSize) == normalized }
    }

    fun findPreset(rimSize: String, label: String, circumference: Float? = null): WheelPreset? {
        val presets = presetsForRim(rimSize)
        val normalizedLabel = normalizeKey(label)
        return presets.firstOrNull { normalizeKey(it.label) == normalizedLabel }
            ?: circumference?.let { wheel ->
                presets.firstOrNull { kotlin.math.abs(it.circumference - wheel) < 2f }
            }
    }
}
