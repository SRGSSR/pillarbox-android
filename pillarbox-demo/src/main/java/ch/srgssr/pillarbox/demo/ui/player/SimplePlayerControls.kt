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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.SRGErrorMessageProvider
import ch.srgssr.pillarbox.demo.ui.theme.Black50

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
    var controlVisible by remember {
        mutableStateOf(false)
    }
    Box(modifier = modifier.clickable { controlVisible = !controlVisible }) {
        when {
            playerStates.error.value != null -> {
                ErrorMessage(
                    modifier = Modifier
                        .matchParentSize(),
                    exception = playerStates.error.value!!
                )
            }
            controlVisible && playerStates.playbackState.value != Player.STATE_IDLE -> {
                PlaybackControls(
                    modifier = Modifier
                        .matchParentSize()
                        .background(color = Black50),
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
@Suppress("MagicNumber")
@Composable
fun PlaybackControls(player: Player, modifier: Modifier = Modifier, playerStates: PlayerStates = rememberPlayerAsState(player = player)) {
    BoxWithConstraints(modifier) {
        val buttonCountMax = when {
            maxWidth <= 190.dp -> 1
            maxWidth <= 380.dp -> 3
            else -> 5
        }
        Column(modifier = modifier, verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.padding(12.dp), Arrangement.Center) {
                if (buttonCountMax >= 5) {
                    Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = { player.seekToPrevious() }) {
                        Image(imageVector = Icons.Filled.SkipPrevious, contentDescription = "Skip previous")
                    }
                }
                if (buttonCountMax >= 3) {
                    Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = { player.seekBack() }) {
                        Image(imageVector = Icons.Filled.Replay10, contentDescription = "Replay 10 seconds")
                    }
                }

                if (playerStates.playbackState.value == Player.STATE_BUFFERING) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterVertically))
                } else {
                    Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = {
                        if (player.playbackState == Player.STATE_ENDED) {
                            player.seekToDefaultPosition()
                        }
                        player.playWhenReady = !playerStates.isPlaying.value
                    }) {
                        val icon = if (playerStates.isPlaying.value) Icons.Filled.Pause else Icons.Filled.PlayArrow
                        val contentDescription = if (playerStates.isPlaying.value) "Play" else "Pause"
                        Image(imageVector = icon, contentDescription = contentDescription)
                    }
                }

                if (buttonCountMax >= 3) {
                    Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = { player.seekForward() }) {
                        Image(imageVector = Icons.Filled.Forward10, contentDescription = "Forward 10 seconds")
                    }
                }

                if (buttonCountMax >= 5) {
                    Button(modifier = Modifier.align(Alignment.CenterVertically), onClick = { player.seekToNext() }) {
                        Image(imageVector = Icons.Filled.SkipNext, contentDescription = "Skip next")
                    }
                }
            }
            TimelineView(player = player, modifier = Modifier, playerStates = playerStates)
        }
    }
}

/**
 * Display a slider to seek in the media
 *
 * @param player
 * @param modifier
 * @param playerStates
 */
@Composable
fun TimelineView(player: Player, modifier: Modifier = Modifier, playerStates: PlayerStates = rememberPlayerAsState(player = player)) {
    val progressPercentage = playerStates.progressPercentage.collectAsState(initial = 0.0f)
    // LinearProgressIndicator(modifier = modifier, progress = progressPercentage.value)
    var sliderPosition by remember { mutableStateOf(0.0f) }
    var isUserSeeking by remember { mutableStateOf(false) }
    if (!isUserSeeking) {
        sliderPosition = progressPercentage.value
    }
    Slider(
        modifier = modifier, value = sliderPosition, onValueChange = {
            isUserSeeking = true
            sliderPosition = it
            player.seekTo((it * player.duration).toLong())
        },
        onValueChangeFinished = {
            isUserSeeking = false
        },
        enabled = playerStates.isContentSeekable.value
    )
}
