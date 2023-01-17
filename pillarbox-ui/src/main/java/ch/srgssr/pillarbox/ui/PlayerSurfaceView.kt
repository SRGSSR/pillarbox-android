/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.media3.common.Player
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Player surface view
 */
internal class PlayerSurfaceView(context: Context) : SurfaceView(context) {
    private var isSurfaceCreated = AtomicBoolean(false)

    /**
     * Player if null is passed just clear surface
     */
    var player: Player? = null
        set(value) {
            if (field != value) {
                field?.clearVideoSurfaceView(this)
                if (isSurfaceCreated.get()) {
                    value?.setVideoSurfaceView(this)
                }
            }
            field = value
        }

    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                isSurfaceCreated.set(true)
                player?.setVideoSurfaceView(this@PlayerSurfaceView)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                // Nothing
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                isSurfaceCreated.set(false)
            }
        })
    }
}
