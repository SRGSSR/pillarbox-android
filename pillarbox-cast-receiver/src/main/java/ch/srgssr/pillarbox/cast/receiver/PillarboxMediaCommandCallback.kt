/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
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
import ch.srgssr.pillarbox.cast.PillarboxCastUtil
import ch.srgssr.pillarbox.cast.receiver.extensions.setMediaTracksFromTracks
import ch.srgssr.pillarbox.cast.receiver.extensions.setPlaybackRateFromPlaybackParameter
import ch.srgssr.pillarbox.cast.receiver.extensions.setSupportedMediaCommandsFromAvailableCommand
import ch.srgssr.pillarbox.player.tracks.disableTextTrack
import ch.srgssr.pillarbox.player.tracks.selectTrack
import ch.srgssr.pillarbox.player.tracks.setAutoAudioTrack
import ch.srgssr.pillarbox.player.tracks.setAutoVideoTrack
import ch.srgssr.pillarbox.player.tracks.tracks
import com.google.android.gms.cast.MediaLiveSeekableRange
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaTrack
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
import kotlin.math.absoluteValue

/**
 * It is responsible to synchronize player items with [MediaQueueManager.getQueueItems] when senders send commands.
 * It provides also some utility methods to synchronize from [Player].
 */
internal class PillarboxMediaCommandCallback(
    private val player: Player,
    private val mediaManager: MediaManager,
    mediaItemConverter: MediaItemConverter,
) : MediaCommandCallback(), Player.Listener {

    private val mediaQueueManager = mediaManager.mediaQueueManager
    private val mediaStatusModifier = mediaManager.mediaStatusModifier

    private val mediaQueueSynchronizer = MediaQueueSynchronizer(player, mediaItemConverter, mediaQueueManager::autoGenerateItemId)

    fun notifySetMediaItems(mediaItems: List<MediaItem>, startIndex: Int) {
        mediaQueueManager.queueItems = mediaQueueSynchronizer.notifySetMediaItems(mediaItems)
        if (startIndex != C.INDEX_UNSET) {
            mediaQueueManager.currentItemId = mediaQueueSynchronizer[startIndex].itemId
        }
        mediaManager.broadcastMediaStatus()
    }

    fun addMediaItems(mediaItems: List<MediaItem>, index: Int) {
        if (mediaQueueManager.queueItems.isNullOrEmpty()) {
            val startIndex = 0
            player.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
            notifySetMediaItems(mediaItems, startIndex)
            return
        }
        val itemAdded = mediaQueueSynchronizer.addMediaItems(index, mediaItems)
        val indexAfterItem = index + 1
        val insertBefore = if (indexAfterItem >= mediaQueueSynchronizer.size) {
            null
        } else {
            mediaQueueSynchronizer[indexAfterItem].itemId
        }
        mediaQueueManager.notifyItemsInserted(itemAdded.map { it.itemId }, insertBefore)
        mediaManager.broadcastMediaStatus()
    }

    fun removeMediaItems(fromIndex: Int, toIndex: Int) {
        val removedItemIndex = mediaQueueSynchronizer.removeMediaItems(fromIndex, toIndex)
        if (removedItemIndex.isNotEmpty()) {
            mediaQueueManager.notifyItemsRemoved(removedItemIndex)
        }
        mediaManager.broadcastMediaStatus()
    }

    fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        mediaQueueSynchronizer.moveMediaItems(fromIndex, toIndex, newIndex)
        mediaQueueManager.notifyQueueFullUpdate()
        mediaManager.broadcastMediaStatus()
    }

    override fun onQueueInsert(senderId: String?, requestData: QueueInsertRequestData): Task<Void?> {
        Log.d(TAG, "onQueueInsert $senderId ${requestData.items.size} before ${requestData.insertBefore}")
        Log.d(TAG, "Items: ${requestData.items.map { "${it.media?.metadata?.getString(MediaMetadata.KEY_TITLE)}" }}")

        mediaQueueSynchronizer.queueInsert(requestData.items, requestData.insertBefore)
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
        if (requestData.itemIds.isEmpty()) {
            return task
        }
        mediaQueueSynchronizer.queueReorder(requestData.itemIds, requestData.insertBefore)
        mediaQueueManager.notifyQueueFullUpdate()
        mediaManager.broadcastMediaStatus()
        return task
    }

    override fun onQueueRemove(senderId: String?, requestData: QueueRemoveRequestData): Task<Void?> {
        Log.d(TAG, "onQueueRemove ${requestData.itemIds}")
        val removedItemIds = mediaQueueSynchronizer.removeQueueItems(requestData.itemIds)
        if (removedItemIds.isNotEmpty()) {
            mediaQueueManager.notifyItemsRemoved(removedItemIds)
        }
        mediaManager.broadcastMediaStatus()
        return Tasks.forResult<Void?>(null)
    }

    override fun onQueueUpdate(senderId: String?, requestData: QueueUpdateRequestData): Task<Void?> {
        Log.d(
            TAG,
            "onQueueUpdate items = ${requestData.items} ${requestData.currentItemId} -> ${requestData.jump} " +
                "${requestData.shuffle} ${requestData.repeatMode}"
        )
        requestData.shuffle?.let {
            if (mediaQueueSynchronizer.isEmpty()) return@let
            mediaQueueSynchronizer.shuffle()
            mediaQueueManager.notifyQueueFullUpdate()
            mediaManager.broadcastMediaStatus()
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
            val index = mediaQueueSynchronizer.getIndexOfItemIdOrNull(currentItemId)
            index?.let {
                player.seekTo(index, C.TIME_UNSET)
            }
        }

        // Do not call super method, jump is handled in it but not other features.
        return Tasks.forResult<Void?>(null)
    }

    override fun onSetPlaybackRate(senderId: String?, requestData: SetPlaybackRateRequestData): Task<Void?> {
        val relativePlaybackRate = requestData.relativePlaybackRate
        Log.d(TAG, "onSetPlaybackRate: rate = ${requestData.playbackRate} relative = $relativePlaybackRate")
        val newSpeed = if (relativePlaybackRate != null) {
            (player.playbackParameters.speed * relativePlaybackRate).toFloat()
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
        // Empty means automatic tracks or disable the track? When using ExoPlayer clicking in audio "auto" it will disable track
        if (mediaTracks.isEmpty()) {
            when (type) {
                MediaTrack.TYPE_AUDIO -> player.setAutoAudioTrack() // Auto when not specific track is selected
                MediaTrack.TYPE_TEXT -> player.disableTextTrack() // Disabled when no track selected.
                MediaTrack.TYPE_VIDEO -> player.setAutoVideoTrack() // Auto when not specific track is selected
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
                val liveSeekableRange = MediaLiveSeekableRange.Builder()
                    .setIsLiveDone(false)
                    .setIsMovingWindow(window.isDynamic)
                    .setStartTime(0)
                    .setEndTime(window.durationMs)
                    .build()
                mediaStatusModifier.liveSeekableRange = liveSeekableRange
                mediaStatusModifier.mediaInfoModifier?.streamDuration = window.durationMs
            } else {
                mediaStatusModifier.liveSeekableRange = null
                val duration = if (player.duration == C.TIME_UNSET) null else player.duration
                mediaStatusModifier.mediaInfoModifier?.streamDuration = duration
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
    }
}
