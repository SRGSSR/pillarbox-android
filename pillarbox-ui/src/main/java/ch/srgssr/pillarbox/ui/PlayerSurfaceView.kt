/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player

/**
 * Render [player] content on a [PlayerSurfaceView]
 *
 * @param player The player to render on the SurfaceView.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun PlayerSurfaceView(player: Player?, modifier: Modifier = Modifier) {
    val playerSurfaceView = rememberPlayerView()
    AndroidView(
        /*
         * On some devices (Pixel 2 XL Android 11)
         * the "black" background of the SurfaceView shows outside its bound.
         */
        modifier = modifier.clipToBounds(),
        factory = {
            playerSurfaceView
        }, update = { view ->
            view.player = player
        }
    )
}

/**
 * Remember player view
 *
 * Create a [PlayerSurfaceView] that is Lifecyle aware.
 * OnDispose remove player from the view.
 *
 * @return the [PlayerSurfaceView]
 */
@Composable
private fun rememberPlayerView(): PlayerSurfaceView {
    val context = LocalContext.current
    val playerView = remember {
        PlayerSurfaceView(context)
    }

    DisposableEffect(playerView) {
        onDispose {
            playerView.player = null
        }
    }
    return playerView
}

/**
 * Player surface view
 */
internal class PlayerSurfaceView(context: Context) : SurfaceView(context) {
    private var isSurfaceCreated = false

    /**
     * Player if null is passed just clear surface
     */
    var player: Player? = null
        set(value) {
            if (field != value) {
                field?.clearVideoSurfaceView(this)
                if (isSurfaceCreated) {
                    value?.setVideoSurfaceView(this)
                }
            }
            field = value
        }

    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                isSurfaceCreated = true
                player?.setVideoSurfaceView(this@PlayerSurfaceView)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                // Nothing
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                isSurfaceCreated = false
            }
        })
    }
}
