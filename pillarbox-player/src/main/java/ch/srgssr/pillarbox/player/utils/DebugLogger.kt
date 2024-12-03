/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import android.util.Log
import ch.srgssr.pillarbox.player.BuildConfig

/**
 * A utility class for logging debug messages.
 *
 * This logger only logs messages if Pillarbox is built in debug mode.
 */
object DebugLogger {

    /**
     * Logs a debug message.
     *
     * @param tag The tag to associate with the log message.
     * @param message The message to log.
     * @param throwable An optional [Throwable] to include in the log message.
     */
    fun debug(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message, throwable)
        }
    }

    /**
     * Logs an info message.
     *
     * @param tag The tag to associate with the log message.
     * @param message The message to log.
     * @param throwable An optional [Throwable] to include in the log message.
     */
    fun info(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message, throwable)
        }
    }

    /**
     * Logs a warning message.
     *
     * @param tag The tag to associate with the log message.
     * @param message The message to log.
     * @param throwable An optional [Throwable] to include in the log message.
     */
    fun warning(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message, throwable)
        }
    }

    /**
     * Logs an error message.
     *
     * @param tag The tag to associate with the log message.
     * @param message The message to log.
     * @param throwable An optional [Throwable] to include in the log message.
     */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        }
    }
}
