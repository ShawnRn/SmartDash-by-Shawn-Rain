package com.shawnrain.sdash.data.history

import java.security.MessageDigest

fun computeRideDetailFingerprint(
    trackPoints: List<RideTrackPoint>,
    samples: List<RideMetricSample>
): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val payload = buildString {
        append("tracks:")
        trackPoints.forEach { point ->
            append(point.latitude)
            append(',')
            append(point.longitude)
            append(';')
        }
        append("|samples:")
        samples.forEach { sample ->
            RideMetricSampleSchema.toValueMap(sample).forEach { (key, value) ->
                append(key)
                append('=')
                append(value ?: "null")
                append('|')
            }
            append(';')
        }
    }
    return digest.digest(payload.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
