/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Screen on keeper
 * @param keepScreenOn keeps the screen on, disable it when disposed or set to false
 *
 * source : https://stackoverflow.com/questions/69039723/is-there-a-jetpack-compose-equivalent-for-androidkeepscreenon-to-keep-screen-al
 */
@Composable
fun ScreenOnKeeper(keepScreenOn: Boolean) {
    val activity = LocalContext.current as Activity

    DisposableEffect(Unit) {
        enableScreenOn(activity, keepScreenOn)
        onDispose {
            enableScreenOn(activity, false)
        }
    }
}

private fun enableScreenOn(activity: Activity, enable: Boolean) {
    if (enable) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
