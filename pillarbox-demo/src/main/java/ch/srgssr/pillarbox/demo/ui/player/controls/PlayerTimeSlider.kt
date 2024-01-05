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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.Pillarbox
import ch.srgssr.pillarbox.player.extension.canSeek
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.SimpleProgressTrackerState
import ch.srgssr.pillarbox.ui.SmoothProgressTrackerState
import ch.srgssr.pillarbox.ui.extension.availableCommandsAsState
import ch.srgssr.pillarbox.ui.extension.currentBufferedPercentageAsState
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration.Companion.milliseconds

/**
 * Creates a [ProgressTrackerState] to track manual changes made to the current media being player.
 *
 * @param player The [Player] to observe.
 * @param smoothTracker `true` to use smooth tracking, ie. the media position is updated while tracking is in progress, `false` to update the
 * media position only when tracking is finished.
 * @param coroutineScope
 */
@Composable
fun rememberProgressTrackerState(
    player: Player,
    smoothTracker: Boolean,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): ProgressTrackerState {
    return remember(player, smoothTracker) {
        if (smoothTracker && player is Pillarbox) {
            SmoothProgressTrackerState(player, coroutineScope)
        } else {
            SimpleProgressTrackerState(player, coroutineScope)
        }
    }
}

/**
 * Component used to display the time progression of the media being played, and manually changing the progression, if supported.
 *
 * @param player The [Player] to observe.
 * @param modifier The [Modifier] to apply to the layout.
 * @param progressTracker The progress tracker.
 * @param interactionSource The [Slider] interaction source.
 */
@Composable
fun PlayerTimeSlider(
    player: Player,
    modifier: Modifier = Modifier,
    progressTracker: ProgressTrackerState = rememberProgressTrackerState(player = player, smoothTracker = true),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val currentProgress by progressTracker.progress.collectAsState()
    val currentProgressPercent = currentProgress.inWholeMilliseconds / player.duration.coerceAtLeast(1).toFloat()
    val bufferPercentage by player.currentBufferedPercentageAsState()
    val availableCommands by player.availableCommandsAsState()
    Box(modifier = modifier) {
        Slider(
            value = bufferPercentage,
            onValueChange = {},
            enabled = false,
            colors = playerSecondaryColors(),
        )

        Slider(
            value = currentProgressPercent,
            onValueChange = { percent ->
                progressTracker.onChanged((percent * player.duration).toLong().milliseconds)
            },
            onValueChangeFinished = progressTracker::onFinished,
            enabled = availableCommands.canSeek(),
            colors = playerPrimaryColors(),
            interactionSource = interactionSource,
        )
    }
}

@Composable
private fun playerPrimaryColors(): SliderColors = SliderDefaults.colors(
    thumbColor = Color.White,
    activeTrackColor = Color.Red,
    activeTickColor = Color.Transparent,
    inactiveTrackColor = Color.Transparent,
    inactiveTickColor = Color.Transparent,
)

@Composable
private fun playerSecondaryColors(): SliderColors = SliderDefaults.colors(
    thumbColor = Color.Transparent,
    activeTrackColor = Color.Transparent,
    inactiveTrackColor = Color.Transparent,
    inactiveTickColor = Color.Transparent,
    disabledThumbColor = Color.Transparent,
    disabledActiveTrackColor = Color.Gray,
    disabledInactiveTrackColor = Color.White
)
