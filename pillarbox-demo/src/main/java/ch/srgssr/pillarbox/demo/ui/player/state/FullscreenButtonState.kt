/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.state

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Creates a [FullscreenButtonState] that is remembered across compositions.
 *
 * @return A [FullscreenButtonState] instance.
 */
@Composable
fun rememberFullscreenButtonState(): FullscreenButtonState {
    val activity = checkNotNull(LocalActivity.current)

    return remember(activity) { FullscreenButtonState(activity) }
}

/**
 * State that holds all interactions to correctly deal with a UI component representing a fullscreen button.
 *
 * @param activity The [Activity] that goes in and out of fullscreen.
 */
class FullscreenButtonState(activity: Activity) {
    private val windowInsetsController by lazy { activity.getWindowInsetsControllerCompat() }

    /**
     * Whether the [Activity] is currently in fullscreen mode.
     */
    var isInFullscreen by mutableStateOf(activity.isInFullscreen())
        private set

    /**
     * Enter or exit fullscreen mode.
     */
    fun onClick() {
        isInFullscreen = !isInFullscreen

        if (isInFullscreen) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun Activity.getWindowInsetsControllerCompat(): WindowInsetsControllerCompat {
        return WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun Activity.isInFullscreen(): Boolean {
        val insets = ViewCompat.getRootWindowInsets(window.decorView)

        return insets?.isVisible(WindowInsetsCompat.Type.systemBars()) ?: true
    }
}
