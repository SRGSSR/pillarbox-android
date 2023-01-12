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
    private val _currentPlayingItem = MutableStateFlow(0)

    /**
     * Current playing item
     */
    val currentPlayingItem: StateFlow<Int> = _currentPlayingItem
    private val _currentPlaylistItems = MutableStateFlow(listOf<PlaylistItem>())

    /**
     * Current list of MediaItems in the player playlist
     */
    val currentPlaylistItems: StateFlow<List<PlaylistItem>> = _currentPlaylistItems

    init {
        viewModelScope.launch {
            player.collectLatest {
                it?.let {
                    _currentPlayingItem.value = it.currentMediaItemIndex
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
     * Add media item to playlist
     *
     * @param mediaItem
     */
    fun addMediaItemToPlaylist(mediaItem: MediaItem) {
        getPlayer().addMediaItem(mediaItem)
        if (getPlayer().playbackState == Player.STATE_IDLE || getPlayer().playbackState == Player.STATE_ENDED) {
            getPlayer().prepare()
        }
    }

    /**
     * Play item or restart the media if  it is current
     *
     * @param mediaItem PlayListItem to play
     */
    fun playItem(mediaItem: PlaylistItem) {
        if (getPlayer().currentMediaItemIndex == mediaItem.index) {
            getPlayer().seekToDefaultPosition()
        } else {
            getPlayer().seekToDefaultPosition(mediaItem.index)
        }
        getPlayer().play()
    }

    /**
     * Move up increase the item position inside the playlist
     *
     * @param mediaItem
     */
    fun moveUp(mediaItem: PlaylistItem) {
        val upIndex = mediaItem.index + 1
        getPlayer().moveMediaItem(mediaItem.index, upIndex)
    }

    /**
     * Move down decrease the item position inside the playlist
     *
     * @param mediaItem
     */
    fun moveDown(mediaItem: PlaylistItem) {
        val downIndex = mediaItem.index - 1
        getPlayer().moveMediaItem(mediaItem.index, downIndex)
    }

    /**
     * Remove from playlist
     *
     * @param mediaItem Item to remove from playlist
     */
    fun removeFromPlaylist(mediaItem: PlaylistItem) {
        getPlayer().removeMediaItem(mediaItem.index)
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
        _currentPlayingItem.value = getPlayer().currentMediaItemIndex
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        player.value?.let {
            _currentPlaylistItems.value = getPlayerListItems(it)
            _currentPlayingItem.value = it.currentMediaItemIndex
        }
    }

    private fun getPlayerListItems(player: Player): List<PlaylistItem> {
        val mediaList = mutableListOf<PlaylistItem>()
        val itemCount = player.mediaItemCount
        for (i in 0 until itemCount) {
            val mediaItem = player.getMediaItemAt(i)
            mediaList += PlaylistItem(index = i, mediaId = mediaItem.mediaId, title = mediaItem.mediaMetadata.title.toString())
        }
        return mediaList
    }

    companion object {
        private const val PAGE_SIZE = 100
    }
}

/**
 * Playlist item representing a Player Item
 *
 * @property index Index of the item inside the player playlist
 * @property mediaId MediaId of the item
 * @property title Title of the item
 * @constructor Create empty Playlist item
 */
data class PlaylistItem(val index: Int, val mediaId: String, val title: String)
