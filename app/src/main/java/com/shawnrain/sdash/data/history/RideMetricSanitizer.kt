package com.shawnrain.sdash.data.history

import com.shawnrain.sdash.data.telemetry.TelemetryStreamProcessor
import com.shawnrain.sdash.debug.AppLogger
import kotlin.math.abs

object RideMetricSanitizer {
    private const val TAG = "RideMetricSanitizer"

    fun sanitize(
        samples: List<RideMetricSample>,
        batterySeries: Int
    ): List<RideMetricSample> {
        if (samples.isEmpty()) return samples

        val minValidVoltage = TelemetryStreamProcessor.recommendedMinPackVoltageV(batterySeries)
        val trimmed = samples.toMutableList()

        // 1. Trim dirty zero-value samples from the end
        while (trimmed.isNotEmpty() && isDirtyZeroValueSample(trimmed.last(), minValidVoltage)) {
            trimmed.removeAt(trimmed.lastIndex)
        }
        if (trimmed.size < 3) return trimmed

        // 2. Filter out startup SoC glitches at the beginning (e.g., within first 3000ms)
        val startupWindowMs = 3000L
        val startupSamples = trimmed.filter { it.elapsedMs <= startupWindowMs }
        if (startupSamples.size >= 2) {
            val maxStartupSoc = startupSamples.maxOfOrNull { it.soc } ?: 0f
            if (maxStartupSoc > 1.0f) {
                var trimCount = 0
                for (sample in trimmed) {
                    if (sample.elapsedMs <= startupWindowMs && maxStartupSoc - sample.soc > 5.0f) {
                        trimCount++
                    } else {
                        break
                    }
                }
                if (trimCount > 0 && trimCount <= trimmed.size - 2) {
                    repeat(trimCount) {
                        trimmed.removeAt(0)
                    }
                    val offset = trimmed.first().elapsedMs
                    if (offset > 0L) {
                        for (i in trimmed.indices) {
                            trimmed[i] = trimmed[i].copy(elapsedMs = trimmed[i].elapsedMs - offset)
                        }
                    }
                    AppLogger.i(TAG, "Trimmed $trimCount startup glitch samples, offset shifted by $offset ms.")
                }
            }
        }

        // 3. Bridging/filtering intermediate dirty samples
        return trimmed.filterIndexed { index, sample ->
            if (!isDirtyZeroValueSample(sample, minValidVoltage)) return@filterIndexed true
            val prev = trimmed.getOrNull(index - 1)
            val next = trimmed.getOrNull(index + 1)
            val bridgedByValidNeighbors =
                prev != null && next != null &&
                    isClearlyValidRideSample(prev, minValidVoltage) &&
                    isClearlyValidRideSample(next, minValidVoltage)
            !bridgedByValidNeighbors
        }
    }

    private fun isDirtyZeroValueSample(sample: RideMetricSample, minValidVoltage: Float): Boolean {
        val voltageTooLow = sample.voltage in 0.0f..<minValidVoltage
        val controllerTempCollapsed = sample.controllerTemp <= 1.0f
        val mostlyIdle = sample.speedKmH <= 1.5f && abs(sample.busCurrent) <= 3.0f && sample.rpm <= 80.0f
        return voltageTooLow && controllerTempCollapsed && mostlyIdle
    }

    private fun isClearlyValidRideSample(sample: RideMetricSample, minValidVoltage: Float): Boolean {
        return sample.voltage >= minValidVoltage && sample.controllerTemp > 1.0f
    }
}
