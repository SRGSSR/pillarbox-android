/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts.thumbnail

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.image.ImageOutput
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import ch.srgssr.media.maestro.MediaRouteButton
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.ui.SmoothProgressTrackerState

/**
 * Thumbnail view
 */
@Composable
fun ThumbnailView() {
    val thumbnailViewModel = viewModel<ThumbnailViewModel>()
    val player = thumbnailViewModel.player
    player?.let {
        PlayerView(modifier = Modifier.fillMaxWidth(), player, thumbnailViewModel, thumbnailViewModel.thumbnail)
    }
}

@Suppress("ForbiddenComment")
@Composable
private fun PlayerView(
    modifier: Modifier = Modifier,
    player: PillarboxPlayer,
    imageOutput: ImageOutput,
    thumbnail: Bitmap?
) {
    LifecycleResumeEffect(player) {
        player.play()
        onPauseOrDispose {
            player.pause()
        }
    }
    val coroutineScope = rememberCoroutineScope()

    // FIXME use rememberProgressTrackerState updated with imageOutput once #1082 is merged.
    val progressTracker = remember(player) {
        SmoothProgressTrackerState(player, coroutineScope, imageOutput)
    }
    Box(modifier) {
        PlayerView(
            player,
            progressTracker = progressTracker
        )

        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = thumbnail != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            thumbnail?.let {
                Image(
                    bitmap = thumbnail.asImageBitmap(),
                    contentDescription = null,
                )
            } ?: Box(
                Modifier
                    .background(color = Color.Black)
            )
        }
        MediaRouteButton(
            modifier = Modifier.align(Alignment.TopEnd),
            routeSelector = MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                .build(),
            colors = IconButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.White,
            ),
        )
    }
}
