/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaQueueData
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.tv.media.MediaCommandCallback
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.QueueInsertRequestData
import com.google.android.gms.cast.tv.media.QueueRemoveRequestData
import com.google.android.gms.cast.tv.media.QueueReorderRequestData
import com.google.android.gms.cast.tv.media.QueueUpdateRequestData
import com.google.android.gms.common.images.WebImage
import com.google.android.gms.tasks.Task

class ReceiverCallback(val player: PillarboxPlayer, val mediaManager: MediaManager): MediaCommandCallback() {
    override fun onQueueUpdate(senderId: String?, request: QueueUpdateRequestData): Task<Void?> {
        Log.d("ReceiverCallback", "onQueueUpdate ${request.currentItemId}")
        // Issue when we jump several times frenetically.
        request.currentItemId?.let { itemIdToSeekTo ->
            val index = player.getCurrentMediaItems().indexOfFirst { it.mediaId.toIntOrNull() == itemIdToSeekTo }
            if (index >= 0) {
                mediaManager.mediaQueueManager.currentItemId = itemIdToSeekTo
                player.seekTo(index, C.TIME_UNSET)
                Log.d("ReceiverCallback", "SeekTo Item ID $itemIdToSeekTo")
            } else {
                Log.w("ReceiverCallback", "Item with ID $itemIdToSeekTo not found")
            }
        }

        mediaManager.broadcastMediaStatusWithUpdatedMediaInfo()

        return super.onQueueUpdate(senderId, request)
    }
    override fun onQueueInsert(senderId: String?, request: QueueInsertRequestData): Task<Void?> {

        Log.d("ReceiverCallback", "QueueInsertRequestData: ${request.items} before ${request.insertBefore}")
        // Insert into the Pillarbox player!

        val mediaItems: List<MediaItem> = request.items?.map { item ->
            val metadata = MediaMetadata
                .Builder()
                .setTitle(item.media?.metadata?.getString(com.google.android.gms.cast.MediaMetadata.KEY_TITLE))
                .setArtworkUri(item.media?.metadata?.images?.get(0)?.url)
                .build()
            val mediaItem = MediaItem
                .Builder()
                .setUri(item.media?.contentUrl)
                .setMediaMetadata(metadata)
                .setMediaId(mediaManager.mediaQueueManager.autoGenerateItemId().toString()) // https://developers.google.com/cast/docs/android_tv_receiver/queueing#changing_the_queue
                .build()
            Log.d("ReceiverCallback", "Insert mediaItemId ${mediaItem.mediaId}")
            mediaItem
        } ?: emptyList()

        val mediaQueueItems: List<MediaQueueItem> = mediaItems.map { item ->
            val metadata = com.google.android.gms.cast.MediaMetadata()
            metadata.putString(com.google.android.gms.cast.MediaMetadata.KEY_TITLE, item.mediaMetadata.title.toString())
            metadata.addImage(WebImage(item.mediaMetadata.artworkUri!!))

            val mediaInfo = MediaInfo.Builder()
                .setContentUrl(item.localConfiguration!!.uri.toString())
                .setMetadata(metadata)
                .build()
            val mediaQueueItem = MediaQueueItem.Builder(mediaInfo)
                .setItemId(item.mediaId.toInt())
                .build()
            Log.d("ReceiverCallback", "Insert mediaQueueItemId ${mediaQueueItem.itemId}")
            mediaQueueItem
        }

        val index = mediaManager.mediaQueueManager.mediaQueueData?.items?.indexOfFirst { it.itemId == request.insertBefore }
        index?.let { index ->
            if (index != C.INDEX_UNSET) {
                mediaManager.mediaQueueManager.queueItems?.addAll(index, mediaQueueItems)
                mediaManager.mediaQueueManager.mediaQueueData = MediaQueueData.Builder().setItems(mediaManager.mediaQueueManager.queueItems).build()
                mediaManager.mediaQueueManager.notifyItemsInserted(mediaQueueItems.map { it.itemId }, request.insertBefore)
                mediaManager.broadcastMediaStatus()
                player.addMediaItems(index, mediaItems)
            }
            else {
                mediaManager.mediaQueueManager.queueItems?.addAll(mediaQueueItems)
                mediaManager.mediaQueueManager.mediaQueueData = MediaQueueData.Builder().setItems(mediaManager.mediaQueueManager.queueItems).build()
                mediaManager.mediaQueueManager.notifyItemsInserted(mediaQueueItems.map { it.itemId }, request.insertBefore)
                mediaManager.broadcastMediaStatus()
                player.addMediaItems(mediaItems)
            }
        }
        return super.onQueueInsert(senderId, request)
    }

    override fun onQueueRemove(senderId: String?, request: QueueRemoveRequestData): Task<Void?> {
        val itemIdsToBeRemoved = request.itemIds

        mediaManager.mediaQueueManager.notifyItemsRemoved(itemIdsToBeRemoved)

        player.getCurrentMediaItems().forEachIndexed { index, mediaItem ->
            if (itemIdsToBeRemoved.contains(mediaItem.mediaId.toInt())) {
                player.removeMediaItem(index)
            }
        }

        mediaManager.broadcastMediaStatusWithUpdatedMediaInfo()

        Log.d("ReceiverCallback", "QueueRemoveRequestData: itemIdsToBeRemoved -> $itemIdsToBeRemoved")
        return super.onQueueRemove(senderId, request)
    }

    override fun onQueueReorder(senderId: String?, request: QueueReorderRequestData): Task<Void?> {
        Log.d("ReceiverCallback", "QueueReorderRequestData")
        return super.onQueueReorder(senderId, request)
    }
}
