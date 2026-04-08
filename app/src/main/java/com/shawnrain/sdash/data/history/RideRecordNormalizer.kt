package com.shawnrain.sdash.data.history

import com.shawnrain.sdash.debug.AppLogger
import kotlin.math.abs

class RideRecordNormalizer {
    companion object {
        private const val TAG = "RideRecordNormalizer"
        private const val MIN_VALID_DISTANCE_METERS = 3f
        private const val MIN_SPEED_FOR_DISTANCE_KMH = 0.8f
        private const val MAX_DT_SECONDS = 3f
        private const val ENERGY_ABSOLUTE_TOLERANCE_WH = 20f
        private const val ENERGY_INFLATION_RATIO = 2.5f
    }

    /**
     * Normalizes a RideHistoryRecord by re-deriving summary statistics from its samples.
     * Returns a new record if changes were made, or the original record if it's already sane.
     */
    fun normalize(record: RideHistoryRecord): RideHistoryRecord {
        val samples = record.samples
        if (samples.isEmpty()) return record

        val rebuiltSamples = normalizeSampleDistance(samples)
        val lastSample = rebuiltSamples.last()
        val sampleMaxSpeedKmh = rebuiltSamples.maxOfOrNull { it.speedKmH } ?: 0f
        val calculatedMaxSpeedKmh = maxOf(record.maxSpeedKmh, sampleMaxSpeedKmh)

        val sampleMaxDistance = lastSample.distanceMeters.coerceAtLeast(0f)
        val calculatedDistanceMeters = when {
            sampleMaxDistance > MIN_VALID_DISTANCE_METERS -> sampleMaxDistance
            record.distanceMeters > MIN_VALID_DISTANCE_METERS -> record.distanceMeters
            else -> 0f
        }

        val durationSec = record.durationMs / 1000f
        val calculatedAvgSpeedKmh = if (durationSec > 0f && calculatedDistanceMeters > 0f) {
            (calculatedDistanceMeters / 1000f) / (durationSec / 3600f)
        } else {
            maxOf(record.avgSpeedKmh, 0f)
        }

        val sampleTotalEnergyWh = rebuiltSamples.maxOfOrNull { it.totalEnergyWh } ?: 0f
        val sampleTractionEnergyWh = rebuiltSamples.maxOfOrNull { it.tractionEnergyWh } ?: 0f
        val sampleRegenEnergyWh = rebuiltSamples.maxOfOrNull {
            maxOf(it.regenEnergyWh, it.recoveredEnergyWh)
        } ?: 0f
        val sampleNetEnergyWh = (sampleTractionEnergyWh - sampleRegenEnergyWh).coerceAtLeast(0f)
        val shouldTrustSampleNetEnergy = sampleNetEnergyWh > 0.5f && (
            sampleTotalEnergyWh <= 0.5f ||
                sampleTotalEnergyWh > maxOf(
                    sampleNetEnergyWh * ENERGY_INFLATION_RATIO,
                    sampleTractionEnergyWh + ENERGY_ABSOLUTE_TOLERANCE_WH
                )
        )
        val calculatedTractionEnergyWh = when {
            sampleTractionEnergyWh > 0.5f -> sampleTractionEnergyWh
            else -> maxOf(record.tractionEnergyWh, sampleTotalEnergyWh + sampleRegenEnergyWh)
        }
        val calculatedRegenEnergyWh = when {
            sampleRegenEnergyWh > 0.5f -> sampleRegenEnergyWh
            else -> record.regenEnergyWh.coerceAtLeast(0f)
        }
        val calculatedTotalEnergyWh = when {
            shouldTrustSampleNetEnergy -> sampleNetEnergyWh
            sampleTotalEnergyWh > 0.5f -> sampleTotalEnergyWh
            calculatedTractionEnergyWh > 0.5f -> (calculatedTractionEnergyWh - calculatedRegenEnergyWh).coerceAtLeast(0f)
            else -> record.totalEnergyWh.coerceAtLeast(0f)
        }

        val distanceKm = calculatedDistanceMeters / 1000f
        val calculatedAvgEfficiencyWhKm = if (distanceKm > 0.05f) {
            calculatedTotalEnergyWh / distanceKm
        } else {
            maxOf(record.avgEfficiencyWhKm, record.avgNetEfficiencyWhKm)
        }
        val calculatedAvgTractionEfficiencyWhKm = if (distanceKm > 0.05f) {
            calculatedTractionEnergyWh / distanceKm
        } else {
            record.avgTractionEfficiencyWhKm
        }

        // Check if any significant change happened
        val hasChanges = abs(record.maxSpeedKmh - calculatedMaxSpeedKmh) > 0.5f ||
                abs(record.distanceMeters - calculatedDistanceMeters) > 1.0f ||
                abs(record.totalEnergyWh - calculatedTotalEnergyWh) > 0.1f ||
                abs(record.tractionEnergyWh - calculatedTractionEnergyWh) > 0.1f ||
                abs(record.regenEnergyWh - calculatedRegenEnergyWh) > 0.1f ||
                abs(record.avgNetEfficiencyWhKm - calculatedAvgEfficiencyWhKm) > 0.1f ||
                record.maxSpeedKmh < 0.1f && calculatedMaxSpeedKmh > 0.1f

        if (!hasChanges) return record

        AppLogger.i(TAG, "Normalizing ride ${record.id}: dist=${record.distanceMeters}->${calculatedDistanceMeters}, maxSpeed=${record.maxSpeedKmh}->${calculatedMaxSpeedKmh}")

        return record.copy(
            distanceMeters = calculatedDistanceMeters,
            maxSpeedKmh = calculatedMaxSpeedKmh,
            avgSpeedKmh = calculatedAvgSpeedKmh,
            totalEnergyWh = calculatedTotalEnergyWh,
            tractionEnergyWh = calculatedTractionEnergyWh,
            regenEnergyWh = calculatedRegenEnergyWh,
            avgEfficiencyWhKm = calculatedAvgEfficiencyWhKm,
            avgNetEfficiencyWhKm = calculatedAvgEfficiencyWhKm,
            avgTractionEfficiencyWhKm = calculatedAvgTractionEfficiencyWhKm,
            samples = rebuiltSamples
        )
    }

    /**
     * Specifically fixes records where max speed is 0 but distance/avg speed are non-zero.
     */
    fun isBroken(record: RideHistoryRecord): Boolean {
        if (record.samples.isEmpty()) return false
        
        val hasMetricsButNoMaxSpeed = record.avgSpeedKmh > 1f && record.maxSpeedKmh < 0.1f
        val hasSamplesButNoDistance = record.samples.any { it.speedKmH > 2f } && record.distanceMeters < 1f
        val hasEnergyMismatch = record.samples.lastOrNull()?.totalEnergyWh?.let { sEnergy ->
            sEnergy > 1f && record.totalEnergyWh < 0.1f
        } ?: false
        val hasBrokenEfficiency = record.distanceMeters > 30f &&
                (record.avgNetEfficiencyWhKm <= 0.1f || record.avgEfficiencyWhKm <= 0.1f)

        return hasMetricsButNoMaxSpeed || hasSamplesButNoDistance || hasEnergyMismatch || hasBrokenEfficiency
    }

    private fun normalizeSampleDistance(samples: List<RideMetricSample>): List<RideMetricSample> {
        if (samples.size < 2) return samples
        var integratedDistanceMeters = 0f
        return buildList(samples.size) {
            add(samples.first().copy(distanceMeters = maxOf(0f, samples.first().distanceMeters)))
            for (index in 1 until samples.size) {
                val previous = samples[index - 1]
                val current = samples[index]
                val deltaElapsedSeconds = (current.elapsedMs - previous.elapsedMs).toFloat() / 1000f
                val deltaTimestampSeconds = (current.timestampMs - previous.timestampMs).toFloat() / 1000f
                val dtSeconds = when {
                    deltaElapsedSeconds > 0f -> deltaElapsedSeconds
                    deltaTimestampSeconds > 0f -> deltaTimestampSeconds
                    else -> 0f
                }.coerceIn(0f, MAX_DT_SECONDS)
                val avgSpeedKmh = ((previous.speedKmH + current.speedKmH) * 0.5f).coerceAtLeast(0f)
                if (dtSeconds > 0f && avgSpeedKmh >= MIN_SPEED_FOR_DISTANCE_KMH) {
                    integratedDistanceMeters += (avgSpeedKmh / 3.6f) * dtSeconds
                }
                add(current.copy(distanceMeters = integratedDistanceMeters))
            }
        }
    }
}
