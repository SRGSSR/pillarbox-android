/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.widget.player

import android.view.SurfaceView
import android.view.TextureView
import androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView

/**
 * Represents the type of surface used for video rendering.
 */
enum class SurfaceType {
    /**
     * Renders the video into a [SurfaceView].
     *
     * This is the most optimized option, and it supports DRM content.
     */
    Surface,

    /**
     * Renders the video into a [TextureView].
     *
     * This option may be interesting when dealing with animation, and the [SurfaceType.Surface] option doesn't work as expected. However, it does
     * not support DRM content.
     */
    Texture,

    /**
     * Renders the video into a [SphericalGLSurfaceView].
     *
     * This is suited for 360° video content. However, it does not support DRM content.
     */
    Spherical,
}
