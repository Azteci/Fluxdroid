package com.azteci.fluxdroid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NewProjectScreen(onCreate: (Int, Int, Boolean) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("New Project", style = MaterialTheme.typography.headlineMedium)
        Text("Start simple. The deeper workspace appears after you create the document.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        ProjectPreset("Portrait", "1080 × 1350") { onCreate(1080, 1350, false) }
        ProjectPreset("Square", "1080 × 1080") { onCreate(1080, 1080, false) }
        ProjectPreset("Landscape", "1920 × 1080") { onCreate(1920, 1080, false) }
        ProjectPreset("Animation", "1080 × 1080 • 12 FPS") { onCreate(1080, 1080, true) }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) { Text("Cancel") }
    }
}

@Composable
private fun ProjectPreset(name: String, detail: String, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) { Text(name, style = MaterialTheme.typography.titleMedium); Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
