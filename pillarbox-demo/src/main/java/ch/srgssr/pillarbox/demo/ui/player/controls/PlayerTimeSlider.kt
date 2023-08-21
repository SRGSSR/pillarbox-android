/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.Player
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
    TimeSlider(
        modifier = modifier,
        position = player.currentPositionAsState(),
        duration = player.durationAsState(),
        enabled = player.availableCommandsAsState().canSeek(),
        interactionSource = interactionSource,
        onSeek = { positionMs, finished ->
            if (finished) {
                player.seekTo(positionMs)
            }
        }
    )
}

@Composable
private fun TimeSlider(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    enabled: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onSeek: ((Long, Boolean) -> Unit)? = null,
) {
    val progressPercentage = position / duration.coerceAtLeast(1).toFloat()
    var sliderPosition by remember { mutableStateOf(0.0f) }
    var isUserSeeking by remember { mutableStateOf(false) }
    if (!isUserSeeking) {
        sliderPosition = progressPercentage
    }
    Slider(
        modifier = modifier, value = sliderPosition,
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

@Composable
private fun playerCustomColors(): SliderColors = SliderDefaults.colors(
    activeTickColor = Color.Transparent,
    inactiveTickColor = Color.Transparent,
    inactiveTrackColor = Color.LightGray,
    activeTrackColor = Color.White,
    thumbColor = Color.White
)

@Preview(showBackground = false)
@Composable
fun TimeSliderPreview() {
    TimeSlider(position = 34 * 3600 * 1000L, duration = 67 * 3600 * 1000L, enabled = true)
}
