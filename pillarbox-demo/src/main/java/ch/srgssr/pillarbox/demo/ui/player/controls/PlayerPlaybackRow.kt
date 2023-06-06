/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.canPlayPause
import ch.srgssr.pillarbox.player.canSeekBack
import ch.srgssr.pillarbox.player.canSeekForward
import ch.srgssr.pillarbox.player.canSeekToNext
import ch.srgssr.pillarbox.player.canSeekToPrevious
import ch.srgssr.pillarbox.ui.availableCommandsAsState
import ch.srgssr.pillarbox.ui.isPlayingAsState

/**
 * Player playback button row.
 * @param player The [Player] to observe.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun PlayerPlaybackRow(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val availableCommands = player.availableCommandsAsState()
    val togglePlaybackFunction = remember(player) {
        {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekToDefaultPosition()
                player.play()
            } else {
                player.playWhenReady = !player.playWhenReady
            }
        }
    }
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(
            icon = Icons.Default.SkipPrevious,
            contentDescription = "Skip previous",
            isEnabled = availableCommands.canSeekToPrevious(),
            onClick = player::seekToPrevious
        )
        Button(
            icon = Icons.Default.FastRewind,
            contentDescription = "Fast rewind",
            isEnabled = availableCommands.canSeekBack(),
            onClick = player::seekBack
        )
        val isPlaying = player.isPlayingAsState()
        Button(
            isEnabled = availableCommands.canPlayPause(),
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            onClick = togglePlaybackFunction
        )
        Button(
            icon = Icons.Default.FastForward,
            contentDescription = "Fast forward",
            isEnabled = availableCommands.canSeekForward(),
            onClick = player::seekForward
        )
        Button(
            icon = Icons.Default.SkipNext,
            contentDescription = "Skip next",
            isEnabled = availableCommands.canSeekToNext(),
            onClick = player::seekToNext
        )
    }
}

@Composable
private fun Button(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    IconButton(modifier = modifier, onClick = onClick, enabled = isEnabled) {
        Icon(
            imageVector = icon, contentDescription = contentDescription,
            tint = if (isEnabled) Color.White else Color.LightGray,
        )
    }
}
