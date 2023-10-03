/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

private const val TimerDelay = 1_000L
private const val DefaultHideSeconds = 3

/**
 * Default auto hide mode [AutoHideMode.Delayed] with [DefaultHideSeconds].
 */
val DefaultAutoHideMode: AutoHideMode = AutoHideMode.Delayed()

/**
 * Auto hide state
 *
 * @property delay
 * @constructor
 *
 * @param coroutineScope
 */
class ToggleVisibilityState internal constructor(
    private val delay: AutoHideMode = DefaultAutoHideMode,
    coroutineScope: CoroutineScope,
) {
    /**
     * Is displayed
     */
    var isDisplayed by mutableStateOf(true)
    private val timer = MutableStateFlow(delay)

    init {
        coroutineScope.launch {
            timer.filterIsInstance(AutoHideMode.Delayed::class).collectLatest { time ->
                if (!isDisplayed) return@collectLatest
                if (time.seconds > 0) {
                    delay(TimerDelay)
                    timer.emit(time.copy(seconds = time.seconds - 1))
                } else {
                    isDisplayed = false
                }
            }
        }
    }

    /**
     * Toggle the content with give autoHideMode.
     *
     * @param autoHideMode The [AutoHideMode] to use.
     */
    suspend fun toggle(autoHideMode: AutoHideMode = delay) {
        if (isDisplayed) {
            hide()
        } else {
            show(autoHideMode)
        }
    }

    /**
     * Show the content.
     *
     * @param autoHideMode The [AutoHideMode] to use.
     */
    suspend fun show(autoHideMode: AutoHideMode = delay) {
        isDisplayed = true
        timer.emit(autoHideMode)
    }

    /**
     * Hide the content.
     *
     * Immediately hide the view.
     */
    suspend fun hide() {
        isDisplayed = false
        timer.emit(AutoHideMode.Delayed(0))
    }
}

/**
 * Auto hide mode
 *
 * @constructor Create empty Auto hide mode
 */
sealed interface AutoHideMode {
    /**
     * Disable auto hide
     */
    data object Disable : AutoHideMode

    /**
     * Delayed
     *
     * @property seconds The number of seconds to wait to hide the content.
     */
    data class Delayed(val seconds: Int = DefaultHideSeconds) : AutoHideMode
}

/**
 * Remember auto hide state
 *
 * @param autoHideMode The [AutoHideMode] to use.
 * @param coroutineScope
 */
@Composable
fun rememberAutoHideState(
    autoHideMode: AutoHideMode = DefaultAutoHideMode,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): ToggleVisibilityState {
    val state = remember(autoHideMode) {
        ToggleVisibilityState(delay = autoHideMode, coroutineScope = coroutineScope)
    }
    return state
}
