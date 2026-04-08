package com.shawnrain.sdash.data.gps

/**
 * 方位文字格式化器，带滞回（hysteresis），防止临界角附近文字来回跳变。
 *
 * 例如 8 方位时，北 ↔ 东北 边界在 22.5°。
 * 如果角度在 21° / 24° / 22° / 25° 抖，文字会在「北 / 东北」反复横跳。
 * 本类通过要求方向「深入新扇区一定角度」后才切换，消除这种视觉抽动。
 */
class DirectionLabelFormatter(
    private val labels: List<String> = listOf("北", "东北", "东", "东南", "南", "西南", "西", "西北"),
    private val hysteresisDeg: Float = 12f
) {
    private val sectorSize = 360f / labels.size
    private var currentIndex: Int? = null

    fun reset() {
        currentIndex = null
    }

    fun format(directionDeg: Float): String {
        val rawIndex = (((directionDeg + sectorSize / 2f) % 360f + 360f) % 360f / sectorSize).toInt()
            .coerceIn(0, labels.size - 1)

        val existing = currentIndex
        if (existing == null) {
            currentIndex = rawIndex
            return labels[rawIndex]
        }

        if (rawIndex == existing) return labels[existing]

        // Hysteresis: check if the direction is deep enough into the new sector
        val sectorCenter = (rawIndex * sectorSize + sectorSize / 2f) % 360f
        val sectorEdge = sectorSize / 2f - hysteresisDeg
        val deltaToCenter = shortestAngleDelta(directionDeg, sectorCenter)

        // Only switch if we're past the hysteresis boundary from the sector edge
        if (abs(deltaToCenter) < sectorEdge) {
            return labels[existing]
        }

        currentIndex = rawIndex
        return labels[rawIndex]
    }

    private fun shortestAngleDelta(from: Float, to: Float): Float {
        val normalized = ((to - from + 540f) % 360f) - 180f
        return if (normalized == -180f) 180f else normalized
    }

    private fun abs(value: Float): Float = kotlin.math.abs(value)
}
