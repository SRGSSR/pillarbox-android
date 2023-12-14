/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.extension

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

private val DPadEventsKeyCodes = listOf(
    Key.DirectionLeft,
    Key.SystemNavigationLeft,
    Key.DirectionUp,
    Key.SystemNavigationUp,
    Key.DirectionRight,
    Key.SystemNavigationRight,
    Key.DirectionDown,
    Key.SystemNavigationDown,
    Key.DirectionCenter,
    Key.Enter,
    Key.NumPadEnter,
)

/**
 * Handle d pad key events
 *
 * @param onLeft action when left button is pressed.
 * @param onUp action when up button is pressed.
 * @param onRight action when right button is pressed.
 * @param onDown action when down button is pressed.
 * @param onEnter action when enter button is pressed.
 */
fun Modifier.handleDPadKeyEvents(
    onLeft: (() -> Unit)? = null,
    onUp: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null,
    onDown: (() -> Unit)? = null,
    onEnter: (() -> Unit)? = null,
) = onPreviewKeyEvent {
    if (it.type != KeyEventType.KeyUp || it.key !in DPadEventsKeyCodes) {
        return@onPreviewKeyEvent false
    }

    when (it.key) {
        Key.DirectionLeft,
        Key.SystemNavigationLeft,
        -> onLeft?.let {
            it()
            return@onPreviewKeyEvent true
        }

        Key.DirectionUp,
        Key.SystemNavigationUp,
        -> onUp?.let {
            it()
            return@onPreviewKeyEvent true
        }

        Key.DirectionRight,
        Key.SystemNavigationRight,
        -> onRight?.let {
            it()
            return@onPreviewKeyEvent true
        }

        Key.DirectionDown,
        Key.SystemNavigationDown,
        -> onDown?.let {
            it()
            return@onPreviewKeyEvent true
        }

        Key.Enter,
        Key.DirectionCenter,
        Key.NumPadEnter,
        -> onEnter?.let {
            it()
            return@onPreviewKeyEvent true
        }
    }

    false
}
