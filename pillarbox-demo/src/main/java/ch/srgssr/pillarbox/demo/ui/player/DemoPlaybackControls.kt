/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.canPlayPause
import ch.srgssr.pillarbox.player.canSeek
import ch.srgssr.pillarbox.player.canSeekBack
import ch.srgssr.pillarbox.player.canSeekForward
import ch.srgssr.pillarbox.player.canSeekToNext
import ch.srgssr.pillarbox.player.canSeekToPrevious
import ch.srgssr.pillarbox.player.viewmodel.PlayerViewModel
import ch.srgssr.pillarbox.ui.viewmodel.availableCommands
import ch.srgssr.pillarbox.ui.viewmodel.currentPosition
import ch.srgssr.pillarbox.ui.viewmodel.duration
import ch.srgssr.pillarbox.ui.viewmodel.isPlaying
import ch.srgssr.pillarbox.ui.viewmodel.playbackState
import ch.srgssr.pillarbox.ui.viewmodel.rememberPlayerViewModel

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
    val playerViewModel: PlayerViewModel = rememberPlayerViewModel(player = player)
    Box(modifier = modifier) {
        if (playerViewModel.playbackState() == Player.STATE_BUFFERING) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        }
        PlaybackButtonRow(player = player, playerViewModel = playerViewModel, modifier = Modifier.align(Alignment.Center))
        PlayerTimeSlider(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp),
            playerViewModel = playerViewModel,
            onSeek = { positionMs, finished ->
                if (finished) {
                    player.seekTo(positionMs)
                }
            }
        )
    }
}

@Composable
private fun PlaybackButtonRow(player: Player, playerViewModel: PlayerViewModel, modifier: Modifier = Modifier) {
    val availableCommands = playerViewModel.availableCommands()
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(icon = Icons.Default.SkipPrevious, contentDescription = "Skip previous", isEnable = availableCommands.canSeekToPrevious()) {
            player.seekToPrevious()
        }
        Button(icon = Icons.Default.FastRewind, contentDescription = "Fast rewind", isEnable = availableCommands.canSeekBack()) {
            player.seekBack()
        }
        val isPlaying = playerViewModel.isPlaying()
        Button(
            isEnable = availableCommands.canPlayPause(),
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play"
        ) {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekToDefaultPosition()
                player.play()
            } else {
                player.playWhenReady = !player.playWhenReady
            }
        }
        Button(icon = Icons.Default.FastForward, contentDescription = "Fast forward", isEnable = availableCommands.canSeekForward()) {
            player.seekForward()
        }
        Button(icon = Icons.Default.SkipNext, contentDescription = "Skip next", isEnable = availableCommands.canSeekToNext()) {
            player.seekToNext()
        }
    }
}

@Composable
private fun Button(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    isEnable: Boolean = true,
    onClick: () -> Unit = {}
) {
    IconButton(modifier = modifier, onClick = onClick, enabled = isEnable) {
        Icon(
            imageVector = icon, contentDescription = contentDescription,
            tint = if (isEnable) Color.White else Color.LightGray,
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

/**
 * Player slider with [PlayerViewModel] Avoid recomposition of the whole view
 */
@Composable
private fun PlayerTimeSlider(
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    onSeek: ((Long, Boolean) -> Unit)? = null
) {
    TimeSlider(
        modifier = modifier,
        position = playerViewModel.currentPosition(),
        duration = playerViewModel.duration(),
        enabled = playerViewModel.availableCommands().canSeek(),
        onSeek = onSeek
    )
}

@Composable
private fun TimeSlider(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    enabled: Boolean = false,
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
