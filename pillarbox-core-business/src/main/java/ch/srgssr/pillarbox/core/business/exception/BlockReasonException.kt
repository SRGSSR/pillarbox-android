/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.exception

import ch.srgssr.pillarbox.core.business.integrationlayer.data.BlockReason
import java.io.IOException

/**
 * Block reason exception
 *
 * @property blockReason the reason a [Chapter] or a [Segment] is blocked.
 */
class BlockReasonException(val blockReason: BlockReason) : IOException(blockReason.name) {
    /*
     * ExoPlaybackException bundles cause exception with class name and message.
     * In order to recreate the cause throwable, it needs throwable class with constructor(string).
     */
    internal constructor(message: String) : this(parseMessage(message))

    companion object {
        @Suppress("SwallowedException")
        private fun parseMessage(message: String): BlockReason {
            return try {
                BlockReason.valueOf(message)
            } catch (e: IllegalArgumentException) {
                BlockReason.UNKNOWN
            }
        }
    }
}
