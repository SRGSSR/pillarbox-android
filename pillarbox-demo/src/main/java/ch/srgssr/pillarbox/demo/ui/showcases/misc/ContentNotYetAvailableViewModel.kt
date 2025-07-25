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
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.core.business.source.SRGAssetLoader
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.player.Default
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * ViewModel that load a media that send always a [BlockReasonException.StartDate] during the first minute.
 *
 * @param application The [Application].
 */
class ContentNotYetAvailableViewModel(application: Application) : AndroidViewModel(application) {
    private class AlwaysStartDateBlockedAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
        private val srgAssetLoader = SRGAssetLoader(context)
        private val validFrom = Clock.System.now().plus(2.days + 1.hours + 34.minutes)
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
    val player: PillarboxExoPlayer = PillarboxExoPlayer(application, Default) {
        +AlwaysStartDateBlockedAssetLoader(application)
    }

    init {
        player.prepare()
        player.setMediaItem(SamplesSRG.OnDemandHorizontalVideo.toMediaItem())
        player.play()
    }

    override fun onCleared() {
        player.release()
    }
}
