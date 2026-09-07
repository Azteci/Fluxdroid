package com.azteci.fluxdroid.engine

import com.azteci.fluxdroid.domain.CanvasDocument

class HistoryManager(private val limit: Int = 80) {
    private val undoStack = ArrayDeque<CanvasDocument>()
    private val redoStack = ArrayDeque<CanvasDocument>()

    fun record(before: CanvasDocument) {
        undoStack.addLast(before)
        if (undoStack.size > limit) undoStack.removeFirst()
        redoStack.clear()
    }

    fun undo(current: CanvasDocument): CanvasDocument? {
        val previous = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(current)
        return previous
    }

    fun redo(current: CanvasDocument): CanvasDocument? {
        val next = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(current)
        return next
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
}
