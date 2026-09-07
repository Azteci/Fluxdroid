package com.azteci.fluxdroid.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azteci.fluxdroid.domain.*
import com.azteci.fluxdroid.engine.BrushEngine
import com.azteci.fluxdroid.engine.CanvasRenderer

@Composable
fun CanvasScreen(vm: FluxViewModel = viewModel()) {
    val state by vm.ui.collectAsState()
    val doc = state.document ?: return
    var scale by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    val brushEngine = remember(state.brush) { BrushEngine { state.brush } }
    var strokePoints by remember { mutableStateOf(emptyList<StrokePoint>()) }

    Box(Modifier.fillMaxSize().background(Color(0xFF07090D))) {
        Canvas(
            Modifier.fillMaxSize()
                .pointerInput(state.tool, state.brush) {
                    detectTransformGestures { _, panChange, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.2f, 8f)
                        pan += panChange
                    }
                }
                .pointerInput(state.tool, state.brush, scale, pan) {
                    detectDragGestures(
                        onDragStart = { position -> strokePoints = listOf(brushEngine.begin(screenToCanvas(position, scale, pan), 1f, 0f)) },
                        onDrag = { change, _ ->
                            brushEngine.move(screenToCanvas(change.position, scale, pan), 1f, 0f)?.let { strokePoints = strokePoints + it }
                        },
                        onDragEnd = { val final = brushEngine.finish(null, 1f, 0f); if (final.isNotEmpty()) vm.beginStroke(final); strokePoints = emptyList() },
                        onDragCancel = { strokePoints = emptyList() }
                    )
                }
                .pointerInput(state.tool) {
                    detectTapGestures(onTap = { position -> vm.beginStroke(listOf(brushEngine.begin(screenToCanvas(position, scale, pan), 1f, 0f))) })
                }
        ) {
            CanvasRenderer.drawDocument(this, doc, scale, pan)
            if (strokePoints.isNotEmpty()) {
                val preview = Stroke(points = strokePoints, color = if (state.tool == Tool.ERASER) doc.background else state.color, width = state.brush.size, opacity = state.brush.opacity, tool = state.tool)
                CanvasRenderer.run { drawStrokePreview(this@Canvas, preview, scale, pan) }
            }
        }

        CanvasHeader(vm = vm, title = doc.name)
        ToolDock(vm = vm, state = state)
        BottomControls(vm, state)
        if (state.showColorPicker) ColorPanel(vm)
        if (state.showBrushLibrary) BrushPanel(vm)
        if (state.showLayers) LayerPanel(vm, doc)
    }
}

private fun screenToCanvas(position: Offset, scale: Float, pan: Offset): Offset = Offset((position.x - pan.x) / scale, (position.y - pan.y) / scale)

@Composable
private fun CanvasHeader(vm: FluxViewModel, title: String) {
    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = vm::openHome) { Icon(Icons.Default.ArrowBack, "Back") }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text("Autosaved • Local", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = vm::undo) { Icon(Icons.Default.Undo, "Undo") }
        IconButton(onClick = vm::redo) { Icon(Icons.Default.Redo, "Redo") }
        IconButton(onClick = vm::toggleLayers) { Icon(Icons.Default.Layers, "Layers") }
    }
}

@Composable
private fun ToolDock(vm: FluxViewModel, state: FluxUiState) {
    Column(Modifier.align(Alignment.CenterStart).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        ToolButton(Icons.Default.Brush, state.tool == Tool.BRUSH) { vm.setTool(Tool.BRUSH) }
        ToolButton(Icons.Default.AutoFixHigh, state.tool == Tool.ERASER) { vm.setTool(Tool.ERASER) }
        ToolButton(Icons.Default.Colorize, false, vm::toggleColorPicker)
        ToolButton(Icons.Default.Brush, false, vm::toggleBrushLibrary)
        ToolButton(Icons.Default.FormatColorFill, state.tool == Tool.FILL) { vm.setTool(Tool.FILL) }
        ToolButton(Icons.Default.Remove, false) { vm.setBrushSize(state.brush.size - 2f) }
        ToolButton(Icons.Default.Add, false) { vm.setBrushSize(state.brush.size + 2f) }
    }
}

@Composable
private fun ToolButton(icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    FilledTonalIconButton(onClick = onClick, modifier = Modifier.padding(vertical = 4.dp), colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)) { Icon(icon, null) }
}

@Composable
private fun BottomControls(vm: FluxViewModel, state: FluxUiState) {
    Column(Modifier.align(Alignment.BottomCenter).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = vm::toggleBrushLibrary) { Icon(Icons.Default.Tune, "Brush settings") }
            Slider(value = state.brush.size, onValueChange = vm::setBrushSize, valueRange = 1f..160f, modifier = Modifier.width(150.dp))
            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(38.dp).background(state.color, CircleShape))
            Spacer(Modifier.width(8.dp))
            Slider(value = state.brush.opacity, onValueChange = vm::setOpacity, valueRange = 0.05f..1f, modifier = Modifier.width(120.dp))
        }
    }
}

@Composable
private fun ColorPanel(vm: FluxViewModel) {
    val state by vm.ui.collectAsState()
    val colors = listOf(Color(0xFFFFFFFF), Color(0xFF000000), Color(0xFFFF4D6D), Color(0xFFFFB000), Color(0xFFB8FF4A), Color(0xFF33D1FF), Color(0xFF9B6CFF), Color(0xFFFF74D5))
    Surface(Modifier.align(Alignment.CenterEnd).padding(16.dp), shape = MaterialTheme.shapes.extraLarge, tonalElevation = 8.dp) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Color Wheel", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ColorWheel(state.color, vm::setColor)
            Spacer(Modifier.height(4.dp))
            colors.chunked(4).forEach { row ->
                Row { row.forEach { c -> IconButton(onClick = { vm.setColor(c) }) { Box(Modifier.size(30.dp).background(c, CircleShape)) } } }
            }
            OutlinedButton(onClick = vm::toggleColorPicker) { Text("Done") }
        }
    }
}

@Composable
private fun BrushPanel(vm: FluxViewModel) {
    val presets = remember {
        listOf(
            BrushPreset("ink", "Ink", "Inking", BrushSettings(size = 14f, hardness = .95f), "—"),
            BrushPreset("pencil", "Pencil", "Sketch", BrushSettings(size = 7f, opacity = .78f, hardness = .35f), "///"),
            BrushPreset("marker", "Marker", "Paint", BrushSettings(size = 28f, opacity = .72f, hardness = .65f), "▰"),
            BrushPreset("air", "Soft Air", "Paint", BrushSettings(size = 54f, opacity = .25f, hardness = .08f), "◌"),
        )
    }
    Surface(Modifier.align(Alignment.CenterEnd).padding(10.dp), shape = MaterialTheme.shapes.extraLarge, tonalElevation = 8.dp) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Brush Selector", style = MaterialTheme.typography.titleMedium)
            RadialBrushMenu { p -> vm.setBrushSize(p.settings.size); vm.setOpacity(p.settings.opacity); if (p.name == "Erase") vm.setTool(Tool.ERASER) else vm.setTool(Tool.BRUSH); vm.toggleBrushLibrary() }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                presets.forEach { p -> FilterChip(selected = false, onClick = { vm.setBrushSize(p.settings.size); vm.setOpacity(p.settings.opacity) }, label = { Text(p.name) }) }
            }
            OutlinedButton(onClick = vm::toggleBrushLibrary, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}

@Composable
private fun LayerPanel(vm: FluxViewModel, doc: CanvasDocument) {
    Surface(Modifier.align(Alignment.TopEnd).padding(top = 68.dp, end = 12.dp).width(250.dp), shape = MaterialTheme.shapes.extraLarge, tonalElevation = 8.dp) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Layers", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f)); IconButton(onClick = vm::addLayer) { Icon(Icons.Default.Add, "Add layer") } }
            doc.layers.asReversed().forEach { layer ->
                ElevatedCard(onClick = { vm.selectLayer(layer.id) }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (layer.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        Spacer(Modifier.width(8.dp)); Text(layer.name, Modifier.weight(1f))
                        if (layer.id == doc.activeLayerId) Text("ACTIVE", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Button(onClick = vm::deleteActiveLayer, modifier = Modifier.fillMaxWidth()) { Text("Delete active layer") }
        }
    }
}

