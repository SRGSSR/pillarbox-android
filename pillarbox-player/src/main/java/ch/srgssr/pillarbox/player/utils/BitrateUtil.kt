/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

/**
 * Bitrate util
 */
object BitrateUtil {

    /**
     * @return Convert Int in bits rate to Int in byte rate.
     */
    fun Int.toByteRate(): Int {
        return this / Byte.SIZE_BITS
    }

    /**
     * @return Convert Long in bits rate to Long in byte rate.
     */
    fun Long.toByteRate(): Long {
        return this / Byte.SIZE_BITS
    }
}
