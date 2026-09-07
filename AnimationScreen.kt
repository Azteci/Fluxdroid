package com.azteci.fluxdroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.azteci.fluxdroid.domain.AnimationDocument

@Composable
fun AnimationScreen(animation: AnimationDocument, onBack: () -> Unit) {
    var active by remember(animation) { mutableIntStateOf(animation.activeFrame.coerceAtMost(animation.frames.lastIndex.coerceAtLeast(0))) }
    var fps by remember(animation) { mutableIntStateOf(animation.fps) }
    Column(Modifier.fillMaxSize().background(Color(0xFF080A0F))) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.Close, "Close") }
            Column(Modifier.weight(1f)) { Text("Animation", style = MaterialTheme.typography.titleLarge); Text("Frame-by-frame timeline", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text("${fps} FPS", style = MaterialTheme.typography.labelLarge)
        }
        ElevatedCard(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) { Icon(Icons.Default.SkipPrevious, "Previous frame") }
                FilledIconButton(onClick = {}) { Icon(Icons.Default.PlayArrow, "Play") }
                IconButton(onClick = {}) { Icon(Icons.Default.SkipNext, "Next frame") }
                Spacer(Modifier.width(12.dp))
                Text("Onion skin", Modifier.weight(1f)); Switch(checked = true, onCheckedChange = {})
            }
        }
        Text("TIMELINE", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            animation.frames.forEach { frame ->
                ElevatedCard(onClick = { active = frame.index }, colors = CardDefaults.elevatedCardColors(containerColor = if (active == frame.index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.width(82.dp).padding(10.dp)) { Text("${frame.index + 1}"); Text("${frame.durationMs}ms", style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
        Text("Frames are stored inside the same project document, so autosave and undo remain consistent.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("FPS", Modifier.weight(1f)); Slider(value = fps.toFloat(), onValueChange = { fps = it.toInt() }, valueRange = 1f..60f)
        }
    }
}
