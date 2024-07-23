/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.C
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.ui.components.PillarboxSlider
import ch.srgssr.pillarbox.demo.shared.ui.getFormatter
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.extension.canSeek
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.SimpleProgressTrackerState
import ch.srgssr.pillarbox.ui.SmoothProgressTrackerState
import ch.srgssr.pillarbox.ui.extension.availableCommandsAsState
import ch.srgssr.pillarbox.ui.extension.currentBufferedPercentageAsState
import ch.srgssr.pillarbox.ui.extension.durationAsState
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

/**
 * Creates a [ProgressTrackerState] to track manual changes made to the current media being player.
 *
 * @param player The [Player] to observe.
 * @param smoothTracker `true` to use smooth tracking, i.e., the media position is updated while tracking is in progress, `false` to update the
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
        if (smoothTracker && player is PillarboxExoPlayer) {
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
 * @param interactionSource The [PillarboxSlider] interaction source.
 */
@Composable
fun PlayerTimeSlider(
    player: Player,
    modifier: Modifier = Modifier,
    progressTracker: ProgressTrackerState = rememberProgressTrackerState(player = player, smoothTracker = true),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val progressTracker by rememberUpdatedState(progressTracker)
    val durationMs by player.durationAsState()
    val duration = remember(durationMs) {
        if (durationMs == C.TIME_UNSET) ZERO
        else durationMs.milliseconds
    }
    val currentProgress by progressTracker.progress.collectAsState()
    val currentProgressPercent = currentProgress.inWholeMilliseconds / player.duration.coerceAtLeast(1).toFloat()
    val bufferPercentage by player.currentBufferedPercentageAsState()
    val availableCommands by player.availableCommandsAsState()
    val formatter = duration.getFormatter()
    Row(
        modifier = modifier.padding(horizontal = MaterialTheme.paddings.mini),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.mini)
    ) {
        Text(text = formatter(currentProgress), color = Color.White)

        PillarboxSlider(
            value = currentProgressPercent,
            range = 0f..1f,
            compactMode = false,
            modifier = Modifier.weight(1f),
            secondaryValue = bufferPercentage,
            enabled = availableCommands.canSeek(),
            thumbColorEnabled = Color.White,
            thumbColorDisabled = Color.White,
            activeTrackColorEnabled = Color.Red,
            activeTrackColorDisabled = Color.Red,
            inactiveTrackColorEnabled = Color.White,
            inactiveTrackColorDisabled = Color.White,
            secondaryTrackColorEnabled = Color.Gray,
            secondaryTrackColorDisabled = Color.Gray,
            interactionSource = interactionSource,
            onValueChange = { percent ->
                progressTracker.onChanged((percent * player.duration).toLong().milliseconds)
            },
            onValueChangeFinished = progressTracker::onFinished,
        )

        Text(text = formatter(duration), color = Color.White)
    }
}
