/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.SRGErrorMessageProvider

/**
 * Player view
 *
 * @param player
 * @param modifier
 * @param useController
 */
@Composable
fun PlayerView(player: Player?, modifier: Modifier = Modifier, useController: Boolean = true) {
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
