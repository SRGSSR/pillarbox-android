/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import ch.srgssr.pillarbox.player.PlayerState
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Animated visibility auto hide
 *
 * @param visible defines whether the content should be visible
 * @param playerState player state to listen isPlaying
 * @param modifier modifier for the [Layout] created to contain the [content]
 * @param interactionSource interaction source to keep control visibile
 * @param content content to hide or show
 */
@Composable
fun AnimatedVisibilityAutoHide(
    visible: Boolean,
    playerState: PlayerState,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val sliderDragged = interactionSource.collectIsDraggedAsState()
    var controlVisible by remember(visible) {
        mutableStateOf(visible)
    }
    val playerIsPlaying = playerState.isPlaying()
    LaunchedEffect(controlVisible, playerIsPlaying, sliderDragged.value) {
        if (playerIsPlaying && controlVisible && !sliderDragged.value) {
            delay(DEFAULT_DURATION)
            controlVisible = false
        }
    }
    AnimatedVisibility(
        visible = controlVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
        content = content
    )
}

private val DEFAULT_DURATION = 4_000.toDuration(DurationUnit.MILLISECONDS)
