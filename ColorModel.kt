package com.azteci.fluxdroid.domain

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

/** HSV/HSL helpers used by the visual color selector. */
object ColorModel {
    fun fromHsv(hue: Float, saturation: Float, value: Float, alpha: Float = 1f): Color {
        val h = (hue % 360f + 360f) % 360f
        val c = value * saturation
        val x = c * (1f - kotlin.math.abs((h / 60f % 2f) - 1f))
        val m = value - c
        val (r, g, b) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return Color(r + m, g + m, b + m, alpha.coerceIn(0f, 1f))
    }

    fun rgbToHsv(color: Color): FloatArray {
        val r = color.red
        val g = color.green
        val b = color.blue
        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        val d = max - min
        var h = 0f
        if (d != 0f) {
            h = when (max) {
                r -> 60f * (((g - b) / d) % 6f)
                g -> 60f * (((b - r) / d) + 2f)
                else -> 60f * (((r - g) / d) + 4f)
            }
            if (h < 0f) h += 360f
        }
        val s = if (max == 0f) 0f else d / max
        return floatArrayOf(h, s, max)
    }
}
