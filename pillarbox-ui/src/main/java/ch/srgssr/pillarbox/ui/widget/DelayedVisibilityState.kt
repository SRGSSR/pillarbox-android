/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget

import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.Indication
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.playWhenReadyAsFlow
import ch.srgssr.pillarbox.ui.extension.playbackStateAsState
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

/**
 * Delayed visibility state
 *
 * @param initialVisible Initial visible
 * @param initialDuration Initial duration
 */
@Stable
class DelayedVisibilityState internal constructor(
    initialVisible: Boolean = true,
    initialDuration: Duration = DefaultDuration
) {
    internal var autoHideResetTrigger by mutableStateOf(false)
        private set

    /**
     * Visible
     */
    var isVisible by mutableStateOf(initialVisible)

    /**
     * Duration
     */
    var duration by mutableStateOf(initialDuration)

    /**
     * Toggle
     */
    fun toggle() {
        this.isVisible = !isVisible
    }

    /**
     * Show
     */
    fun show() {
        this.isVisible = true
    }

    /**
     * Hide
     */
    fun hide() {
        this.isVisible = false
    }

    /**
     * Disable auto hide
     */
    fun disableAutoHide() {
        duration = DisabledDuration
    }

    /**
     * Reset the auto hide countdown
     */
    fun resetAutoHide() {
        autoHideResetTrigger = !autoHideResetTrigger
    }

    /**
     * Is auto hide enabled
     */
    fun isAutoHideEnabled(): Boolean {
        return duration < INFINITE && duration > ZERO
    }

    @Suppress("UndocumentedPublicClass")
    companion object {
        /**
         * Default duration
         */
        val DefaultDuration = 3.seconds

        /**
         * Disabled duration
         */
        val DisabledDuration = ZERO
    }
}

/**
 * Toggleable
 *
 * @param enabled whether or not this toggleable will handle input events and appear enabled for semantics purposes
 * @param role the type of user interface element. Accessibility services might use this to describe the element or do customizations
 * @param delayedVisibilityState the delayed visibility state to link
 */
fun Modifier.toggleable(
    enabled: Boolean = true,
    role: Role? = Role.Switch,
    delayedVisibilityState: DelayedVisibilityState
): Modifier = composed {
    toggleable(
        enabled = enabled,
        role = role,
        interactionSource = remember {
            MutableInteractionSource()
        },
        delayedVisibilityState = delayedVisibilityState
    )
}

/**
 * Toggleable
 *
 * @param enabled whether or not this toggleable will handle input events and appear enabled for semantics purposes
 * @param role the type of user interface element. Accessibility services might use this to describe the element or do customizations
 * @param indication indication to be shown when modified element is pressed. Be default, indication from LocalIndication will be used.
 * Pass null to show no indication, or current value from LocalIndication to show theme default
 * @param interactionSource MutableInteractionSource that will be used to emit PressInteraction.Press when this toggleable is being pressed.
 * @param delayedVisibilityState the delayed visibility state to link
 */
fun Modifier.toggleable(
    enabled: Boolean = true,
    role: Role? = Role.Switch,
    indication: Indication? = null,
    interactionSource: MutableInteractionSource,
    delayedVisibilityState: DelayedVisibilityState
): Modifier = this.then(
    Modifier
        .toggleable(
            value = delayedVisibilityState.isVisible,
            enabled = enabled,
            indication = indication,
            interactionSource = interactionSource,
            role = role,
            onValueChange = {
                delayedVisibilityState.isVisible = it
            }
        )
        .onEnterPressed(delayedVisibilityState::toggle)
        .focusable(enabled = enabled)
)

/**
 * Maintain visibility on focus
 *
 * @param delayedVisibilityState the delayed visibility state to link
 */
fun Modifier.maintainVisibleOnFocus(delayedVisibilityState: DelayedVisibilityState): Modifier {
    return onFocusChanged {
        if (it.isFocused) {
            delayedVisibilityState.show()
        }
    }
}

/**
 * Remember delayed visibility state
 *
 * @param player The player to listen if it is playing or not
 * @param visible visibility state of the content
 * @param autoHideEnabled true to enable hide after [duration]
 * @param duration the duration to wait after hiding the content.
 */
@Composable
fun rememberDelayedVisibilityState(
    player: Player,
    visible: Boolean = true,
    autoHideEnabled: Boolean = true,
    duration: Duration = DelayedVisibilityState.DefaultDuration
): DelayedVisibilityState {
    val playWhenReadyFlow = remember(player) {
        player.playWhenReadyAsFlow()
    }
    val playbackState by player.playbackStateAsState()
    val stateReady = playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING
    val playWhenReady by playWhenReadyFlow.collectAsState(initial = player.playWhenReady)
    return rememberDelayedVisibilityState(visible = visible, autoHideEnabled && playWhenReady && stateReady, duration)
}

/**
 * Remember delayed visibility state
 *
 * @param visible visibility state of the content.
 * @param autoHideEnabled true to enable hide after [duration]. Auto hide is always disabled when accessibility is on.
 * @param duration the duration to wait after hiding the content.
 */
@Composable
fun rememberDelayedVisibilityState(
    visible: Boolean = true,
    autoHideEnabled: Boolean = true,
    duration: Duration = DelayedVisibilityState.DefaultDuration
): DelayedVisibilityState {
    val context = LocalContext.current
    val ac = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.getSystemService(AccessibilityManager::class.java)
    } else {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }
    var isTalkBackEnabled by remember {
        mutableStateOf(ac.isEnabled)
    }
    DisposableEffect(context) {
        val l = AccessibilityManager.AccessibilityStateChangeListener { isTalkBackEnabled = ac.isEnabled }
        ac.addAccessibilityStateChangeListener(l)
        onDispose {
            ac.removeAccessibilityStateChangeListener(l)
        }
    }
    val autoHideEnabledAccessibility = autoHideEnabled && !isTalkBackEnabled
    val delayedVisibilityState = remember {
        DelayedVisibilityState(visible, duration).apply {
            if (!autoHideEnabledAccessibility) {
                disableAutoHide()
            }
        }
    }

    LaunchedEffect(duration, autoHideEnabledAccessibility) {
        if (autoHideEnabledAccessibility) {
            delayedVisibilityState.duration = duration
        } else {
            delayedVisibilityState.disableAutoHide()
        }
    }

    LaunchedEffect(visible) {
        delayedVisibilityState.isVisible = visible
    }

    LaunchedEffect(delayedVisibilityState.isVisible, delayedVisibilityState.duration, delayedVisibilityState.autoHideResetTrigger) {
        if (delayedVisibilityState.isVisible && delayedVisibilityState.isAutoHideEnabled()) {
            delay(delayedVisibilityState.duration)
            delayedVisibilityState.hide()
        }
    }

    return delayedVisibilityState
}

private fun Modifier.onEnterPressed(action: () -> Unit): Modifier {
    return this then Modifier.onPreviewKeyEvent {
        val isEnterKey = it.key == Key.Enter || it.key == Key.DirectionCenter || it.key == Key.NumPadEnter
        val isKeyUp = it.type == KeyEventType.KeyUp

        if (isEnterKey && isKeyUp) {
            action()
            true
        } else {
            false
        }
    }
}
