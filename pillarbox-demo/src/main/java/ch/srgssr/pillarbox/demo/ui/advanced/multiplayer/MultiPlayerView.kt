/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.advanced.multiplayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerView

/**
 * Multi player view
 *
 * When click on the muted player, will swap the two player,
 * One is muted, the other one is not muted and have the media notification, ie background playback.
 *
 * @param multiPlayerViewModel
 */
@Suppress("MagicNumber")
@Composable
fun MultiPlayerView(multiPlayerViewModel: MultiPlayerViewModel = viewModel()) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        SimplePlayerView(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(16 / 9f)
                .clickable {
                    multiPlayerViewModel.setMainPlayer(multiPlayerViewModel.player1)
                },
            player = multiPlayerViewModel.player1, showControls = false
        )

        SimplePlayerView(
            modifier = Modifier
                .aspectRatio(16 / 9f)
                .clickable {
                    multiPlayerViewModel.setMainPlayer(multiPlayerViewModel.player2)
                },
            player = multiPlayerViewModel.player2, showControls = false
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                multiPlayerViewModel.resume()
            } else if (event == Lifecycle.Event.ON_STOP) {
                multiPlayerViewModel.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
