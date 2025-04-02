/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("TopLevelPropertyNaming")

package ch.srgssr.pillarbox.player

import androidx.media3.exoplayer.DefaultLoadControl

private const val DEFAULT_MIN_BUFFER_MS = 50000
private const val DEFAULT_MAX_BUFFER_MS = 50000
private const val DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 5000
private const val DEFAULT_BUFFER_FOR_PLAYBACK_MS = 2500

fun PillarboxTestLoadControl() = DefaultLoadControl.Builder()
    .setBufferDurationsMs(
        DEFAULT_MIN_BUFFER_MS,
        DEFAULT_MAX_BUFFER_MS,
        DEFAULT_BUFFER_FOR_PLAYBACK_MS,
        DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
    )
    .build()
