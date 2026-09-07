package com.azteci.fluxdroid.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.azteci.fluxdroid.domain.BrushSettings
import com.azteci.fluxdroid.domain.StrokePoint
import kotlin.math.sqrt

/**
 * Centralizes pressure mapping, smoothing and spacing decisions so UI code never
 * needs to know how a brush stroke is produced.
 */
class BrushEngine(private val settingsProvider: () -> BrushSettings) {
    private var lastAccepted: Offset? = null
    private val points = ArrayList<StrokePoint>()

    fun begin(position: Offset, pressure: Float, tilt: Float): StrokePoint {
        points.clear()
        lastAccepted = position
        val point = StrokePoint(position, normalizePressure(pressure), tilt)
        points += point
        return point
    }

    fun move(position: Offset, pressure: Float, tilt: Float): StrokePoint? {
        val start = lastAccepted ?: return begin(position, pressure, tilt)
        val settings = settingsProvider()
        val distance = distance(start, position)
        val minSpacing = maxOf(0.75f, settings.size * settings.spacing)
        if (distance < minSpacing) return null
        val blend = settings.smoothing.coerceIn(0f, 0.95f)
        val smoothed = Offset(
            x = start.x + (position.x - start.x) * (1f - blend),
            y = start.y + (position.y - start.y) * (1f - blend),
        )
        lastAccepted = smoothed
        val point = StrokePoint(smoothed, normalizePressure(pressure), tilt)
        points += point
        return point
    }

    fun finish(position: Offset?, pressure: Float, tilt: Float): List<StrokePoint> {
        if (position != null) move(position, pressure, tilt)
        lastAccepted = null
        return points.toList()
    }

    private fun normalizePressure(raw: Float): Float {
        if (!settingsProvider().pressureEnabled) return 1f
        return raw.coerceIn(0.05f, 1f)
    }

    private fun distance(a: Offset, b: Offset): Float = sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))
}
