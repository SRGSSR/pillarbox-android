/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.ui.extension.currentMediaMetadataAsState
import ch.srgssr.pillarbox.ui.widget.player.PlayerFrame

/**
 * Updatable media item view
 */
@Composable
fun UpdatableMediaItemShowcase() {
    val updatableMediaItemViewModel: UpdatableMediaItemViewModel = viewModel()
    val player = updatableMediaItemViewModel.player
    val currentItem by player.currentMediaMetadataAsState()
    PlayerFrame(player = player) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            Text(
                color = Color.Green,
                text = "${currentItem.title}"
            )
        }
    }
    LifecycleResumeEffect(player) {
        player.play()
        onPauseOrDispose {
            player.pause()
        }
    }
}
