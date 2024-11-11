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
import androidx.compose.foundation.interaction.Interaction
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
 * Represents a state that manages visibility, with an optional delayed auto-hiding functionality.
 *
 * @param initialVisible The initial visibility state of the component.
 * @param initialDuration The initial duration before auto-hiding.
 */
@Stable
class DelayedVisibilityState internal constructor(
    initialVisible: Boolean = true,
    initialDuration: Duration = DefaultDuration
) {
    internal var autoHideResetTrigger by mutableStateOf(false)
        private set

    /**
     * Represents the visibility state of the component.
     *
     * This property is observable and changes to this property will trigger recomposition.
     */
    var isVisible by mutableStateOf(initialVisible)

    /**
     * Represents the delay until auto-hide is performed.
     *
     * This property is observable and changes to this property will trigger recomposition.
     */
    var duration by mutableStateOf(initialDuration)

    /**
     * Toggles the visibility of the component.
     */
    fun toggle() {
        this.isVisible = !isVisible
    }

    /**
     * Makes the component visible.
     */
    fun show() {
        this.isVisible = true
    }

    /**
     * Makes the component invisible.
     */
    fun hide() {
        this.isVisible = false
    }

    /**
     * Disables the auto-hide behavior of the component.
     */
    fun disableAutoHide() {
        duration = DisabledDuration
    }

    /**
     * Resets the auto-hide countdown.
     */
    fun resetAutoHide() {
        autoHideResetTrigger = !autoHideResetTrigger
    }

    /**
     * Checks if the auto-hide feature is enabled.
     *
     * @return `true` if auto-hide is enabled, `false` otherwise.
     */
    fun isAutoHideEnabled(): Boolean {
        return duration < INFINITE && duration > ZERO
    }

    @Suppress("UndocumentedPublicClass")
    companion object {
        /**
         * Default auto-hide duration.
         */
        val DefaultDuration = 3.seconds

        /**
         * Disabled auto-hide duration.
         */
        val DisabledDuration = ZERO
    }
}

/**
 * Makes a Composable toggleable, controlling the visibility state of a [DelayedVisibilityState].
 *
 * @param enabled Whether to handle input events and appear enabled for semantics purposes.
 * @param role The type of UI element. Accessibility services might use this to describe the element.
 * @param delayedVisibilityState The [DelayedVisibilityState] instance to control the visibility of the component.
 */
fun Modifier.toggleable(
    enabled: Boolean = true,
    role: Role? = Role.Switch,
    delayedVisibilityState: DelayedVisibilityState
): Modifier = toggleable(
    enabled = enabled,
    role = role,
    interactionSource = null,
    delayedVisibilityState = delayedVisibilityState,
)

/**
 * Makes a Composable toggleable, controlling the visibility state of a [DelayedVisibilityState].
 *
 * @param enabled Whether to handle input events and appear enabled for semantics purposes.
 * @param role The type of UI element. Accessibility services might use this to describe the element.
 * @param indication Indication to be shown when the Composable is pressed. If `null`, no indication will be shown.
 * @param interactionSource The [MutableInteractionSource] that will be used to dispatch [Interaction]s when this toggleable component is being
 * interacted with.
 * @param delayedVisibilityState The [DelayedVisibilityState] instance to control the visibility of the component.
 */
fun Modifier.toggleable(
    enabled: Boolean = true,
    role: Role? = Role.Switch,
    indication: Indication? = null,
    interactionSource: MutableInteractionSource?,
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
 * Maintains a Composable visible when it gains focus.
 *
 * @param delayedVisibilityState The [DelayedVisibilityState] instance to control the visibility of the component.
 */
fun Modifier.maintainVisibleOnFocus(delayedVisibilityState: DelayedVisibilityState): Modifier {
    return onFocusChanged {
        if (it.isFocused) {
            delayedVisibilityState.show()
        }
    }
}

/**
 * Remembers a [DelayedVisibilityState] for the provided [Player]
 *
 * @param player The [Player] to associate with the [DelayedVisibilityState].
 * @param visible Whether the component is initially visible.
 * @param autoHideEnabled Whether auto-hiding is enabled. Auto-hiding is always disabled when accessibility is on.
 * @param duration The duration to wait before hiding the component.
 *
 * @return A [DelayedVisibilityState] instance.
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
 * Remembers a [DelayedVisibilityState].
 *
 * @param visible Whether the component is initially visible.
 * @param autoHideEnabled Whether auto-hiding is enabled. Auto-hiding is always disabled when accessibility is on.
 * @param duration The duration to wait before hiding the component.
 *
 * @return A [DelayedVisibilityState] instance.
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
