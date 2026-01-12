/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.state

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
 * @param pictureInPictureParamsProvider A provider to get the parameters to use when entering Picture-in-Picture mode.
 * @return A [PictureInPictureButtonState] instance.
 */
@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun rememberPictureInPictureButtonState(pictureInPictureParamsProvider: () -> PictureInPictureParams): PictureInPictureButtonState {
    val activity = LocalActivity.current as ComponentActivity
    val pictureInPictureParamsProvider by rememberUpdatedState(pictureInPictureParamsProvider)
    val pictureInPictureButtonState = remember(activity, pictureInPictureParamsProvider) {
        PictureInPictureButtonStateApi26(activity, pictureInPictureParamsProvider)
    }

    DisposableEffect(activity, pictureInPictureParamsProvider) {
        pictureInPictureButtonState.startObserving()

        onDispose {
            pictureInPictureButtonState.stopObserving()
        }
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
    private val pictureInPictureParamsProvider: () -> PictureInPictureParams,
) : PictureInPictureButtonStateBase(activity) {
    override fun onClick() {
        activity?.enterPictureInPictureMode(pictureInPictureParamsProvider())
    }
}
