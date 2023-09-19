/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Default auto hide delay
 */
val DefaultAutoHideDelay = 3.seconds

/**
 * Toggle state
 *
 * @property duration The duration after the view becomes hidden.
 * @param initialVisible The initial visible state.
 */
@Suppress("UndocumentedPublicClass", "OutdatedDocumentation")
@Stable
class ToggleState internal constructor(
    initialVisible: Boolean,
    private val duration: Duration = DefaultAutoHideDelay
) {
    private val _visibleDuration = mutableStateOf(duration)

    /**
     * Visible state
     */
    val visibleState = MutableTransitionState(initialState = initialVisible)

    /**
     * Visible duration
     */
    val visibleDuration = _visibleDuration

    internal val userInteracting = mutableStateOf(false)

    /**
     * Auto hide enable
     *
     * @param enable Enable auto hide after [duration].
     */
    fun autoHideEnable(enable: Boolean) {
        _visibleDuration.value = if (enable) duration else Duration.ZERO
    }

    /**
     * Set visible
     *
     * @param visible
     */
    fun setVisible(visible: Boolean) {
        visibleState.targetState = visible
    }

    /**
     * Toggle visible
     */
    fun toggleVisible() {
        visibleState.targetState = !visibleState.currentState
    }

    /**
     * Is visible
     *
     * @return
     */
    fun isVisible(): Boolean {
        return visibleState.currentState
    }

    /**
     * Is auto hide enabled
     *
     * @return
     */
    fun isAutoHideEnabled(): Boolean {
        return visibleDuration.value > Duration.ZERO
    }

    internal suspend fun autoHide() {
        if (isVisible() && isAutoHideEnabled() && !userInteracting.value) {
            delay(visibleDuration.value)
            visibleState.targetState = false
        }
    }
}

/**
 * Remember toggle state
 *
 * @param player The player to listen [Player.isPlaying] to disable auto hide when in pause.
 * @param initialVisible Initial visibility.
 * @param interactionSource Interaction source to disable auto hide when user is dragging.
 * @param duration The duration after the view is hide.
 */
@Composable
fun rememberToggleState(
    player: Player,
    initialVisible: Boolean,
    interactionSource: InteractionSource? = null,
    duration: Duration = 4.seconds
): ToggleState {
    val isPlaying = player.isPlayingAsState()
    val toggleState = rememberToggleState(
        initialVisible = initialVisible,
        interactionSource = interactionSource, duration
    )
    toggleState.autoHideEnable(isPlaying)
    return toggleState
}

/**
 * Remember toggle state
 *
 * @param initialVisible Initial visibility.
 * @param interactionSource Interaction source to disable auto hide when user is dragging.
 * @param duration The duration after the view is hide.
 */
@Composable
fun rememberToggleState(
    initialVisible: Boolean,
    interactionSource: InteractionSource? = null,
    duration: Duration = 4.seconds,
): ToggleState {
    val toggleState = remember(initialVisible, duration) {
        ToggleState(initialVisible = initialVisible, duration = duration)
    }
    interactionSource?.let {
        toggleState.userInteracting.value = interactionSource.collectIsDraggedAsState().value
    }
    LaunchedEffect(toggleState.isVisible(), toggleState.isAutoHideEnabled(), toggleState.userInteracting.value) {
        toggleState.autoHide()
    }
    return toggleState
}

/**
 * Toggle view
 *
 * @param toggleState The toggle state for the view [rememberToggleState].
 * @param modifier The modifier for layout.
 * @param enter EnterTransition(s) used for the appearing animation, fading in while expanding by default.
 * @param exit ExitTransition(s) used for the disappearing animation, fading out while shrinking by default.
 * @param content Content to appear or disappear based on the value of [toggleState].
 * @receiver
 */
@Composable
fun ToggleView(
    toggleState: ToggleState,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    content: @Composable() AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visibleState = toggleState.visibleState,
        enter = enter,
        exit = exit,
        content = content
    )
}

@Composable
@Preview
private fun TogglePreview() {
    var isVisible by remember {
        mutableStateOf(true)
    }
    Column {
        val toggleState = rememberToggleState(
            initialVisible = isVisible,
            duration = 1.seconds
        )
        val duration = toggleState.visibleDuration.value
        ToggleView(
            toggleState = toggleState,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp, 120.dp)
                    .background(color = Color.Red),
                contentAlignment = Alignment.Center
            ) {
                BasicText(text = "View to auto hide")
            }
        }
        Row {
            BasicText(
                modifier = Modifier
                    .clickable { toggleState.setVisible(true) },
                text = "Visible"
            )
            BasicText(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { toggleState.toggleVisible() },
                text = "Toggle"
            )
            BasicText(
                modifier = Modifier
                    .padding(start = 4.dp)

                    .clickable { toggleState.setVisible(false) },
                text = "Hide"
            )
        }
        Row {
            BasicText(
                modifier = Modifier
                    .clickable { toggleState.autoHideEnable(true) },
                text = "Enable auto hide",
            )
            BasicText(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { toggleState.autoHideEnable(false) },
                text = "Disable auto hide"
            )
        }
        BasicText(
            modifier = Modifier
                .padding(start = 4.dp)
                .clickable { isVisible = !isVisible },
            text = "Toggle visible"
        )
        BasicText(text = duration.toString())
    }
}
