package com.shawnrain.habe.data

data class WheelPreset(val label: String, val circumference: Float)

object WheelPresets {
    val common = listOf(
        WheelPreset("10寸 3.00-10", 1260f),
        WheelPreset("10寸 3.50-10", 1370f),
        WheelPreset("10寸 90/90-10", 1300f),
        WheelPreset("12寸 90/90-12", 1460f),
        WheelPreset("12寸 100/90-12", 1520f),
        WheelPreset("12寸 110/70-12", 1400f),
        WheelPreset("12寸 120/70-12", 1450f),
        WheelPreset("17寸 70/90-17", 1880f),
        WheelPreset("17寸 130/70-17", 1850f)
    )
}
