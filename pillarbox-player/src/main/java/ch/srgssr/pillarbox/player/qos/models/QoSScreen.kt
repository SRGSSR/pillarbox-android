/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

/**
 * Information about the device screen.
 *
 * @property height The height of the screen, in pixels.
 * @property width The width of the screen, in pixels.
 */
data class QoSScreen(
    val height: Int,
    val width: Int,
)
