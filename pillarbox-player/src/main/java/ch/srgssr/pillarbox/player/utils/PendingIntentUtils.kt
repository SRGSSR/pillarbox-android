/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import android.app.PendingIntent
import android.content.Context
import android.os.Build

/**
 * PendingIntent utils
 */
object PendingIntentUtils {
    /**
     * Try to get application launcher intent
     */
    @JvmStatic
    fun getDefaultPendingIntent(context: Context): PendingIntent? {
        return context.packageManager?.getLaunchIntentForPackage(context.packageName)?.let { sessionIntent ->
            PendingIntent.getActivity(context, 0, sessionIntent, appendImmutableFlagIfNeeded(PendingIntent.FLAG_UPDATE_CURRENT))
        }
    }

    /**
     * From Android 23, PendingIntent needs to add PendingIntent.FLAG_IMMUTABLE
     * @param flags add [PendingIntent.FLAG_IMMUTABLE] for android 23+
     * @return [flags] with IMMUTABLE flag.
     */
    @JvmStatic
    fun appendImmutableFlagIfNeeded(flags: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags or PendingIntent.FLAG_IMMUTABLE else flags
    }
}
