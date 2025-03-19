/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.isCastSessionAvailableAsFlow
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.core.business.cast.SRGMediaItemConverter
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import com.google.android.gms.cast.MediaStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform

/**
 * ViewModel that olds current player and handle local to remote player switch.
 *
 * Best result can be achieved with a PillarboxMediaSessionService and with PillarboxMediaController.
 *
 * @param application The application context.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val castPlayer = PillarboxCastPlayer(application)
    private val localPlayer = PillarboxExoPlayer(application)

    /**
     * The current player, it can be either a [PillarboxCastPlayer] or a [PillarboxExoPlayer].
     */
    val currentPlayer = castPlayer.isCastSessionAvailableAsFlow()
        .map { if (it) castPlayer else localPlayer }
        .distinctUntilChanged()
        .onEach(onFirstValue = ::setupPlayer, onRemainingValues = ::switchPlayer)
        .stateIn(viewModelScope, WhileSubscribed(), if (castPlayer.isCastSessionAvailable()) castPlayer else localPlayer)

    override fun onCleared() {
        castPlayer.release()
        localPlayer.release()
    }

    private fun setupPlayer(player: Player) {
        if (player is PillarboxCastPlayer) {
            setupCastPlayer()
            return
        }
        if (player.mediaItemCount == 0) {
            val mediaItems = listOf(
                DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_TTML,
                DemoItem.GoogleDashH265_CENC_Widewine,
                DemoItem.UnifiedStreamingOnDemandLimitedBandwidth,
                DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_RFC_tags,
                DemoItem.OnDemandAudio,
                DemoItem.OnDemandAudioMP3,
                DemoItem.OnDemandHorizontalVideo,
                DemoItem.DvrVideo,
            ).map { it.toMediaItem() }
            player.setMediaItems(mediaItems)
            player.prepare()
            player.play()
        }
    }

    private fun setupCastPlayer() {
        // As playlist are not yet fully supported we have to create playlist with RemoteClient.
        if (castPlayer.remoteMediaClient?.mediaQueue?.itemCount == 0) {
            val mediaItemConverter = SRGMediaItemConverter()
            val mediaItems = listOf(
                DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_TTML,
                DemoItem.GoogleDashH265_CENC_Widewine,
                DemoItem.UnifiedStreamingOnDemandLimitedBandwidth,
                DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_RFC_tags,
                DemoItem.OnDemandAudio,
                DemoItem.OnDemandAudioMP3,
                DemoItem.OnDemandHorizontalVideo,
                DemoItem.DvrVideo,
            )
                .map { it.toMediaItem() }
                .map(mediaItemConverter::toMediaQueueItem)
                .toTypedArray()
            val startIndex = localPlayer.currentMediaItemIndex
            val repeatMode = when (localPlayer.repeatMode) {
                REPEAT_MODE_OFF -> MediaStatus.REPEAT_MODE_REPEAT_OFF
                REPEAT_MODE_ONE -> MediaStatus.REPEAT_MODE_REPEAT_SINGLE
                REPEAT_MODE_ALL -> MediaStatus.REPEAT_MODE_REPEAT_ALL
                else -> MediaStatus.REPEAT_MODE_REPEAT_OFF
            }

            castPlayer.remoteMediaClient?.queueLoad(mediaItems, startIndex, repeatMode, null)
        }
    }

    private fun switchPlayer(player: Player) {
        val oldPlayer = if (player == castPlayer) localPlayer else castPlayer
        if (player is PillarboxCastPlayer) {
            setupCastPlayer()
        }
        player.repeatMode = oldPlayer.repeatMode
        player.playWhenReady = oldPlayer.playWhenReady
        oldPlayer.stop()
        oldPlayer.clearMediaItems()
    }

    private fun <T> Flow<T>.onEach(
        onFirstValue: suspend (value: T) -> Unit,
        onRemainingValues: suspend (value: T) -> Unit,
    ): Flow<T> {
        var first = true
        return transform { value ->
            if (first) {
                onFirstValue(value)
                first = false
            } else {
                onRemainingValues(value)
            }

            emit(value)
        }
    }
}
