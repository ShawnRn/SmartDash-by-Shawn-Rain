package com.shawnrain.habe.ui.poster

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.text.TextPaint
import com.shawnrain.habe.data.RideSession
import com.shawnrain.habe.data.history.RideHistoryRecord
import com.shawnrain.habe.data.history.RideTrackPoint
import com.shawnrain.habe.data.speedtest.SpeedTestRecord
import com.shawnrain.habe.data.speedtest.SpeedTestTrackPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PosterRenderer(private val context: Context) {
    private companion object {
        const val POSTER_WIDTH = 1080f
        const val POSTER_HEIGHT = 1920f
        const val SIDE_MARGIN = 60f
        const val CONTENT_WIDTH = POSTER_WIDTH - SIDE_MARGIN * 2f
        const val GRID_GUTTER = 20f
        const val SMALL_CARD_WIDTH = (CONTENT_WIDTH - GRID_GUTTER) / 2f
        const val HERO_HEIGHT = 360f
        const val SMALL_CARD_HEIGHT = 166f
        const val SMALL_CARD_ROW_GAP = 20f
        const val HERO_TOP = 244f
        const val STATS_TOP = HERO_TOP + HERO_HEIGHT + 32f
        const val ROUTE_TOP = STATS_TOP + SMALL_CARD_HEIGHT * 3f + SMALL_CARD_ROW_GAP * 2f + 32f
    }

    fun render(session: RideSession): Bitmap {
        val record = RideHistoryRecord(
            id = session.date,
            title = session.date,
            startedAtMs = 0L,
            endedAtMs = 0L,
            durationMs = session.durationMin * 60_000L,
            distanceMeters = (session.distanceKm * 1000.0).toFloat(),
            maxSpeedKmh = session.maxSpeed,
            avgSpeedKmh = session.avgSpeed,
            peakPowerKw = 0f,
            totalEnergyWh = session.totalWh,
            avgEfficiencyWhKm = session.avgEfficiency,
            trackPoints = emptyList(),
            samples = emptyList()
        )
        return renderRideHistory(record)
    }

    fun renderSpeedTest(record: SpeedTestRecord): Bitmap {
        return createPosterCanvas("HABE PERFORMANCE", formatTime(record.timestampMs)) { canvas, width, height ->
            drawHeroMetric(
                canvas = canvas,
                label = record.label,
                value = formatSeconds(record.timeMs),
                unit = "秒",
                top = 280f
            )
            drawStatGrid(
                canvas = canvas,
                top = 760f,
                stats = listOf(
                    Triple("目标速度", "${record.targetSpeedKmh.toInt()}", "km/h"),
                    Triple("最高速度", formatFloat(record.maxSpeedKmh), "km/h"),
                    Triple("峰值功率", formatFloat(record.peakPowerKw), "kW"),
                    Triple("峰值母线电流", formatFloat(record.peakBusCurrentA), "A"),
                    Triple("最低电压", formatFloat(record.minVoltage), "V"),
                    Triple("冲刺距离", formatFloat(record.distanceMeters), "m")
                )
            )
            drawRouteCard(
                canvas = canvas,
                title = "冲刺轨迹",
                subtitle = if (record.trackPoints.size >= 2) "共记录 ${record.trackPoints.size} 个轨迹点" else "本次未记录到足够轨迹点",
                top = 1370f,
                height = 360f,
                route = record.trackPoints.map { RideTrackPoint(it.latitude, it.longitude) }
            )
            drawFooter(canvas, width, height)
        }
    }

    fun renderRideHistory(record: RideHistoryRecord): Bitmap {
        return createPosterCanvas("HABE RIDE LOG", formatTime(record.startedAtMs)) { canvas, width, height ->
            drawHeroMetric(
                canvas = canvas,
                label = record.title.ifBlank { "行程记录" },
                value = formatFloat(record.distanceMeters / 1000f),
                unit = "km",
                top = 280f
            )
            drawStatGrid(
                canvas = canvas,
                top = 760f,
                stats = listOf(
                    Triple("行程时长", formatDurationMinutes(record.durationMs), "min"),
                    Triple("最高速度", formatFloat(record.maxSpeedKmh), "km/h"),
                    Triple("平均速度", formatFloat(record.avgSpeedKmh), "km/h"),
                    Triple("峰值功率", formatFloat(record.peakPowerKw), "kW"),
                    Triple("耗电量", formatFloat(record.totalEnergyWh), "Wh"),
                    Triple("平均能耗", formatFloat(record.avgEfficiencyWhKm), "Wh/km")
                )
            )
            drawRouteCard(
                canvas = canvas,
                title = "路线轨迹",
                subtitle = if (record.trackPoints.size >= 2) "共记录 ${record.trackPoints.size} 个轨迹点" else "路线轨迹不足，可能未开启 GPS",
                top = 1370f,
                height = 360f,
                route = record.trackPoints
            )
            drawFooter(canvas, width, height)
        }
    }

    private fun createPosterCanvas(
        title: String,
        subtitle: String,
        content: (Canvas, Int, Int) -> Unit
    ): Bitmap {
        val width = POSTER_WIDTH.toInt()
        val height = POSTER_HEIGHT.toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                intArrayOf(Color.parseColor("#0B1020"), Color.parseColor("#07090F"), Color.parseColor("#121726")),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.82f,
                height * 0.18f,
                width * 0.48f,
                intArrayOf(Color.parseColor("#4D9CCBFF"), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width * 0.82f, height * 0.18f, width * 0.48f, glowPaint)

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 64f
            isFakeBoldText = true
        }
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A7B2C8")
            textSize = 34f
        }
        canvas.drawText(title, SIDE_MARGIN, 132f, titlePaint)
        canvas.drawText(subtitle, SIDE_MARGIN, 186f, subtitlePaint)
        content(canvas, width, height)
        return bitmap
    }

    private fun drawHeroMetric(
        canvas: Canvas,
        label: String,
        value: String,
        unit: String,
        top: Float
    ) {
        val card = RectF(SIDE_MARGIN, top, POSTER_WIDTH - SIDE_MARGIN, top + HERO_HEIGHT)
        drawGlassCard(canvas, card)

        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A7B2C8")
            textSize = 38f
        }
        val valuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 176f
            isFakeBoldText = true
        }
        val unitPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#DCE6FF")
            textSize = 52f
            isFakeBoldText = true
        }
        val valueBaseline = card.top + 234f
        canvas.drawText(label, card.left + 48f, card.top + 82f, labelPaint)
        drawMetricValue(
            canvas = canvas,
            value = value,
            unit = unit,
            startX = card.left + 48f,
            baselineY = valueBaseline,
            valuePaint = valuePaint,
            unitPaint = unitPaint,
            unitGap = 18f
        )
    }

    private fun drawStatGrid(
        canvas: Canvas,
        top: Float,
        stats: List<Triple<String, String, String>>
    ) {
        stats.chunked(2).forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, stat ->
                val left = SIDE_MARGIN + columnIndex * (SMALL_CARD_WIDTH + GRID_GUTTER)
                val rowTop = top + rowIndex * (SMALL_CARD_HEIGHT + SMALL_CARD_ROW_GAP)
                val card = RectF(left, rowTop, left + SMALL_CARD_WIDTH, rowTop + SMALL_CARD_HEIGHT)
                drawGlassCard(canvas, card)
                val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#92A0B8")
                    textSize = 30f
                }
                val valuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 62f
                    isFakeBoldText = true
                }
                val unitPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#A7B2C8")
                    textSize = 28f
                }
                canvas.drawText(stat.first, card.left + 34f, card.top + 52f, labelPaint)
                drawMetricValue(
                    canvas = canvas,
                    value = stat.second,
                    unit = stat.third,
                    startX = card.left + 34f,
                    baselineY = card.top + 116f,
                    valuePaint = valuePaint,
                    unitPaint = unitPaint,
                    unitGap = 14f
                )
            }
        }
    }

    private fun drawRouteCard(
        canvas: Canvas,
        title: String,
        subtitle: String,
        top: Float,
        height: Float,
        route: List<RideTrackPoint>
    ) {
        val card = RectF(SIDE_MARGIN, top, POSTER_WIDTH - SIDE_MARGIN, top + height)
        drawGlassCard(canvas, card)
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 40f
            isFakeBoldText = true
        }
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#92A0B8")
            textSize = 28f
        }
        canvas.drawText(title, card.left + 34f, card.top + 58f, titlePaint)
        canvas.drawText(subtitle, card.left + 34f, card.top + 100f, subtitlePaint)
        val routeRect = RectF(card.left + 28f, card.top + 130f, card.right - 28f, card.bottom - 28f)
        drawRoute(canvas, routeRect, route)
    }

    private fun drawRoute(canvas: Canvas, rect: RectF, route: List<RideTrackPoint>) {
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#182030")
        }
        canvas.drawRoundRect(rect, 42f, 42f, backgroundPaint)
        if (route.size < 2) {
            val hintPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#8C97AD")
                textSize = 30f
            }
            canvas.drawText("等待更完整的 GPS 轨迹", rect.left + 34f, rect.centerY(), hintPaint)
            return
        }

        val minLat = route.minOf { it.latitude }
        val maxLat = route.maxOf { it.latitude }
        val minLon = route.minOf { it.longitude }
        val maxLon = route.maxOf { it.longitude }
        val latSpan = (maxLat - minLat).takeIf { it > 0.000001 } ?: 0.000001
        val lonSpan = (maxLon - minLon).takeIf { it > 0.000001 } ?: 0.000001

        val path = Path()
        route.forEachIndexed { index, point ->
            val x = rect.left + ((point.longitude - minLon) / lonSpan).toFloat() * rect.width()
            val y = rect.bottom - ((point.latitude - minLat) / latSpan).toFloat() * rect.height()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        val routePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A7C7FF")
            style = Paint.Style.STROKE
            strokeWidth = 10f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(path, routePaint)

        val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val start = route.first()
        val end = route.last()
        val startX = rect.left + ((start.longitude - minLon) / lonSpan).toFloat() * rect.width()
        val startY = rect.bottom - ((start.latitude - minLat) / latSpan).toFloat() * rect.height()
        val endX = rect.left + ((end.longitude - minLon) / lonSpan).toFloat() * rect.width()
        val endY = rect.bottom - ((end.latitude - minLat) / latSpan).toFloat() * rect.height()
        pointPaint.color = Color.parseColor("#7EE787")
        canvas.drawCircle(startX, startY, 12f, pointPaint)
        pointPaint.color = Color.parseColor("#FFB3B3")
        canvas.drawCircle(endX, endY, 12f, pointPaint)
    }

    private fun drawGlassCard(canvas: Canvas, rect: RectF) {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E283A")
            alpha = 232
        }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3B4964")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(rect, 54f, 54f, fill)
        canvas.drawRoundRect(rect, 54f, 54f, stroke)
    }

    private fun drawFooter(canvas: Canvas, width: Int, height: Int) {
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7D879A")
            textSize = 28f
        }
        val text = "Made with Habe Dashboard"
        canvas.drawText(text, (width - footerPaint.measureText(text)) / 2f, height - 86f, footerPaint)
    }

    private fun drawMetricValue(
        canvas: Canvas,
        value: String,
        unit: String,
        startX: Float,
        baselineY: Float,
        valuePaint: TextPaint,
        unitPaint: TextPaint,
        unitGap: Float
    ) {
        canvas.drawText(value, startX, baselineY, valuePaint)
        if (unit.isBlank()) return
        val adjustedBaseline = baselineY - (valuePaint.fontMetrics.descent - unitPaint.fontMetrics.descent)
        canvas.drawText(unit, startX + valuePaint.measureText(value) + unitGap, adjustedBaseline, unitPaint)
    }

    private fun formatSeconds(durationMs: Long): String {
        return String.format(Locale.getDefault(), "%.2f", durationMs / 1000f)
    }

    private fun formatDurationMinutes(durationMs: Long): String {
        return String.format(Locale.getDefault(), "%.1f", durationMs / 60000f)
    }

    private fun formatFloat(value: Float): String {
        return String.format(Locale.getDefault(), "%.1f", value)
    }

    private fun formatTime(timestampMs: Long): String {
        if (timestampMs <= 0L) return ""
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestampMs))
    }
}
