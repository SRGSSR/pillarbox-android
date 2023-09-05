/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.C
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.FasterSeeker
import ch.srgssr.pillarbox.player.bufferedPercentageAsFlow
import ch.srgssr.pillarbox.player.canSeek
import ch.srgssr.pillarbox.ui.availableCommandsAsState
import ch.srgssr.pillarbox.ui.currentPositionAsState
import ch.srgssr.pillarbox.ui.durationAsState

/**
 * Player time slider
 *
 * @param player The [StatefulPlayer] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param interactionSource The Slider interaction source.
 */
@Composable
fun PlayerTimeSlider(
    player: Player,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val fastSeeker = remember(player) {
        FasterSeeker(player)
    }
    DisposableEffect(player) {
        player.addListener(fastSeeker)
        onDispose {
            player.removeListener(fastSeeker)
        }
    }

    val bufferedPercentageFlow = remember(player) {
        player.bufferedPercentageAsFlow()
    }
    val bufferedPercentage = bufferedPercentageFlow.collectAsState(initial = 0.0f)

    TimeSlider(
        modifier = modifier,
        bufferingPercentage = bufferedPercentage.value,
        position = player.currentPositionAsState(),
        duration = player.durationAsState(),
        enabled = player.availableCommandsAsState().canSeek(),
        interactionSource = interactionSource,
        onSeek = { positionMs, finished ->
            if (finished) {
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    .setPreferredVideoRoleFlags(0)
                    .build()
            } else {
                if (!player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_AUDIO)) {
                    player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                        .setPreferredVideoRoleFlags(C.ROLE_FLAG_TRICK_PLAY)
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                        .build()
                }
            }
            player.playWhenReady = finished
            fastSeeker.seekTo(positionMs)
        }
    )
}

@Composable
private fun TimeSlider(
    position: Long,
    duration: Long,
    bufferingPercentage: Float = 0.0f,
    modifier: Modifier = Modifier,
    enabled: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onSeek: ((Long, Boolean) -> Unit)? = null,
) {
    val progressPercentage = position / duration.coerceAtLeast(1).toFloat()
    var sliderPosition by remember { mutableFloatStateOf(0.0f) }
    var isUserSeeking by remember { mutableStateOf(false) }
    if (!isUserSeeking) {
        sliderPosition = progressPercentage
    }
    Box(modifier = modifier) {
        Slider(
            modifier = Modifier.matchParentSize(),
            colors = playerSecondaryColors(),
            enabled = false,
            value = bufferingPercentage,
            onValueChange = {},
        )
        Slider(
            modifier = Modifier.matchParentSize(),
            value = sliderPosition,
            interactionSource = interactionSource,
            onValueChange = {
                isUserSeeking = true
                sliderPosition = it
                onSeek?.let { it1 -> it1((sliderPosition * duration).toLong(), false) }
            },
            onValueChangeFinished = {
                onSeek?.let { it((sliderPosition * duration).toLong(), true) }
                isUserSeeking = false
            },
            enabled = enabled,
            colors = playerCustomColors(),
        )
    }
}

@Composable
private fun playerCustomColors(): SliderColors = SliderDefaults.colors(
    activeTickColor = Color.Transparent,
    inactiveTickColor = Color.Transparent,
    inactiveTrackColor = Color.Transparent,
    activeTrackColor = Color.White,
    thumbColor = Color.White,
)

@Composable
private fun playerSecondaryColors(): SliderColors = SliderDefaults.colors(
    inactiveTickColor = Color.Transparent,
    inactiveTrackColor = Color.Transparent,
    activeTrackColor = Color.Transparent,
    thumbColor = Color.Transparent,
    disabledThumbColor = Color.Transparent,
    disabledActiveTrackColor = Color.Red,
    disabledInactiveTrackColor = Color.LightGray
)

@Preview(showBackground = false)
@Composable
fun TimeSliderPreview() {
    TimeSlider(position = 34 * 3600 * 1000L, duration = 67 * 3600 * 1000L, enabled = true)
}
