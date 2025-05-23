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
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesUnifiedStreaming
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.SmoothProgressTrackerState
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowConversionToBitmap
import coil3.size.Scale
import coil3.toBitmap

/**
 * A ViewModel to demonstrate how to work with Image track.
 *
 * @param application The [Application].
 */
class ThumbnailViewModel(application: Application) : AndroidViewModel(application), ImageOutput {
    private val imageLoader = application.imageLoader

    /**
     * Player
     */
    val player = PillarboxExoPlayer(application) {
        srgAssetLoader(application) {
            spriteSheetLoader { spriteSheet ->
                val request = ImageRequest.Builder(application)
                    .data(spriteSheet.url)
                    .scale(Scale.FILL) // FILL to have the source image size!
                    .allowConversionToBitmap(enable = true)
                    .build()
                val result = imageLoader.execute(request)
                when (result) {
                    is SuccessResult -> Result.success(result.image.toBitmap())
                    is ErrorResult -> Result.failure(result.throwable)
                }
            }
        }
    }

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
        player.addMediaItem(SamplesSRG.OnDemandHorizontalVideo.toMediaItem())
        player.addMediaItem(SRGMediaItem("urn:rsi:video:2366175"))
        player.addMediaItem(SamplesUnifiedStreaming.DASH_Tiled_Thumbnails.toMediaItem())
        player.addMediaItem(SamplesUnifiedStreaming.DASH_TrickPlay.toMediaItem())
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
