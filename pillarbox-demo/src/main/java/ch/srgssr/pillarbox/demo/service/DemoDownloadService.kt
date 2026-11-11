/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.app.PendingIntent
import android.content.Intent
import ch.srgssr.pillarbox.demo.MainActivity
import ch.srgssr.pillarbox.demo.download.DemoDownloads
import ch.srgssr.pillarbox.download.PillarboxDownloadManager
import ch.srgssr.pillarbox.download.PillarboxDownloadService

/**
 * Demo download service, running the downloads in the background.
 */
class DemoDownloadService : PillarboxDownloadService() {
    override val pillarboxDownloadManager: PillarboxDownloadManager by lazy {
        DemoDownloads.getDownloadManager(this)
    }

    override fun createNotificationContentIntent(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getActivity(applicationContext, 0, intent, flags)
    }
}
