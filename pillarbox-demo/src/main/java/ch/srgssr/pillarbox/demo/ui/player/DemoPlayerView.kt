/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.cast.CastPlayer
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.mediarouter.app.MediaRouteButton
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.ui.SRGErrorMessageProvider
import com.google.android.gms.cast.framework.CastButtonFactory

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

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }, actions = {
            MediaRouteButtonView()
        })
    }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            val currentPlayer = playerViewModel.playerController.currentPlayer.collectAsState()
            SimplePlayerView(modifier = Modifier.fillMaxWidth(), player = currentPlayer.value)
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

/**
 * Simple player view
 *
 * @param modifier
 * @param player
 */
@Composable
fun SimplePlayerView(modifier: Modifier, player: Player) {
    val isRemotePlayer = player is CastPlayer
    AndroidView(modifier = modifier, factory = { context ->
        PlayerView(context).also { view ->
            view.controllerAutoShow = true
            view.useController = true
            view.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            view.setErrorMessageProvider(SRGErrorMessageProvider())
        }
    }, update = { view ->
            view.player = player
            view.touchscreenBlocksFocus = isRemotePlayer
        })
}

/**
 * Media route button view
 */
@Composable
fun MediaRouteButtonView() {
    AndroidView(factory = { context ->
        MediaRouteButton(context).apply {
            CastButtonFactory.setUpMediaRouteButton(context, this)
            isEnabled = true
        }
    }, update = {})
}
