package com.shawnrain.sdash.ui.poster

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.shawnrain.sdash.data.history.RideTrackPoint
import com.shawnrain.sdash.ui.poster.model.PosterAspectRatio
import com.shawnrain.sdash.ui.poster.model.PosterMetric
import com.shawnrain.sdash.ui.poster.model.PosterSpec
import kotlin.math.max
import kotlin.math.min

class PosterRendererV2(
    private val context: Context
) {
    fun render(spec: PosterSpec): Bitmap {
        val width = spec.aspectRatio.width.toFloat()
        val height = spec.aspectRatio.height.toFloat()
        val shortSide = min(width, height)
        val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawBackground(canvas, spec, width, height)

        val horizontalPadding = width * 0.052f
        val topPadding = height * 0.035f
        val bottomPadding = height * 0.035f
        val sectionGap = shortSide * 0.022f
        val footerHeight = max(shortSide * 0.05f, 34f)
        val contentRect = RectF(
            horizontalPadding,
            topPadding,
            width - horizontalPadding,
            height - bottomPadding
        )

        val headerTitleSize = fitTextSize(
            text = spec.title,
            maxWidth = contentRect.width(),
            initialSize = width * 0.058f,
            minSize = width * 0.038f,
            maxLines = 2,
            isBold = true
        )
        val subtitleText = spec.subtitle.orEmpty()
        val subtitleSize = width * 0.022f
        val headerHeight = measureHeaderHeight(
            title = spec.title,
            subtitle = subtitleText,
            width = contentRect.width(),
            badgeSize = width * 0.016f,
            titleSize = headerTitleSize,
            subtitleSize = subtitleSize
        )
        val headerRect = RectF(
            contentRect.left,
            contentRect.top,
            contentRect.right,
            contentRect.top + headerHeight
        )
        drawHeader(canvas, spec, headerRect, width)

        val bodyAvailableHeight = contentRect.bottom - headerRect.bottom - sectionGap - footerHeight - sectionGap
        val heroHeight = computeHeroHeight(spec.aspectRatio, bodyAvailableHeight, shortSide)
        val heroRect = RectF(
            contentRect.left,
            headerRect.bottom + sectionGap,
            contentRect.right,
            headerRect.bottom + sectionGap + heroHeight
        )
        drawHero(canvas, spec, heroRect, shortSide)

        val bodyRect = RectF(
            contentRect.left,
            heroRect.bottom + sectionGap,
            contentRect.right,
            contentRect.bottom - footerHeight - sectionGap
        )
        if (bodyRect.height() > shortSide * 0.14f) {
            drawBody(canvas, spec, bodyRect, spec.aspectRatio, shortSide)
        }

        val footerRect = RectF(
            contentRect.left,
            contentRect.bottom - footerHeight,
            contentRect.right,
            contentRect.bottom
        )
        drawFooter(canvas, spec, footerRect, width)
        return bitmap
    }

    private fun drawBackground(canvas: Canvas, spec: PosterSpec, width: Float, height: Float) {
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                width,
                height,
                spec.theme.backgroundTop,
                spec.theme.backgroundBottom,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, background)

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = android.graphics.RadialGradient(
                width * 0.82f,
                height * 0.14f,
                width * 0.58f,
                intArrayOf(adjustAlpha(spec.theme.accent, 0.26f), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width * 0.82f, height * 0.14f, width * 0.58f, glow)
    }

    private fun drawHeader(canvas: Canvas, spec: PosterSpec, rect: RectF, width: Float) {
        val badgePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.accent
            textSize = width * 0.016f
            isFakeBoldText = true
            letterSpacing = 0.08f
        }
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.textPrimary
            textSize = fitTextSize(
                text = spec.title,
                maxWidth = rect.width(),
                initialSize = width * 0.058f,
                minSize = width * 0.038f,
                maxLines = 2,
                isBold = true
            )
            isFakeBoldText = true
        }
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.textSecondary
            textSize = width * 0.022f
        }

        val badgeTop = rect.top
        val badgeHeight = drawParagraph(
            canvas = canvas,
            text = "SMARTDASH POSTER",
            left = rect.left,
            top = badgeTop,
            width = rect.width(),
            paint = badgePaint,
            maxLines = 1
        )
        val titleTop = badgeTop + badgeHeight + width * 0.012f
        val titleHeight = drawParagraph(
            canvas = canvas,
            text = spec.title,
            left = rect.left,
            top = titleTop,
            width = rect.width(),
            paint = titlePaint,
            maxLines = 2,
            lineSpacingExtra = width * 0.004f
        )

        spec.subtitle?.takeIf { it.isNotBlank() }?.let {
            drawParagraph(
                canvas = canvas,
                text = it,
                left = rect.left,
                top = titleTop + titleHeight + width * 0.008f,
                width = rect.width(),
                paint = subtitlePaint,
                maxLines = 2,
                lineSpacingExtra = width * 0.0025f
            )
        }
    }

    private fun drawHero(canvas: Canvas, spec: PosterSpec, rect: RectF, shortSide: Float) {
        drawCard(canvas, spec, rect, shortSide, strong = true)

        val inset = rect.width() * 0.07f
        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.textSecondary
            textSize = fitSingleLineTextSize(
                text = spec.heroMetric.label,
                maxWidth = rect.width() - inset * 2f,
                initialSize = rect.height() * 0.14f,
                minSize = rect.height() * 0.1f,
                isBold = false
            )
        }
        val valuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.textPrimary
            textSize = min(rect.height() * 0.44f, rect.width() * 0.19f)
            isFakeBoldText = true
        }
        val unitPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.accent
            textSize = rect.height() * 0.16f
            isFakeBoldText = true
        }

        val labelHeight = drawParagraph(
            canvas = canvas,
            text = spec.heroMetric.label,
            left = rect.left + inset,
            top = rect.top + rect.height() * 0.12f,
            width = rect.width() - inset * 2f,
            paint = labelPaint,
            maxLines = 1
        )

        val valueTop = rect.top + rect.height() * 0.12f + labelHeight + rect.height() * 0.08f
        drawMetricValueBlock(
            canvas = canvas,
            metric = spec.heroMetric,
            left = rect.left + inset,
            right = rect.right - inset,
            top = valueTop,
            bottom = rect.bottom - rect.height() * 0.12f,
            valuePaint = valuePaint,
            unitPaint = unitPaint,
            gap = rect.width() * 0.016f
        )
    }

    private fun drawBody(
        canvas: Canvas,
        spec: PosterSpec,
        rect: RectF,
        ratio: PosterAspectRatio,
        shortSide: Float
    ) {
        val hasTrack = spec.track != null && spec.options.showTrack
        if (!hasTrack) {
            drawMetricGrid(canvas, spec, rect, shortSide, compact = spec.options.compactMetrics)
            return
        }

        val gap = shortSide * 0.018f
        val trackFraction = when (ratio) {
            PosterAspectRatio.STORY_9_16 -> if (spec.options.emphasizeTrack) 0.54f else 0.44f
            PosterAspectRatio.PORTRAIT_4_5 -> if (spec.options.emphasizeTrack) 0.48f else 0.38f
            PosterAspectRatio.SQUARE_1_1 -> if (spec.options.emphasizeTrack) 0.42f else 0.34f
        }
        val minTrackHeight = shortSide * 0.22f
        val minMetricsHeight = shortSide * 0.28f
        var trackHeight = rect.height() * trackFraction
        trackHeight = trackHeight.coerceAtLeast(minTrackHeight)
        trackHeight = min(trackHeight, rect.height() - minMetricsHeight - gap)

        val trackRect = RectF(rect.left, rect.top, rect.right, rect.top + trackHeight)
        val metricsRect = RectF(rect.left, trackRect.bottom + gap, rect.right, rect.bottom)

        if (spec.options.emphasizeTrack) {
            drawTrackCard(canvas, spec, trackRect, shortSide)
            drawMetricGrid(canvas, spec, metricsRect, shortSide, compact = true)
        } else {
            drawTrackCard(canvas, spec, trackRect, shortSide)
            drawMetricGrid(canvas, spec, metricsRect, shortSide, compact = spec.options.compactMetrics)
        }
    }

    private fun drawMetricGrid(
        canvas: Canvas,
        spec: PosterSpec,
        rect: RectF,
        shortSide: Float,
        compact: Boolean
    ) {
        if (rect.height() <= 0f) return
        val candidateMetrics = spec.metrics.sortedByDescending { it.priority }.take(if (compact) 6 else 8)
        if (candidateMetrics.isEmpty()) return

        val columns = 2
        val gap = shortSide * 0.015f
        val minCardHeight = max(shortSide * 0.12f, 96f)
        val maxRowsByHeight = ((rect.height() + gap) / (minCardHeight + gap)).toInt().coerceAtLeast(1)
        val maxVisibleMetrics = (maxRowsByHeight * columns).coerceAtLeast(columns)
        val metrics = candidateMetrics.take(maxVisibleMetrics)
        val rows = ((metrics.size + columns - 1) / columns).coerceAtLeast(1)
        val cardWidth = (rect.width() - gap * (columns - 1)) / columns
        val cardHeight = (rect.height() - gap * (rows - 1)) / rows

        metrics.forEachIndexed { index, metric ->
            val row = index / columns
            val col = index % columns
            val left = rect.left + col * (cardWidth + gap)
            val top = rect.top + row * (cardHeight + gap)
            drawMetricCard(
                canvas = canvas,
                spec = spec,
                rect = RectF(left, top, left + cardWidth, top + cardHeight),
                metric = metric,
                shortSide = shortSide
            )
        }
    }

    private fun drawMetricCard(
        canvas: Canvas,
        spec: PosterSpec,
        rect: RectF,
        metric: PosterMetric,
        shortSide: Float
    ) {
        drawCard(canvas, spec, rect, shortSide, strong = false)

        val inset = rect.width() * 0.09f
        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.textSecondary
            textSize = fitSingleLineTextSize(
                text = metric.label,
                maxWidth = rect.width() - inset * 2f,
                initialSize = min(rect.height() * 0.18f, rect.width() * 0.11f),
                minSize = min(rect.height() * 0.13f, rect.width() * 0.08f),
                isBold = false
            )
        }
        val valuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.textPrimary
            textSize = min(rect.height() * 0.34f, rect.width() * 0.18f)
            isFakeBoldText = true
        }
        val unitPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.accent
            textSize = min(rect.height() * 0.14f, rect.width() * 0.09f)
        }

        val labelTop = rect.top + rect.height() * 0.14f
        val labelHeight = drawParagraph(
            canvas = canvas,
            text = metric.label,
            left = rect.left + inset,
            top = labelTop,
            width = rect.width() - inset * 2f,
            paint = labelPaint,
            maxLines = 1
        )

        val valueTop = labelTop + labelHeight + rect.height() * 0.08f
        drawMetricValueBlock(
            canvas = canvas,
            metric = metric,
            left = rect.left + inset,
            right = rect.right - inset,
            top = valueTop,
            bottom = rect.bottom - rect.height() * 0.14f,
            valuePaint = valuePaint,
            unitPaint = unitPaint,
            gap = rect.width() * 0.018f
        )
    }

    private fun drawTrackCard(canvas: Canvas, spec: PosterSpec, rect: RectF, shortSide: Float) {
        val track = spec.track ?: return
        drawCard(canvas, spec, rect, shortSide, strong = false)

        val inset = rect.width() * 0.06f
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.textPrimary
            textSize = fitSingleLineTextSize(
                text = track.title,
                maxWidth = rect.width() - inset * 2f,
                initialSize = min(rect.height() * 0.12f, rect.width() * 0.08f),
                minSize = min(rect.height() * 0.09f, rect.width() * 0.06f),
                isBold = true
            )
            isFakeBoldText = true
        }
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.textSecondary
            textSize = min(rect.height() * 0.08f, rect.width() * 0.05f)
        }

        val titleTop = rect.top + rect.height() * 0.08f
        val titleHeight = drawParagraph(
            canvas = canvas,
            text = track.title,
            left = rect.left + inset,
            top = titleTop,
            width = rect.width() - inset * 2f,
            paint = titlePaint,
            maxLines = 1
        )
        val subtitleHeight = if (rect.height() >= shortSide * 0.2f) {
            drawParagraph(
                canvas = canvas,
                text = track.subtitle,
                left = rect.left + inset,
                top = titleTop + titleHeight + rect.height() * 0.02f,
                width = rect.width() - inset * 2f,
                paint = subtitlePaint,
                maxLines = 2,
                lineSpacingExtra = rect.height() * 0.004f
            )
        } else {
            0f
        }

        val routeTop = titleTop + titleHeight + subtitleHeight + rect.height() * 0.05f
        val routeRect = RectF(
            rect.left + inset,
            routeTop,
            rect.right - inset,
            rect.bottom - rect.height() * 0.07f
        )
        if (routeRect.height() > 0f) {
            drawRoute(canvas, spec, routeRect, track.points, shortSide)
        }
    }

    private fun drawRoute(
        canvas: Canvas,
        spec: PosterSpec,
        rect: RectF,
        route: List<RideTrackPoint>,
        shortSide: Float
    ) {
        val routeBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = adjustAlpha(spec.theme.cardFill, 0.62f)
        }
        val radius = min(rect.width(), rect.height()) * 0.07f
        canvas.drawRoundRect(rect, radius, radius, routeBackground)
        drawRouteGrid(canvas, spec, rect, shortSide)

        if (route.size < 2) {
            val hintPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = spec.theme.textSecondary
                textSize = min(rect.height() * 0.12f, rect.width() * 0.06f)
            }
            drawParagraph(
                canvas = canvas,
                text = "等待更完整的 GPS 轨迹",
                left = rect.left + rect.width() * 0.08f,
                top = rect.centerY() - rect.height() * 0.06f,
                width = rect.width() - rect.width() * 0.16f,
                paint = hintPaint,
                maxLines = 1
            )
            return
        }

        val projected = projectRoutePoints(rect, route)
        val path = buildSmoothPath(projected)
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = adjustAlpha(Color.BLACK, 0.2f)
            style = Paint.Style.STROKE
            strokeWidth = max(4f, shortSide * 0.012f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val routePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.accent
            style = Paint.Style.STROKE
            strokeWidth = max(2.5f, shortSide * 0.0065f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            pathEffect = CornerPathEffect(shortSide * 0.015f)
        }
        canvas.drawPath(path, shadowPaint)
        canvas.drawPath(path, routePaint)

        val markerStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.textPrimary
            style = Paint.Style.STROKE
            strokeWidth = max(1.5f, shortSide * 0.0024f)
        }
        val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = spec.theme.accentSecondary }
        val endPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = spec.theme.accent }
        val start = projected.first()
        val end = projected.last()
        val startRadius = max(4f, shortSide * 0.008f)
        val endRadius = max(5f, shortSide * 0.010f)
        canvas.drawCircle(start.x, start.y, startRadius, startPaint)
        canvas.drawCircle(start.x, start.y, startRadius, markerStroke)
        canvas.drawCircle(end.x, end.y, endRadius, endPaint)
        canvas.drawCircle(end.x, end.y, endRadius, markerStroke)
    }

    private fun drawRouteGrid(canvas: Canvas, spec: PosterSpec, rect: RectF, shortSide: Float) {
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.routeGrid
            strokeWidth = max(1f, shortSide * 0.0018f)
        }
        val inset = min(rect.width(), rect.height()) * 0.06f
        for (index in 1 until 5) {
            val x = rect.left + rect.width() * index / 5f
            val y = rect.top + rect.height() * index / 5f
            canvas.drawLine(x, rect.top + inset, x, rect.bottom - inset, gridPaint)
            canvas.drawLine(rect.left + inset, y, rect.right - inset, y, gridPaint)
        }
    }

    private fun drawCard(canvas: Canvas, spec: PosterSpec, rect: RectF, shortSide: Float, strong: Boolean) {
        val radius = if (strong) shortSide * 0.05f else shortSide * 0.038f
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (strong) adjustAlpha(spec.theme.cardFill, 0.98f) else spec.theme.cardFill
        }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.cardStroke
            style = Paint.Style.STROKE
            strokeWidth = if (strong) max(1.4f, shortSide * 0.0026f) else max(1f, shortSide * 0.0018f)
        }
        canvas.drawRoundRect(rect, radius, radius, fill)
        canvas.drawRoundRect(rect, radius, radius, stroke)
    }

    private fun drawFooter(canvas: Canvas, spec: PosterSpec, rect: RectF, width: Float) {
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.theme.textSecondary
            textSize = fitSingleLineTextSize(
                text = listOfNotNull(spec.footer.line, spec.footer.watermark.takeIf { spec.options.showWatermark }).joinToString("  •  "),
                maxWidth = rect.width(),
                initialSize = width * 0.018f,
                minSize = width * 0.012f,
                isBold = false
            )
            textAlign = Paint.Align.CENTER
        }
        val watermark = spec.footer.watermark.takeIf { spec.options.showWatermark }
        val text = listOfNotNull(spec.footer.line, watermark).joinToString("  •  ")
        val fitted = trimToWidth(text, rect.width(), footerPaint)
        val baseline = rect.bottom - footerPaint.fontMetrics.descent
        canvas.drawText(fitted, rect.centerX(), baseline, footerPaint)
    }

    private fun drawMetricValueBlock(
        canvas: Canvas,
        metric: PosterMetric,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        valuePaint: TextPaint,
        unitPaint: TextPaint,
        gap: Float
    ) {
        val unit = metric.unit?.takeIf { it.isNotBlank() }
        val maxWidth = right - left
        fitMetricValueText(metric.value, unit, maxWidth, valuePaint, unitPaint, gap)
        val valueHeight = valuePaint.fontMetrics.descent - valuePaint.fontMetrics.ascent
        val baseline = min(bottom, top + valueHeight) - valuePaint.fontMetrics.descent
        canvas.drawText(metric.value, left, baseline, valuePaint)
        if (unit != null) {
            val adjustedBaseline = baseline - (valuePaint.fontMetrics.descent - unitPaint.fontMetrics.descent)
            val unitLeft = left + valuePaint.measureText(metric.value) + gap
            canvas.drawText(unit, unitLeft, adjustedBaseline, unitPaint)
        }
    }

    private fun fitMetricValueText(
        value: String,
        unit: String?,
        maxWidth: Float,
        valuePaint: TextPaint,
        unitPaint: TextPaint,
        gap: Float
    ) {
        val minValueSize = valuePaint.textSize * 0.6f
        val minUnitSize = unitPaint.textSize * 0.7f
        while (
            valuePaint.textSize > minValueSize &&
            valuePaint.measureText(value) + (if (unit != null) unitPaint.measureText(unit) + gap else 0f) > maxWidth
        ) {
            valuePaint.textSize *= 0.94f
            unitPaint.textSize = max(minUnitSize, unitPaint.textSize * 0.96f)
        }
    }

    private fun drawParagraph(
        canvas: Canvas,
        text: String,
        left: Float,
        top: Float,
        width: Float,
        paint: TextPaint,
        maxLines: Int,
        lineSpacingExtra: Float = 0f
    ): Float {
        if (text.isBlank() || width <= 0f) return 0f
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width.toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setLineSpacing(lineSpacingExtra, 1f)
            .build()
        canvas.save()
        canvas.translate(left, top)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    private fun measureHeaderHeight(
        title: String,
        subtitle: String,
        width: Float,
        badgeSize: Float,
        titleSize: Float,
        subtitleSize: Float
    ): Float {
        val badgePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = badgeSize }
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = titleSize
            isFakeBoldText = true
        }
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = subtitleSize }
        val badgeHeight = measureParagraphHeight("SMARTDASH POSTER", width, badgePaint, 1)
        val titleHeight = measureParagraphHeight(title, width, titlePaint, 2, width * 0.004f)
        val subtitleHeight = if (subtitle.isBlank()) 0f else measureParagraphHeight(subtitle, width, subtitlePaint, 2, width * 0.0025f)
        return badgeHeight + titleHeight + subtitleHeight + width * 0.03f
    }

    private fun measureParagraphHeight(
        text: String,
        width: Float,
        paint: TextPaint,
        maxLines: Int,
        lineSpacingExtra: Float = 0f
    ): Float {
        if (text.isBlank() || width <= 0f) return 0f
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width.toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setLineSpacing(lineSpacingExtra, 1f)
            .build()
        return layout.height.toFloat()
    }

    private fun fitTextSize(
        text: String,
        maxWidth: Float,
        initialSize: Float,
        minSize: Float,
        maxLines: Int,
        isBold: Boolean
    ): Float {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = initialSize
            isFakeBoldText = isBold
        }
        while (paint.textSize > minSize) {
            val height = measureParagraphHeight(text, maxWidth, paint, maxLines)
            val lineHeight = paint.fontMetrics.descent - paint.fontMetrics.ascent
            if (height <= lineHeight * maxLines * 1.04f) break
            paint.textSize *= 0.94f
        }
        return max(minSize, paint.textSize)
    }

    private fun fitSingleLineTextSize(
        text: String,
        maxWidth: Float,
        initialSize: Float,
        minSize: Float,
        isBold: Boolean
    ): Float {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = initialSize
            isFakeBoldText = isBold
        }
        while (paint.textSize > minSize && paint.measureText(text) > maxWidth) {
            paint.textSize *= 0.94f
        }
        return max(minSize, paint.textSize)
    }

    private fun computeHeroHeight(
        ratio: PosterAspectRatio,
        availableHeight: Float,
        shortSide: Float
    ): Float {
        val preferred = when (ratio) {
            PosterAspectRatio.STORY_9_16 -> availableHeight * 0.24f
            PosterAspectRatio.PORTRAIT_4_5 -> availableHeight * 0.22f
            PosterAspectRatio.SQUARE_1_1 -> availableHeight * 0.2f
        }
        val minHeight = shortSide * 0.18f
        val maxHeight = availableHeight * 0.32f
        return preferred.coerceIn(minHeight, maxHeight)
    }

    private fun projectRoutePoints(rect: RectF, route: List<RideTrackPoint>): List<PointF> {
        val minLat = route.minOf { it.latitude }
        val maxLat = route.maxOf { it.latitude }
        val minLon = route.minOf { it.longitude }
        val maxLon = route.maxOf { it.longitude }
        val latSpan = (maxLat - minLat).takeIf { it > 0.000001 } ?: 0.000001
        val lonSpan = (maxLon - minLon).takeIf { it > 0.000001 } ?: 0.000001
        val padding = min(rect.width(), rect.height()) * 0.12f
        val inner = RectF(rect.left + padding, rect.top + padding, rect.right - padding, rect.bottom - padding)
        val scale = min(inner.width() / lonSpan.toFloat(), inner.height() / latSpan.toFloat())
        val offsetX = inner.left + (inner.width() - lonSpan.toFloat() * scale) / 2f
        val offsetY = inner.top + (inner.height() - latSpan.toFloat() * scale) / 2f
        return route.map { point ->
            PointF(
                offsetX + (point.longitude - minLon).toFloat() * scale,
                offsetY + latSpan.toFloat() * scale - (point.latitude - minLat).toFloat() * scale
            )
        }
    }

    private fun buildSmoothPath(points: List<PointF>): Path {
        val path = Path()
        if (points.isEmpty()) return path
        path.moveTo(points.first().x, points.first().y)
        if (points.size == 2) {
            path.lineTo(points[1].x, points[1].y)
            return path
        }
        for (index in 1 until points.size) {
            val previous = points[index - 1]
            val current = points[index]
            val midX = (previous.x + current.x) / 2f
            val midY = (previous.y + current.y) / 2f
            if (index == 1) {
                path.lineTo(midX, midY)
            } else {
                path.quadTo(previous.x, previous.y, midX, midY)
            }
        }
        path.lineTo(points.last().x, points.last().y)
        return path
    }

    private fun trimToWidth(text: String, maxWidth: Float, paint: TextPaint): String {
        if (paint.measureText(text) <= maxWidth) return text
        var candidate = text
        while (candidate.isNotEmpty() && paint.measureText("$candidate…") > maxWidth) {
            candidate = candidate.dropLast(1)
        }
        return "$candidate…"
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}
