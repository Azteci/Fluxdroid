package com.azteci.fluxdroid.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import com.azteci.fluxdroid.domain.CanvasDocument
import java.io.File

class ExportService(private val context: Context) {
    fun exportPng(document: CanvasDocument): File {
        val out = File(context.cacheDir, "${document.name.replace(Regex("[^A-Za-z0-9._-]"), "_")}.png")
        val bitmap = Bitmap.createBitmap(document.width, document.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(document.background.toArgb())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
        document.layers.asReversed().filter { it.visible }.forEach { layer ->
            documentLayer(canvas, paint, layer, 1f)
        }
        out.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return out
    }

    private fun documentLayer(canvas: Canvas, paint: Paint, layer: com.azteci.fluxdroid.domain.Layer, scale: Float) {
        layer.strokes.forEach { stroke ->
            if (stroke.points.isEmpty()) return@forEach
            paint.color = stroke.color.toArgb()
            paint.alpha = (stroke.opacity * layer.opacity * 255f).toInt().coerceIn(0, 255)
            paint.strokeWidth = stroke.width * scale
            val path = android.graphics.Path().apply {
                val first = stroke.points.first().position
                moveTo(first.x * scale, first.y * scale)
                stroke.points.drop(1).forEach { point -> lineTo(point.position.x * scale, point.position.y * scale) }
            }
            canvas.drawPath(path, paint)
        }
    }
}
