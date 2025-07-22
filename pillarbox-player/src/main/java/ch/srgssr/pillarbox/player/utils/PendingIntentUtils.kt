/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.Context

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
            PendingIntent.getActivity(context, 0, sessionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    /**
     * Adds the [PendingIntent.FLAG_IMMUTABLE] flag to the provided [flags].
     *
     * @param flags The initial flags of the [PendingIntent].
     * @return The provided [flags] with the [PendingIntent.FLAG_IMMUTABLE] flag added.
     */
    @JvmStatic
    @Deprecated(
        "Set the immutable flag directly on flags",
        ReplaceWith("flags or PendingIntent.FLAG_IMMUTABLE", imports = ["android.app.PendingIntent"]),
    )
    fun appendImmutableFlagIfNeeded(flags: Int): Int {
        return flags or PendingIntent.FLAG_IMMUTABLE
    }
}
