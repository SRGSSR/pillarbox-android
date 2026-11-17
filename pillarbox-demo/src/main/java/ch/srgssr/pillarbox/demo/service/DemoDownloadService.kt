/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.media3.exoplayer.offline.Download
import ch.srgssr.pillarbox.demo.MainActivity
import ch.srgssr.pillarbox.demo.download.DemoDownloads
import ch.srgssr.pillarbox.download.PillarboxDownloadManager
import ch.srgssr.pillarbox.download.PillarboxDownloadService
import ch.srgssr.pillarbox.download.title

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

    override fun getForegroundNotification(downloads: List<Download>, notMetRequirements: Int): Notification {
        return notificationHelper.buildProgressNotification(
            this,
            notificationSmallIcon,
            createNotificationContentIntent(),
            getNotificationMessage(downloads),
            downloads,
            notMetRequirements,
        )
    }

    /**
     * Returns "<title> <index + 1>/<number of downloads>" for the first download in progress, or `null` if none is in progress.
     */
    private fun getNotificationMessage(downloads: List<Download>): String? {
        val index = downloads.indexOfFirst { it.state == Download.STATE_DOWNLOADING }
        if (index == -1) {
            return null
        }

        val title = downloads[index].title ?: downloads[index].request.id
        return "$title ${index + 1}/${downloads.size}"
    }
}
