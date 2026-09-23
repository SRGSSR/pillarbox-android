/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.media3.common.Player
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import androidx.media3.ui.compose.state.rememberSeekBackButtonState
import androidx.media3.ui.compose.state.rememberSeekForwardButtonState
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * A row of playback controls for a [Player].
 *
 * @param player The [Player] instance to control.
 * @param modifier The [Modifier] to be applied to this row.
 */
@Composable
fun PlayerPlaybackRow(
    player: Player,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            val previousButtonState = rememberPreviousButtonState(player)
            val fastRewindButtonState = rememberSeekBackButtonState(player)
            val playPauseButtonState = rememberPlayPauseButtonState(player)
            val fastForwardButtonState = rememberSeekForwardButtonState(player)
            val nextButtonState = rememberNextButtonState(player)

            SkipPreviousButton(
                enabled = previousButtonState.isEnabled,
                onClick = previousButtonState::onClick,
            )

            FastRewindButton(
                enabled = fastRewindButtonState.isEnabled,
                onClick = fastRewindButtonState::onClick,
            )

            PlayPauseButton(
                enabled = playPauseButtonState.isEnabled,
                showPlay = playPauseButtonState.showPlay,
                onClick = playPauseButtonState::onClick,
            )

            FastForwardButton(
                enabled = fastForwardButtonState.isEnabled,
                onClick = fastForwardButtonState::onClick,
            )

            SkipNextButton(
                enabled = nextButtonState.isEnabled,
                onClick = nextButtonState::onClick,
            )
        }
    }
}

@Composable
private fun SkipPreviousButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            imageVector = Icons.Default.SkipPrevious,
            contentDescription = stringResource(R.string.previous_button),
        )
    }
}

@Composable
private fun FastRewindButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            imageVector = Icons.Default.FastRewind,
            contentDescription = stringResource(R.string.fast_rewind_button),
        )
    }
}

@Composable
private fun PlayPauseButton(
    enabled: Boolean,
    showPlay: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        val imageVector = if (showPlay) Icons.Default.PlayArrow else Icons.Default.Pause
        val contentDescription = if (showPlay) stringResource(R.string.play_button) else stringResource(R.string.pause_button)

        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun FastForwardButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            imageVector = Icons.Default.FastForward,
            contentDescription = stringResource(R.string.fast_forward_button),
        )
    }
}

@Composable
private fun SkipNextButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = stringResource(R.string.next_button),
        )
    }
}
