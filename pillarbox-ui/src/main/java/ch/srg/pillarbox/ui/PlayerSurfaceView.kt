/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.ui

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.media3.common.C
import androidx.media3.common.C.VideoScalingMode
import androidx.media3.exoplayer.ExoPlayer

/**
 * Player surface view
 * @param context
 */
class PlayerSurfaceView(context: Context) : SurfaceView(context) {
    private var isSurfaceCreated = false

    /**
     * Video scaling mode to set fo
     */
    var videoScalingMode: @VideoScalingMode Int = C.VIDEO_SCALING_MODE_DEFAULT
        set(value) {
            if (value != field && isSurfaceCreated) {
                player?.videoScalingMode = value
            }
            field = value
        }

    /**
     * Player if null is passed just clear surface
     */
    var player: ExoPlayer? = null
        set(value) {
            if (field != value) {
                field?.clearVideoSurfaceView(this)
                if (isSurfaceCreated) {
                    value?.setVideoSurfaceView(this)
                    value?.videoScalingMode = videoScalingMode
                }
            }
            field = value
        }

    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                isSurfaceCreated = true
                player?.setVideoSurfaceView(this@PlayerSurfaceView)
                player?.videoScalingMode = videoScalingMode
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
