/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

/**
 * Default auto-hide duration.
 */
val DefaultKeepDelay = 3.seconds

/**
 * For users that needs accessibility, [keepVisibleDelay] should be increased or set to [Duration.ZERO] to disable auto hide.
 *
 *
 *
 * @param initialVisibility
 * @param keepVisibleDelay
 * @return
 */

/**
 * Creates and remembers a [MutableState] of Boolean representing visibility, with a delay before hiding.
 *
 * This composable function manages the visibility state of an element, initially set to `initialVisibility`.
 * It utilizes the [KeepVisibleDelay] extension function to introduce a delay before hiding the element,
 * ensuring it remains visible for at least the specified `keepVisibleDelay` duration.
 *
 * @param initialVisibility The initial visibility state of the element.
 * @param keepVisibleDelay The duration for which the element should remain visible after a visibility trigger.
 *
 * @return A [MutableState] of Boolean representing the current visibility state of the element.
 */
@Composable
fun rememberKeepVisibleDelay(initialVisibility: Boolean, keepVisibleDelay: Duration): MutableState<Boolean> {
    val controlsVisible = remember(initialVisibility) { mutableStateOf(initialVisibility) }
    controlsVisible.KeepVisibleDelay(keepVisibleDelay)
    return controlsVisible
}

/**
 * Keeps the value of a [MutableState]<[Boolean]> true for a specified duration.
 *
 * This composable function is designed to be used with a boolean state variable
 * that you want to temporarily set to true and then automatically revert to
 * false after a given delay.
 *
 * @param keepVisibleDelay The duration for which the state value should remain true.
 * If this duration is zero or negative, the function does nothing.
 *
 * [AccessibilityManager.calculateRecommendedTimeoutMillis] can help choosing the right delay for accessibility users.
 *
 * Usage Example:
 * ```kotlin
 * var isVisible by remember { mutableStateOf(false) }
 *
 * // ... Some event triggers isVisible to become true ...
 * isVisible = true
 *
 * isVisible.KeepVisibleDelay(Duration.milliseconds(500))
 *
 * // After 500 milliseconds, isVisible will automatically be set back to false.
 * ```
 */
@Composable
fun MutableState<Boolean>.KeepVisibleDelay(keepVisibleDelay: Duration) {
    if (keepVisibleDelay <= ZERO) return
    LaunchedEffect(value, keepVisibleDelay) {
        if (value) {
            delay(keepVisibleDelay)
            value = false
        }
    }
}

@Preview
@Composable
private fun KeepVisibleDelayPreview() {
    var duration by remember { mutableStateOf(DefaultKeepDelay) }
    var controlsVisible by rememberKeepVisibleDelay(initialVisibility = true, keepVisibleDelay = duration)

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .clickable { controlsVisible = !controlsVisible },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Green)
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = controlsVisible,
                modifier = Modifier
                    .fillMaxSize(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(modifier = Modifier.background(color = Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    BasicText(text = "Text to hide", color = { Color.Red })
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BasicText(
                text = "Show",
                modifier = Modifier.clickable {
                    controlsVisible = true
                }
            )
            BasicText(
                text = "Toggle",
                modifier = Modifier.clickable {
                    controlsVisible = !controlsVisible
                }
            )
            BasicText(
                text = "Hide",
                modifier = Modifier.clickable {
                    controlsVisible = false
                }
            )
            BasicText(
                text = "Disable",
                modifier = Modifier.clickable {
                    duration = ZERO
                }
            )
            BasicText(
                text = "Enable",
                modifier = Modifier.clickable {
                    duration = DefaultKeepDelay
                }
            )
        }
    }
}
