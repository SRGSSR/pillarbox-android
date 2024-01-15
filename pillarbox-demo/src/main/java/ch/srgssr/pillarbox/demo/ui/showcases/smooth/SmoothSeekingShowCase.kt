/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.smooth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerPlaybackRow
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerTimeSlider
import ch.srgssr.pillarbox.demo.ui.player.controls.rememberProgressTrackerState
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView
import ch.srgssr.pillarbox.ui.extension.playbackStateAsState
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface

/**
 * Smooth seeking show case
 */
@Composable
fun SmoothSeekingShowCase() {
    val smoothSeekingViewModel: SmoothSeekingViewModel = viewModel()
    val player = smoothSeekingViewModel.player
    var smoothSeekingEnabled by remember {
        mutableStateOf(false)
    }

    Column {
        Box(modifier = Modifier.aspectRatio(16 / 9f)) {
            val playbackState by player.playbackStateAsState()
            val isBuffering = playbackState == Player.STATE_BUFFERING
            PlayerSurface(player = player) {
                if (isBuffering) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                    }
                }
                ExoPlayerSubtitleView(player = player)
            }
            PlayerPlaybackRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                player = player
            )
            PlayerTimeSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.paddings.small)
                    .align(Alignment.BottomCenter),
                player = player,
                progressTracker = rememberProgressTrackerState(player = player, smoothTracker = smoothSeekingEnabled)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.paddings.baseline),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = smoothSeekingEnabled,
                onCheckedChange = { enabled ->
                    smoothSeekingEnabled = enabled
                    smoothSeekingViewModel.setSmoothSeekingEnabled(enabled)
                }
            )

            Text(text = stringResource(id = R.string.smooth_seeking_example))
        }
    }
    LifecycleStartEffect(Unit) {
        player.play()

        onStopOrDispose {
            player.pause()
        }
    }
}
