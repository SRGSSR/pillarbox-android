/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import ch.srgssr.pillarbox.player.utils.DebugLogger
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.media.MediaQueue

internal data class CastItemData(val id: Int, val item: MediaQueueItem?)

internal class MediaQueueTracker(
    private val mediaQueue: MediaQueue,
    private val invalidateState: () -> Unit
) : MediaQueue.Callback() {

    private val mapFetchedMediaQueueItem = mutableMapOf<Int, MediaQueueItem>()

    // Store the corresponding ids
    private var lastItemIds = mediaQueue.itemIds

    /**
     * A list of [CastItemData] that has the same size as [MediaQueue.getItemIds].
     */
    var listCastItemData: List<CastItemData> = emptyList()
        private set

    init {
        mediaQueue.registerCallback(this)
    }

    fun release() {
        mediaQueue.unregisterCallback(this)
        mapFetchedMediaQueueItem.clear()
        listCastItemData = emptyList()
    }

    fun updateWithMediaStatus(mediaStatus: MediaStatus) {
        DebugLogger.debug(TAG, "updateWithMediaStatus ${mediaStatus.queueItems.map { it.itemId }} mapSize = ${mapFetchedMediaQueueItem.size}")
        mediaStatus.queueItems.forEach {
            mapFetchedMediaQueueItem[it.itemId] = it
        }
        if (mediaStatus.queueItemCount != mediaQueue.itemCount) {
            fetchAllIfNeeded()
        }
        update()
    }

    private fun fetchAllIfNeeded() {
        val itemIds = mediaQueue.itemIds
        for (i in 0 until mediaQueue.itemCount) {
            val itemId = itemIds[i]
            val fetchIsNeeded = !mapFetchedMediaQueueItem.containsKey(itemId)
            mediaQueue.getItemAtIndex(i, fetchIsNeeded)?.let {
                mapFetchedMediaQueueItem[itemId] = it
            }
        }
    }

    private fun update() {
        lastItemIds = mediaQueue.itemIds
        listCastItemData = lastItemIds.map { itemId ->
            CastItemData(itemId, mapFetchedMediaQueueItem[itemId])
        }
        invalidateState()
    }

    /**
     * Called when the queue has been entirely reloaded.
     */
    override fun itemsReloaded() {
        DebugLogger.debug(TAG, "itemsReloaded #${mediaQueue.itemCount}")
    }

    /**
     * Called when one or more changes have been made to the queue.
     */
    override fun mediaQueueChanged() {
        DebugLogger.debug(TAG, "mediaQueueChanged #${mediaQueue.itemCount}")
        /*lastMediaStatus?.let {
            updateWithMediaStatus(it)
        }*/
        update()
    }

    /**
     * Called when one or more changes are about to be made to the queue.
     */
    override fun mediaQueueWillChange() {
        DebugLogger.debug(TAG, "mediaQueueWillChange #${mediaQueue.itemCount}")
    }

    override fun itemsInsertedInRange(insertIndex: Int, insertCount: Int) {
        DebugLogger.debug(TAG, "itemsInsertedInRange $insertIndex $insertCount")
    }

    /*
     * Is called when mediaQueue.getItemAt(xx, true) fetches item.
     * mediaQueueChanged is also called
     */
    override fun itemsUpdatedAtIndexes(indexes: IntArray) {
        DebugLogger.debug(TAG, "itemUpdatedAtIndex ${indexes.contentToString()}")
        indexes.forEach { index ->
            mediaQueue.getItemAtIndex(index, false)?.let {
                mapFetchedMediaQueueItem[it.itemId] = it
            }
        }
        fetchAllIfNeeded()
    }

    override fun itemsRemovedAtIndexes(indexes: IntArray) {
        DebugLogger.debug(
            TAG,
            "itemsRemovedAtIndexes ${indexes.contentToString()} into ${lastItemIds.contentToString()} current ${mediaQueue.itemIds.contentToString()}"
        )
        indexes.forEach {
            mapFetchedMediaQueueItem.remove(lastItemIds[it])
        }
    }

    override fun itemsReorderedAtIndexes(indexes: List<Int>, insertBeforeIndex: Int) {
        DebugLogger.debug(TAG, "itemsReorderedAtIndexes $insertBeforeIndex $indexes")
    }

    private companion object {
        const val TAG = "MediaQueue"
    }
}
