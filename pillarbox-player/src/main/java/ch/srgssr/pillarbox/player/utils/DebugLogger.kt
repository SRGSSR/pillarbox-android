/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import android.util.Log
import ch.srgssr.pillarbox.player.BuildConfig

/**
 * Debug logger use Android Log only if BuildConfig.DEBUG
 */
object DebugLogger {

    /**
     * @param tag tag to log
     * @param message message to log
     * @param throwable error to log
     */
    fun debug(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message, throwable)
        }
    }

    /**
     * @param tag tag to log
     * @param message message to log
     * @param throwable error to log
     */
    fun info(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message, throwable)
        }
    }

    /**
     * @param tag tag to log
     * @param message message to log
     * @param throwable error to log
     */
    fun warning(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message, throwable)
        }
    }

    /**
     * @param tag tag to log
     * @param message message to log
     * @param throwable error to log
     */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        }
    }
}
