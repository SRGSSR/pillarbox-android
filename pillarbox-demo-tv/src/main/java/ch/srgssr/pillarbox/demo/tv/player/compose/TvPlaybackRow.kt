/*
 * Copyright (c) SRG SSR. All rights reserved.
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
import androidx.media3.common.Player
import androidx.tv.material3.ExperimentalTvMaterial3Api
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
import ch.srgssr.pillarbox.ui.widget.DelayedVisibilityState

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
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
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
