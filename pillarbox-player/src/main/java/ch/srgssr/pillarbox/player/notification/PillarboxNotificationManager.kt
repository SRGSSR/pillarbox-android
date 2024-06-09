/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.notification

import android.content.Context
import androidx.annotation.IntRange
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager

/**
 * Pillarbox notification manager
 */
object PillarboxNotificationManager {
    /**
     * Preconfigured [PlayerNotificationManager] for Pillarbox.
     *
     * @param context
     * @param notificationId The id of the notification to be posted. Must be greater than 0.
     * @param channelId The id of the notification channel of an existing notification channel or of the channel that should be automatically created.
     * In the latter case, setChannelNameResourceId(int) needs to be called as well.
     */
    class Builder(
        context: Context,
        @IntRange(from = 1) notificationId: Int,
        channelId: String
    ) : PlayerNotificationManager.Builder(context, notificationId, channelId) {
        private var mediaSession: MediaSession? = null

        /**
         * Set media session to link with the PlayerNotification.
         *
         * Don't call [setMediaDescriptionAdapter] when using this method. It won't have any effect otherwise.
         *
         * @param mediaSession
         */
        fun setMediaSession(mediaSession: MediaSession): Builder {
            this.mediaSession = mediaSession
            setMediaDescriptionAdapter(PillarboxMediaDescriptionAdapter(context = context, pendingIntent = mediaSession.sessionActivity))
            return this
        }

        override fun build(): PlayerNotificationManager {
            val notificationManager = super.build()
            mediaSession?.let {
                notificationManager.setMediaSessionToken(it.platformToken)
            }
            return notificationManager
        }
    }
}
