/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.ui.player.rememberProgressTrackerState
import ch.srgssr.pillarbox.demo.ui.player.LiveIndicator
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.extension.canSeek
import ch.srgssr.pillarbox.player.extension.getChapterAtPosition
import ch.srgssr.pillarbox.player.extension.isAtLiveEdge
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.extension.availableCommandsAsState
import ch.srgssr.pillarbox.ui.extension.currentMediaMetadataAsState
import ch.srgssr.pillarbox.ui.extension.currentPositionAsState
import ch.srgssr.pillarbox.ui.extension.isCurrentMediaItemLiveAsState
import ch.srgssr.pillarbox.ui.extension.playWhenReadyAsState
import kotlinx.coroutines.flow.map

/**
 * Player controls
 *
 * @param player The [Player] to interact with.
 * @param modifier The modifier to be applied to the layout.
 * @param backgroundColor The background color to apply behind the controls.
 * @param interactionSource The interaction source of the slider.
 * @param progressTracker The progress tracker.
 * @param credit The current credit, or `null`.
 * @param content The content to display under the slider.
 * @receiver
 */
@Composable
fun PlayerControls(
    player: Player,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black.copy(0.5f),
    interactionSource: MutableInteractionSource? = null,
    progressTracker: ProgressTrackerState = rememberProgressTrackerState(player = player),
    credit: Credit? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val currentMediaMetadata by player.currentMediaMetadataAsState()
    val currentChapterMediaMetadata by remember(player) {
        progressTracker.progress.map { player.getChapterAtPosition(it.inWholeMilliseconds)?.mediaMetadata }
    }.collectAsState(initial = player.getChapterAtPosition()?.mediaMetadata)

    val isCurrentItemLive by player.isCurrentMediaItemLiveAsState()
    val availableCommand by player.availableCommandsAsState()

    Box(
        modifier = modifier.background(color = backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        MediaMetadataView(
            modifier = Modifier
                .padding(MaterialTheme.paddings.baseline)
                .fillMaxWidth()
                .align(Alignment.TopStart),
            mediaMetadata = currentChapterMediaMetadata ?: currentMediaMetadata,
        )
        PlayerPlaybackRow(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            player = player
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            if (credit != null) {
                SkipButton(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(MaterialTheme.paddings.baseline),
                    onClick = { player.seekTo(credit.end) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (availableCommand.canSeek()) {
                    PlayerTimeSlider(
                        player = player,
                        progressTracker = progressTracker,
                        interactionSource = interactionSource
                    )
                }

                if (isCurrentItemLive) {
                    val currentPlaybackPosition by player.currentPositionAsState()
                    val isPlayWhenReady by player.playWhenReadyAsState()
                    val isAtLiveEdge = isPlayWhenReady && player.isAtLiveEdge(currentPlaybackPosition)
                    LiveIndicator(
                        modifier = Modifier
                            .padding(MaterialTheme.paddings.mini),
                        isAtLive = isAtLiveEdge,
                        onClick = {
                            player.seekToDefaultPosition()
                        }
                    )
                }
            }
            content(this)
        }
    }
}
