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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import ch.srgssr.pillarbox.player.tracks.videoTracks
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView
import ch.srgssr.pillarbox.ui.extension.getAspectRatioAsState

/**
 * Pillarbox player surface
 *
 * @param player The player to render in this [SurfaceView].
 * @param modifier The [Modifier] to be applied to the layout.
 * @param scaleMode The scale mode to use. Only used for video content. Only used when the aspect ratio is strictly positive.
 * @param contentAlignment The "letterboxing" content alignment inside the parent. Only used when the aspect ratio is strictly positive.
 * @param defaultAspectRatio The aspect ratio to use while video is loading or for audio content.
 * @param displayDebugView When `true`, displays debug information on top of the surface. Only used when the aspect ratio is strictly positive.
 * @param surfaceContent The Composable content to display on top of the [SurfaceView]. By default, render the subtitles. Only used when the aspect
 * ratio is strictly positive.
 */
@Composable
fun PlayerSurface(
    player: Player,
    modifier: Modifier = Modifier,
    scaleMode: ScaleMode = ScaleMode.Fit,
    contentAlignment: Alignment = Alignment.Center,
    defaultAspectRatio: Float? = null,
    displayDebugView: Boolean = false,
    surfaceContent: @Composable (BoxScope.() -> Unit)? = { ExoPlayerSubtitleView(player = player) },
) {
    var lastKnownVideoAspectRatio by remember { mutableFloatStateOf(defaultAspectRatio ?: 1f) }
    val videoAspectRatio by player.getAspectRatioAsState(defaultAspectRatio = lastKnownVideoAspectRatio)

    // If the media has tracks, but no video tracks, we reset the aspect ratio to 0
    if (!player.currentTracks.isEmpty && player.currentTracks.videoTracks.isEmpty()) {
        lastKnownVideoAspectRatio = 0f
    } else if (videoAspectRatio > 0f) {
        lastKnownVideoAspectRatio = videoAspectRatio
    }

    if (lastKnownVideoAspectRatio <= 0f) {
        Box(modifier)
        return
    }

    BoxWithConstraints(
        contentAlignment = contentAlignment,
        modifier = modifier.clipToBounds()
    ) {
        val width = constraints.minWidth.coerceAtLeast(constraints.maxWidth)
        val height = constraints.minHeight.coerceAtLeast(constraints.maxHeight).coerceAtLeast(1)
        val viewAspectRatio = width / height.toFloat()

        val videoSurfaceModifier = when (scaleMode) {
            ScaleMode.Fit -> {
                Modifier.aspectRatio(lastKnownVideoAspectRatio, viewAspectRatio > lastKnownVideoAspectRatio)
            }

            ScaleMode.Crop -> {
                Modifier
                    .fillMaxSize()
                    .aspectRatio(lastKnownVideoAspectRatio, viewAspectRatio <= lastKnownVideoAspectRatio)
            }

            ScaleMode.Fill -> {
                Modifier
            }
        }

        AndroidPlayerSurfaceView(modifier = videoSurfaceModifier, player = player)

        surfaceContent?.let {
            val overlayModifier = if (scaleMode != ScaleMode.Crop) {
                videoSurfaceModifier
            } else {
                Modifier.fillMaxSize()
            }

            Box(modifier = overlayModifier, content = it)
        }

        if (displayDebugView) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                BasicText(
                    text = "Size: ${width}x$height",
                    color = { Color.Green }
                )
                BasicText(
                    text = "Aspect ratio view: $viewAspectRatio, video: $lastKnownVideoAspectRatio",
                    color = { Color.Green }
                )
            }
            DebugPlayerView(videoSurfaceModifier)
        }
    }
}

/**
 * Debug player view
 *
 * @param modifier The modifier to use to layout.
 */
@Composable
private fun DebugPlayerView(modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawLine(
            color = Color.Green,
            start = Offset.Zero,
            end = Offset(size.width, size.height),
            strokeWidth = 2f,
        )
        drawLine(
            color = Color.Green,
            start = Offset(size.width, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 2f,
        )
        drawRect(
            color = Color.Magenta,
            style = Stroke(width = 4f),
        )
    }
}

/**
 * Render the [player] content on a [SurfaceView].
 *
 * @param player The player to render on the SurfaceView.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
private fun AndroidPlayerSurfaceView(player: Player, modifier: Modifier = Modifier) {
    AndroidView(
        /*
         * On some devices (Pixel 2 XL Android 11),
         * the "black" background of the SurfaceView shows outside its bound.
         */
        modifier = modifier.clipToBounds(),
        factory = { context ->
            PlayerSurfaceView(context)
        }, update = { view ->
            view.player = player
        }, onRelease = { view ->
            view.player = null
        }, onReset = { view ->
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
