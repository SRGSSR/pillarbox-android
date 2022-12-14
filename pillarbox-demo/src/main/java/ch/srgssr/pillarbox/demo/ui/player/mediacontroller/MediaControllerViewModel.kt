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
 * @param application
 */
class MediaControllerViewModel(application: Application) : AndroidViewModel(application), Player.Listener {
    private val controllerConnection = MediaBrowserConnection(application, ComponentName(application, DemoMediaLibraryService::class.java))

    /**
     * Player
     */
    val player = controllerConnection.mediaController
    private val _items = MutableStateFlow(listOf<MediaItem>())

    /**
     * List of playable items that are inside the MediaLibrary.
     */
    val items: StateFlow<List<MediaItem>> = _items
    private val _currentPlayingItem = MutableStateFlow(MediaItem.EMPTY)

    /**
     * Current playing item
     */
    val currentPlayingItem: StateFlow<MediaItem> = _currentPlayingItem
    private val _currentPlaylistItems = MutableStateFlow(listOf<MediaItem>())

    /**
     * Current list of MediaItems in the player playlist
     */
    val currentPlaylistItems: StateFlow<List<MediaItem>> = _currentPlaylistItems

    init {
        viewModelScope.launch {
            player.collectLatest {
                it?.let {
                    _currentPlayingItem.value = it.currentMediaItem ?: MediaItem.EMPTY
                    _currentPlaylistItems.value = getPlayerListItems(it)
                    it.addListener(this@MediaControllerViewModel)
                }
                _items.value = it?.let { getListItems(it) } ?: emptyList()
            }
        }
    }

    /**
     * Get list of MediaItems, not all MediaItems are playable.
     * They are use to build the tree hierarchy of the Android auto content.
     * For this demo, you flatten the hierarchy and provide a single list of items.
     */
    private suspend fun getListItems(mediaBrowser: MediaBrowser): List<MediaItem> {
        val root = mediaBrowser.getLibraryRoot(null).await().value!!
        val playlists = getChildren(mediaBrowser, root.mediaId)
        val listItems = mutableListOf<MediaItem>()
        for (playlist in playlists) {
            // In our case, children are the playlists so children are the playable content
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
     * @param mediaItem MediaIterm to add
     * @param autoPlay AutoPlay set to true to start playback too
     */
    fun addItemToPlaylist(mediaItem: MediaItem, autoPlay: Boolean = false) {
        getPlayer().addMediaItem(mediaItem)
        if (getPlayer().playbackState == Player.STATE_IDLE || getPlayer().playbackState == Player.STATE_ENDED) {
            getPlayer().prepare()
        }
        if (autoPlay) {
            getPlayer().seekToDefaultPosition(getPlayer().mediaItemCount - 1)
            getPlayer().play()
        }
    }

    /**
     * Remove item from the playlist
     *
     * @param mediaItem MediaItem to remove
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
        player.value?.removeListener(this)
        controllerConnection.release()
    }

    private fun getPlayer(): Player {
        return player.value!!
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        _currentPlayingItem.value = mediaItem ?: MediaItem.EMPTY
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        player.value?.let {
            _currentPlaylistItems.value = getPlayerListItems(it)
        }
    }

    private fun getPlayerListItems(player: Player): List<MediaItem> {
        val mediaList = mutableListOf<MediaItem>()
        val itemCount = player.mediaItemCount
        for (i in 0 until itemCount) {
            mediaList += player.getMediaItemAt(i)
        }
        return mediaList
    }

    companion object {
        private const val PAGE_SIZE = 100
    }
}
