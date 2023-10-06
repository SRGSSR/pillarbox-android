/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.player.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import ch.srgssr.pillarbox.player.canSeekBack
import ch.srgssr.pillarbox.player.canSeekForward
import ch.srgssr.pillarbox.player.canSeekToNext
import ch.srgssr.pillarbox.player.canSeekToPrevious
import ch.srgssr.pillarbox.ui.availableCommandsAsState
import ch.srgssr.pillarbox.ui.isPlayingAsState
import ch.srgssr.pillarbox.ui.layout.DelayedVisibilityState

/**
 * Tv playback row
 *
 * @param player
 * @param state
 * @param modifier
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvPlaybackRow(
    player: Player,
    state: DelayedVisibilityState,
    modifier: Modifier = Modifier,
) {
    val isPlaying = player.isPlayingAsState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(state.isVisible) {
        if (state.isVisible) {
            focusRequester.requestFocus()
        }
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(
            modifier = Modifier,
            enabled = player.availableCommandsAsState().canSeekToPrevious(),
            onClick = {
                player.seekToPrevious()
            },
        ) {
            Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null)
        }

        IconButton(
            modifier = Modifier,
            enabled = player.availableCommandsAsState().canSeekBack(),
            onClick = {
                player.seekBack()
            },
        ) {
            Icon(imageVector = Icons.Default.FastRewind, contentDescription = null)
        }

        IconButton(
            modifier = Modifier.focusRequester(focusRequester),
            onClick = {
                player.playWhenReady = !player.playWhenReady
            },
        ) {
            if (isPlaying) {
                Icon(imageVector = Icons.Default.Pause, contentDescription = null)
            } else {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
            }
        }

        IconButton(
            modifier = Modifier,
            enabled = player.availableCommandsAsState().canSeekForward(),
            onClick = {
                player.seekForward()
            },
        ) {
            Icon(imageVector = Icons.Default.FastForward, contentDescription = null)
        }

        IconButton(
            modifier = Modifier,
            enabled = player.availableCommandsAsState().canSeekToNext(),
            onClick = {
                player.seekToNext()
            },
        ) {
            Icon(imageVector = Icons.Default.SkipNext, contentDescription = null)
        }
    }
}
