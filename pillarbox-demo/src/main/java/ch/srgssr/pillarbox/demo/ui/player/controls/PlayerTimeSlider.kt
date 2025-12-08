/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.C
import androidx.media3.common.Timeline.Window
import ch.srgssr.pillarbox.demo.shared.ui.components.PillarboxSlider
import ch.srgssr.pillarbox.demo.shared.ui.getFormatter
import ch.srgssr.pillarbox.demo.shared.ui.localTimeFormatter
import ch.srgssr.pillarbox.demo.shared.ui.player.rememberProgressTrackerState
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.canSeek
import ch.srgssr.pillarbox.player.extension.getUnixTimeMs
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.extension.availableCommandsAsState
import ch.srgssr.pillarbox.ui.extension.currentBufferedPercentageAsState
import ch.srgssr.pillarbox.ui.extension.durationAsState
import ch.srgssr.pillarbox.ui.extension.isCurrentMediaItemLiveAsState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/**
 * Component used to display the time progression of the media being played, and manually changing the progression, if supported.
 *
 * @param player The [PillarboxPlayer] to observe.
 * @param modifier The [Modifier] to apply to the layout.
 * @param progressTracker The progress tracker.
 * @param interactionSource The [PillarboxSlider] interaction source.
 */
@Composable
fun PlayerTimeSlider(
    player: PillarboxPlayer,
    modifier: Modifier = Modifier,
    progressTracker: ProgressTrackerState = rememberProgressTrackerState(player = player),
    interactionSource: MutableInteractionSource? = null,
) {
    val window = remember { Window() }
    val rememberedProgressTracker by rememberUpdatedState(progressTracker)
    val durationMs by player.durationAsState()
    val duration = remember(durationMs) {
        if (durationMs == C.TIME_UNSET) ZERO else durationMs.milliseconds
    }
    val currentProgress by rememberedProgressTracker.progress.collectAsState()
    val currentProgressPercent = currentProgress.inWholeMilliseconds / player.duration.coerceAtLeast(1).toFloat()
    val bufferPercentage by player.currentBufferedPercentageAsState()
    val availableCommands by player.availableCommandsAsState()
    val formatter = duration.getFormatter()
    val isDragged = interactionSource != null && interactionSource.collectIsDraggedAsState().value
    val isPressed = interactionSource != null && interactionSource.collectIsPressedAsState().value
    val compactSlider = !isDragged && !isPressed

    Row(
        modifier = modifier.padding(horizontal = MaterialTheme.paddings.mini),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.mini)
    ) {
        val isLive by player.isCurrentMediaItemLiveAsState()
        val timePosition = if (isLive) player.getUnixTimeMs(currentProgress.inWholeMilliseconds, window) else C.TIME_UNSET
        // We choose to display local time only when it is live, but it is possible to have timestamp inside VoD.
        val positionLabel =
            when (timePosition) {
                C.TIME_UNSET -> formatter(currentProgress)

                else -> {
                    val localTime = Instant.fromEpochMilliseconds(timePosition).toLocalDateTime(TimeZone.currentSystemDefault()).time
                    localTimeFormatter.format(localTime)
                }
            }

        Text(text = positionLabel, color = Color.White)

        PillarboxSlider(
            value = currentProgressPercent,
            range = 0f..1f,
            compactMode = compactSlider,
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
