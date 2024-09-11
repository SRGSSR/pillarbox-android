/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.app.PendingIntent
import android.content.Intent
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.player.service.PlaybackService
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils

/**
 * Demo playback service
 *
 * It doesn't stop playback after 1min while in the background and maintains a MediaNotification.
 *
 * @constructor Create empty Demo playback service
 */
class DemoPlaybackService : PlaybackService() {

    override fun pendingIntent(): PendingIntent {
        val intent = Intent(applicationContext, SimplePlayerActivity::class.java)
        val flags = PendingIntentUtils.appendImmutableFlagIfNeeded(PendingIntent.FLAG_UPDATE_CURRENT)
        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            flags
        )
    }
}
