/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.core.business.DefaultPillarbox
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * ViewModel that load a media that send always a [BlockReasonException.StartDate] during the first minute.
 *
 * @param application The [Application].
 */
class ContentNotYetAvailableViewModel(application: Application) : AndroidViewModel(application) {
    private class AlwaysStartDateBlockedAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
        private val srgAssetLoader = SRGAssetLoader(context)
        private val validFrom = Clock.System.now().plus(1.minutes)
        override fun canLoadAsset(mediaItem: MediaItem): Boolean {
            return srgAssetLoader.canLoadAsset(mediaItem)
        }

        override suspend fun loadAsset(mediaItem: MediaItem): Asset {
            if (validFrom <= Clock.System.now()) {
                return srgAssetLoader.loadAsset(mediaItem)
            }
            throw BlockReasonException.StartDate(validFrom)
        }
    }

    /**
     * Player
     */
    val player: PillarboxExoPlayer = PillarboxExoPlayer(
        context = application,
        mediaSourceFactory = PillarboxMediaSourceFactory(
            context = application
        ).apply {
            addAssetLoader(AlwaysStartDateBlockedAssetLoader(application))
        },
        monitoringMessageHandler = DefaultPillarbox.defaultMonitoringMessageHandler,
    )

    init {
        player.prepare()
        player.setMediaItem(DemoItem.OnDemandHorizontalVideo.toMediaItem())
        player.play()
    }

    override fun onCleared() {
        player.release()
    }
}
