/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import ch.srgssr.pillarbox.demo.ui.SRGErrorMessageProvider

/**
 * Demo player view demonstrate how to integrate PlayerView with Compose
 *
 * doc : https://developer.android.com/jetpack/compose/interop/interop-apis#fragments-in-compose
 *
 * @param player
 * @param notificationClicked action to show or hide notification
 */
@Composable
fun DemoPlayerView(player: Player, notificationClicked: (Boolean) -> Unit) {
    Column {
        PlayerView(player = player)
        NotificationActionView(notificationClicked = notificationClicked)
    }
}

@Composable
private fun NotificationActionView(notificationClicked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = { notificationClicked(true) }) {
            Text(text = "Show Notification")
        }
        Button(onClick = { notificationClicked(false) }) {
            Text(text = "Hide Notification")
        }
    }
}

@Composable
private fun PlayerView(player: Player) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    ScreenOnKeeper()
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { context ->
            PlayerView(context).also { view ->
                view.controllerAutoShow = true
                view.useController = true
                view.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                view.setErrorMessageProvider(SRGErrorMessageProvider())
                // view.keepScreenOn = true // doesn't work
            }
        },
        update = { view ->
            view.player = player
        }
    )
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                player.play()
            } else if (event == Lifecycle.Event.ON_STOP) {
                player.pause()
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
 * Keep screen on
 */
@Composable
fun ScreenOnKeeper() {
    val view = LocalView.current

    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }
}
