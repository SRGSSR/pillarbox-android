/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts.thumbnail

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerControls
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface

/**
 * Thumbnail view
 */
@Composable
fun ThumbnailView() {
    val thumbnailViewModel = viewModel<ThumbnailViewModel>()
    val player = thumbnailViewModel.player
    LifecycleResumeEffect(Unit) {
        player.play()
        onPauseOrDispose {
            player.pause()
        }
    }

    Box {
        PlayerSurface(player) {
            val thumbnail: Bitmap? by thumbnailViewModel.thumbnail
            thumbnail?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        }
        val interactionSource = remember { MutableInteractionSource() }
        PlayerControls(
            modifier = Modifier.matchParentSize(),
            player = player,
            progressTracker = thumbnailViewModel.progressTrackerState,
            interactionSource = interactionSource
        ) {}
    }
}
