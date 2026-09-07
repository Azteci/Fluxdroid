package com.azteci.fluxdroid.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.azteci.fluxdroid.domain.ColorModel
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
fun ColorWheel(selected: Color, onSelected: (Color) -> Unit) {
    Canvas(
        Modifier.size(220.dp).pointerInput(Unit) {
            detectTapGestures { p ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val dx = p.x - center.x
                val dy = p.y - center.y
                val radius = hypot(dx, dy)
                val maxRadius = size.minDimension / 2f
                if (radius <= maxRadius) {
                    val angle = (Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 360.0) % 360.0
                    val hue = angle.toFloat()
                    val saturation = (radius / maxRadius).coerceIn(0f, 1f)
                    onSelected(ColorModel.fromHsv(hue, saturation, 1f, selected.alpha))
                }
            }
        }
    ) {
        val center = center
        val radius = size.minDimension / 2f
        for (i in 0 until 360 step 3) {
            val start = Math.toRadians(i.toDouble()).toFloat()
            val end = Math.toRadians((i + 4).toDouble()).toFloat()
            val c = ColorModel.fromHsv(i.toFloat(), 1f, 1f)
            val x = center.x + radius * kotlin.math.cos(start)
            val y = center.y + radius * kotlin.math.sin(start)
            val x2 = center.x + radius * kotlin.math.cos(end)
            val y2 = center.y + radius * kotlin.math.sin(end)
            drawLine(c, Offset(x, y), Offset(x2, y2), strokeWidth = 12f)
        }
        drawCircle(Brush.radialGradient(listOf(Color.White, Color.Transparent), center, radius), radius = radius * .74f, center = center)
        drawCircle(Color.Black.copy(alpha = .06f), radius = radius * .72f, center = center)
        val hsv = ColorModel.rgbToHsv(selected)
        val selectedAngle = Math.toRadians(hsv[0].toDouble()).toFloat()
        val selectedRadius = hsv[1] * radius * .74f
        val marker = Offset(center.x + kotlin.math.cos(selectedAngle) * selectedRadius, center.y + kotlin.math.sin(selectedAngle) * selectedRadius)
        drawCircle(Color.White, radius = 10f, center = marker, style = androidx.compose.ui.graphics.drawscope.Fill)
        drawCircle(Color.Black, radius = 7f, center = marker, style = androidx.compose.ui.graphics.drawscope.Fill)
        drawCircle(selected, radius = 5f, center = marker, style = androidx.compose.ui.graphics.drawscope.Fill)
    }
}
