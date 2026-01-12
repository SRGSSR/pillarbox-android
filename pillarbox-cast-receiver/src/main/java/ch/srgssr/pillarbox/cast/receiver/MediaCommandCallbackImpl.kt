/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import ch.srgssr.pillarbox.cast.PillarboxCastUtil
import ch.srgssr.pillarbox.cast.TracksConverter
import ch.srgssr.pillarbox.cast.receiver.extensions.contains
import ch.srgssr.pillarbox.cast.receiver.extensions.getItemIndex
import ch.srgssr.pillarbox.cast.receiver.extensions.getItemIndexOrNull
import ch.srgssr.pillarbox.cast.receiver.extensions.insert
import ch.srgssr.pillarbox.cast.receiver.extensions.move
import ch.srgssr.pillarbox.cast.receiver.extensions.queueSize
import ch.srgssr.pillarbox.cast.receiver.extensions.remove
import ch.srgssr.pillarbox.player.extension.setTrackOverride
import ch.srgssr.pillarbox.player.tracks.disableTextTrack
import ch.srgssr.pillarbox.player.tracks.setAutoAudioTrack
import ch.srgssr.pillarbox.player.tracks.setAutoVideoTrack
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.tv.CastReceiverContext
import com.google.android.gms.cast.tv.media.FetchItemsRequestData
import com.google.android.gms.cast.tv.media.MediaCommandCallback
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.MediaQueueManager
import com.google.android.gms.cast.tv.media.QueueInsertRequestData
import com.google.android.gms.cast.tv.media.QueueRemoveRequestData
import com.google.android.gms.cast.tv.media.QueueReorderRequestData
import com.google.android.gms.cast.tv.media.QueueUpdateRequestData
import com.google.android.gms.cast.tv.media.SetPlaybackRateRequestData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.util.Collections
import kotlin.collections.forEach
import kotlin.math.absoluteValue

/**
 * Callback for Cast media commands.
 * This class has the default implementation to call methods of the MediaSession which MediaManager currently attaches to.
 * https://developers.google.com/android/reference/com/google/android/gms/cast/tv/media/MediaCommandCallback
 */
internal class MediaCommandCallbackImpl(
    private val player: Player,
    private val mediaItemConverter: MediaItemConverter,
    private val tracksConverter: TracksConverter,
    private val mediaManager: MediaManager = CastReceiverContext.getInstance().mediaManager,
    private val mediaQueueManager: MediaQueueManager = mediaManager.mediaQueueManager,
) : MediaCommandCallback() {

    override fun onQueueInsert(senderId: String?, requestData: QueueInsertRequestData): Task<Void?> {
        with(requestData) {
            Log.d(
                TAG,
                "onQueueInsert insertBefore = $insertBefore items = $items currentItemId = $currentItemId currentItemIndex = $currentItemIndex"
            )
        }
        val insertIndex = requestData.insertBefore?.let {
            mediaQueueManager.getItemIndexOrNull(it)
        } ?: Int.MAX_VALUE
        mediaQueueManager.insert(requestData.items, insertIndex)
        player.addMediaItems(insertIndex, requestData.items.map { mediaItemConverter.toMediaItem(it) })
        mediaQueueManager.notifyItemsInserted(requestData.items.map { item -> item.itemId }, requestData.insertBefore)
        mediaManager.broadcastMediaStatus()
        return voidTask()
    }

    override fun onQueueRemove(senderId: String?, requestData: QueueRemoveRequestData): Task<Void?> {
        with(requestData) {
            Log.d(TAG, "onQueueRemove  itemIds = $itemIds currentItemId = $currentItemId currentTime = $currentTime")
        }
        val removedItemIds = mutableListOf<Int>()
        check(mediaQueueManager.queueSize == player.mediaItemCount)
        requestData.itemIds.forEach { itemId ->
            mediaQueueManager.getItemIndexOrNull(itemId)?.let { index ->
                mediaQueueManager.remove(index)
                player.removeMediaItem(index)
                removedItemIds.add(itemId)
            }
        }
        mediaQueueManager.notifyItemsRemoved(removedItemIds)
        mediaManager.broadcastMediaStatus()
        return voidTask()
    }

    /**
     * Call when sender queueReorder
     * https://developers.google.com/android/reference/com/google/android/gms/cast/framework/media/RemoteMediaClient#queueReorderItems(int[],%20int,%20org.json.JSONObject)
     */
    override fun onQueueReorder(senderId: String?, requestData: QueueReorderRequestData): Task<Void?> {
        with(requestData) {
            Log.d(TAG, "onQueueReorder itemIds = $itemIds insertBefore = $insertBefore")
        }
        check(mediaQueueManager.queueSize == player.mediaItemCount)
        val insertBeforeId = requestData.insertBefore ?: MediaQueueItem.INVALID_ITEM_ID
        if (insertBeforeId != MediaQueueItem.INVALID_ITEM_ID && mediaQueueManager.contains(insertBeforeId)) {
            reorderQueueItemsBeforeItemId(insertBeforeId, requestData.itemIds)
        } else {
            moveAtTheEndOfTheQueue(requestData.itemIds)
        }
        mediaQueueManager.notifyQueueFullUpdate()
        mediaManager.broadcastMediaStatus()
        return super.onQueueReorder(senderId, requestData)
    }

    /*
     * [A,D,G,H,B,E] reorder at the end [D,H,B] => [A,G,E,D,H,B]
     */
    private fun moveAtTheEndOfTheQueue(itemIds: List<Int>) {
        itemIds.forEach { itemId ->
            mediaQueueManager.getItemIndexOrNull(itemId)?.let { index ->
                mediaQueueManager.move(index, index + 1, player.mediaItemCount)
                player.moveMediaItem(index, player.mediaItemCount)
            }
        }
    }

    private fun reorderQueueItemsBeforeItemId(insertBeforeId: Int, itemIds: List<Int>) {
        itemIds.forEach { itemId ->
            val index = mediaQueueManager.getItemIndex(itemId)
            val insertBeforeIndex = mediaQueueManager.getItemIndex(insertBeforeId)
            if (index >= 0 && insertBeforeIndex >= 0) {
                val indexToMove = if (index > insertBeforeIndex) insertBeforeIndex else (insertBeforeIndex - 1).coerceAtLeast(0)
                mediaQueueManager.move(index, index + 1, indexToMove)
                player.moveMediaItem(index, indexToMove)
            }
        }
    }

    override fun onQueueUpdate(senderId: String?, requestData: QueueUpdateRequestData): Task<Void?> {
        with(requestData) {
            Log.d(
                TAG,
                "onQueueUpdate items = $items jump from $currentItemId to $jump " +
                    "shuffle = $shuffle repeatMode = $repeatMode"
            )
        }
        requestData.shuffle?.let {
            shuffle()
        }
        requestData.repeatMode?.let {
            player.repeatMode = PillarboxCastUtil.getRepeatModeFromQueueRepeatMode(it)
        }
        requestData.jump.takeIf { it != 0 }?.let { jump ->
            repeat(jump.absoluteValue) {
                if (jump < 0 && player.isCommandAvailable(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)) {
                    player.seekToPreviousMediaItem()
                }
                if (jump > 0 && player.isCommandAvailable(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)) {
                    player.seekToNextMediaItem()
                }
            }
        }

        requestData.currentItemId?.let { currentItemId ->
            mediaQueueManager.getItemIndexOrNull(currentItemId)?.let { i ->
                player.seekTo(i, C.TIME_UNSET)
            }
        }

        // Do not call super method, jump is handled in it but not other features.
        return voidTask()
    }

    private fun shuffle() {
        if (mediaQueueManager.queueItems.isNullOrEmpty()) return
        val queueItems = checkNotNull(mediaQueueManager.queueItems)
        val queueItemIds = queueItems.map { item -> item.itemId }
        Collections.shuffle(queueItemIds)
        moveAtTheEndOfTheQueue(queueItemIds)
        mediaQueueManager.notifyQueueFullUpdate()
        mediaManager.broadcastMediaStatus()
    }

    override fun onFetchItems(p0: String?, requestData: FetchItemsRequestData): Task<Void?> {
        with(requestData) {
            Log.d(TAG, "onFetchItems fetch $itemId nextCount = $nextCount previousCount = $prevCount")
        }
        return super.onFetchItems(p0, requestData)
    }

    override fun onSelectTracksByType(
        senderId: String?,
        type: Int,
        mediaTracks: List<MediaTrack>
    ): Task<Void?> {
        Log.d(TAG, "onSelectTracksByType: type = $type tracks = ${mediaTracks.map { it.id }}")
        val tracksInfo = tracksConverter.toCastTracksInfo(player.currentTracks)
        mediaTracks.forEach { mediaTrack ->
            val trackIndex = tracksInfo.mediaTracks.indexOfFirst { mediaTrack.id == it.id }
            if (trackIndex >= 0) {
                val trackOverride = tracksInfo.trackSelectionOverrides[trackIndex]
                player.setTrackOverride(trackOverride)
            }
        }
        // Empty means automatic tracks or disable the track? When using ExoPlayer clicking in audio "auto" it will disable track
        if (mediaTracks.isEmpty()) {
            when (type) {
                MediaTrack.TYPE_AUDIO -> player.setAutoAudioTrack() // Auto when not specific track is selected
                MediaTrack.TYPE_TEXT -> player.disableTextTrack() // Disabled when no track selected.
                MediaTrack.TYPE_VIDEO -> player.setAutoVideoTrack() // Auto when not specific track is selected
            }
        }
        return voidTask()
    }

    override fun onSetPlaybackRate(senderId: String?, requestData: SetPlaybackRateRequestData): Task<Void?> {
        with(requestData) {
            Log.d(TAG, "onSetPlaybackRate: rate = $playbackRate relative = $relativePlaybackRate")
        }
        val relativePlaybackRate = requestData.relativePlaybackRate
        val newSpeed = if (relativePlaybackRate != null) {
            (player.playbackParameters.speed * relativePlaybackRate).toFloat()
        } else {
            requestData.playbackRate?.toFloat()?.takeIf { it > 0f }
        }

        newSpeed?.let {
            player.setPlaybackSpeed(it)
        }
        return voidTask()
    }

    companion object {
        private const val TAG = "MediaCommandCallback"
        private fun voidTask() = Tasks.forResult<Void?>(null)
    }
}
