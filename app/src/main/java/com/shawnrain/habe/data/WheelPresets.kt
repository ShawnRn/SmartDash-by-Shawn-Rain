package com.shawnrain.habe.data

import kotlin.math.PI

data class WheelPreset(
    val label: String,
    val rimSize: String,
    val circumference: Float,
    val group: String,
    val recommended: Boolean = false
)

object WheelPresets {
    private fun imperial(rimInch: Float, sectionInch: Float): Float {
        val diameterMm = (rimInch + sectionInch * 2f) * 25.4f
        return (diameterMm * PI).toFloat()
    }

    private fun metric(rimInch: Float, widthMm: Float, aspectRatio: Float): Float {
        val diameterMm = rimInch * 25.4f + widthMm * (aspectRatio / 100f) * 2f
        return (diameterMm * PI).toFloat()
    }

    val common = listOf(
        WheelPreset("8.5寸 50/75-6.1", "8.5寸", metric(6.1f, 50f, 75f), "小轮径通勤"),
        WheelPreset("8.5寸 60/70-6.5", "8.5寸", metric(6.5f, 60f, 70f), "小轮径通勤"),
        WheelPreset("9寸 3.00-4", "9寸", imperial(4f, 3f), "小轮径通勤"),
        WheelPreset("10寸 2.125", "10寸", imperial(10f, 2.125f), "10寸轮毂电机"),
        WheelPreset("10寸 2.50", "10寸", imperial(10f, 2.5f), "10寸轮毂电机"),
        WheelPreset("10寸 3.00-10", "10寸", imperial(10f, 3f), "10寸轮毂电机"),
        WheelPreset("10寸 3.50-10", "10寸", imperial(10f, 3.5f), "10寸轮毂电机", recommended = true),
        WheelPreset("10寸 90/90-10", "10寸", metric(10f, 90f, 90f), "10寸轮毂电机"),
        WheelPreset("10寸 100/80-10", "10寸", metric(10f, 100f, 80f), "10寸轮毂电机"),
        WheelPreset("10寸 100/90-10", "10寸", metric(10f, 100f, 90f), "10寸轮毂电机"),
        WheelPreset("11寸 90/90-11", "11寸", metric(11f, 90f, 90f), "11寸轮毂电机"),
        WheelPreset("11寸 100/80-11", "11寸", metric(11f, 100f, 80f), "11寸轮毂电机"),
        WheelPreset("11寸 3.50-11", "11寸", imperial(11f, 3.5f), "11寸轮毂电机"),
        WheelPreset("12寸 2.125", "12寸", imperial(12f, 2.125f), "12寸轮毂电机"),
        WheelPreset("12寸 90/90-12", "12寸", metric(12f, 90f, 90f), "12寸轮毂电机"),
        WheelPreset("12寸 100/70-12", "12寸", metric(12f, 100f, 70f), "12寸轮毂电机"),
        WheelPreset("12寸 100/80-12", "12寸", metric(12f, 100f, 80f), "12寸轮毂电机"),
        WheelPreset("12寸 100/90-12", "12寸", metric(12f, 100f, 90f), "12寸轮毂电机"),
        WheelPreset("12寸 110/70-12", "12寸", metric(12f, 110f, 70f), "12寸轮毂电机"),
        WheelPreset("12寸 120/70-12", "12寸", metric(12f, 120f, 70f), "12寸轮毂电机"),
        WheelPreset("12寸 130/70-12", "12寸", metric(12f, 130f, 70f), "12寸轮毂电机"),
        WheelPreset("13寸 110/70-13", "13寸", metric(13f, 110f, 70f), "大踏板"),
        WheelPreset("14寸 80/90-14", "14寸", metric(14f, 80f, 90f), "大踏板"),
        WheelPreset("16寸 2.125", "16寸", imperial(16f, 2.125f), "自行车/跨骑"),
        WheelPreset("17寸 70/90-17", "17寸", metric(17f, 70f, 90f), "跨骑/街车"),
        WheelPreset("17寸 130/70-17", "17寸", metric(17f, 130f, 70f), "跨骑/街车")
    ).sortedWith(compareBy<WheelPreset> { it.group }.thenBy { it.circumference })

    val recommended: List<WheelPreset> = common.filter { it.recommended }
    val rimOptions: List<String> = common.map { it.rimSize }.distinct()

    fun presetsForRim(rimSize: String): List<WheelPreset> {
        return common.filter { it.rimSize == rimSize }
    }
}
