/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.util.Log
import com.google.android.gms.cast.MediaQueueItem
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
     * A list of [CastItemData] that have same size as [MediaQueue.getItemIds]
     */
    var listCastItemData: List<CastItemData> = emptyList()

    init {
        mediaQueue.registerCallback(this)
        update()
    }

    fun release() {
        mediaQueue.unregisterCallback(this)
        mapFetchedMediaQueueItem.clear()
        listCastItemData = emptyList()
    }

    fun update() {
        for (i in 0 until mediaQueue.itemCount) {
            val itemId = mediaQueue.itemIds[i]
            val fetchIsNeeded = !mapFetchedMediaQueueItem.containsKey(itemId)
            mediaQueue.getItemAtIndex(i, fetchIsNeeded)?.let {
                mapFetchedMediaQueueItem[itemId] = it
            }
        }
        lastItemIds = mediaQueue.itemIds
        listCastItemData = mediaQueue.itemIds.map { itemId ->
            CastItemData(itemId, mapFetchedMediaQueueItem[itemId])
        }
        invalidateState()
    }

    override fun itemsReloaded() {
        Log.d(TAG, "itemsReloaded")
    }

    override fun mediaQueueChanged() {
        Log.d(TAG, "mediaQueueChanged")
        update()
    }

    override fun mediaQueueWillChange() {
        Log.d(TAG, "mediaQueueWillChange")
    }

    override fun itemsInsertedInRange(insertIndex: Int, insertCount: Int) {
        Log.d(TAG, "itemsInsertedInRange $insertIndex $insertCount")
    }

    /*
     * Is called when mediaQueue.getItemAt(xx,true) fetches item.
     * mediaQueueChanged is also called
     */
    override fun itemsUpdatedAtIndexes(indexes: IntArray) {
        Log.d(TAG, "itemUpdatedAtIndex ${indexes.contentToString()}")
        indexes.forEach { index ->
            mediaQueue.getItemAtIndex(index, false)?.let {
                mapFetchedMediaQueueItem[it.itemId] = it
            }
        }
    }

    override fun itemsRemovedAtIndexes(indexes: IntArray) {
        Log.d(
            TAG,
            "itemsRemovedAtIndexes ${indexes.contentToString()} into ${lastItemIds.contentToString()} current ${mediaQueue.itemIds.contentToString()}"
        )
        indexes.forEach {
            mapFetchedMediaQueueItem.remove(lastItemIds[it])
        }
    }

    override fun itemsReorderedAtIndexes(indexes: List<Int>, insertBeforeIndex: Int) {
        Log.d(TAG, "itemsReorderedAtIndexes $insertBeforeIndex $indexes")
    }

    private companion object {
        const val TAG = "MediaQueue"
    }
}
