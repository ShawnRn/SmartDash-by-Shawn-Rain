package com.shawnrain.habe.ui.poster

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import com.shawnrain.habe.data.RideSession

class PosterRenderer(private val context: Context) {

    fun render(session: RideSession): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background Gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(Color.parseColor("#1A1A1A"), Color.parseColor("#0A0A0A")),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Accent Glow (Premium feel)
        val glowPaint = Paint().apply {
            isAntiAlias = true
            shader = RadialGradient(
                width * 0.8f, height * 0.2f, width * 0.6f,
                intArrayOf(Color.parseColor("#3300D4FF"), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width * 0.8f, height * 0.2f, width * 0.6f, glowPaint)

        // 3. Header Text
        val titlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 80f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("HABE RIDE", 80f, 160f, titlePaint)
        
        val datePaint = TextPaint().apply {
            color = Color.GRAY
            textSize = 40f
            isAntiAlias = true
        }
        canvas.drawText(session.date, 80f, 220f, datePaint)

        // 4. Main Metric: Distance
        val distLabelPaint = TextPaint().apply {
            color = Color.parseColor("#B0B0B0")
            textSize = 45f
            isAntiAlias = true
        }
        canvas.drawText("TOTAL DISTANCE", 80f, 450f, distLabelPaint)
        
        val distValuePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 220f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val distStr = String.format("%.2f", session.distanceKm)
        canvas.drawText(distStr, 80f, 650f, distValuePaint)
        
        val unitPaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 60f
            isAntiAlias = true
        }
        canvas.drawText("KM", 80f + distValuePaint.measureText(distStr) + 20f, 650f, unitPaint)

        // 5. Grid of Stats (2x2)
        drawStat(canvas, "MAX SPEED", String.format("%.1f", session.maxSpeed), "KM/H", 80f, 850f)
        drawStat(canvas, "AVG SPEED", String.format("%.1f", session.avgSpeed), "KM/H", 580f, 850f)
        drawStat(canvas, "EFFICIENCY", String.format("%.1f", session.avgEfficiency), "WH/KM", 80f, 1100f)
        drawStat(canvas, "DURATION", "${session.durationMin}", "MIN", 580f, 1100f)

        // 6. Branding / Footer
        val footerPaint = Paint().apply {
            color = Color.WHITE
            alpha = 100
            textSize = 30f
        }
        canvas.drawText("DESIGNED FOR E-RIDERS BY HABE DASHBOARD", width / 2f - 300f, height - 100f, footerPaint)

        return bitmap
    }

    private fun drawStat(canvas: Canvas, label: String, value: String, unit: String, x: Float, y: Float) {
        val labelPaint = TextPaint().apply {
            color = Color.parseColor("#808080")
            textSize = 36f
            isAntiAlias = true
        }
        val valuePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 80f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val unitPaint = TextPaint().apply {
            color = Color.GRAY
            textSize = 30f
            isAntiAlias = true
        }

        canvas.drawText(label, x, y, labelPaint)
        canvas.drawText(value, x, y + 90f, valuePaint)
        canvas.drawText(unit, x + valuePaint.measureText(value) + 12f, y + 90f, unitPaint)
    }
}
