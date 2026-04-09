package com.shawnrain.sdash.data.history

private const val MIN_VALID_SUMMARY_DISTANCE_METERS = 3f

data class RideSummaryStats(
    val distanceMeters: Float,
    val maxSpeedKmh: Float,
    val avgSpeedKmh: Float,
    val maxUphillGradePercent: Float,
    val maxDownhillGradePercent: Float,
    val avgSignedGradePercent: Float,
    val maxAltitudeMeters: Float,
    val minAltitudeMeters: Float,
    val avgAltitudeMeters: Float
)

fun computeRideSummaryStats(record: RideHistoryRecord): RideSummaryStats {
    val sampleDistanceMeters = record.samples.lastOrNull()?.distanceMeters
        ?.takeIf { it.isFinite() && it > MIN_VALID_SUMMARY_DISTANCE_METERS }
        ?: 0f
    val distanceMeters = sampleDistanceMeters
        .takeIf { it > 0f }
        ?: record.distanceMeters.coerceAtLeast(0f)

    val sampleMaxSpeedKmh = record.samples
        .asSequence()
        .map { it.speedKmH }
        .filter { it.isFinite() && it >= 0f && it <= 300f }
        .maxOrNull()
        ?: 0f
    val maxSpeedKmh = sampleMaxSpeedKmh
        .takeIf { it > 0f }
        ?: record.maxSpeedKmh.coerceAtLeast(0f)

    val avgSpeedKmh = if (record.durationMs > 0L && distanceMeters > 1f) {
        ((distanceMeters / 1000f) / (record.durationMs.toFloat() / 3_600_000f)).coerceAtLeast(0f)
    } else {
        record.avgSpeedKmh.coerceAtLeast(0f)
    }

    val gradeValues = record.samples
        .asSequence()
        .map { it.gradePercent }
        .filter { it.isFinite() && it in -35f..35f }
        .toList()
    val maxUphillGradePercent = gradeValues.filter { it > 0f }.maxOrNull() ?: 0f
    val maxDownhillGradePercent = gradeValues.filter { it < 0f }.minOrNull() ?: 0f
    val avgSignedGradePercent = if (gradeValues.isNotEmpty()) {
        gradeValues.sum() / gradeValues.size.toFloat()
    } else {
        0f
    }

    val altitudeValues = record.samples
        .asSequence()
        .mapNotNull { it.altitudeMeters?.toFloat() }
        .filter { it.isFinite() }
        .toList()
    val maxAltitudeMeters = altitudeValues.maxOrNull() ?: 0f
    val minAltitudeMeters = altitudeValues.minOrNull() ?: 0f
    val avgAltitudeMeters = if (altitudeValues.isNotEmpty()) {
        altitudeValues.sum() / altitudeValues.size.toFloat()
    } else {
        0f
    }

    return RideSummaryStats(
        distanceMeters = distanceMeters,
        maxSpeedKmh = maxSpeedKmh,
        avgSpeedKmh = avgSpeedKmh,
        maxUphillGradePercent = maxUphillGradePercent,
        maxDownhillGradePercent = maxDownhillGradePercent,
        avgSignedGradePercent = avgSignedGradePercent,
        maxAltitudeMeters = maxAltitudeMeters,
        minAltitudeMeters = minAltitudeMeters,
        avgAltitudeMeters = avgAltitudeMeters
    )
}
