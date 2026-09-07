package com.azteci.fluxdroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.azteci.fluxdroid.domain.BrushSettings
import com.azteci.fluxdroid.domain.BrushPreset

@Composable
fun RadialBrushMenu(onPick: (BrushPreset) -> Unit) {
    val presets = listOf(
        BrushPreset("pencil", "Pencil", "Sketch", BrushSettings(size = 6f, opacity = .72f), ""),
        BrushPreset("ink", "Ink", "Ink", BrushSettings(size = 13f, hardness = .95f), ""),
        BrushPreset("paint", "Paint", "Paint", BrushSettings(size = 32f, opacity = .65f), ""),
        BrushPreset("marker", "Marker", "Marker", BrushSettings(size = 24f, opacity = .8f), ""),
        BrushPreset("soft", "Soft", "Air", BrushSettings(size = 54f, opacity = .24f, hardness = .08f), ""),
        BrushPreset("eraser", "Erase", "Utility", BrushSettings(size = 28f), ""),
    )
    Box(Modifier.size(270.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(78.dp).background(Color(0xFF1A1E28), CircleShape), contentAlignment = Alignment.Center) {
            Text("BRUSH", color = Color.White)
        }
        presets.forEachIndexed { index, preset ->
            val angle = index * (360f / presets.size) - 90f
            val r = 88f
            val x = kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * r
            val y = kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() * r
            Box(Modifier.offset(x.dp, y.dp).size(58.dp).background(Color(0xFF202632), CircleShape).clickable { onPick(preset) }, contentAlignment = Alignment.Center) {
                Text(preset.name.take(1), color = Color(0xFFB8FF4A))
            }
        }
    }
}
