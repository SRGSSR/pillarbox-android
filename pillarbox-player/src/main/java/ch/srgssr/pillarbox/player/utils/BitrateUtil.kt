/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

/**
 * Utility object providing functions for converting bit rates to byte rates.
 */
object BitrateUtil {

    /**
     * Converts a bit rate to a byte rate.
     *
     * @return The byte rate equivalent of the given bit rate.
     */
    fun Int.toByteRate(): Int {
        return this / Byte.SIZE_BITS
    }

    /**
     * Converts a bit rate to a byte rate.
     *
     * @return The byte rate equivalent of the given bit rate.
     */
    fun Long.toByteRate(): Long {
        return this / Byte.SIZE_BITS
    }
}
