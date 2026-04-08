package com.shawnrain.sdash.ui.poster

import com.shawnrain.sdash.data.RideSession
import com.shawnrain.sdash.data.history.RideHistoryRecord
import com.shawnrain.sdash.data.history.RideTrackPoint
import com.shawnrain.sdash.data.speedtest.SpeedTestRecord
import com.shawnrain.sdash.ui.poster.model.PosterAspectRatio
import com.shawnrain.sdash.ui.poster.model.PosterFooter
import com.shawnrain.sdash.ui.poster.model.PosterMetric
import com.shawnrain.sdash.ui.poster.model.PosterRenderOptions
import com.shawnrain.sdash.ui.poster.model.PosterSpec
import com.shawnrain.sdash.ui.poster.model.PosterTrackData
import com.shawnrain.sdash.ui.poster.template.PerformanceDarkTemplate
import com.shawnrain.sdash.ui.poster.template.PosterTemplate
import com.shawnrain.sdash.ui.poster.template.RideMinimalTemplate
import com.shawnrain.sdash.ui.poster.template.TrackFocusTemplate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class PosterFactory {
    fun buildRidePosterSpec(
        record: RideHistoryRecord,
        aspectRatio: PosterAspectRatio = PosterAspectRatio.STORY_9_16,
        template: PosterTemplate = defaultRideTemplate(record)
    ): PosterSpec {
        val track = buildTrackData(
            title = if (record.trackPoints.size >= 2) "路线轨迹" else "路线信息",
            subtitle = if (record.trackPoints.size >= 2) {
                "共记录 ${record.trackPoints.size} 个轨迹点"
            } else {
                "路线轨迹不足，可能未开启 GPS"
            },
            points = record.trackPoints
        )
        val netEfficiency = when {
            record.avgNetEfficiencyWhKm > 0.01f -> record.avgNetEfficiencyWhKm
            else -> record.avgEfficiencyWhKm
        }
        val metrics = listOf(
            PosterMetric("duration", "行程时长", formatMinutes(record.durationMs), "min", priority = 3),
            PosterMetric("max_speed", "最高速度", format1(record.maxSpeedKmh), "km/h", priority = 5),
            PosterMetric("avg_speed", "平均速度", format1(record.avgSpeedKmh), "km/h", priority = 4),
            PosterMetric("peak_power", "峰值功率", format1(record.peakPowerKw), "kW", priority = 4),
            PosterMetric("energy", "净耗电量", format1(record.totalEnergyWh), "Wh", priority = 5),
            PosterMetric("regen", "回收能量", format1(record.regenEnergyWh), "Wh", priority = 2),
            PosterMetric("efficiency", "平均能耗", format1(netEfficiency), "Wh/km", priority = 5),
            PosterMetric("traction_efficiency", "驱动能耗", format1(record.avgTractionEfficiencyWhKm), "Wh/km", priority = 1)
        )

        return PosterSpec(
            aspectRatio = aspectRatio,
            theme = template.theme,
            title = record.title.ifBlank { "行程记录" },
            subtitle = buildRideSubtitle(record),
            heroMetric = PosterMetric("distance", "本次里程", format1(record.distanceMeters / 1000f), "km", priority = 10),
            metrics = metrics,
            track = track,
            footer = PosterFooter(
                watermark = if (record.trackPoints.size >= 2) "Track • Ride • SmartDash" else "Ride • SmartDash"
            ),
            options = PosterRenderOptions(
                showTrack = true,
                showWatermark = true,
                emphasizeTrack = template.showTrackFirst,
                compactMetrics = template.compactMetrics
            )
        )
    }

    fun buildSpeedTestPosterSpec(
        record: SpeedTestRecord,
        aspectRatio: PosterAspectRatio = PosterAspectRatio.STORY_9_16,
        template: PosterTemplate = PerformanceDarkTemplate
    ): PosterSpec {
        val metrics = listOf(
            PosterMetric("target_speed", "目标速度", format0(record.targetSpeedKmh), "km/h", priority = 4),
            PosterMetric("max_speed", "最高速度", format1(record.maxSpeedKmh), "km/h", priority = 5),
            PosterMetric("peak_power", "峰值功率", format1(record.peakPowerKw), "kW", priority = 5),
            PosterMetric("peak_current", "峰值母线电流", format1(record.peakBusCurrentA), "A", priority = 4),
            PosterMetric("min_voltage", "最低电压", format1(record.minVoltage), "V", priority = 3),
            PosterMetric("distance", "冲刺距离", format1(record.distanceMeters), "m", priority = 3)
        )
        return PosterSpec(
            aspectRatio = aspectRatio,
            theme = template.theme,
            title = record.label,
            subtitle = formatTimestamp(record.timestampMs),
            heroMetric = PosterMetric("time", "加速成绩", formatSeconds(record.timeMs), "秒", priority = 10),
            metrics = metrics,
            track = buildTrackData(
                title = "冲刺轨迹",
                subtitle = if (record.trackPoints.size >= 2) "共记录 ${record.trackPoints.size} 个轨迹点" else "本次未记录到足够轨迹点",
                points = record.trackPoints.map { RideTrackPoint(it.latitude, it.longitude) }
            ),
            footer = PosterFooter(watermark = "Performance • SmartDash"),
            options = PosterRenderOptions(
                showTrack = true,
                showWatermark = true,
                emphasizeTrack = template.showTrackFirst,
                compactMetrics = template.compactMetrics
            )
        )
    }

    fun buildRidePosterSpec(session: RideSession): PosterSpec {
        val record = RideHistoryRecord(
            id = session.date,
            title = session.date,
            startedAtMs = 0L,
            endedAtMs = 0L,
            durationMs = session.durationMin * 60_000L,
            distanceMeters = session.distanceKm * 1000f,
            maxSpeedKmh = session.maxSpeed,
            avgSpeedKmh = session.avgSpeed,
            peakPowerKw = 0f,
            totalEnergyWh = session.totalWh,
            avgEfficiencyWhKm = session.avgEfficiency,
            avgNetEfficiencyWhKm = session.avgEfficiency,
            avgTractionEfficiencyWhKm = 0f,
            trackPoints = emptyList(),
            samples = emptyList()
        )
        return buildRidePosterSpec(record)
    }

    private fun defaultRideTemplate(record: RideHistoryRecord): PosterTemplate {
        return when {
            record.trackPoints.size >= 8 -> TrackFocusTemplate
            record.distanceMeters >= 1000f -> RideMinimalTemplate
            else -> PerformanceDarkTemplate
        }
    }

    private fun buildRideSubtitle(record: RideHistoryRecord): String {
        val startText = formatTimestamp(record.startedAtMs)
        return if (startText.isNotBlank()) {
            "$startText • ${max(record.trackPoints.size, 0)} 个采样轨迹点"
        } else {
            "${formatMinutes(record.durationMs)} min • ${record.trackPoints.size} 个采样轨迹点"
        }
    }

    private fun buildTrackData(
        title: String,
        subtitle: String,
        points: List<RideTrackPoint>
    ): PosterTrackData = PosterTrackData(
        title = title,
        subtitle = subtitle,
        points = points
    )

    private fun format1(value: Number): String = "%.1f".format(Locale.getDefault(), value.toDouble())

    private fun format0(value: Number): String = "%.0f".format(Locale.getDefault(), value.toDouble())

    private fun formatSeconds(durationMs: Long): String =
        "%.2f".format(Locale.getDefault(), durationMs / 1000f)

    private fun formatMinutes(durationMs: Long): String =
        "%.1f".format(Locale.getDefault(), durationMs / 60000f)

    private fun formatTimestamp(timestampMs: Long): String {
        if (timestampMs <= 0L) return ""
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestampMs))
    }
}
