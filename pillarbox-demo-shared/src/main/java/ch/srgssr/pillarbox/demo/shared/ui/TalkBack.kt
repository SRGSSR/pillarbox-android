/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui

import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

/**
 * Remembers the current touch exploration state and provides it as a Composable state.
 *
 * @return A boolean indicating whether touch exploration is currently enabled.
 */
@Composable
fun rememberIsTouchExplorationEnabled(): Boolean {
    val accessibilityManager = LocalContext.current.getSystemService<AccessibilityManager>() ?: return false
    val (isTouchExplorationEnabled, setIsTouchExplorationEnabled) = remember {
        mutableStateOf(accessibilityManager.isTouchExplorationEnabled)
    }

    DisposableEffect(Unit) {
        val callback = AccessibilityManager.TouchExplorationStateChangeListener(setIsTouchExplorationEnabled)

        accessibilityManager.addTouchExplorationStateChangeListener(callback)

        onDispose {
            accessibilityManager.removeTouchExplorationStateChangeListener(callback)
        }
    }

    return isTouchExplorationEnabled
}
