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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
        SimplePlayerView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            player = player
        )
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

/**
 * Simple player view
 *
 * @param modifier
 * @param player
 * @param showControls
 */
@Composable
fun SimplePlayerView(modifier: Modifier, player: Player, showControls: Boolean = true) {
    ScreenOnKeeper()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).also { view ->
                view.controllerAutoShow = true
                view.useController = showControls
                view.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                view.setErrorMessageProvider(SRGErrorMessageProvider())
                // view.keepScreenOn = true // doesn't work
            }
        },
        update = { view ->
            view.player = player
        }
    )
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
