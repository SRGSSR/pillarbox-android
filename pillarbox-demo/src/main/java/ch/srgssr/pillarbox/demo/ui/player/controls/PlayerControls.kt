/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.LiveIndicator
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.extension.canSeek
import ch.srgssr.pillarbox.player.extension.isAtLiveEdge
import ch.srgssr.pillarbox.ui.extension.availableCommandsAsState
import ch.srgssr.pillarbox.demo.ui.player.chapters.ChapterList
import ch.srgssr.pillarbox.player.currentMediaItemAsFlow
import ch.srgssr.pillarbox.player.extension.getChapterAtPosition
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
 * @param content The content to display under the slider.
 * @receiver
 */
@Composable
fun PlayerControls(
    player: Player,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black.copy(0.5f),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) {
    val mediaMetadata by player.currentMediaMetadataAsState()
    val isCurrentItemLive by player.isCurrentMediaItemLiveAsState()
    val availableCommand by player.availableCommandsAsState()
    val mediaItemFlow = remember(player) {
        player.currentMediaItemAsFlow()
    }
    val mediaItem by mediaItemFlow.collectAsState(initial = player.currentMediaItem)

    Box(
        modifier = modifier.background(color = backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.align(Alignment.TopStart),
            text = mediaMetadata.title.toString(), color = Color.Gray
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (availableCommand.canSeek()) {
                    val progressTracker = rememberProgressTrackerState(player = player, smoothTracker = true)

                    PlayerTimeSlider(
                        modifier = Modifier,
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
            val chapters = mediaItem.getPillarboxDataOrNull()?.chapters
            if (!chapters.isNullOrEmpty()) {
                val currentPosition by player.currentPositionAsState()
                val progressPosition by progressTracker.progress.map {
                    it.inWholeMilliseconds
                }.collectAsState(initial = player.currentPosition)
                val isInteracting by interactionSource.collectIsDraggedAsState()
                val currentChapter by remember(player) {
                    derivedStateOf {
                        val pos = if (isInteracting) progressPosition else currentPosition
                        player.getChapterAtPosition(pos)
                    }
                }
                ChapterList(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(vertical = 12.dp),
                    chapters = chapters,
                    interactionSource = interactionSource,
                    currentChapter = currentChapter,
                    onChapterClicked = { chapter ->
                        player.seekTo(chapter.start)
                    },
                )
            }
        }
    }
}
