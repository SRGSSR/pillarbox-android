/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player

import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.core.business.cast.SRGMediaItemConverter
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession
import ch.srgssr.pillarbox.player.tracks.AudioTrack
import ch.srgssr.pillarbox.player.tracks.TextTrack
import ch.srgssr.pillarbox.player.tracks.VideoTrack
import ch.srgssr.pillarbox.player.tracks.selectTrack
import ch.srgssr.pillarbox.player.tracks.tracks
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.RequestData
import com.google.android.gms.cast.TextTrackStyle
import com.google.android.gms.cast.tv.CastReceiverContext
import com.google.android.gms.cast.tv.media.EditAudioTracksData
import com.google.android.gms.cast.tv.media.EditTracksInfoData
import com.google.android.gms.cast.tv.media.FetchItemsRequestData
import com.google.android.gms.cast.tv.media.MediaCommandCallback
import com.google.android.gms.cast.tv.media.MediaLoadCommandCallback
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.MediaResumeSessionRequestData
import com.google.android.gms.cast.tv.media.QueueInsertRequestData
import com.google.android.gms.cast.tv.media.QueueRemoveRequestData
import com.google.android.gms.cast.tv.media.QueueReorderRequestData
import com.google.android.gms.cast.tv.media.QueueUpdateRequestData
import com.google.android.gms.cast.tv.media.SeekRequestData
import com.google.android.gms.cast.tv.media.SetPlaybackRateRequestData
import com.google.android.gms.cast.tv.media.StoreSessionRequestData
import com.google.android.gms.cast.tv.media.StoreSessionResponseData
import com.google.android.gms.cast.tv.media.UserActionRequestData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class PillarboxCastReceiver(private val mediaSession: PillarboxMediaSession) {
    private val castReceiverContext = CastReceiverContext.getInstance()
    private val mediaManager = CastReceiverContext.getInstance().mediaManager
    private val mediaCommandCallback = MediaCommandCallbackImpl()
    private val mediaLoadCommandCallback = MediaLoadCommandCallbackImpl()

    private val playerListener = PlayerComponent()

    private val itemConvert = SRGMediaItemConverter()

    init {
        val token = MediaSessionCompat.Token.fromToken(mediaSession.mediaSession.platformToken)
        mediaManager.setSessionCompatToken(token)
        mediaManager.setMediaLoadCommandCallback(mediaLoadCommandCallback)
        mediaManager.setMediaCommandCallback(mediaCommandCallback)

        mediaManager.mediaQueueManager.setQueueStatusLimit(false)

        mediaSession.player.addListener(playerListener)
    }

    fun release() {
        mediaSession.player.removeListener(playerListener)
        mediaManager.setMediaCommandCallback(null)
        mediaManager.setMediaLoadCommandCallback(null)
        mediaManager.broadcastMediaStatus()
        mediaManager.setSessionCompatToken(null)
        mediaManager.mediaStatusModifier.clear()
    }

    fun onNewIntent(intent: Intent): Boolean {
        if (mediaManager.onNewIntent(intent)) {
            return true
        }
        mediaManager.mediaStatusModifier.clear()
        return false
    }

    fun MediaManager.getIndexOfMediaId(mediaId: Int): Int {
        return mediaManager.mediaQueueManager.queueItems.orEmpty().indexOfFirst { it.itemId == mediaId }
    }

    /**
     * The queue model in Cast is different from that in MediaSession. The Cast Connect library doesn't support reading a queue provided by MediaSession.
     */
    internal inner class MediaCommandCallbackImpl : MediaCommandCallback() {
        // This class has the default implementation to call methods of the MediaSession which MediaManager currently attaches to.
        override fun onQueueInsert(senderId: String?, requestData: QueueInsertRequestData): Task<Void?> {
            Log.d(TAG, "onQueueInsert $senderId")
            Log.d(TAG, " ${requestData.currentItemId} ${requestData.currentItemIndex} before ${requestData.insertBefore}")
            Log.d(TAG, " items: ${requestData.items.map { it.prettyString() }}")
            Log.d(TAG, "items : ${mediaManager.currentMediaStatus?.queueItems?.map { it.prettyString() }}")
            Log.d(TAG, "items queuemanager : ${mediaManager.mediaQueueManager.queueItems?.map { it.prettyString() }}")
            val list = requestData.items.map {
                itemConvert.toMediaItem(it)
            }
            mediaSession.player.addMediaItems(list)
            return super.onQueueInsert(senderId, requestData)
        }

        override fun onQueueRemove(senderId: String?, requestData: QueueRemoveRequestData): Task<Void?> {
            Log.d(TAG, "onQueueRemove")
            Log.d(TAG, " ${requestData.currentItemId} ${requestData.itemIds}")
            // MediaQueueManager as the same size as the Player.timeline
            requestData.itemIds.forEach { id ->
                val indexToRemove = mediaManager.getIndexOfMediaId(id)
                if (indexToRemove >= 0) {
                    mediaSession.player.removeMediaItem(indexToRemove)
                }
            }
            return super.onQueueRemove(senderId, requestData)
        }

        /*
         * [A,D,G,H,B,E] reorder at the end [D,H,B] => [A,G,E,D,H,B]
         */
        private fun addToTheEndOfTheQueue(queueItems: MutableList<MediaQueueItem>, itemIds: List<Int>) {
            itemIds.forEach { itemId ->
                val index = queueItems.indexOfFirst { it.itemId == itemId }
                if (index >= 0) {
                    queueItems.add(queueItems.removeAt(index))
                    mediaSession.player.moveMediaItem(index, mediaSession.player.mediaItemCount)
                }
            }
        }

        private fun reorderQueueItemsBeforeItemId(queueItems: MutableList<MediaQueueItem>, insertBeforeId: Int, itemIds: List<Int>) {
            Log.d(TAG, "queue : ${queueItems.map { it.itemId }} itemsId = $itemIds beforeId = $insertBeforeId")
            itemIds.forEach { itemId ->
                val index = queueItems.indexOfFirst { it.itemId == itemId }
                val insertBeforeIndex = queueItems.indexOfFirst { it.itemId == insertBeforeId }
                if (index >= 0 && insertBeforeIndex >= 0) {
                    val indexToMove = if (index > insertBeforeIndex) insertBeforeIndex else insertBeforeIndex - 1.coerceAtLeast(0)
                    mediaSession.player.moveMediaItem(index, indexToMove)
                }
            }
        }

        /**
         * https://developers.google.com/android/reference/com/google/android/gms/cast/tv/media/QueueReorderRequestData
         */
        override fun onQueueReorder(senderId: String?, requestData: QueueReorderRequestData): Task<Void?> {
            Log.d(TAG, "onQueueReorder")
            Log.d(TAG, " ${requestData.currentItemId} ${requestData.itemIds} before ${requestData.insertBefore}")
            if (mediaManager.mediaQueueManager.queueItems.isNullOrEmpty() || requestData.itemIds.isEmpty()) {
                return super.onQueueReorder(
                    senderId,
                    requestData
                )
            }
            mediaManager.mediaQueueManager.queueItems?.let { queueItems ->
                val insertBeforeId = requestData.insertBefore
                if (insertBeforeId == null) {
                    addToTheEndOfTheQueue(queueItems, requestData.itemIds)
                } else {
                    reorderQueueItemsBeforeItemId(queueItems, insertBeforeId, requestData.itemIds)
                }
            }
            return super.onQueueReorder(senderId, requestData)
        }

        override fun onQueueUpdate(senderId: String?, requestData: QueueUpdateRequestData): Task<Void?> {
            Log.d(
                TAG,
                "onQueueUpdate currentItemId = ${mediaManager.mediaQueueManager.currentItemId} -> ${requestData.currentItemId} jump = ${requestData.jump} ${mediaManager.mediaQueueManager.queueItems?.size} ${requestData.shuffle}"
            )
            requestData.items?.map { Log.d(TAG, "  ${it.prettyString()}") } ?: Log.d(TAG, "No items")
            var newItemId = MediaQueueItem.INVALID_ITEM_ID
            if (requestData.jump != null) {
                newItemId = requestData.jump!!
            } else if (requestData.currentItemId != null) {
                newItemId = requestData.currentItemId!!
            }
            if (newItemId != MediaQueueItem.INVALID_ITEM_ID) {
                mediaManager.mediaQueueManager.currentItemId = newItemId
                val index = mediaManager.mediaQueueManager.queueItems?.indexOfFirst { it.itemId == newItemId } ?: -1
                if (index > -1) {
                    mediaSession.player.seekTo(index, C.TIME_UNSET)
                    mediaManager.broadcastMediaStatus()
                }
            }
            return super.onQueueUpdate(senderId, requestData)
        }

        override fun onFetchItems(senderId: String?, requestData: FetchItemsRequestData): Task<Void?> {
            Log.d(TAG, "onFetchItems")
            return super.onFetchItems(senderId, requestData)
        }

        override fun onPause(
            senderId: String?,
            requestData: RequestData
        ): Task<Void?> {
            Log.d(TAG, "onPause: ")
            return super.onPause(senderId, requestData)
        }

        override fun onPlay(
            senderId: String?,
            requestData: RequestData
        ): Task<Void?> {
            Log.d(TAG, "onPlay: ")
            return super.onPlay(senderId, requestData)
        }

        override fun onPlayAgain(
            senderId: String?,
            requestData: RequestData
        ): Task<Void?> {
            Log.d(TAG, "onPlayAgain: ")
            return super.onPlayAgain(senderId, requestData)
        }

        override fun onSeek(
            senderId: String?,
            requestData: SeekRequestData
        ): Task<Void?> {
            Log.d(TAG, "onSeek: ")
            return super.onSeek(senderId, requestData)
        }

        override fun onSelectTracksByType(
            senderId: String?,
            type: Int,
            mediaTracks: List<MediaTrack>
        ): Task<Void?> {
            Log.d(TAG, "onSelectTracksByType: $type ${mediaTracks.map { it.id }}")
            // MediaTrack.id is the index in the list of tracks
            val tracks = mediaSession.player.currentTracks.tracks
            mediaTracks.forEach { mediaTrack ->
                if (mediaTrack.id.toInt() < tracks.size) {
                    val track = tracks[mediaTrack.id.toInt()]
                    mediaSession.player.selectTrack(track)
                }
            }
            return super.onSelectTracksByType(senderId, type, mediaTracks)
        }

        override fun onSetPlaybackRate(
            senderId: String?,
            requestData: SetPlaybackRateRequestData
        ): Task<Void?> {
            Log.d(TAG, "onSetPlaybackRate: ${requestData.playbackRate} ${requestData.relativePlaybackRate}")
            requestData.playbackRate?.let {
                mediaSession.player.setPlaybackSpeed(it.toFloat())
                mediaManager.mediaStatusModifier.playbackRate = it
                mediaManager.broadcastMediaStatus()
            }
            return super.onSetPlaybackRate(senderId, requestData)
        }

        override fun onSetTextTrackStyle(
            senderId: String?,
            p1: TextTrackStyle
        ): Task<Void?> {
            Log.d(TAG, "onSetTextTrackStyle: ")
            return super.onSetTextTrackStyle(senderId, p1)
        }

        override fun onSkipAd(
            senderId: String?,
            requestData: RequestData
        ): Task<Void?> {
            Log.d(TAG, "onSkipAd: ")
            return super.onSkipAd(senderId, requestData)
        }

        override fun onStop(
            senderId: String?,
            requestData: RequestData
        ): Task<Void?> {
            Log.d(TAG, "onStop: ")
            return super.onStop(senderId, requestData)
        }

        override fun onStoreSession(
            senderId: String?,
            requestData: StoreSessionRequestData
        ): Task<StoreSessionResponseData?> {
            Log.d(TAG, "onStoreSession: ")
            return super.onStoreSession(senderId, requestData)
        }

        override fun onUserAction(
            senderId: String?,
            userActionRequestData: UserActionRequestData
        ): Task<Void?> {
            Log.d(TAG, "onUserAction: ")
            return super.onUserAction(senderId, userActionRequestData)
        }
    }

    internal inner class MediaLoadCommandCallbackImpl : MediaLoadCommandCallback() {
        override fun onLoad(
            senderId: String?,
            loadRequest: MediaLoadRequestData
        ): Task<MediaLoadRequestData?> {
            val mediaInfo = loadRequest.mediaInfo
            val queueData = loadRequest.queueData
            Log.d(TAG, "onLoad $senderId ${loadRequest.mediaInfo?.contentUrl}")
            queueData?.let { queueData ->
                Log.d(TAG, "onLoad from QueueData ${queueData.items?.size} queueItems = ${mediaManager.mediaQueueManager.queueItems?.size}")
                val mediaItems = queueData.items.orEmpty().mapNotNull(itemConvert::toMediaItem)
                val currentIndex = queueData.startIndex
                val position = queueData.startTime
                mediaManager.mediaQueueManager.clear()
                mediaSession.player.clearMediaItems()
                mediaSession.player.setMediaItems(mediaItems, currentIndex, position)
            } ?: {
                mediaInfo?.let {
                    Log.d(TAG, "onLoad from MediaInfo")
                    mediaSession.player.setMediaItem(itemConvert.toMediaItem(MediaQueueItem.Builder(it).build()))
                }
            }

            mediaSession.player.prepare()
            mediaSession.player.play()
            mediaManager.mediaStatusModifier.clear()
            mediaManager.setDataFromLoad(loadRequest)
            mediaManager.broadcastMediaStatus()
            return Tasks.forResult(loadRequest)
        }

        override fun onResumeSession(
            senderId: String?,
            loadRequestData: MediaResumeSessionRequestData
        ): Task<MediaLoadRequestData?> {
            Log.d(TAG, "onResumeSession: $senderId")
            return super.onResumeSession(senderId, loadRequestData)
        }
    }

    private inner class PlayerComponent : Player.Listener {

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) return
            Log.d(TAG, "onTimelineChanged: ${timeline.windowCount}")
            val window = Timeline.Window()
            val listItems = mutableListOf<MediaQueueItem>()
            for (i in 0 until timeline.windowCount) {
                timeline.getWindow(i, window)
                val mediaItem = window.mediaItem
                val queueItem = itemConvert.toMediaQueueItem(mediaItem)
                queueItem.writer.setItemId(mediaManager.mediaQueueManager.autoGenerateItemId())
                listItems.add(queueItem)
            }

            mediaManager.mediaQueueManager.queueItems = listItems
            mediaManager.broadcastMediaStatus()
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            mediaManager.mediaStatusModifier.playbackRate = playbackParameters.speed.toDouble()
        }

        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            mediaManager.mediaStatusModifier.setMediaCommandSupported(
                MediaStatus.COMMAND_EDIT_TRACKS,
                availableCommands.contains(Player.COMMAND_GET_TRACKS) && availableCommands.contains(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)
            )
        }

        override fun onTracksChanged(tracks: Tracks) {
            val listTracks = mutableListOf<MediaTrack>()
            val listSelectedTracks = mutableListOf<Long>()
            tracks.tracks.forEachIndexed { index, track ->
                val type = when (track) {
                    is TextTrack -> MediaTrack.TYPE_TEXT
                    is AudioTrack -> MediaTrack.TYPE_AUDIO
                    is VideoTrack -> MediaTrack.TYPE_VIDEO
                }
                val mediaTrack = MediaTrack.Builder(index.toLong(), type)
                    .setLanguage(track.format.language)
                    .setContentType(track.format.sampleMimeType)
                    .setName(track.format.label)
                    .setContentId(track.format.id)
                    .build()
                listTracks.add(mediaTrack)
                if (track.isSelected) listSelectedTracks.add(mediaTrack.id)
            }
            mediaManager.mediaStatusModifier.mediaInfoModifier?.mediaTracks = listTracks
            mediaManager.mediaStatusModifier.mediaTracksModifier.setActiveTrackIds(listSelectedTracks.toLongArray())
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(Player.EVENT_MEDIA_ITEM_TRANSITION, Player.EVENT_TIMELINE_CHANGED)) {
                val currentItemIndex = player.currentMediaItemIndex
                if (currentItemIndex == C.INDEX_UNSET) return
                val queueItemCount = mediaManager.currentMediaStatus?.queueItems?.size ?: 0
                if (currentItemIndex < queueItemCount) {
                    mediaManager.mediaQueueManager.queueItems?.get(currentItemIndex)?.let {
                        mediaManager.mediaQueueManager.currentItemId = it.itemId
                    }
                }
            }
            mediaManager.broadcastMediaStatus()
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            Log.d(TAG, "Player.onMediaMetadataChanged: ${mediaMetadata.title}")
        }
    }

    companion object {
        private const val TAG = "PillarboxCastReceiver"

        fun MediaQueueItem.prettyString(): String {
            return "[$itemId]: contentId = ${media?.contentId} contentUrl = ${media?.contentUrl} ${
                this.media?.metadata?.getString(
                    com.google.android.gms.cast.MediaMetadata.KEY_TITLE
                )
            }"
        }
    }
}
