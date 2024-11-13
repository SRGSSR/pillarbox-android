/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts.thumbnail

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.image.ImageOutput
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.SmoothProgressTrackerState

/**
 * A ViewModel to demonstrate how to work with Image track.
 *
 * @param application The [Application].
 */
class ThumbnailViewModel(application: Application) : AndroidViewModel(application), ImageOutput {
    /**
     * Player
     */
    val player = PillarboxExoPlayer(application)

    /**
     * Thumbnail
     */
    var thumbnail by mutableStateOf<Bitmap?>(null)
        private set

    /**
     * Progress tracker state
     */
    val progressTrackerState: ProgressTrackerState = SmoothProgressTrackerState(player, viewModelScope, this)

    init {
        player.prepare()
        player.addMediaItem(SRGMediaItem("urn:srf:video:881be9c2-65ec-4fa9-ba4a-926d15d046ef"))
        player.addMediaItem(DemoItem.OnDemandHorizontalVideo.toMediaItem())
        player.addMediaItem(DemoItem.UnifiedStreamingOnDemand_Dash_TiledThumbnails.toMediaItem())
        player.addMediaItem(DemoItem.UnifiedStreamingOnDemand_Dash_TrickPlay.toMediaItem())
    }

    override fun onCleared() {
        player.release()
    }

    override fun onImageAvailable(presentationTimeUs: Long, bitmap: Bitmap) {
        thumbnail = bitmap
    }

    override fun onDisabled() {
        thumbnail = null
    }
}
