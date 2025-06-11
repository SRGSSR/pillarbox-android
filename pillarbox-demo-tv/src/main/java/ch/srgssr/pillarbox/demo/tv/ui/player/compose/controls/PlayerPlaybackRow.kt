/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.player.extension.canSeekBack
import ch.srgssr.pillarbox.player.extension.canSeekForward
import ch.srgssr.pillarbox.player.extension.canSeekToNext
import ch.srgssr.pillarbox.player.extension.canSeekToPrevious
import ch.srgssr.pillarbox.ui.extension.availableCommandsAsState
import ch.srgssr.pillarbox.ui.extension.isPlayingAsState

/**
 * Tv playback row
 *
 * @param player
 * @param modifier
 */
@Composable
fun PlayerPlaybackRow(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val isPlaying by player.isPlayingAsState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val availableCommands by player.availableCommandsAsState()
        val toggleOrResumePlayback = remember(player) {
            {
                if (player.playbackState == Player.STATE_IDLE) {
                    player.prepare()
                }
                if (player.playbackState == Player.STATE_ENDED) {
                    player.seekToDefaultPosition()
                } else {
                    player.playWhenReady = !player.playWhenReady
                }
            }
        }
        IconButton(
            enabled = availableCommands.canSeekToPrevious(),
            onClick = player::seekToPrevious,
        ) {
            Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null)
        }

        IconButton(
            enabled = availableCommands.canSeekBack(),
            onClick = player::seekBack,
        ) {
            Icon(imageVector = Icons.Default.FastRewind, contentDescription = null)
        }

        IconButton(
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(56.dp),
            onClick = toggleOrResumePlayback,
        ) {
            if (isPlaying) {
                Icon(imageVector = Icons.Default.Pause, contentDescription = null)
            } else {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
            }
        }

        IconButton(
            enabled = availableCommands.canSeekForward(),
            onClick = player::seekForward,
        ) {
            Icon(imageVector = Icons.Default.FastForward, contentDescription = null)
        }

        IconButton(
            enabled = availableCommands.canSeekToNext(),
            onClick = player::seekToNext,
        ) {
            Icon(imageVector = Icons.Default.SkipNext, contentDescription = null)
        }
    }
}
