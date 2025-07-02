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
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.exoplayer.image.ImageOutput
import androidx.media3.session.SessionToken
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.core.business.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesUnifiedStreaming
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import ch.srgssr.pillarbox.player.session.PillarboxMediaController
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowConversionToBitmap
import coil3.size.Scale
import coil3.toBitmap
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

/**
 * A ViewModel to demonstrate how to work with Image track regardless the player. Of course, when casting, some features aren't supported.
 *
 * @param application The [Application].
 */
class ThumbnailViewModel(application: Application) : AndroidViewModel(application), ImageOutput, SessionAvailabilityListener {
    private val imageLoader = application.imageLoader
    private val mediaSession: PillarboxMediaSession

    private val localPlayer = PillarboxExoPlayer(application) {
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
    private val castPlayer: PillarboxCastPlayer = PillarboxCastPlayer(application).apply { setSessionAvailabilityListener(this@ThumbnailViewModel) }

    /**
     * Thumbnail
     */
    var thumbnail by mutableStateOf<Bitmap?>(null)
        private set

    /**
     * Player may be null until [PillarboxMediaController] is built.
     */
    var player by mutableStateOf<PillarboxPlayer?>(null)
        private set

    init {
        mediaSession = PillarboxMediaSession.Builder(application, localPlayer)
            .setId("ThumbnailMediaSession")
            .build()

        viewModelScope.launch {
            val sessionToken = SessionToken.createSessionToken(application, mediaSession.token).await()
            // Connect PillarboxMediaController to the MediaSession instead of the MediaSessionService. We don't need background playback.
            player = PillarboxMediaController.Builder(application, sessionToken).build().apply {
                prepare()
                addMediaItem(SamplesSRG.OnDemandHorizontalVideo.toMediaItem())
                addMediaItem(SRGMediaItem("urn:srf:video:881be9c2-65ec-4fa9-ba4a-926d15d046ef"))
                addMediaItem(SRGMediaItem("urn:rsi:video:2366175"))
                addMediaItem(SamplesUnifiedStreaming.DASH_Tiled_Thumbnails.toMediaItem())
                addMediaItem(SamplesUnifiedStreaming.DASH_TrickPlay.toMediaItem())
            }
        }
    }

    override fun onCastSessionAvailable() {
        localPlayer.stop()
        castPlayer.setMediaItems(localPlayer.getCurrentMediaItems(), localPlayer.currentMediaItemIndex, localPlayer.currentPosition)
        castPlayer.prepare()
        castPlayer.play()
        mediaSession.player = castPlayer
    }

    override fun onCastSessionUnavailable() {
        localPlayer.seekTo(castPlayer.currentMediaItemIndex, castPlayer.currentPosition)
        castPlayer.stop()
        localPlayer.prepare()
        localPlayer.play()
        mediaSession.player = localPlayer
    }

    override fun onCleared() {
        player?.release()
        mediaSession.release()
        localPlayer.release()
        castPlayer.release()
    }

    override fun onImageAvailable(presentationTimeUs: Long, bitmap: Bitmap) {
        thumbnail = bitmap
    }

    override fun onDisabled() {
        thumbnail = null
    }
}
