/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.SRGErrorMessageProvider

/**
 * Demo player view demonstrate how to integrate PlayerView with Compose
 *
 * doc : https://developer.android.com/jetpack/compose/interop/interop-apis#fragments-in-compose
 *
 * @param playerViewModel
 * @param pipClick picture in picture button click action
 */
@Composable
fun DemoPlayerView(
    playerViewModel: SimplePlayerViewModel,
    pipClick: () -> Unit = {}
) {
    val pauseOnBackground = playerViewModel.pauseOnBackground.collectAsState()
    val hideUi = playerViewModel.pictureInPictureEnabled.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        PlayerView(
            player = playerViewModel.player,
            useController = !hideUi.value,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
        if (!hideUi.value) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { playerViewModel.togglePauseOnBackground() }) {
                    Text(text = if (pauseOnBackground.value) "Pause on background" else "Continue on background")
                }
                IconButton(onClick = pipClick) {
                    Icon(imageVector = Icons.Default.PictureInPicture, contentDescription = "Go in picture in picture")
                }
            }
        }
    }
}

@Composable
private fun PlayerView(player: Player, modifier: Modifier = Modifier, useController: Boolean = true) {
    ScreenOnKeeper()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            androidx.media3.ui.PlayerView(context).also { view ->
                // Seems not working with Compose
                // view.keepScreenOn = true
                view.setShowBuffering(androidx.media3.ui.PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                view.setErrorMessageProvider(SRGErrorMessageProvider())
            }
        },
        update = { view ->
            view.player = player
            view.controllerAutoShow = useController
            view.useController = useController
        }
    )
}

/**
 * Screen on keeper
 *
 * source : https://stackoverflow.com/questions/69039723/is-there-a-jetpack-compose-equivalent-for-androidkeepscreenon-to-keep-screen-al
 */
@Composable
fun ScreenOnKeeper() {
    val activity = LocalContext.current as Activity

    DisposableEffect(Unit) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
