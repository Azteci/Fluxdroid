package com.azteci.fluxdroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(vm: FluxViewModel) {
    val gradient = Brush.linearGradient(listOf(Color(0xFFB8FF4A), Color(0xFF7DE7FF), Color(0xFF9B6CFF)))
    Column(Modifier.fillMaxSize().background(Color(0xFF080A0F)).padding(horizontal = 18.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(gradient, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text("F", color = Color(0xFF080A0F), style = MaterialTheme.typography.headlineSmall) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text("FluxDroid", style = MaterialTheme.typography.headlineMedium); Text("Create without the learning curve.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = {}) { Icon(Icons.Default.Settings, "Settings") }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { vm.newProject() }, modifier = Modifier.fillMaxWidth().height(68.dp), shape = RoundedCornerShape(22.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) {
            Icon(Icons.Default.Add, null); Spacer(Modifier.width(10.dp)); Text("NEW PROJECT", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(18.dp))
        Text("QUICK START", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickCard("Draw", Icons.Default.Brush, Modifier.weight(1f)) { vm.newProject() }
            QuickCard("Animate", Icons.Default.Movie, Modifier.weight(1f)) { vm.newProject(animate = true) }
            QuickCard("Import", Icons.Default.FileOpen, Modifier.weight(1f)) { }
        }
        Spacer(Modifier.height(22.dp))
        Text("YOUR WORK", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("Ready for a blank canvas?", style = MaterialTheme.typography.titleLarge)
                Text("FluxDroid keeps the essential controls close and reveals deeper tools only when you need them.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(14.dp))
                OutlinedButton(onClick = { vm.newProject() }) { Text("Start drawing") }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("FluxDroid 0.10 • offline-first workspace", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = modifier.height(112.dp), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) { Icon(icon, label); Text(label, style = MaterialTheme.typography.titleSmall) }
    }
}
