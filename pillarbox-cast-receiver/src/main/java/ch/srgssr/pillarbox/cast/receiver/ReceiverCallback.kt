/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import com.google.android.gms.cast.tv.media.MediaCommandCallback
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.QueueInsertRequestData
import com.google.android.gms.cast.tv.media.QueueRemoveRequestData
import com.google.android.gms.cast.tv.media.QueueReorderRequestData
import com.google.android.gms.cast.tv.media.QueueUpdateRequestData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class ReceiverCallback(val player: PillarboxPlayer, val mediaManager: MediaManager): MediaCommandCallback() {
    override fun onQueueUpdate(senderId: String?, request: QueueUpdateRequestData): Task<Void?> {
        Log.d("ReceiverCallback", "onQueueUpdate")
        return super.onQueueUpdate(senderId, request)
    }
    override fun onQueueInsert(senderId: String?, request: QueueInsertRequestData): Task<Void?> {
        Log.d("ReceiverCallback", "QueueInsertRequestData")
        return super.onQueueInsert(senderId, request)
    }

    override fun onQueueRemove(senderId: String?, request: QueueRemoveRequestData): Task<Void?> {
        val itemIdsToBeRemoved = request.itemIds
        val currentId = mediaManager.mediaQueueManager.currentItemId

        mediaManager.mediaQueueManager.notifyItemsRemoved(itemIdsToBeRemoved)

        player.getCurrentMediaItems().forEachIndexed { index, mediaItem ->
            if (itemIdsToBeRemoved.contains(mediaItem.mediaId.toInt())) {
                player.removeMediaItem(index)
            }
        }

        Log.d("ReceiverCallback", "QueueRemoveRequestData: itemIdsToBeRemoved -> $itemIdsToBeRemoved")
        return super.onQueueRemove(senderId, request)
    }

    override fun onQueueReorder(senderId: String?, request: QueueReorderRequestData): Task<Void?> {
        Log.d("ReceiverCallback", "QueueReorderRequestData")
        return super.onQueueReorder(senderId, request)
    }
}
