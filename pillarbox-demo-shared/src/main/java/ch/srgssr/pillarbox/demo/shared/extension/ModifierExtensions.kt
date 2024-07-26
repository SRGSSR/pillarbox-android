/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.extension

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * This [Modifier] allows you to define actions to perform when a button of the D-pad or the back button is press. Each action returns a [Boolean]
 * to indicate if the event was handled or not.
 *
 * @param eventType The event type to check before calling the provided actions.
 * @param onLeft The action to perform when the left button is press.
 * @param onUp The action to perform when the up button is press.
 * @param onRight The action to perform when the right button is press.
 * @param onDown The action to perform when the down button is press.
 * @param onEnter The action to perform when the enter button is press.
 * @param onBack The action to perform when the back button is press.
 */
fun Modifier.onDpadEvent(
    eventType: KeyEventType = KeyEventType.KeyDown,
    onLeft: () -> Boolean = { false },
    onUp: () -> Boolean = { false },
    onRight: () -> Boolean = { false },
    onDown: () -> Boolean = { false },
    onEnter: () -> Boolean = { false },
    onBack: () -> Boolean = { false }
): Modifier {
    return onPreviewKeyEvent {
        if (it.type == eventType) {
            when (it.key) {
                Key.DirectionLeft,
                Key.SystemNavigationLeft,
                -> onLeft()

                Key.DirectionUp,
                Key.SystemNavigationUp,
                -> onUp()

                Key.DirectionRight,
                Key.SystemNavigationRight,
                -> onRight()

                Key.DirectionDown,
                Key.SystemNavigationDown,
                -> onDown()

                Key.Enter,
                Key.DirectionCenter,
                Key.NumPadEnter,
                -> onEnter()

                Key.Back -> onBack()

                else -> false
            }
        } else {
            false
        }
    }
}
