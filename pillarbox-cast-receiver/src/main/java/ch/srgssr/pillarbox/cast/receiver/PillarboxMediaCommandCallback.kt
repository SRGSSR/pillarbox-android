/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
import android.view.Window
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.Player.EVENT_AVAILABLE_COMMANDS_CHANGED
import androidx.media3.common.Player.EVENT_MEDIA_ITEM_TRANSITION
import androidx.media3.common.Player.EVENT_PLAYBACK_PARAMETERS_CHANGED
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.util.Util
import ch.srgssr.pillarbox.cast.PillarboxCastUtil
import ch.srgssr.pillarbox.cast.receiver.extensions.setMediaTracksFromTracks
import ch.srgssr.pillarbox.cast.receiver.extensions.setPlaybackRateFromPlaybackParameter
import ch.srgssr.pillarbox.cast.receiver.extensions.setSupportedMediaCommandsFromAvailableCommand
import ch.srgssr.pillarbox.player.tracks.selectTrack
import ch.srgssr.pillarbox.player.tracks.tracks
import com.google.android.gms.cast.MediaLiveSeekableRange
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.tv.media.MediaCommandCallback
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.MediaQueueItemWriter
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
 * It is responsible to synchronize player items with [MediaQueueManager.getQueueItems] when senders send commands.
 * It provides also some utility methods to synchronize from [Player].
 */
internal class PillarboxMediaCommandCallback(
    private val player: Player,
    private val mediaManager: MediaManager,
    private val mediaItemConverter: MediaItemConverter,
) : MediaCommandCallback(), Player.Listener {

    private val mediaQueueManager = mediaManager.mediaQueueManager
    private val mediaStatusModifier = mediaManager.mediaStatusModifier

    fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int) {
        mediaQueueManager.queueItems = mediaItems.map { item ->
            val queueItem = mediaItemConverter.toMediaQueueItem(item)
            MediaQueueItemWriter(queueItem)
                .setItemId(mediaQueueManager.autoGenerateItemId())
            queueItem
        }.also {
            if (startIndex != C.INDEX_UNSET) {
                mediaQueueManager.currentItemId = it[startIndex].itemId
            }
        }
        mediaManager.broadcastMediaStatus()
    }

    fun addMediaItems(mediaItems: List<MediaItem>, index: Int) {
        val itemsToAdd = mediaItems.map(mediaItemConverter::toMediaQueueItem)
        val indexAfterItem = index + 1
        val insertBefore = if (indexAfterItem >= checkNotNull(mediaQueueManager.queueItems).size) {
            null
        } else {
            checkNotNull(mediaQueueManager.queueItems)[indexAfterItem].itemId
        }
        insert(itemsToAdd, index)
        mediaQueueManager.notifyItemsInserted(itemsToAdd.map { it.itemId }, insertBefore)
    }

    fun insert(items: List<MediaQueueItem>, insertIndex: Int) {
        items.forEach { queueItem ->
            queueItem.writer.apply {
                setItemId(mediaQueueManager.autoGenerateItemId())
            }
        }
        mediaQueueManager.queueItems?.let {
            if (insertIndex >= it.size) {
                it.addAll(items)
            } else {
                it.addAll(insertIndex, items)
            }
        }
    }

    fun remove(itemIds: List<Int>): List<Int> {
        val itemsToRemove = checkNotNull(mediaQueueManager.queueItems).filter { item -> itemIds.contains(item.itemId) }
        checkNotNull(mediaQueueManager.queueItems).removeAll(itemsToRemove)
        return itemsToRemove.map { it.itemId }
    }

    fun moveItem(fromIndex: Int, toIndex: Int, newFromIndex: Int) {
        Util.moveItems(checkNotNull(mediaQueueManager.queueItems), fromIndex, toIndex, newFromIndex)
    }

    override fun onQueueInsert(senderId: String?, requestData: QueueInsertRequestData): Task<Void?> {
        Log.d(TAG, "onQueueInsert $senderId ${requestData.items.size} before ${requestData.insertBefore}")
        Log.d(TAG, "Items: ${requestData.items.map { "${it.media?.metadata?.getString(MediaMetadata.KEY_TITLE)}" }}")
        mediaQueueManager.queueItems?.let { queueItems ->
            val insertIndex = requestData.insertBefore?.let {
                mediaQueueManager.getIndexOfItemIdOrNull(it)
            } ?: Int.MAX_VALUE

            insert(requestData.items, insertIndex)

            val mediaItems = requestData.items.map(mediaItemConverter::toMediaItem)
            player.addMediaItems(insertIndex, mediaItems)
        }
        mediaQueueManager.notifyItemsInserted(requestData.items.map { item -> item.itemId }, requestData.insertBefore)
        mediaManager.broadcastMediaStatus()
        return Tasks.forResult<Void?>(null)
    }

    /**
     * https://developers.google.com/android/reference/com/google/android/gms/cast/tv/media/QueueReorderRequestData
     */
    override fun onQueueReorder(senderId: String?, requestData: QueueReorderRequestData): Task<Void?> {
        Log.d(TAG, "onQueueReorder ${requestData.currentItemId} ${requestData.itemIds} before ${requestData.insertBefore}")
        val task = Tasks.forResult<Void?>(null)
        val queueItems = mediaQueueManager.queueItems
        if (queueItems.isNullOrEmpty() || requestData.itemIds.isEmpty()) {
            return task
        }
        reorderItems(queueItems = queueItems, requestData.itemIds, requestData.insertBefore)
        mediaManager.broadcastMediaStatus()
        return task
    }

    override fun onQueueRemove(senderId: String?, requestData: QueueRemoveRequestData): Task<Void?> {
        Log.d(TAG, "onQueueRemove ${requestData.itemIds}")
        mediaQueueManager.queueItems?.let { queueItems ->
            val removeMediaId = mutableListOf<Int>()
            requestData.itemIds.forEach { itemId ->
                mediaQueueManager.getIndexOfItemIdOrNull(itemId)?.let {
                    queueItems.removeAt(it)
                    player.removeMediaItem(it)
                    removeMediaId.add(itemId)
                }
            }
            if (removeMediaId.isNotEmpty()) {
                Log.d(TAG, "onRemovedItems $removeMediaId")
                mediaQueueManager.notifyItemsRemoved(removeMediaId)
            }
        }
        mediaManager.broadcastMediaStatus()
        return Tasks.forResult<Void?>(null)
    }

    @Suppress("MaximumLineLength")
    override fun onQueueUpdate(senderId: String?, requestData: QueueUpdateRequestData): Task<Void?> {
        Log.d(
            TAG,
            "onQueueUpdate items = ${requestData.items} ${requestData.currentItemId} -> ${requestData.jump} " +
                "${requestData.shuffle} ${requestData.repeatMode}"
        )
        requestData.shuffle?.let {
            if (mediaQueueManager.queueItems.isNullOrEmpty()) return Tasks.forResult<Void?>(null)
            mediaQueueManager.queueItems.takeUnless { it.isNullOrEmpty() }?.let { queueItems ->
                val queueItemIds = queueItems.map { item -> item.itemId }
                Collections.shuffle(queueItemIds)
                reorderItems(queueItems, queueItemIds)
                mediaManager.broadcastMediaStatus()
            }
        }
        requestData.repeatMode?.let {
            player.repeatMode = PillarboxCastUtil.getRepeatModeFromQueueRepeatMode(it)
        }
        requestData.jump.takeIf { it != 0 }?.let {
            repeat(it.absoluteValue) { i ->
                if (it < 0 && player.isCommandAvailable(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)) {
                    player.seekToPreviousMediaItem()
                }
                if (it > 0 && player.isCommandAvailable(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)) {
                    player.seekToNextMediaItem()
                }
            }
        }

        requestData.currentItemId?.let { currentItemId ->
            val index = mediaManager.mediaQueueManager.queueItems?.indexOfFirst { it.itemId == currentItemId } ?: -1
            if (index > -1) {
                player.seekTo(index, C.TIME_UNSET)
            }
        }

        // Do not call super method, jump is handled in it but not other features.
        return Tasks.forResult<Void?>(null)
    }

    private fun reorderItems(queueItems: MutableList<MediaQueueItem>, itemIds: List<Int>, insertBeforeId: Int? = null) {
        val insertBeforeId = insertBeforeId
        if (insertBeforeId == null) {
            moveAtTheEndOfTheQueue(queueItems, itemIds)
        } else {
            reorderQueueItemsBeforeItemId(queueItems, insertBeforeId, itemIds)
        }
    }

    /*
     * [A,D,G,H,B,E] reorder at the end [D,H,B] => [A,G,E,D,H,B]
     */
    private fun moveAtTheEndOfTheQueue(queueItems: MutableList<MediaQueueItem>, itemIds: List<Int>) {
        itemIds.forEach { itemId ->
            val index = queueItems.indexOfFirst { it.itemId == itemId }
            if (index >= 0) {
                moveItem(index, index + 1, player.mediaItemCount)
                player.moveMediaItem(index, player.mediaItemCount)
            }
        }
        mediaQueueManager.notifyQueueFullUpdate()
    }

    @Suppress("NestedBlockDepth")
    private fun reorderQueueItemsBeforeItemId(queueItems: MutableList<MediaQueueItem>, insertBeforeId: Int, itemIds: List<Int>) {
        Log.d(TAG, "queue : ${queueItems.map { it.itemId }} itemsId = $itemIds beforeId = $insertBeforeId")
        mediaQueueManager.queueItems?.let {
            itemIds.forEach { itemId ->
                val index = queueItems.indexOfFirst { it.itemId == itemId }
                val insertBeforeIndex = queueItems.indexOfFirst { it.itemId == insertBeforeId }
                if (index >= 0 && insertBeforeIndex >= 0) {
                    val indexToMove = if (index > insertBeforeIndex) insertBeforeIndex else (insertBeforeIndex - 1).coerceAtLeast(0)
                    moveItem(index, index + 1, indexToMove)
                    player.moveMediaItem(index, indexToMove)
                }
            }
            mediaQueueManager.notifyQueueFullUpdate()
        }
    }

    override fun onSetPlaybackRate(senderId: String?, requestData: SetPlaybackRateRequestData): Task<Void?> {
        Log.d(TAG, "onSetPlaybackRate: rate = ${requestData.playbackRate} relative = ${requestData.relativePlaybackRate}")
        val newSpeed = if (requestData.relativePlaybackRate != null) {
            (player.playbackParameters.speed * checkNotNull(requestData.relativePlaybackRate)).toFloat()
        } else {
            requestData.playbackRate?.toFloat()?.takeIf { it > 0f }
        }

        newSpeed?.let {
            player.setPlaybackSpeed(it)
        }
        return Tasks.forResult<Void?>(null)
    }

    override fun onSelectTracksByType(
        senderId: String?,
        type: Int,
        mediaTracks: List<MediaTrack>
    ): Task<Void?> {
        Log.d(TAG, "onSelectTracksByType: type = $type tracks = ${mediaTracks.map { it.id }}")
        // MediaTrack.id is the index in the list of tracks
        val tracks = player.currentTracks.tracks
        mediaTracks.forEach { mediaTrack ->
            val trackIndex = mediaTrack.id.toInt()
            if (trackIndex >= 0 && trackIndex < tracks.size) {
                val track = tracks[trackIndex]
                player.selectTrack(track)
            }
        }
        return Tasks.forResult<Void?>(null)
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        mediaQueueManager.queueRepeatMode = PillarboxCastUtil.getQueueRepeatModeFromRepeatMode(repeatMode)
        mediaManager.broadcastMediaStatus()
    }

    override fun onTracksChanged(tracks: Tracks) {
        mediaStatusModifier.setMediaTracksFromTracks(tracks)
        mediaManager.broadcastMediaStatus()
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.containsAny(
                EVENT_PLAYBACK_PARAMETERS_CHANGED,
                EVENT_MEDIA_ITEM_TRANSITION,
                EVENT_AVAILABLE_COMMANDS_CHANGED,
                Player.EVENT_TIMELINE_CHANGED,
            )
        ) {
            mediaStatusModifier.setSupportedMediaCommandsFromAvailableCommand(player.availableCommands)
            mediaStatusModifier.setPlaybackRateFromPlaybackParameter(player.playbackParameters)

            if (player.isCurrentMediaItemLive) {
                val window = player.currentTimeline.getWindow(player.currentMediaItemIndex, Timeline.Window())
                if (window.windowStartTimeMs != C.TIME_UNSET && window.isSeekable) {
                    val liveSeekableRange = MediaLiveSeekableRange.Builder()
                        .setIsLiveDone(false)
                        .setIsMovingWindow(window.isDynamic)
                        .setStartTime(0)
                        .setEndTime(window.durationMs)
                        .build()
                    mediaStatusModifier.liveSeekableRange = liveSeekableRange
                    mediaStatusModifier.mediaInfoModifier?.streamDuration = window.durationMs
                }
            } else {
                mediaStatusModifier.liveSeekableRange = null
            }

            if (player.currentMediaItemIndex != C.INDEX_UNSET && player.mediaItemCount > 0) {
                val currentId = mediaQueueManager.queueItems?.get(player.currentMediaItemIndex)?.itemId
                if (currentId != mediaQueueManager.currentItemId) {
                    mediaQueueManager.currentItemId = currentId
                }
            }

            mediaManager.broadcastMediaStatus()
        }
    }

    private companion object {
        private const val TAG = "PillarboxCastReceiver"

        fun MediaQueueManager.getIndexOfItemIdOrNull(itemId: Int) = queueItems?.indexOfFirst { item -> item.itemId == itemId }?.takeIf { it >= 0 }
    }
}
