/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import android.content.Context
import android.view.TextureView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player

/**
 * Render the [player] content on a [TextureView]. Does not work with DRM content!
 *
 * @param player The player to render on the TextureView.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
internal fun AndroidPlayerTextureView(player: Player, modifier: Modifier = Modifier) {
    AndroidView(
        /*
         * On some devices (Pixel 2 XL Android 11),
         * the "black" background of the SurfaceView shows outside its bound.
         */
        modifier = modifier.clipToBounds(),
        factory = { context ->
            PlayerTextureView(context)
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

private class PlayerTextureView(context: Context) : TextureView(context) {
    /**
     * Player if null is passed just clear surface
     */
    var player: Player? = null
        set(value) {
            if (field != value) {
                field?.clearVideoTextureView(this)
                value?.setVideoTextureView(this)
            }
            field = value
        }
}
