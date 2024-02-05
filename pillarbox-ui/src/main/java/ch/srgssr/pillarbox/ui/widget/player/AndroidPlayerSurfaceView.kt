/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import android.content.Context
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player

/**
 * Render [player] content on a [SurfaceView]
 *
 * @param player The player to render on the SurfaceView.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun AndroidPlayerSurfaceView(player: Player, modifier: Modifier = Modifier) {
    AndroidView(
        /*
         * On some devices (Pixel 2 XL Android 11)
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
            // onRested is called before update when composable is reuse with different context.
            view.player = null
        }
    )
}

/**
 * Player surface view
 */
internal class PlayerSurfaceView(context: Context) : SurfaceView(context) {

    /**
     * Player if null is passed just clear surface
     */
    var player: Player? = null
        set(value) {
            if (field != value) {
                field?.clearVideoSurfaceView(this)
                value?.setVideoSurfaceView(this)
            }
            field = value
        }
}
