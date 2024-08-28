/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

import kotlinx.serialization.Serializable

/**
 * Represents the timings until the current media started to play, during the preload phase.
 *
 * @property asset The time spent to load the asset, in milliseconds.
 * @property drm The time spent to load the DRM, in milliseconds.
 * @property metadata The time spent to load the media source, in milliseconds.
 * @property token The time spent to load the token, in milliseconds.
 */
@Serializable
data class QoSTimings(
    val asset: Long? = null,
    val drm: Long? = null,
    val metadata: Long? = null,
    val token: Long? = null,
)
