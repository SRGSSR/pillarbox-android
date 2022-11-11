/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import ch.srg.pillarbox.ui.PlayerView
import ch.srgssr.pillarbox.demo.ui.theme.Black50

/**
 * Demo player view demonstrate how to integrate PlayerView with Compose
 *
 * doc : https://developer.android.com/jetpack/compose/interop/interop-apis#fragments-in-compose
 *
 * @param playerViewModel
 */
@Composable
fun DemoPlayerView(playerViewModel: SimplePlayerViewModel) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    Box(modifier = Modifier) {
        PlayerView(
            modifier = Modifier
                .background(color = Color.Black)
                .fillMaxWidth()
                .fillMaxHeight(),
            player = playerViewModel.player, crop = true, contentAlignment = Alignment.TopStart
        ) {
            SimplePlayerControls(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black50),
                player = playerViewModel.player
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                playerViewModel.resumePlayback()
            } else if (event == Lifecycle.Event.ON_STOP) {
                playerViewModel.pausePlayback()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
