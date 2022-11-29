/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import ch.srg.pillarbox.ui.PlayerView
import ch.srg.pillarbox.ui.ScaleMode

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
    val mediaTitle = playerViewModel.mediaTitle.collectAsState()
    Column(modifier = Modifier.fillMaxWidth()) {
        PlayerView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = Color.Blue),
            player = playerViewModel.player, scaleMode = ScaleMode.Fit, contentAlignment = Alignment.Center
        ) {
            SimplePlayerControls(
                modifier = Modifier.fillMaxSize(),
                player = playerViewModel.player
            )
        }
        mediaTitle.value?.let {
            Text(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp), text = it, style = MaterialTheme.typography.h2)
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
