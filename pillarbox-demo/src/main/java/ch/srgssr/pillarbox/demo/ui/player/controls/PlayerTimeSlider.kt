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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import ch.srgssr.pillarbox.ui.ProgressTracker
import ch.srgssr.pillarbox.ui.rememberProgressTracker

/**
 * Player time slider
 *
 * @param player The [Player] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param sliderColors The slider colors to apply.
 * @param progressTracker The progress track.
 * @param interactionSource The Slider interaction source.
 */
@Composable
fun PlayerTimeSlider(
    player: Player,
    modifier: Modifier = Modifier,
    sliderColors: SliderColors = playerCustomColors(),
    progressTracker: ProgressTracker = rememberProgressTracker(player = player),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val sliderPosition = progressTracker.progressPercent()
    Slider(
        modifier = modifier,
        value = sliderPosition.value,
        interactionSource = interactionSource,
        onValueChange = progressTracker::userSeek,
        onValueChangeFinished = progressTracker::userSeekFinish,
        enabled = progressTracker.canSeek().value,
        colors = sliderColors,
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
