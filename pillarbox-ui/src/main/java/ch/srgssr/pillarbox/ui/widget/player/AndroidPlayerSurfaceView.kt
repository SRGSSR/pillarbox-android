/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.SurfaceControl
import android.view.SurfaceView
import android.window.SurfaceSyncGroup
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player

/**
 * Render the [player] content on a [SurfaceView].
 *
 * @param player The player to render on the SurfaceView.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
internal fun AndroidPlayerSurfaceView(player: Player, modifier: Modifier = Modifier) {
    AndroidView(
        /*
         * On some devices (Pixel 2 XL Android 11),
         * the "black" background of the SurfaceView shows outside its bound.
         */
        modifier = modifier.clipToBounds(),
        factory = { context ->
            PlayerSurfaceView(context)
        },
        update = { view ->
            view.player = player
        },
        onRelease = { view ->
            view.player = null
        },
        onReset = { view ->
            // onReset is called before `update` when the composable is reused with a different context.
            view.player = null
        }
    )
}

/**
 * Player surface view
 */
private class PlayerSurfaceView(context: Context) : SurfaceView(context) {
    private val playerListener = PlayerListener()
    private val surfaceSyncGroupV34 = when {
        isInEditMode -> null
        needSurfaceSyncWorkaround() -> SurfaceSyncGroupCompatV34()
        else -> null
    }

    /**
     * Player if null is passed just clear surface
     */
    var player: Player? = null
        set(value) {
            if (field != value) {
                field?.clearVideoSurfaceView(this)
                field?.removeListener(playerListener)
                value?.setVideoSurfaceView(this)
                value?.addListener(playerListener)
            }
            field = value
        }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        if (needSurfaceSyncWorkaround()) {
            surfaceSyncGroupV34?.maybeMarkSyncReadyAndClear()
        }
    }

    // Workaround for a surface sync issue on API 34: https://github.com/androidx/media/issues/1237
    // Imported from https://github.com/androidx/media/commit/30cb76269a67e09f6e1662ea9ead6aac70667028
    private fun needSurfaceSyncWorkaround(): Boolean {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

    private inner class PlayerListener : Player.Listener {
        private val mainLooperHandler = Handler(Looper.getMainLooper())

        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            if (needSurfaceSyncWorkaround()) {
                surfaceSyncGroupV34?.postRegister(mainLooperHandler, this@PlayerSurfaceView)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private class SurfaceSyncGroupCompatV34 {
        private var surfaceSyncGroup: SurfaceSyncGroup? = null

        fun postRegister(
            mainLooperHandler: Handler,
            surfaceView: SurfaceView,
        ) {
            mainLooperHandler.post {
                // The SurfaceView isn't attached to a window, so don't apply the workaround.
                val rootSurfaceControl = surfaceView.getRootSurfaceControl() ?: return@post

                surfaceSyncGroup = SurfaceSyncGroup("exo-sync-b-334901521")
                surfaceSyncGroup?.add(rootSurfaceControl) {}
                surfaceView.invalidate()
                rootSurfaceControl.applyTransactionOnDraw(SurfaceControl.Transaction())
            }
        }

        fun maybeMarkSyncReadyAndClear() {
            surfaceSyncGroup?.markSyncReady()
            surfaceSyncGroup = null
        }
    }
}
