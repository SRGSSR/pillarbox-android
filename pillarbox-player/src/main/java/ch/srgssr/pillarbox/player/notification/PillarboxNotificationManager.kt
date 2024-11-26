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
 * This object provides a builder to easily create a [PlayerNotificationManager] pre-configured for use with Pillarbox.
 */
object PillarboxNotificationManager {
    /**
     * A builder for creating a preconfigured [PlayerNotificationManager] tailored for Pillarbox.
     *
     * @param context The [Context].
     * @param notificationId The id of the notification to be posted. Must be greater than 0.
     * @param channelId The id of the notification channel. This can be an existing channel or a new one to be created. If creating a new channel,
     * ensure to call [setChannelNameResourceId][PlayerNotificationManager.Builder.setChannelNameResourceId] as well.
     */
    class Builder(
        context: Context,
        @IntRange(from = 1) notificationId: Int,
        channelId: String
    ) : PlayerNotificationManager.Builder(context, notificationId, channelId) {
        private var mediaSession: MediaSession? = null

        /**
         * Links the player notification to a given [MediaSession].
         *
         * **Note:** don't call [setMediaDescriptionAdapter] after this method, otherwise it won't have any effect.
         *
         * @param mediaSession The [MediaSession] to link with the notification.
         * @return This [Builder] instance for chaining.
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
