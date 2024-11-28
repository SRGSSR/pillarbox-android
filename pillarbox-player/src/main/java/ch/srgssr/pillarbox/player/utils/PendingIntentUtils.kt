/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.os.Build

/**
 * Utility class providing helper functions for working with [PendingIntent]s.
 */
object PendingIntentUtils {
    /**
     * Retrieves a [PendingIntent] that launches the default [Activity] of the application.
     *
     * @param context The [Context].
     * @return A [PendingIntent] that launches the default [Activity], or `null` if it could not be created.
     */
    @JvmStatic
    fun getDefaultPendingIntent(context: Context): PendingIntent? {
        return context.packageManager?.getLaunchIntentForPackage(context.packageName)?.let { sessionIntent ->
            PendingIntent.getActivity(context, 0, sessionIntent, appendImmutableFlagIfNeeded(PendingIntent.FLAG_UPDATE_CURRENT))
        }
    }

    /**
     * Adds the [PendingIntent.FLAG_IMMUTABLE] flag to the provided [flags] if the device is running Android 6.0 (Marshmallow) or higher.
     *
     * @param flags The initial flags of the [PendingIntent].
     * @return The provided [flags] with the [PendingIntent.FLAG_IMMUTABLE] flag added if the device is running Android 6.0 or higher, otherwise the
     * original [flags] unchanged.
     */
    @JvmStatic
    fun appendImmutableFlagIfNeeded(flags: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags or PendingIntent.FLAG_IMMUTABLE else flags
    }
}
