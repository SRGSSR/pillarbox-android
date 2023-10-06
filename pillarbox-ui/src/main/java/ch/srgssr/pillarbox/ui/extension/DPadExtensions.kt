/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.extension

import android.os.Build
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_DPAD_CENTER
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.KeyEvent.KEYCODE_NUMPAD_ENTER
import android.view.KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT
import android.view.KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent

private val DPadEventsKeyCodes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
    listOf(
        KEYCODE_DPAD_LEFT,
        KEYCODE_SYSTEM_NAVIGATION_LEFT,
        KEYCODE_DPAD_RIGHT,
        KEYCODE_SYSTEM_NAVIGATION_RIGHT,
        KEYCODE_DPAD_CENTER,
        KEYCODE_ENTER,
        KEYCODE_NUMPAD_ENTER,
    )
} else {
    listOf(
        KEYCODE_DPAD_LEFT,
        KEYCODE_DPAD_RIGHT,
        KEYCODE_DPAD_CENTER,
        KEYCODE_ENTER,
        KEYCODE_NUMPAD_ENTER,
    )
}

/**
 * Handle d pad key events
 *
 * @param onLeft action when left button is pressed.
 * @param onRight action when right button is pressed.
 * @param onEnter action when enter button is pressed.
 */
fun Modifier.handleDPadKeyEvents(
    onLeft: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null,
    onEnter: (() -> Unit)? = null,
) = onPreviewKeyEvent {
    fun onActionUp(block: () -> Unit) {
        if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) block()
    }

    if (!DPadEventsKeyCodes.contains(it.nativeKeyEvent.keyCode)) return@onPreviewKeyEvent false

    when (it.nativeKeyEvent.keyCode) {
        KEYCODE_ENTER,
        KEYCODE_DPAD_CENTER,
        KEYCODE_NUMPAD_ENTER,
        -> {
            onEnter?.apply {
                onActionUp(::invoke)
                return@onPreviewKeyEvent true
            }
        }

        KEYCODE_DPAD_LEFT,
        KEYCODE_SYSTEM_NAVIGATION_LEFT,
        -> {
            onLeft?.apply {
                onActionUp(::invoke)
                return@onPreviewKeyEvent true
            }
        }

        KEYCODE_DPAD_RIGHT,
        KEYCODE_SYSTEM_NAVIGATION_RIGHT,
        -> {
            onRight?.apply {
                onActionUp(::invoke)
                return@onPreviewKeyEvent true
            }
        }
    }
    false
}
