/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.app.Application
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.isCastSessionAvailableAsFlow
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.currentMediaMetadataAsFlow
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import coil3.SingletonImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    private val itemTracking = ItemsTracking()
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

    /**
     * The artwork drawable of the currently playing media item, or `null` if no artwork is available.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val artworkDrawable = currentPlayer.flatMapLatest { it.currentMediaMetadataAsFlow(withPlaylistMediaMetadata = true) }
        .onEach { Log.d("MainViewModel", "🆕️ mediaMetadata($it), artworkUri(${it.artworkUri})") }
        .flowOn(Dispatchers.Main)
        .map {
            val artworkUri = it.artworkUri ?: return@map null
            val imageRequest = ImageRequest.Builder(application)
                .data(artworkUri)
                .build()

            SingletonImageLoader.get(application)
                .execute(imageRequest)
                .image
                ?.asDrawable(application.resources)
        }
        .map { it ?: ContextCompat.getDrawable(application, R.drawable.ic_cast_128) }
        .onEach { Log.d("MainViewModel", "✍🏻️ artworkDrawable($it)") }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, WhileSubscribed(), null)

    private var listItems = emptyList<MediaItem>()

    override fun onCleared() {
        castPlayer.release()
        localPlayer.release()
    }

    private fun setupPlayer(player: Player) {
        if (player.mediaItemCount == 0) {
            val mediaItems = (Playlist.StoryUrns.items + Playlist.VideoUrls.items).map { it.toMediaItem() }
            player.setMediaItems(mediaItems)
            player.prepare()
            player.play()
        }
        listItems = player.getCurrentMediaItems()
        player.addListener(itemTracking)
    }

    private fun switchPlayer(player: Player) {
        val oldPlayer = if (player == castPlayer) localPlayer else castPlayer
        oldPlayer.removeListener(itemTracking)

        player.addListener(itemTracking)
        player.playWhenReady = oldPlayer.playWhenReady
        player.setMediaItems(listItems, oldPlayer.currentMediaItemIndex, oldPlayer.currentPosition)
        player.prepare()
        oldPlayer.stop()
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

    private inner class ItemsTracking : PillarboxPlayer.Listener {
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            // Currently when restoring from a CastPlayer, the playlist is cleared. It might be fixed in a future version of Media3.
            listItems = currentPlayer.value.getCurrentMediaItems()
                .filter { it.localConfiguration != null }
        }
    }
}
