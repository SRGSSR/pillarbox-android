/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.LiveIndicator
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.ui.extension.currentMediaMetadataAsState
import ch.srgssr.pillarbox.ui.extension.currentPositionAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentDefaultPositionAsState
import ch.srgssr.pillarbox.ui.extension.isCurrentMediaItemLiveAsState

private const val LiveEdgeThreshold = 5_000L

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
    val currentPlaybackPosition by player.currentPositionAsState()
    val currentDefaultPosition by player.getCurrentDefaultPositionAsState()
    val isAtLiveEdge = currentPlaybackPosition >= (currentDefaultPosition - LiveEdgeThreshold)

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerTimeSlider(
                    modifier = Modifier.weight(1f),
                    player = player,
                    interactionSource = interactionSource
                )
                if (isCurrentItemLive) {
                    LiveIndicator(
                        modifier = Modifier.padding(MaterialTheme.paddings.mini),
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
