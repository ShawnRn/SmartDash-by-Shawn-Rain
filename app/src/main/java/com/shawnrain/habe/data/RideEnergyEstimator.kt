package com.shawnrain.habe.data

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class RideEnergyEstimate(
    val displaySocPercent: Float,
    val socByAhPercent: Float,
    val socByOcvPercent: Float,
    val estimatedRangeKm: Float,
    val remainingEnergyWh: Float,
    val learnedInternalResistanceOhm: Float,
    val filteredVoltage: Float,
    val filteredCurrent: Float,
    val averageWindowEfficiencyWhKm: Float
)

private data class RangeWindowSample(
    val distanceKm: Double,
    val energyWh: Double
)

class RideEnergyEstimator {
    companion object {
        private const val EMA_ALPHA = 0.12f
        private const val CURRENT_STEP_THRESHOLD_A = 10f
        private const val STATIONARY_CURRENT_THRESHOLD_A = 1f
        private const val STATIONARY_RECALIBRATION_MS = 45_000L
        private const val STATIONARY_STRONG_RECALIBRATION_MS = 120_000L
        private const val RANGE_WINDOW_DISTANCE_KM = 2.0
        private const val MIN_RANGE_WINDOW_DISTANCE_KM = 0.2
    }

    private val rangeWindow = ArrayDeque<RangeWindowSample>()

    private var filteredVoltage = Float.NaN
    private var filteredCurrent = Float.NaN
    private var previousRawVoltage = 0f
    private var previousRawCurrent = 0f
    private var lastSampleAtMs = 0L
    private var stationarySinceMs = 0L

    private var baseSocPercent = Float.NaN
    private var consumedAh = 0.0
    private var consumedPositiveEnergyWh = 0.0
    private var displayedSocPercent = Float.NaN
    private var learnedInternalResistanceOhm = 0f

    private var lastRangeDistanceKm = 0.0
    private var lastRangeEnergyWh = 0.0
    private var rangeWindowDistanceKm = 0.0
    private var rangeWindowEnergyWh = 0.0

    fun reset(initialLearnedInternalResistanceOhm: Float = 0f) {
        filteredVoltage = Float.NaN
        filteredCurrent = Float.NaN
        previousRawVoltage = 0f
        previousRawCurrent = 0f
        lastSampleAtMs = 0L
        stationarySinceMs = 0L
        baseSocPercent = Float.NaN
        consumedAh = 0.0
        consumedPositiveEnergyWh = 0.0
        displayedSocPercent = Float.NaN
        learnedInternalResistanceOhm = initialLearnedInternalResistanceOhm.coerceAtLeast(0f)
        lastRangeDistanceKm = 0.0
        lastRangeEnergyWh = 0.0
        rangeWindowDistanceKm = 0.0
        rangeWindowEnergyWh = 0.0
        rangeWindow.clear()
    }

    fun currentLearnedInternalResistanceOhm(): Float {
        return learnedInternalResistanceOhm
    }

    fun estimate(
        rawVoltage: Float,
        rawCurrent: Float,
        distanceKm: Double,
        batterySeries: Int,
        batteryCapacityAh: Float,
        usableEnergyRatio: Float,
        fallbackSocPercent: Float?,
        nowMs: Long
    ): RideEnergyEstimate {
        val safeSeries = batterySeries.coerceAtLeast(1)
        val safeCapacityAh = batteryCapacityAh.coerceAtLeast(1f)
        val safeUsableRatio = usableEnergyRatio.coerceIn(0.72f, 0.98f)

        filteredVoltage = if (filteredVoltage.isNaN()) rawVoltage else ema(filteredVoltage, rawVoltage)
        filteredCurrent = if (filteredCurrent.isNaN()) rawCurrent else ema(filteredCurrent, rawCurrent)

        updateLearnedResistance(rawVoltage = rawVoltage, rawCurrent = rawCurrent, nowMs = nowMs)

        val dtSeconds = ((nowMs - lastSampleAtMs).coerceAtLeast(0L) / 1000.0).coerceAtMost(5.0)
        if (lastSampleAtMs > 0L && dtSeconds > 0.0) {
            val dischargeCurrent = filteredCurrent.coerceAtLeast(0f)
            consumedAh += (dischargeCurrent * dtSeconds) / 3600.0
            consumedPositiveEnergyWh += (filteredVoltage * dischargeCurrent * dtSeconds) / 3600.0
        }

        val ocvCompensationCurrent = filteredCurrent.coerceAtLeast(0f)
        val compensatedOpenCircuitVoltage = filteredVoltage + (ocvCompensationCurrent * learnedInternalResistanceOhm)
        val socByOcvPercent = socFromPackVoltage(compensatedOpenCircuitVoltage, safeSeries)
        val initialSoc = fallbackSocPercent?.takeIf { it in 1f..100f } ?: socByOcvPercent
        if (baseSocPercent.isNaN()) {
            baseSocPercent = initialSoc
        }

        if (abs(filteredCurrent) < STATIONARY_CURRENT_THRESHOLD_A) {
            if (stationarySinceMs == 0L) {
                stationarySinceMs = nowMs
            }
        } else {
            stationarySinceMs = 0L
        }

        val socByAhPercent = (baseSocPercent - ((consumedAh / safeCapacityAh) * 100.0).toFloat()).coerceIn(0f, 100f)
        val (wAh, wOcv) = weightsForCurrent(abs(filteredCurrent), nowMs)
        val fusedSoc = ((socByAhPercent * wAh) + (socByOcvPercent * wOcv)).coerceIn(0f, 100f)

        val targetSoc = when {
            abs(filteredCurrent) > 12f -> socByAhPercent
            else -> fusedSoc
        }.coerceIn(0f, 100f)
        val lowLoadAdjustedTargetSoc = if (abs(filteredCurrent) <= 1.5f) {
            ((targetSoc * 0.6f) + (socByOcvPercent * 0.4f)).coerceIn(0f, 100f)
        } else {
            targetSoc
        }
        displayedSocPercent = updateDisplayedSoc(
            previousSoc = displayedSocPercent,
            targetSoc = lowLoadAdjustedTargetSoc,
            absCurrent = abs(filteredCurrent),
            current = filteredCurrent,
            dtSeconds = dtSeconds,
            nowMs = nowMs
        )

        updateRangeWindow(distanceKm = distanceKm, consumedEnergyWh = consumedPositiveEnergyWh)

        val averageWindowEfficiencyWhKm = if (rangeWindowDistanceKm >= MIN_RANGE_WINDOW_DISTANCE_KM) {
            (rangeWindowEnergyWh / rangeWindowDistanceKm).toFloat()
        } else {
            0f
        }
        val nominalPackEnergyWh = safeCapacityAh * nominalPackVoltage(safeSeries)
        val remainingEnergyWh = (displayedSocPercent / 100f) * nominalPackEnergyWh * safeUsableRatio
        val estimatedRangeKm = if (averageWindowEfficiencyWhKm > 0.1f) {
            (remainingEnergyWh / averageWindowEfficiencyWhKm).coerceAtLeast(0f)
        } else {
            0f
        }

        previousRawVoltage = rawVoltage
        previousRawCurrent = rawCurrent
        lastSampleAtMs = nowMs

        return RideEnergyEstimate(
            displaySocPercent = displayedSocPercent,
            socByAhPercent = socByAhPercent,
            socByOcvPercent = socByOcvPercent,
            estimatedRangeKm = estimatedRangeKm,
            remainingEnergyWh = remainingEnergyWh,
            learnedInternalResistanceOhm = learnedInternalResistanceOhm,
            filteredVoltage = filteredVoltage,
            filteredCurrent = filteredCurrent,
            averageWindowEfficiencyWhKm = averageWindowEfficiencyWhKm
        )
    }

    private fun ema(previous: Float, current: Float): Float {
        return (EMA_ALPHA * current) + ((1f - EMA_ALPHA) * previous)
    }

    private fun updateLearnedResistance(rawVoltage: Float, rawCurrent: Float, nowMs: Long) {
        if (lastSampleAtMs == 0L) return
        val dtMs = nowMs - lastSampleAtMs
        if (dtMs !in 80L..2000L) return

        val deltaCurrent = rawCurrent - previousRawCurrent
        if (abs(deltaCurrent) < CURRENT_STEP_THRESHOLD_A) return

        val candidate = abs((previousRawVoltage - rawVoltage) / deltaCurrent)
        if (candidate !in 0.001f..1f) return

        learnedInternalResistanceOhm = if (learnedInternalResistanceOhm <= 0f) {
            candidate
        } else {
            ((learnedInternalResistanceOhm * 0.85f) + (candidate * 0.15f)).coerceAtLeast(0f)
        }
    }

    private fun weightsForCurrent(absCurrent: Float, nowMs: Long): Pair<Float, Float> {
        return when {
            absCurrent < STATIONARY_CURRENT_THRESHOLD_A &&
                stationarySinceMs > 0L &&
                nowMs - stationarySinceMs >= STATIONARY_RECALIBRATION_MS -> 0.30f to 0.70f
            absCurrent <= 2f -> 0.62f to 0.38f
            absCurrent <= 5f -> 0.78f to 0.22f
            absCurrent <= 15f -> 0.92f to 0.08f
            else -> 0.985f to 0.015f
        }
    }

    private fun updateDisplayedSoc(
        previousSoc: Float,
        targetSoc: Float,
        absCurrent: Float,
        current: Float,
        dtSeconds: Double,
        nowMs: Long
    ): Float {
        if (previousSoc.isNaN()) return targetSoc
        val safeDt = dtSeconds.coerceIn(0.0, 5.0).toFloat().takeIf { it > 0f } ?: 0.25f
        val stationaryDurationMs = if (stationarySinceMs > 0L) {
            (nowMs - stationarySinceMs).coerceAtLeast(0L)
        } else {
            0L
        }

        val maxDropPerSecond = when {
            absCurrent >= 35f -> 2.6f
            absCurrent >= 18f -> 1.5f
            else -> 0.9f
        }
        val maxRisePerSecond = when {
            stationaryDurationMs >= STATIONARY_STRONG_RECALIBRATION_MS -> 1.3f
            stationaryDurationMs >= STATIONARY_RECALIBRATION_MS -> 0.9f
            current <= -2f -> 0.6f
            absCurrent <= 2f -> 0.25f
            else -> 0.08f
        }

        val delta = targetSoc - previousSoc
        val next = if (delta < 0f) {
            previousSoc + max(delta, -(maxDropPerSecond * safeDt))
        } else {
            previousSoc + min(delta, maxRisePerSecond * safeDt)
        }
        return next.coerceIn(0f, 100f)
    }

    private fun updateRangeWindow(distanceKm: Double, consumedEnergyWh: Double) {
        val deltaDistanceKm = (distanceKm - lastRangeDistanceKm).coerceAtLeast(0.0)
        val deltaEnergyWh = (consumedEnergyWh - lastRangeEnergyWh).coerceAtLeast(0.0)

        if (deltaDistanceKm > 0.001) {
            rangeWindow.addLast(
                RangeWindowSample(
                    distanceKm = deltaDistanceKm,
                    energyWh = deltaEnergyWh
                )
            )
            rangeWindowDistanceKm += deltaDistanceKm
            rangeWindowEnergyWh += deltaEnergyWh
            trimRangeWindow()
        }

        lastRangeDistanceKm = distanceKm
        lastRangeEnergyWh = consumedEnergyWh
    }

    private fun trimRangeWindow() {
        while (rangeWindowDistanceKm > RANGE_WINDOW_DISTANCE_KM && rangeWindow.isNotEmpty()) {
            val overflowKm = rangeWindowDistanceKm - RANGE_WINDOW_DISTANCE_KM
            val first = rangeWindow.removeFirst()
            if (first.distanceKm <= overflowKm + 1e-6) {
                rangeWindowDistanceKm -= first.distanceKm
                rangeWindowEnergyWh -= first.energyWh
            } else {
                val keepDistanceKm = first.distanceKm - overflowKm
                val keepEnergyWh = if (first.distanceKm > 0.0) {
                    first.energyWh * (keepDistanceKm / first.distanceKm)
                } else {
                    0.0
                }
                rangeWindowDistanceKm -= overflowKm
                rangeWindowEnergyWh -= (first.energyWh - keepEnergyWh)
                rangeWindow.addFirst(
                    RangeWindowSample(
                        distanceKm = keepDistanceKm,
                        energyWh = keepEnergyWh
                    )
                )
            }
        }
    }

    private fun nominalPackVoltage(series: Int): Float {
        return series.coerceAtLeast(1) * 3.7f
    }

    private fun socFromPackVoltage(packVoltage: Float, batterySeries: Int): Float {
        val cellVoltage = if (batterySeries > 0) {
            packVoltage / batterySeries
        } else {
            0f
        }
        val table = listOf(
            4.20f to 100f,
            4.15f to 96f,
            4.10f to 91f,
            4.05f to 85f,
            4.00f to 78f,
            3.95f to 71f,
            3.90f to 63f,
            3.85f to 55f,
            3.80f to 47f,
            3.75f to 39f,
            3.70f to 31f,
            3.65f to 24f,
            3.60f to 17f,
            3.55f to 12f,
            3.50f to 8f,
            3.45f to 5f,
            3.40f to 3f,
            3.30f to 1f,
            3.20f to 0f
        )
        if (cellVoltage >= table.first().first) return table.first().second
        if (cellVoltage <= table.last().first) return table.last().second

        table.zipWithNext().forEach { (high, low) ->
            if (cellVoltage <= high.first && cellVoltage >= low.first) {
                val progress = (cellVoltage - low.first) / (high.first - low.first)
                return low.second + ((high.second - low.second) * progress)
            }
        }
        return 0f
    }
}
