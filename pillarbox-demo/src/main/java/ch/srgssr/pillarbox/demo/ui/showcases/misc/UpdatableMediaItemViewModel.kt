/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import ch.srgssr.pillarbox.core.business.SRGMediaItemBuilder
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.notification.PillarboxMediaDescriptionAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.timer
import kotlin.time.Duration.Companion.seconds

/**
 * Updatable media item view model
 *
 * This demo demonstrates how to update an existing [MediaItem.mediaMetadata].
 *
 * @constructor
 *
 * @param application The application
 */
class UpdatableMediaItemViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * The player
     */
    val player = PlayerModule.provideDefaultPlayer(application)
    private val notificationManager: PlayerNotificationManager
    private val timer: Timer
    private val baseTitle = "Update title"
    private var counter = 0
    private val mediaSession = MediaSession.Builder(application, player).build()

    init {
        player.prepare()
        player.setMediaItem(DemoItem.OnDemandHorizontalVideo.toMediaItem())
        player.play()
        notificationManager = PlayerNotificationManager.Builder(application, NOTIFICATION_ID, CHANNEL_ID)
            .setChannelNameResourceId(androidx.media3.session.R.string.default_notification_channel_name)
            .setMediaDescriptionAdapter(PillarboxMediaDescriptionAdapter(context = application, pendingIntent = null))
            .build()
        notificationManager.setPlayer(player)
        notificationManager.setMediaSessionToken(mediaSession.platformToken)

        timer = timer(name = "update-item", period = 3.seconds.inWholeMilliseconds) {
            viewModelScope.launch(Dispatchers.Main) {
                val currentMediaItem = player.currentMediaItem
                currentMediaItem?.let {
                    if (counter < EVENT_COUNT) {
                        updateTitle(it, "$baseTitle - $counter")
                    }
                    if (counter == EVENT_COUNT) {
                        switchToUrn(DemoItem.OnDemandVerticalVideo.uri)
                    }
                    counter++
                }
            }
        }
    }

    private fun switchToUrn(mediaId: String) {
        val updatedMediaItem = SRGMediaItemBuilder(mediaId)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Switched media")
                    .build()
            )
            .build()
        player.replaceMediaItem(player.currentMediaItemIndex, updatedMediaItem)
    }

    private fun updateTitle(mediaItem: MediaItem, title: String) {
        val updatedMediaItem = mediaItem.buildUpon()
            .setMediaMetadata(
                mediaItem.mediaMetadata.buildUpon()
                    .setTitle(title)
                    .build()
            )
            .build()
        player.replaceMediaItem(player.currentMediaItemIndex, updatedMediaItem)
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
        notificationManager.setPlayer(null)
        mediaSession.release()
        player.release()
    }

    companion object {
        private const val CHANNEL_ID = "DemoChannel"
        private const val NOTIFICATION_ID = 456
        private const val EVENT_COUNT = 5
    }
}
