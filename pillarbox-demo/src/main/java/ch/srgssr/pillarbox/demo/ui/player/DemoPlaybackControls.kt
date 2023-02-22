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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.viewmodel.PlayerViewModel
import ch.srgssr.pillarbox.ui.viewmodel.currentPosition
import ch.srgssr.pillarbox.ui.viewmodel.duration
import ch.srgssr.pillarbox.ui.viewmodel.isCurrentMediaItemSeekable
import ch.srgssr.pillarbox.ui.viewmodel.isPlaying
import ch.srgssr.pillarbox.ui.viewmodel.rememberPlayerViewModel

/**
 * Demo controls
 *
 * @param player
 * @param modifier
 * @param playerViewModel
 */
@Composable
fun DemoPlaybackControls(
    player: Player,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = rememberPlayerViewModel(player = player)
) {
    Box(modifier = modifier) {
        Row(modifier = Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.SpaceEvenly) {
            ButtonPlayback(isPlaying = playerViewModel.isPlaying()) {
                if (player.playbackState == Player.STATE_ENDED) {
                    player.seekToDefaultPosition()
                    player.play()
                } else {
                    player.playWhenReady = !player.playWhenReady
                }
            }
        }
        PlayerSlider(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp),
            position = playerViewModel.currentPosition(),
            duration = playerViewModel.duration(),
            enabled = playerViewModel.isCurrentMediaItemSeekable(),
            onSeek = { positionMs, finished ->
                if (finished) {
                    player.seekTo(positionMs)
                }
            }
        )
    }
}

/**
 * Button playback
 *
 * @param isPlaying
 * @param modifier
 * @param onClick
 * @receiver
 */
@Composable
fun ButtonPlayback(isPlaying: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonPlaybackPreview() {
    ButtonPlayback(isPlaying = false)
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
 * Player slider
 *
 * @param position
 * @param duration
 * @param modifier
 * @param enabled
 * @param onSeek
 */
@Composable
fun PlayerSlider(
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

@Preview(showBackground = true)
@Composable
fun TimeSliderPreview() {
    PlayerSlider(position = 34 * 3600 * 1000L, duration = 67 * 3600 * 1000L, enabled = true)
}
