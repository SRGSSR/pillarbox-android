/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrations.adaptive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ch.srg.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srg.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srg.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srg.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSourceImpl
import ch.srgssr.pillarbox.demo.data.DemoPlaylistProvider
import ch.srgssr.pillarbox.demo.data.MixedMediaItemSource
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * Adaptive view model hold two PillarboxPlayer
 *
 * @param application
 */
class AdaptiveViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * Player as PillarboxPlayer
     */
    val player = PillarboxPlayer(
        context = application,
        mediaItemSource = MixedMediaItemSource(
            MediaCompositionMediaItemSource(MediaCompositionDataSourceImpl(application, IlHost.PROD))
        ),
        /**
         * If you plan to play some SRG Token protected content
         */
        /**
         * If you plan to play some SRG Token protected content
         */
        dataSourceFactory = AkamaiTokenDataSource.Factory()
    )

    init {
        val listPlaylists = DemoPlaylistProvider(application).loadDemoItemFromAssets("streams.json")
        val playlist = listPlaylists[0]
        val items = playlist.items.map { it.toMediaItem() }
        player.setMediaItems(items)
        player.prepare()
        player.play()
    }

    override fun onCleared() {
        player.release()
    }
}
