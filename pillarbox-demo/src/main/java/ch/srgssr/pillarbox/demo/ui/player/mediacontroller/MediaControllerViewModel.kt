/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.mediacontroller

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaBrowser
import ch.srgssr.pillarbox.demo.service.DemoMediaLibraryService
import ch.srgssr.pillarbox.player.service.MediaBrowserConnection
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

/**
 * Media controller view model
 *
 * @constructor
 *
 * @param application
 */
class MediaControllerViewModel(application: Application) : AndroidViewModel(application) {
    private val controllerConnection = MediaBrowserConnection(application, ComponentName(application, DemoMediaLibraryService::class.java))

    /**
     * Player
     */
    val player = controllerConnection.mediaBrowser
    private val _items = MutableStateFlow(listOf<MediaItem>())

    /**
     * List of items that are inside the MediaLibrary.
     */
    val items: StateFlow<List<MediaItem>> = _items
    private val _currentPlayingItem = MutableStateFlow(MediaItem.EMPTY)

    /**
     * Current playing item
     */
    val currentPlayingItem: StateFlow<MediaItem> = _currentPlayingItem
    private val listener = ComponentListener()
    private val _currentPlaylistItems = MutableStateFlow(listOf<MediaItem>())

    /**
     * Current list of MediaItems in the playlist of the Player
     */
    val currentPlaylistItems: StateFlow<List<MediaItem>> = _currentPlaylistItems

    init {
        viewModelScope.launch {
            player.collectLatest {
                _currentPlayingItem.value = it?.currentMediaItem ?: MediaItem.EMPTY
                it?.addListener(listener)
                _items.value = it?.let { getListItems(it) } ?: emptyList()
            }
        }
    }

    private suspend fun getListItems(mediaBrowser: MediaBrowser): List<MediaItem> {
        val root = mediaBrowser.getLibraryRoot(null).await().value!!
        // In your case, children are the playlists so children are the playable content
        val playlists = getChildren(mediaBrowser, root.mediaId)
        val listItems = mutableListOf<MediaItem>()
        for (playlist in playlists) {
            listItems += getChildren(mediaBrowser, playlist.mediaId)
        }
        return listItems
    }

    private suspend fun getChildren(mediaBrowser: MediaBrowser, parentId: String): ImmutableList<MediaItem> {
        return mediaBrowser.getChildren(parentId, 0, PAGE_SIZE, null).await().value ?: ImmutableList.of()
    }

    /**
     * Play item
     *
     * @param mediaItem
     */
    fun playItem(mediaItem: MediaItem) {
        var isInPlaylist = false
        for (i in 0 until getPlayer().mediaItemCount) {
            if (getPlayer().getMediaItemAt(i).mediaId == mediaItem.mediaId) {
                getPlayer().seekToDefaultPosition(i)
                isInPlaylist = true
                break
            }
        }
        if (!isInPlaylist) {
            addItemToPlaylist(mediaItem, true)
        }
    }

    /**
     * Add item to playlist
     *
     * @param mediaItem
     * @param play
     */
    fun addItemToPlaylist(mediaItem: MediaItem, play: Boolean = false) {
        getPlayer().addMediaItem(mediaItem)
        if (play) {
            getPlayer().seekToDefaultPosition(getPlayer().mediaItemCount - 1)
            getPlayer().play()
        }
    }

    /**
     * Remove item
     *
     * @param mediaItem
     */
    fun removeItem(mediaItem: MediaItem) {
        val itemCount = getPlayer().mediaItemCount
        for (i in 0 until itemCount) {
            if (getPlayer().getMediaItemAt(i).mediaId == mediaItem.mediaId) {
                getPlayer().removeMediaItem(i)
                break
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.value?.removeListener(listener)
        controllerConnection.release()
    }

    private fun getPlayer(): Player {
        return player.value!!
    }

    private inner class ComponentListener : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            _currentPlayingItem.value = mediaItem ?: MediaItem.EMPTY
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            super.onTimelineChanged(timeline, reason)
            if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
                val mediaList = mutableListOf<MediaItem>()
                for (i in 0 until getPlayer().mediaItemCount) {
                    mediaList += getPlayer().getMediaItemAt(i)
                }
                _currentPlaylistItems.value = mediaList
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 100
    }
}
