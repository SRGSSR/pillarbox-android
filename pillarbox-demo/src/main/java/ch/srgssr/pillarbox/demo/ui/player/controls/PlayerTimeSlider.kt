/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import ch.srgssr.pillarbox.ui.ProgressTracker
import ch.srgssr.pillarbox.ui.extension.currentBufferedPercentageAsState
import ch.srgssr.pillarbox.ui.rememberProgressTracker

/**
 * Player time slider
 *
 * @param player The [Player] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param progressTracker The progress track.
 * @param interactionSource The Slider interaction source.
 */
@Composable
fun PlayerTimeSlider(
    player: Player,
    modifier: Modifier = Modifier,
    progressTracker: ProgressTracker = rememberProgressTracker(player = player),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val sliderPosition by progressTracker.progressPercent()
    val bufferPercentage by player.currentBufferedPercentageAsState()
    val canSeek by progressTracker.canSeek()
    Box(modifier = modifier) {
        Slider(
            colors = playerSecondaryColors(),
            enabled = false,
            value = bufferPercentage,
            onValueChange = {},
        )

        Slider(
            value = sliderPosition,
            interactionSource = interactionSource,
            onValueChange = progressTracker::userSeek,
            onValueChangeFinished = progressTracker::userSeekFinished,
            enabled = canSeek,
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
    thumbColor = Color.White
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
