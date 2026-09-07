package com.azteci.fluxdroid.engine

import com.azteci.fluxdroid.domain.AnimationDocument
import com.azteci.fluxdroid.domain.AnimationFrame
import kotlinx.coroutines.delay

/** Frame timing service. It owns playback timing, not UI state. */
class AnimationEngine {
    suspend fun play(
        animation: AnimationDocument,
        shouldContinue: () -> Boolean,
        onFrame: (AnimationFrame) -> Unit,
    ) {
        if (animation.frames.isEmpty()) return
        var index = animation.activeFrame.coerceIn(0, animation.frames.lastIndex)
        while (shouldContinue()) {
            val frame = animation.frames[index]
            onFrame(frame)
            delay((1000L / animation.fps.coerceAtLeast(1)).coerceAtLeast(frame.durationMs.toLong()))
            index = if (index + 1 <= animation.frames.lastIndex) index + 1 else if (animation.loop) 0 else return
        }
    }
}
