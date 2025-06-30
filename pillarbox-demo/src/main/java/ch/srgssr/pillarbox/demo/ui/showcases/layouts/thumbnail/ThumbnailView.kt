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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.image.ImageOutput
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import ch.srgssr.media.maestro.MediaRouteButton
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerControls
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.ui.SmoothProgressTrackerState
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import androidx.compose.ui.graphics.Color as ComposeColor

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
        PlayerSurface(player) {
            AnimatedVisibility(
                thumbnail != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                thumbnail?.let {
                    Image(
                        modifier = modifier.fillMaxSize(),
                        bitmap = thumbnail.asImageBitmap(),
                        contentDescription = null,
                    )
                } ?: Box(modifier.fillMaxSize().background(color = ComposeColor.Black))
            }
        }
        val interactionSource = remember { MutableInteractionSource() }
        PlayerControls(
            modifier = Modifier.matchParentSize(),
            player = player,
            progressTracker = progressTracker,
            interactionSource = interactionSource
        ) {}

        MediaRouteButton(
            modifier = Modifier.align(Alignment.TopEnd),
            routeSelector = MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                .build(),
            colors = IconButtonColors(
                containerColor = ComposeColor.Transparent,
                contentColor = ComposeColor.White,
                disabledContainerColor = ComposeColor.Transparent,
                disabledContentColor = ComposeColor.White,
            ),
        )
    }
}
