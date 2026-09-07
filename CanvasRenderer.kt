package com.azteci.fluxdroid.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import com.azteci.fluxdroid.domain.CanvasDocument
import com.azteci.fluxdroid.domain.Layer
import com.azteci.fluxdroid.domain.Stroke
import com.azteci.fluxdroid.domain.Tool

/** Pure rendering adapter. It receives document state; it does not mutate it. */
object CanvasRenderer {
    fun drawDocument(scope: DrawScope, document: CanvasDocument, scale: Float, offset: Offset) {
        scope.drawRect(document.background)
        document.layers.asReversed().forEach { layer ->
            if (layer.visible) drawLayer(scope, layer, scale, offset)
        }
    }

    private fun drawLayer(scope: DrawScope, layer: Layer, scale: Float, offset: Offset) {
        val layerAlpha = layer.opacity.coerceIn(0f, 1f)
        layer.strokes.forEach { stroke ->
            drawStroke(scope, stroke, layerAlpha, scale, offset)
        }
    }

    fun drawStrokePreview(scope: DrawScope, stroke: Stroke, scale: Float, offset: Offset) {
        drawStroke(scope, stroke, 1f, scale, offset)
    }

    private fun drawStroke(scope: DrawScope, stroke: Stroke, layerAlpha: Float, scale: Float, offset: Offset) {
        if (stroke.points.isEmpty()) return
        val path = Path().apply {
            val first = stroke.points.first().position
            moveTo(first.x * scale + offset.x, first.y * scale + offset.y)
            stroke.points.drop(1).forEach { p ->
                lineTo(p.position.x * scale + offset.x, p.position.y * scale + offset.y)
            }
        }
        val alpha = (stroke.opacity * layerAlpha).coerceIn(0f, 1f)
        // Eraser strokes are rendered using the document background in this renderer.
        // The document model keeps them tagged as erasers so a future raster backend can
        // switch to true Porter-Duff clearing without changing UI or history code.
        val color = stroke.color
        scope.drawPath(
            path = path,
            color = color,
            alpha = alpha,
            style = DrawStroke(width = (stroke.width * scale).coerceAtLeast(1f)),
        )
        if (stroke.points.size == 1) {
            val p = stroke.points.single().position
            scope.drawCircle(
                color = color,
                radius = (stroke.width * scale) / 2f,
                center = Offset(p.x * scale + offset.x, p.y * scale + offset.y),
                alpha = alpha,
            )
        }
        @Suppress("UNUSED_VARIABLE")
        val isEraser = stroke.tool == Tool.ERASER
    }
}
