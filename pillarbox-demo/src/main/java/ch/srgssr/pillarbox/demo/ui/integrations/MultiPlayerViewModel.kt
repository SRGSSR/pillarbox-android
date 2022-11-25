/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ch.srg.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srg.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srg.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srg.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSourceImpl
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.data.MediaItemSource

/**
 * Multi player view model holding multiple instance of players
 */
class MultiPlayerViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * Player1 playing urn
     */
    val player1 = PillarboxPlayer(
        context = application,
        mediaItemSource = MediaCompositionMediaItemSource(MediaCompositionDataSourceImpl(application, IlHost.PROD)),
        dataSourceFactory = AkamaiTokenDataSource.Factory()
    )

    /**
     * Player2 playing url
     */
    val player2 = PillarboxPlayer(
        context = application,
        mediaItemSource = object : MediaItemSource {
            override suspend fun loadMediaItem(mediaItem: MediaItem): MediaItem {
                return mediaItem
            }
        }
    )

    init {
        player1.setMediaItem(MediaItem.Builder().setMediaId("urn:rts:video:6820736").build())
        player2.setMediaItem(MediaItem.fromUri("https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8"))

        player1.repeatMode = Player.REPEAT_MODE_ALL
        player2.repeatMode = Player.REPEAT_MODE_ALL

        player1.prepare()
        player2.prepare()

        player1.play()
        player2.play()
    }

    override fun onCleared() {
        super.onCleared()
        player1.release()
        player2.release()
    }
}
