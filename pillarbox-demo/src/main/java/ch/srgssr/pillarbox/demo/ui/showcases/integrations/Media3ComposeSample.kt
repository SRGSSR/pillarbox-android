/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPresentationState
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.ui.widget.player.PlayerSubtitle

/**
 * Sample that shows Media3 compose ui components.
 */
@Composable
fun Media3ComposeSample() {
    val context = LocalContext.current
    val player = remember {
        PillarboxExoPlayer(context).apply {
            prepare()
            play()
            setMediaItem(SamplesSRG.OnDemandHorizontalVideo.toMediaItem())
        }
    }

    LifecycleStartEffect(Unit) {
        player.prepare()
        onStopOrDispose {
            player.stop()
        }
    }
    val presentationState = rememberPresentationState(player)
    Box(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .background(color = Color.Black)
                .clipToBounds()
                .resizeWithContentScale(
                    contentScale = ContentScale.Fit,
                    presentationState.videoSizeDp
                )

        ) {
            PlayerSurface(
                surfaceType = SURFACE_TYPE_SURFACE_VIEW,
                player = player
            )
            PlayerSubtitle(player = player, modifier = Modifier.fillMaxSize())
        }

        val playPauseState = rememberPlayPauseButtonState(player)
        val color = if (playPauseState.isEnabled) Color.White else Color.LightGray
        IconButton(
            modifier = Modifier.align(Alignment.Center),
            enabled = playPauseState.isEnabled,
            onClick = playPauseState::onClick
        ) {
            if (playPauseState.showPlay) {
                Icon(Icons.Default.PlayArrow, null, tint = color)
            } else {
                Icon(Icons.Default.Pause, null, tint = color)
            }
        }
    }
}
