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
 * A composable function that returns a boolean indicating whether TalkBack is currently enabled.
 *
 * This function uses a [DisposableEffect] to register an [AccessibilityManager.AccessibilityStateChangeListener]
 * that updates the state of the composable when the accessibility state changes.
 *
 * @return `true` if TalkBack is enabled, `false` otherwise.
 */
@Composable
fun rememberIsTalkBackEnabled(): Boolean {
    val accessibilityManager = LocalContext.current.getSystemService<AccessibilityManager>() ?: return false
    val (isTalkBackEnabled, setTalkBackEnabled) = remember {
        mutableStateOf(accessibilityManager.isEnabled)
    }
    DisposableEffect(Unit) {
        val l = AccessibilityManager.AccessibilityStateChangeListener(setTalkBackEnabled)
        accessibilityManager.addAccessibilityStateChangeListener(l)
        onDispose {
            accessibilityManager.removeAccessibilityStateChangeListener(l)
        }
    }
    return isTalkBackEnabled
}
