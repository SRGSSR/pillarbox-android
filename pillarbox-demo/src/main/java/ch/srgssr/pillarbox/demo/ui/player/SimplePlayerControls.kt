/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.SRGErrorMessageProvider

/**
 * Simple player controls overlay
 *  - Error message
 *  - Playback controls
 *
 * @param player
 * @param modifier
 */
@Composable
fun SimplePlayerControls(player: Player, modifier: Modifier = Modifier) {
    val playerStates = rememberPlayerAsState(player = player)
    var controlVisible = remember {
        mutableStateOf(false)
    }
    Box(modifier = modifier.clickable { controlVisible.value = !controlVisible.value }) {
        when {
            playerStates.error.value != null -> {
                ErrorMessage(
                    modifier = Modifier
                        .matchParentSize(),
                    exception = playerStates.error.value!!
                )
            }
            controlVisible.value && playerStates.playbackState.value != Player.STATE_IDLE -> {
                PlaybackControls(
                    modifier = Modifier
                        .matchParentSize(),
                    player = player, playerStates = playerStates
                )
            }
        }
    }
}

/**
 * Display Error message
 */
@Composable
fun ErrorMessage(modifier: Modifier = Modifier, exception: PlaybackException) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colors.error),
        contentAlignment = Center
    ) {
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Center,
            text = SRGErrorMessageProvider().getErrorMessage(exception).second,
            color = MaterialTheme.colors.onError
        )
    }
}

/**
 * Display Playback controls
 *
 * When player is buffering, show a buffering indicator in place of the centre button.
 */
@Composable
fun PlaybackControls(player: Player, modifier: Modifier = Modifier, playerStates: PlayerStates = rememberPlayerAsState(player = player)) {
    Row(modifier = modifier.padding(12.dp), Arrangement.SpaceBetween) {
        Button(modifier = Modifier.align(CenterVertically), onClick = { player.seekToPrevious() }) {
            Image(imageVector = Icons.Filled.SkipPrevious, contentDescription = "Skip previous")
        }
        Button(modifier = Modifier.align(CenterVertically), onClick = { player.seekBack() }) {
            Image(imageVector = Icons.Filled.Replay10, contentDescription = "Replay 10 seconds")
        }

        if (playerStates.playbackState.value == Player.STATE_BUFFERING) {
            CircularProgressIndicator(modifier = Modifier.align(CenterVertically))
        } else {
            Button(modifier = Modifier.align(CenterVertically), onClick = { player.playWhenReady = !playerStates.isPlaying.value }) {
                val icon = if (playerStates.isPlaying.value) Icons.Filled.Pause else Icons.Filled.PlayArrow
                val contentDescription = if (playerStates.isPlaying.value) "Play" else "Pause"
                Image(imageVector = icon, contentDescription = contentDescription)
            }
        }

        Button(modifier = Modifier.align(CenterVertically), onClick = { player.seekForward() }) {
            Image(imageVector = Icons.Filled.Forward10, contentDescription = "Forward 10 seconds")
        }

        Button(modifier = Modifier.align(CenterVertically), onClick = { player.seekToNext() }) {
            Image(imageVector = Icons.Filled.SkipNext, contentDescription = "Skip next")
        }
    }
}
