/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

/**
 * A class that manages the visibility of controls with a delay.
 *
 * This class is used to control the visibility of UI elements that should be hidden
 * after a certain period of inactivity. The visibility is initially set to [initialVisible]
 * and the delay before hiding is set to [initialDelay].
 *
 * To reset the delay and keep the controls visible, call the [reset] function.
 * This will restart the delay timer.
 *
 * @param initialVisible The initial visibility of the controls.
 * @param initialDelay The initial delay before hiding the controls.
 */
class DelayedControlsVisibility internal constructor(initialVisible: Boolean, initialDelay: Duration) {
    /**
     * Controls visibility.
     */
    var visible by mutableStateOf(initialVisible)

    /**
     * The [delay] after the controls become no more visible.
     * Can be reset with [reset] method.
     */
    var delay by mutableStateOf(initialDelay)
    internal var reset by mutableStateOf<Any?>(null)

    /**
     * Resets the ongoing delay.
     */
    fun reset() {
        if (visible && delay > ZERO) {
            reset = Any()
        }
    }
}

/**
 * Remembers and controls the visibility of UI elements with a delay.
 *
 * Initially sets visibility to [initialVisible]. If visible, hides after [initialDelay].
 *
 * @param initialVisible Initial visibility. Defaults to false.
 * @param initialDelay Delay before hiding, if initially visible. Defaults to 3 seconds.
 * @return A [DelayedControlsVisibility] instance to control and observe visibility.
 */
@Composable
fun rememberDelayedControlsVisibility(initialVisible: Boolean = false, initialDelay: Duration = DefaultVisibilityDelay): DelayedControlsVisibility {
    val visibility = remember(initialVisible, initialDelay) { DelayedControlsVisibility(initialVisible, initialDelay) }
    LaunchedEffect(visibility.visible, visibility.delay, visibility.reset) {
        if (visibility.visible && visibility.delay > ZERO) {
            delay(visibility.delay)
            visibility.visible = false
        }
    }
    return visibility
}

/**
 * Default visibility delay
 */
val DefaultVisibilityDelay = 3.seconds

@Preview
@Composable
private fun KeepVisibleDelayPreview() {
    val visibility = rememberDelayedControlsVisibility(true, 2.seconds)

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .clickable { visibility.visible = !visibility.visible },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Green)
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = visibility.visible,
                modifier = Modifier.fillMaxSize(),
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
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BasicText(
                text = "Show",
                modifier = Modifier.clickable {
                    visibility.visible = true
                    visibility.reset()
                }
            )
            BasicText(
                text = "Toggle",
                modifier = Modifier.clickable {
                    visibility.visible = !visibility.visible
                }
            )
            BasicText(
                text = "Hide",
                modifier = Modifier.clickable {
                    visibility.visible = false
                }
            )
            BasicText(
                text = "Disable",
                modifier = Modifier.clickable {
                    visibility.delay = ZERO
                }
            )
            BasicText(
                text = "Enable",
                modifier = Modifier.clickable {
                    visibility.delay = 2.seconds
                }
            )
        }
    }
}
