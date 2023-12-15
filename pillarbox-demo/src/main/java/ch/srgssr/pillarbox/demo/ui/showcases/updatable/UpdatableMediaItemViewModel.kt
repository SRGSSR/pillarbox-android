/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.updatable

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerNotificationManager
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
 * This demo demonstrate how to update an existing [MediaItem.mediaMetadata].
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

    init {
        player.prepare()
        player.setMediaItem(DemoItem.OnDemandHorizontalVideo.toMediaItem())
        player.play()
        notificationManager = PlayerNotificationManager.Builder(application, NOTIFICATION_ID, CHANNEL_ID)
            .setChannelNameResourceId(androidx.media3.session.R.string.default_notification_channel_name)
            .setMediaDescriptionAdapter(PillarboxMediaDescriptionAdapter(context = application, pendingIntent = null))
            .build()
        notificationManager.setPlayer(player)

        timer = timer(name = "update-item", period = 3.seconds.inWholeMilliseconds) {
            viewModelScope.launch(Dispatchers.Main) {
                val currentMediaItem = player.currentMediaItem
                currentMediaItem?.let {
                    // when localConfiguration is not null, it means the urn has loaded a playable media url.
                    if (it.localConfiguration != null) {
                        updateTitle(it, "$baseTitle - $counter")
                        counter++
                    }
                }
            }
        }
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
        player.release()
    }

    companion object {
        private const val CHANNEL_ID = "DemoChannel"
        private const val NOTIFICATION_ID = 456
    }
}