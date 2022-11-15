/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import ch.srg.pillarbox.ui.PlayerView
import ch.srg.pillarbox.ui.ResizeMode
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
    PlayerView(
        modifier = Modifier
            .size(300.dp, 300.dp)
            .background(Color.Red),
        player = playerViewModel.player, resizeMode = ResizeMode.Fit, contentAlignment = Alignment.Center
    ) {
        SimplePlayerControls(
            modifier = Modifier.background(Black50),
            player = playerViewModel.player
        )
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
