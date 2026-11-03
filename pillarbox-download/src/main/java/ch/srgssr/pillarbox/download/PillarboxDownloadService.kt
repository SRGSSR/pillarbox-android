/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.download

import android.app.Notification
import android.app.PendingIntent
import androidx.annotation.DrawableRes
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Requirements
import androidx.media3.exoplayer.scheduler.Scheduler

/**
 * Base [DownloadService] running the downloads of a [PillarboxDownloadManager], with a notification showing their progress.
 *
 * Extend this class, and declare it in your application manifest:
 *
 * ```xml
 * <service
 *     android:name=".MyDownloadService"
 *     android:exported="false"
 *     android:foregroundServiceType="dataSync">
 *     <intent-filter>
 *         <action android:name="androidx.media3.exoplayer.downloadService.action.RESTART" />
 *         <category android:name="android.intent.category.DEFAULT" />
 *     </intent-filter>
 * </service>
 * ```
 */
abstract class PillarboxDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    NOTIFICATION_CHANNEL_ID,
    R.string.pillarbox_download_notification_channel_name,
    0,
) {
    /**
     * The [DownloadNotificationHelper] used to build the download notifications, posted to the [NOTIFICATION_CHANNEL_ID] channel. Use it
     * to build your own notifications, for example in [getForegroundNotification] or when a download completes.
     */
    protected val notificationHelper by lazy { DownloadNotificationHelper(this, NOTIFICATION_CHANNEL_ID) }

    /**
     * The [PillarboxDownloadManager] whose downloads are run by this service. It should be an application wide singleton.
     */
    abstract val pillarboxDownloadManager: PillarboxDownloadManager

    /**
     * The small icon of the download notification.
     */
    @DrawableRes
    protected open val notificationSmallIcon: Int = android.R.drawable.stat_sys_download

    /**
     * Returns the [PendingIntent] to send when the download notification is clicked, or `null` to do nothing.
     */
    protected open fun createNotificationContentIntent(): PendingIntent? = null

    final override fun getDownloadManager(): DownloadManager {
        return pillarboxDownloadManager.downloadManager
    }

    /**
     * Returns the [Scheduler] used to restart the downloads when the [Requirements] are met while the application is not running, or `null`
     * to only download while the service is running. Returns `null` by default.
     */
    override fun getScheduler(): Scheduler? = null

    /**
     * Returns the notification shown while the downloads are running. By default, a progress notification using [notificationSmallIcon] and
     * [createNotificationContentIntent]. Override it to customize the notification further; it must be posted to the
     * [NOTIFICATION_CHANNEL_ID] channel.
     */
    override fun getForegroundNotification(downloads: List<Download>, notMetRequirements: Int): Notification {
        return notificationHelper.buildProgressNotification(
            this,
            notificationSmallIcon,
            createNotificationContentIntent(),
            null,
            downloads,
            notMetRequirements,
        )
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 0x5049_4C42

        /**
         * The identifier of the notification channel of the download notifications. Its name is the
         * `pillarbox_download_notification_channel_name` string resource, which can be overridden in your application.
         */
        const val NOTIFICATION_CHANNEL_ID = "pillarbox_download_channel"
    }
}
