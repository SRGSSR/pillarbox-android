/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

import kotlin.time.Duration

/**
 * Represents the timings until the current media started to play, during the preload phase.
 *
 * @property asset The time spent to load the asset.
 * @property drm The time spent to load the DRM.
 * @property metadata The time spent to load the media source.
 * @property token The time spent to load the token.
 */
data class QoSTimings(
    val asset: Duration? = null,
    val drm: Duration? = null,
    val metadata: Duration? = null,
    val token: Duration? = null,
)
