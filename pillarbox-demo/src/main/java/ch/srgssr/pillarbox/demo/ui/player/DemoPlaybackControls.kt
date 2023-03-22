/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import ch.srgssr.pillarbox.core.business.SRGErrorMessageProvider
import ch.srgssr.pillarbox.player.PlayerState
import ch.srgssr.pillarbox.player.canPlayPause
import ch.srgssr.pillarbox.player.canSeek
import ch.srgssr.pillarbox.player.canSeekBack
import ch.srgssr.pillarbox.player.canSeekForward
import ch.srgssr.pillarbox.player.canSeekToNext
import ch.srgssr.pillarbox.player.canSeekToPrevious
import ch.srgssr.pillarbox.ui.availableCommands
import ch.srgssr.pillarbox.ui.currentPosition
import ch.srgssr.pillarbox.ui.duration
import ch.srgssr.pillarbox.ui.isPlaying
import ch.srgssr.pillarbox.ui.playbackState
import ch.srgssr.pillarbox.ui.playerError
import ch.srgssr.pillarbox.ui.rememberPlayerState
import kotlinx.coroutines.delay

/**
 * Demo controls
 *
 * @param player
 * @param modifier
 */
@Composable
fun DemoPlaybackControls(
    player: Player,
    modifier: Modifier = Modifier
) {
    val playerState: PlayerState = rememberPlayerState(player = player)
    val playerError = playerState.playerError()
    if (playerError != null) {
        PlayerError(playerError = playerError, onClick = player::prepare)
    } else {
        val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
        val sliderDragged = interactionSource.collectIsDraggedAsState()
        var controlVisible by remember {
            mutableStateOf(true)
        }
        val playerIsPlaying = playerState.isPlaying()
        LaunchedEffect(controlVisible, playerIsPlaying, sliderDragged.value) {
            if (playerIsPlaying && controlVisible && !sliderDragged.value) {
                delay(3000)
                controlVisible = false
            }
        }
        Box(
            modifier = modifier
                .clickable { controlVisible = !controlVisible }
        ) {
            if (playerState.playbackState() == Player.STATE_BUFFERING) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            }
            AnimatedVisibility(
                visible = controlVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .matchParentSize()
                    .align(Alignment.Center)
            ) {
                Box(modifier = Modifier) {
                    PlaybackButtonRow(player = player, playerState = playerState, Modifier.align(Alignment.Center))
                    TimeSlider(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(8.dp),
                        position = playerState.currentPosition(),
                        duration = playerState.duration(),
                        enabled = playerState.availableCommands().canSeek(),
                        interactionSource = interactionSource,
                        onSeek = { positionMs, finished ->
                            if (finished) {
                                player.seekTo(positionMs)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerError(playerError: PlaybackException, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val errorMessageProvider = remember {
        SRGErrorMessageProvider()
    }
    Box(modifier = modifier.clickable { onClick.invoke() }) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = errorMessageProvider.getErrorMessage(playerError).second,
                color = Color.White
            )
            Text(text = "Tap to retry!", color = Color.LightGray, fontStyle = FontStyle.Italic)
        }
    }
}

@Composable
private fun PlaybackButtonRow(player: Player, playerState: PlayerState, modifier: Modifier = Modifier) {
    val availableCommands = playerState.availableCommands()
    val togglePlaybackFunction = remember {
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
        val isPlaying = playerState.isPlaying()
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

@Composable
private fun playerCustomColors(): SliderColors = SliderDefaults.colors(
    activeTickColor = Color.Transparent,
    inactiveTickColor = Color.Transparent,
    inactiveTrackColor = Color.LightGray,
    activeTrackColor = Color.White,
    thumbColor = Color.White
)

@Composable
private fun TimeSlider(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    enabled: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onSeek: ((Long, Boolean) -> Unit)? = null,
) {
    val progressPercentage = position / duration.coerceAtLeast(1).toFloat()
    var sliderPosition by remember { mutableStateOf(0.0f) }
    var isUserSeeking by remember { mutableStateOf(false) }
    if (!isUserSeeking) {
        sliderPosition = progressPercentage
    }
    Slider(
        modifier = modifier, value = sliderPosition,
        interactionSource = interactionSource,
        onValueChange = {
            isUserSeeking = true
            sliderPosition = it
            onSeek?.let { it1 -> it1((sliderPosition * duration).toLong(), false) }
        },
        onValueChangeFinished = {
            onSeek?.let { it((sliderPosition * duration).toLong(), true) }
            isUserSeeking = false
        },
        enabled = enabled,
        colors = playerCustomColors(),
    )
}

@Preview(showBackground = false)
@Composable
fun TimeSliderPreview() {
    TimeSlider(position = 34 * 3600 * 1000L, duration = 67 * 3600 * 1000L, enabled = true)
}
