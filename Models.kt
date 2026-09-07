package com.azteci.fluxdroid.domain

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class Tool { BRUSH, ERASER, FILL, EYEDROPPER, LINE, RECTANGLE, ELLIPSE, SELECTION, TRANSFORM }

data class BrushSettings(
    val size: Float = 18f,
    val opacity: Float = 1f,
    val hardness: Float = 0.85f,
    val spacing: Float = 0.08f,
    val smoothing: Float = 0.35f,
    val pressureEnabled: Boolean = true,
)

data class BrushPreset(
    val id: String,
    val name: String,
    val category: String,
    val settings: BrushSettings,
    val stamp: String,
)

data class StrokePoint(
    val position: Offset,
    val pressure: Float = 1f,
    val tilt: Float = 0f,
)

data class Stroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<StrokePoint>,
    val color: Color,
    val width: Float,
    val opacity: Float,
    val tool: Tool = Tool.BRUSH,
)

data class Layer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val visible: Boolean = true,
    val opacity: Float = 1f,
    val locked: Boolean = false,
    val strokes: List<Stroke> = emptyList(),
)

data class AnimationFrame(
    val index: Int,
    val durationMs: Int = 100,
    val layers: List<Layer> = emptyList(),
)

data class AnimationDocument(
    val fps: Int = 12,
    val loop: Boolean = true,
    val frames: List<AnimationFrame> = emptyList(),
    val activeFrame: Int = 0,
)

data class CanvasDocument(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Untitled",
    val width: Int = 1080,
    val height: Int = 1080,
    val background: Color = Color.White,
    val layers: List<Layer> = listOf(Layer(name = "Sketch")),
    val activeLayerId: String = "",
    val animation: AnimationDocument? = null,
) {
    fun normalized(): CanvasDocument {
        val active = if (layers.any { it.id == activeLayerId }) activeLayerId else layers.firstOrNull()?.id.orEmpty()
        return copy(activeLayerId = active)
    }
}

data class ProjectSummary(
    val id: String,
    val name: String,
    val width: Int,
    val height: Int,
    val modifiedAt: Long,
    val thumbnail: Bitmap? = null,
)
