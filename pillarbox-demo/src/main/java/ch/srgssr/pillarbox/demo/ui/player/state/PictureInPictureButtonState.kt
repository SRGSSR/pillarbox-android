/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.state

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import java.lang.ref.WeakReference

/**
 * Creates a [PictureInPictureButtonState] that is remembered across compositions.
 *
 * It is recommended to use [rememberPictureInPictureButtonState] with [PictureInPictureParams] for
 * Android O and above for more control over the Picture-in-Picture behavior.
 *
 * @return A [PictureInPictureButtonState] instance.
 */
@Composable
fun rememberPictureInPictureButtonState(): PictureInPictureButtonState {
    val activity = LocalActivity.current as ComponentActivity
    val pictureInPictureButtonState = remember(activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.w("PictureInPictureButtonState", "Consider migrating to rememberPictureInPictureButtonState(PictureInPictureParams)")
        }

        PictureInPictureButtonStateBase(activity)
    }

    DisposableEffect(activity) {
        pictureInPictureButtonState.startObserving()

        onDispose {
            pictureInPictureButtonState.stopObserving()
        }
    }

    return pictureInPictureButtonState
}

/**
 * Creates a [PictureInPictureButtonState] that is remembered across compositions.
 *
 * The state keeps the [Activity]'s Picture-in-Picture parameters up to date: they are refreshed each time
 * [PictureInPictureButtonState.sourceRectHint] changes, and each time this composable recomposes. Reading the values used by
 * [pictureInPictureParamsProvider] from a Compose state is therefore enough to keep the parameters in sync.
 *
 * @param pictureInPictureParamsProvider A provider to get the parameters to use when entering Picture-in-Picture mode. It receives the current
 * [PictureInPictureButtonState.sourceRectHint], to pass to [PictureInPictureParams.Builder.setSourceRectHint].
 * @return A [PictureInPictureButtonState] instance.
 */
@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun rememberPictureInPictureButtonState(
    pictureInPictureParamsProvider: (sourceRectHint: Rect?) -> PictureInPictureParams,
): PictureInPictureButtonState {
    val activity = LocalActivity.current as ComponentActivity
    val currentPictureInPictureParamsProvider by rememberUpdatedState(pictureInPictureParamsProvider)
    val pictureInPictureButtonState = remember(activity) {
        PictureInPictureButtonStateApi26(activity) { sourceRectHint ->
            currentPictureInPictureParamsProvider(sourceRectHint)
        }
    }

    DisposableEffect(activity) {
        pictureInPictureButtonState.startObserving()

        onDispose {
            pictureInPictureButtonState.stopObserving()
        }
    }

    SideEffect {
        pictureInPictureButtonState.updatePictureInPictureParams()
    }

    return pictureInPictureButtonState
}

/**
 * State that holds all interactions to correctly deal with a UI component representing a Picture-in-Picture button.
 */
interface PictureInPictureButtonState {
    /**
     * Whether Picture-in-Picture is available.
     */
    val isEnabled: Boolean

    /**
     * Whether the [Activity] is currently in Picture-in-Picture mode.
     */
    val isInPictureInPicture: Boolean

    /**
     * Bounds, in window coordinates, of the content that the system animates into and out of the Picture-in-Picture window, or `null` if they are
     * not known yet. Setting it updates the [Activity]'s Picture-in-Picture parameters.
     *
     * It is ignored below Android O, where [PictureInPictureParams] is not available.
     */
    var sourceRectHint: Rect?

    /**
     * Enter Picture-in-Picture mode.
     */
    fun onClick()

    /**
     * Start observing the [Activity]'s [PictureInPictureModeChangedInfo] events.
     */
    fun startObserving()

    /**
     * Stop observing the [Activity]'s [PictureInPictureModeChangedInfo] events.
     */
    fun stopObserving()
}

private open class PictureInPictureButtonStateBase(
    activity: ComponentActivity,
) : PictureInPictureButtonState {
    private val activityRef = WeakReference(activity)
    private val pictureInPictureObserver = Consumer<PictureInPictureModeChangedInfo> { changedInfo ->
        isInPictureInPicture = changedInfo.isInPictureInPictureMode
    }

    protected val activity: ComponentActivity?
        get() = activityRef.get()

    override val isEnabled by lazy {
        activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

    final override var isInPictureInPicture by mutableStateOf(activity.isInPictureInPictureMode)
        private set

    /**
     * Picture-in-Picture parameters require Android O, so the source rectangle hint is only stored here.
     */
    override var sourceRectHint: Rect? = null

    override fun onClick() {
        @Suppress("DEPRECATION")
        activity?.enterPictureInPictureMode()
    }

    override fun startObserving() {
        activity?.addOnPictureInPictureModeChangedListener(pictureInPictureObserver)
    }

    override fun stopObserving() {
        activity?.removeOnPictureInPictureModeChangedListener(pictureInPictureObserver)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private class PictureInPictureButtonStateApi26(
    activity: ComponentActivity,
    private val pictureInPictureParamsProvider: (sourceRectHint: Rect?) -> PictureInPictureParams,
) : PictureInPictureButtonStateBase(activity) {
    override var sourceRectHint: Rect? = null
        set(value) {
            if (field == value) return

            field = value
            updatePictureInPictureParams()
        }

    override fun onClick() {
        activity?.enterPictureInPictureMode(pictureInPictureParamsProvider(sourceRectHint))
    }

    /**
     * Pushes the current parameters to the [Activity], so that the system can animate the Picture-in-Picture transition, even when it is not
     * triggered by [onClick].
     */
    fun updatePictureInPictureParams() {
        activity?.setPictureInPictureParams(pictureInPictureParamsProvider(sourceRectHint))
    }
}
