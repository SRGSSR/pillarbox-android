/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.story

import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl

/**
 * Story load control
 *
 * Build a Custom [LoadControl] to optimize playbabck startup.
 *
 * Warning bad parameters can lead to OOM pretty quickly.
 */
object StoryLoadControl {
    // Minimum Video you want to buffer while Playing
    private const val MIN_BUFFER_DURATION = 1000

    // Max Video you want to buffer during PlayBack
    private const val MAX_BUFFER_DURATION = DefaultLoadControl.DEFAULT_MAX_BUFFER_MS

    // Min Video you want to buffer before start Playing it
    private const val MIN_PLAYBACK_START_BUFFER = 1000

    // Min video You want to buffer when user resumes video
    private const val MIN_PLAYBACK_RESUME_BUFFER = 1000

    /**
     * Build a new [LoadControl] optimized for Story view
     */
    fun build(): LoadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            MIN_BUFFER_DURATION,
            MAX_BUFFER_DURATION,
            MIN_PLAYBACK_START_BUFFER,
            MIN_PLAYBACK_RESUME_BUFFER
        )
        .build()
}
