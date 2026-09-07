package com.azteci.fluxdroid.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.azteci.fluxdroid.data.ProjectRepository
import com.azteci.fluxdroid.domain.*
import com.azteci.fluxdroid.engine.HistoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AppRoute {
    data object Home : AppRoute
    data class Canvas(val projectId: String) : AppRoute
    data class NewProject(val animate: Boolean = false) : AppRoute
}

data class FluxUiState(
    val route: AppRoute = AppRoute.Home,
    val document: CanvasDocument? = null,
    val tool: Tool = Tool.BRUSH,
    val color: Color = Color(0xFFB8FF4A),
    val brush: BrushSettings = BrushSettings(),
    val showColorPicker: Boolean = false,
    val showBrushLibrary: Boolean = false,
    val showLayers: Boolean = false,
    val message: String? = null,
)

class FluxViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ProjectRepository(application)
    private val history = HistoryManager()
    private val _ui = MutableStateFlow(FluxUiState())
    val ui: StateFlow<FluxUiState> = _ui.asStateFlow()

    fun newProject(width: Int = 1080, height: Int = 1080, animate: Boolean = false) {
        val frame = if (animate) AnimationDocument(frames = listOf(AnimationFrame(0, layers = listOf(Layer(name = "Frame 1"))))) else null
        val document = CanvasDocument(width = width, height = height, name = if (animate) "New Animation" else "New Artwork", animation = frame).normalized()
        history.clear()
        _ui.value = _ui.value.copy(route = AppRoute.Canvas(document.id), document = document, tool = Tool.BRUSH, showColorPicker = false, showBrushLibrary = false, showLayers = false)
        persist()
    }

    fun openHome() { _ui.value = _ui.value.copy(route = AppRoute.Home) }

    fun setTool(tool: Tool) { _ui.value = _ui.value.copy(tool = tool) }
    fun setColor(color: Color) { _ui.value = _ui.value.copy(color = color, tool = Tool.BRUSH, showColorPicker = false) }
    fun setBrushSize(value: Float) { _ui.value = _ui.value.copy(brush = _ui.value.brush.copy(size = value.coerceIn(1f, 240f))) }
    fun setOpacity(value: Float) { _ui.value = _ui.value.copy(brush = _ui.value.brush.copy(opacity = value.coerceIn(0f, 1f))) }
    fun toggleColorPicker() { _ui.value = _ui.value.copy(showColorPicker = !_ui.value.showColorPicker, showBrushLibrary = false) }
    fun toggleBrushLibrary() { _ui.value = _ui.value.copy(showBrushLibrary = !_ui.value.showBrushLibrary, showColorPicker = false) }
    fun toggleLayers() { _ui.value = _ui.value.copy(showLayers = !_ui.value.showLayers) }

    fun beginStroke(points: List<StrokePoint>) {
        val current = _ui.value.document ?: return
        val layerId = current.activeLayerId.ifBlank { current.layers.firstOrNull()?.id ?: return }
        val before = current
        val stroke = Stroke(points = points, color = if (_ui.value.tool == Tool.ERASER) current.background else _ui.value.color, width = _ui.value.brush.size, opacity = _ui.value.brush.opacity, tool = _ui.value.tool)
        val layers = current.layers.map { if (it.id == layerId && !it.locked) it.copy(strokes = it.strokes + stroke) else it }
        history.record(before)
        _ui.value = _ui.value.copy(document = current.copy(layers = layers))
        persist()
    }

    fun undo() {
        val current = _ui.value.document ?: return
        history.undo(current)?.let { _ui.value = _ui.value.copy(document = it) }
    }

    fun redo() {
        val current = _ui.value.document ?: return
        history.redo(current)?.let { _ui.value = _ui.value.copy(document = it) }
    }

    fun addLayer() {
        val current = _ui.value.document ?: return
        val before = current
        val layer = Layer(name = "Layer ${current.layers.size + 1}")
        history.record(before)
        _ui.value = _ui.value.copy(document = current.copy(layers = current.layers + layer, activeLayerId = layer.id))
        persist()
    }

    fun selectLayer(id: String) {
        val current = _ui.value.document ?: return
        _ui.value = _ui.value.copy(document = current.copy(activeLayerId = id))
    }

    fun deleteActiveLayer() {
        val current = _ui.value.document ?: return
        if (current.layers.size <= 1) return
        val remaining = current.layers.filterNot { it.id == current.activeLayerId }
        history.record(current)
        _ui.value = _ui.value.copy(document = current.copy(layers = remaining, activeLayerId = remaining.last().id))
        persist()
    }

    private fun persist() {
        val doc = _ui.value.document ?: return
        viewModelScope.launch { repo.save(doc) }
    }
}
