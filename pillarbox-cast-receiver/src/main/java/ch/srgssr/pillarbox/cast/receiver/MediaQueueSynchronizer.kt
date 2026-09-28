/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import com.google.android.gms.cast.MediaQueueItem
import java.util.Collections

@Suppress("TooManyFunctions")
internal class MediaQueueSynchronizer(
    private val player: Player,
    private val mediaItemConverter: MediaItemConverter,
    private val autoGenerateItemId: () -> Int,
) {
    private var _mediaQueueItems: MutableList<MediaQueueItem> = mutableListOf()
    val mediaQueueItems
        get() = _mediaQueueItems.toList()

    val size: Int
        get() = _mediaQueueItems.size

    operator fun get(index: Int) = _mediaQueueItems[index]

    fun getIndexOfItemIdOrNull(itemId: Int) = _mediaQueueItems.indexOfFirst { item -> item.itemId == itemId }.takeIf { it >= 0 }

    fun isEmpty(): Boolean = _mediaQueueItems.isEmpty()

    /**
     * Does not modify [Player] as it exists a lot of versions of it that is playback related.
     */
    fun notifySetMediaItems(mediaItems: List<MediaItem>): List<MediaQueueItem> {
        _mediaQueueItems = mediaItems.map { mediaItem ->
            mediaItemConverter.toMediaQueueItem(mediaItem)
                .also { it.writer.setItemId(autoGenerateItemId()) }
        }.toMutableList()
        return _mediaQueueItems
    }

    fun addMediaItems(index: Int, mediaItems: List<MediaItem>): List<MediaQueueItem> {
        val itemsToAdd = mediaItems.map(mediaItemConverter::toMediaQueueItem)
        insertMediaQueueItems(index, itemsToAdd)
        player.addMediaItems(index, mediaItems)
        return itemsToAdd
    }

    fun removeMediaItems(fromIndex: Int, toIndex: Int): List<Int> {
        val queueItemsToRemove = _mediaQueueItems.subList(fromIndex, toIndex)
        val itemIdRemoved = queueItemsToRemove.map { it.itemId }
        queueItemsToRemove.clear()
        player.removeMediaItems(fromIndex, toIndex)
        return itemIdRemoved
    }

    fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        moveQueueItems(fromIndex, toIndex, newIndex)
        player.moveMediaItems(fromIndex, toIndex, newIndex)
    }

    fun updateMetadata() {
        for (i in 0 until player.mediaItemCount) {
            updateMetadata(i)
        }
    }

    private fun updateMetadata(index: Int) {
        val mediaItem = player.getMediaItemAt(index)
        val queueItem = _mediaQueueItems[index]
        val updatedQueueItem = mediaItemConverter.toMediaQueueItem(mediaItem)
        updatedQueueItem.media?.metadata?.let {
            queueItem.media?.writer?.setMetadata(it)
        }
    }

    fun queueInsert(itemsToAdd: List<MediaQueueItem>, insertBeforeId: Int? = null) {
        val insertIndex = insertBeforeId?.let {
            getIndexOfItemIdOrNull(it)
        } ?: Int.MAX_VALUE
        insertMediaQueueItems(insertIndex, itemsToAdd)
        player.addMediaItems(insertIndex, itemsToAdd.map { mediaItemConverter.toMediaItem(it) })
    }

    fun queueReorder(itemIds: List<Int>, insertBeforeId: Int? = null) {
        if (insertBeforeId == null) {
            moveAtTheEndOfTheQueue(itemIds)
        } else {
            reorderQueueItemsBeforeItemId(insertBeforeId, itemIds)
        }
    }

    private fun insertMediaQueueItems(insertIndex: Int, itemsToAdd: List<MediaQueueItem>) {
        itemsToAdd.forEach { queueItem ->
            queueItem.writer.apply {
                setItemId(autoGenerateItemId())
            }
        }
        if (insertIndex >= _mediaQueueItems.size) {
            _mediaQueueItems.addAll(itemsToAdd)
        } else {
            _mediaQueueItems.addAll(insertIndex, itemsToAdd)
        }
    }

    fun removeQueueItems(itemIdToRemove: List<Int>): List<Int> {
        val removeMediaId = mutableListOf<Int>()
        itemIdToRemove.forEach { itemId ->
            getIndexOfItemIdOrNull(itemId)?.let {
                _mediaQueueItems.removeAt(it)
                player.removeMediaItem(it)
                removeMediaId.add(itemId)
            }
        }
        return removeMediaId
    }

    fun shuffle() {
        val queueItemIds = _mediaQueueItems.map { item -> item.itemId }
        Collections.shuffle(queueItemIds)
        queueReorder(queueItemIds)
    }

    private fun moveQueueItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        Util.moveItems(_mediaQueueItems, fromIndex, toIndex, newIndex)
    }

    /*
     * [A,D,G,H,B,E] reorder at the end [D,H,B] => [A,G,E,D,H,B]
     */
    private fun moveAtTheEndOfTheQueue(itemIds: List<Int>) {
        itemIds.forEach { itemId ->
            val index = _mediaQueueItems.indexOfFirst { it.itemId == itemId }
            if (index >= 0) {
                moveQueueItems(index, index + 1, player.mediaItemCount)
                player.moveMediaItem(index, player.mediaItemCount)
            }
        }
    }

    private fun reorderQueueItemsBeforeItemId(insertBeforeId: Int, itemIds: List<Int>) {
        itemIds.forEach { itemId ->
            val index = _mediaQueueItems.indexOfFirst { it.itemId == itemId }
            val insertBeforeIndex = _mediaQueueItems.indexOfFirst { it.itemId == insertBeforeId }
            if (index >= 0 && insertBeforeIndex >= 0) {
                val indexToMove = if (index > insertBeforeIndex) insertBeforeIndex else (insertBeforeIndex - 1).coerceAtLeast(0)
                moveQueueItems(index, index + 1, indexToMove)
                player.moveMediaItem(index, indexToMove)
            }
        }
    }
}
