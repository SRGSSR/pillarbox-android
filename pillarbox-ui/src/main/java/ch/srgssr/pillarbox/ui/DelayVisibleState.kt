/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Remember delay visible state to use with [AnimatedVisibility]
 *
 * @param visible initial visibility state
 * @param visibleDelay delay after visibility turn off. [Duration.ZERO] to disable.
 */
@Composable
fun rememberDelayVisibleState(visible: Boolean, visibleDelay: Duration = Duration.ZERO): MutableTransitionState<Boolean> {
    val visibleState = remember {
        MutableTransitionState(visible)
    }
    LaunchedEffect(visibleDelay, visibleState.targetState) {
        if (visibleDelay > Duration.ZERO) {
            delay(visibleDelay)
            visibleState.targetState = false
        }
    }
    return visibleState
}

/**
 * Toggle target state with not the current state.
 */
fun MutableTransitionState<Boolean>.toggleState() {
    targetState = !currentState
}

@Preview
@Composable
fun Preview() {
    val visibleState = rememberDelayVisibleState(visible = true, visibleDelay = Duration.ZERO)
    Column() {
        AnimatedVisibility(
            modifier = Modifier,
            visibleState = visibleState,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BasicText(text = "toolbar")
        }
        Box(modifier = Modifier.clickable { visibleState.targetState = !visibleState.currentState }) {
            BasicText(text = "Toggle Button")
        }

        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BasicText(text = "Footer")
        }
    }
}

/**
 * Default visible delay (4 seconds)
 */
val DefaultVisibleDelay = 4L.seconds
