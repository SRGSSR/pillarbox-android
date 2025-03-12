/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Secure
import ch.srgssr.pillarbox.player.monitoring.models.Session

/**
 * Get a device id that is sent with a [Session].
 *
 * @return A unique device identifier, or empty if not available.
 */
@SuppressLint("HardwareIds")
fun Context.getMonitoringDeviceId(): String {
    return Secure.getString(
        contentResolver,
        Secure.ANDROID_ID
    ) ?: ""
}
