/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.C
import androidx.media3.common.ForwardingSimpleBasePlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import ch.srgssr.pillarbox.cast.receiver.extensions.setPlaybackRateFromPlaybackParameter
import ch.srgssr.pillarbox.cast.receiver.extensions.setSupportedMediaCommandsFromAvailableCommand
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.tv.CastReceiverContext
import com.google.android.gms.cast.tv.SenderDisconnectedEventInfo
import com.google.android.gms.cast.tv.SenderInfo
import com.google.android.gms.cast.tv.media.MediaCommandCallback
import com.google.android.gms.cast.tv.media.MediaLoadCommandCallback
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.MediaQueueItemWriter
import com.google.android.gms.cast.tv.media.MediaQueueManager
import com.google.android.gms.cast.tv.media.MediaResumeSessionRequestData
import com.google.android.gms.cast.tv.media.MediaStatusModifier
import com.google.android.gms.cast.tv.media.QueueInsertRequestData
import com.google.android.gms.cast.tv.media.QueueRemoveRequestData
import com.google.android.gms.cast.tv.media.QueueReorderRequestData
import com.google.android.gms.cast.tv.media.QueueUpdateRequestData
import com.google.android.gms.cast.tv.media.SetPlaybackRateRequestData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.common.util.concurrent.ListenableFuture
import kotlin.math.absoluteValue

/**
 * PoC
 * Player implementation that handles playlist operation, currently they are not handle from [androidx.media3.session.MediaSession].
 */
class PillarboxCastReceiverPlayer(
    val player: PillarboxExoPlayer,
    private val mediaItemConverter: MediaItemConverter,
    private val castReceiver: CastReceiverContext,
) : ForwardingSimpleBasePlayer(player), PillarboxPlayer {

    private val eventCallback = EventCallback()
    private val mediaCommands = MediaCommands()
    private val mediaLoadCommands = MediaLoadCommands()
    private val mediaManager: MediaManager = castReceiver.mediaManager
    private val mediaQueueManager: MediaQueueManager = mediaManager.mediaQueueManager

    private val mediaStatusModifier: MediaStatusModifier = mediaManager.mediaStatusModifier

    override var smoothSeekingEnabled: Boolean
        get() = player.smoothSeekingEnabled
        set(value) {
            player.smoothSeekingEnabled = value
        }
    override var trackingEnabled: Boolean
        get() = player.trackingEnabled
        set(value) {
            player.trackingEnabled = value
        }

    init {
        castReceiver.registerEventCallback(eventCallback)
        mediaManager.setMediaLoadCommandCallback(mediaLoadCommands)
        mediaManager.setMediaCommandCallback(mediaCommands)
        mediaQueueManager.setQueueStatusLimit(false)
        addListener(PlayerComponent())
    }

    fun setupWithMediaSession(mediaSession: PillarboxMediaSession) {
        val token = MediaSessionCompat.Token.fromToken(mediaSession.mediaSession.platformToken)
        mediaManager.setSessionCompatToken(token)
    }

    fun onNewIntent(intent: Intent): Boolean {
        if (mediaManager.onNewIntent(intent)) {
            return true
        }
        mediaStatusModifier.clear()
        return false
    }

    override fun handleSetMediaItems(mediaItems: List<MediaItem>, startIndex: Int, startPositionMs: Long): ListenableFuture<*> {
        Log.d(TAG, "handleSetMediaItems startIndex = $startIndex startPositionMs = $startPositionMs")
        mediaQueueManager.queueItems = mediaItems.map { item ->
            val queueItem = mediaItemConverter.toMediaQueueItem(item)
            MediaQueueItemWriter(queueItem)
                .setItemId(mediaQueueManager.autoGenerateItemId())
            queueItem
        }
        mediaManager.broadcastMediaStatus()
        return super.handleSetMediaItems(mediaItems, startIndex, startPositionMs).apply { debugQueueItems() }
    }

    override fun handleMoveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int): ListenableFuture<*> {
        Log.d(TAG, "handleMoveMediaItems fromIndex = $fromIndex toIndex = $toIndex newIndex = $newIndex")
        mediaQueueManager.queueItems?.let {
            Util.moveItems(it, fromIndex, toIndex, newIndex)
            mediaQueueManager.notifyQueueFullUpdate()
        }
        mediaManager.broadcastMediaStatus()
        return super.handleMoveMediaItems(fromIndex, toIndex, newIndex).apply { debugQueueItems() }
    }

    override fun handleAddMediaItems(index: Int, mediaItems: List<MediaItem>): ListenableFuture<*> {
        Log.d(TAG, "handleAddMediaItems index = $index #items = ${mediaItems.size}")
        if (mediaQueueManager.queueItems == null) return handleSetMediaItems(mediaItems, C.INDEX_UNSET, C.TIME_UNSET)
        mediaQueueManager.queueItems?.let { queueItems ->
            val itemsToAdd = mediaItems.map {
                mediaItemConverter.toMediaQueueItem(it).apply { MediaQueueItemWriter(this).setItemId(mediaQueueManager.autoGenerateItemId()) }
            }
            queueItems.addAll(index, itemsToAdd)
            mediaQueueManager.notifyItemsInserted(itemsToAdd.map { it.itemId }, null)
        }

        mediaManager.broadcastMediaStatus()
        return super.handleAddMediaItems(index, mediaItems).apply { debugQueueItems() }
    }

    override fun handleRemoveMediaItems(fromIndex: Int, toIndex: Int): ListenableFuture<*> {
        Log.d(TAG, "handleRemoveMediaItems fromIndex = $fromIndex toIndex = $toIndex")
        debugQueueItems()
        check((mediaQueueManager.queueItems?.size ?: 0) == player.mediaItemCount) { "MediaQueue and MediaItems should be the same size" }
        mediaQueueManager.queueItems?.let { queueItems ->
            val itemIdRemoved = queueItems.subList(fromIndex, toIndex).map { it.itemId }
            Util.removeRange(queueItems, fromIndex, toIndex)

            mediaQueueManager.notifyItemsRemoved(itemIdRemoved)
        }
        return super.handleRemoveMediaItems(fromIndex, toIndex).apply { debugQueueItems() }
    }

    override fun handleReplaceMediaItems(fromIndex: Int, toIndex: Int, mediaItems: List<MediaItem>): ListenableFuture<*> {
        // TODO implement changes also to the MediaQueueManager if needed.
        return super.handleReplaceMediaItems(fromIndex, toIndex, mediaItems)
    }

    override fun handleRelease(): ListenableFuture<*> {
        mediaManager.setMediaCommandCallback(null)
        mediaManager.setMediaLoadCommandCallback(null)
        castReceiver.unregisterEventCallback(eventCallback)
        mediaManager.setSessionCompatToken(null)
        return super.handleRelease()
    }

    private fun debugQueueItems() {
        Log.d(TAG, "MediaItems: ${player.getCurrentMediaItems().map { it.mediaMetadata.title }}")
        Log.d(TAG, "QueueItems: ${mediaQueueManager.queueItems?.map { item -> item.media?.metadata?.getString(MediaMetadata.KEY_TITLE) }}")
    }

    private inner class MediaCommands : MediaCommandCallback() {
        override fun onQueueInsert(senderId: String?, requestData: QueueInsertRequestData): Task<Void?> {
            Log.d(TAG, "onQueueInsert $senderId ${requestData.items.size} before ${requestData.insertBefore}")
            Log.d(TAG, "Items: ${requestData.items.map { "${it.media?.metadata?.getString(MediaMetadata.KEY_TITLE)}" }}")
            val mediaItems = requestData.items.map {
                mediaItemConverter.toMediaItem(it)
            }
            val insertIndex = requestData.insertBefore?.let {
                mediaQueueManager.queueItems?.indexOfFirst { item -> item.itemId == it }
            } ?: Int.MAX_VALUE
            addMediaItems(insertIndex, mediaItems)
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
            val insertBeforeId = requestData.insertBefore
            if (insertBeforeId == null) {
                addToTheEndOfTheQueue(queueItems, requestData.itemIds)
            } else {
                reorderQueueItemsBeforeItemId(queueItems, insertBeforeId, requestData.itemIds)
            }

            return task
        }

        override fun onQueueRemove(senderId: String?, requestData: QueueRemoveRequestData): Task<Void?> {
            Log.d(TAG, "onQueueRemove ${requestData.itemIds}")
            requestData.itemIds.forEach { id ->
                val indexToRemove = mediaQueueManager.queueItems?.indexOfFirst { it.itemId == id } ?: -1
                if (indexToRemove >= 0) {
                    removeMediaItem(indexToRemove)
                }
            }
            return Tasks.forResult<Void?>(null)
        }

        override fun onQueueUpdate(senderId: String?, requestData: QueueUpdateRequestData): Task<Void?> {
            Log.d(
                TAG,
                "onQueueUpdate items = ${requestData.items} ${requestData.currentItemId} -> ${requestData.jump} ${requestData.shuffle} ${requestData.repeatMode}"
            )
            // TODO: handle shuffle and repeat mode here?
            requestData.jump.takeIf { it != 0 }?.let {
                repeat(it.absoluteValue) { i ->
                    if (it < 0 && isCommandAvailable(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)) {
                        seekToPreviousMediaItem()
                    }
                    if (it > 0 && isCommandAvailable(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)) {
                        seekToNextMediaItem()
                    }
                }
            }

            requestData.currentItemId?.let { currentItemId ->
                val index = mediaManager.mediaQueueManager.queueItems?.indexOfFirst { it.itemId == currentItemId } ?: -1
                if (index > -1) {
                    seekTo(index, C.TIME_UNSET)
                }
            }

            // Do not call super method, jump is handled in it but not other features.
            return Tasks.forResult<Void?>(null)
        }

        /*
         * [A,D,G,H,B,E] reorder at the end [D,H,B] => [A,G,E,D,H,B]
         */
        private fun addToTheEndOfTheQueue(queueItems: MutableList<MediaQueueItem>, itemIds: List<Int>) {
            itemIds.forEach { itemId ->
                val index = queueItems.indexOfFirst { it.itemId == itemId }
                if (index >= 0) {
                    moveMediaItem(index, mediaItemCount)
                }
            }
        }

        private fun reorderQueueItemsBeforeItemId(queueItems: MutableList<MediaQueueItem>, insertBeforeId: Int, itemIds: List<Int>) {
            Log.d(TAG, "queue : ${queueItems.map { it.itemId }} itemsId = $itemIds beforeId = $insertBeforeId")
            itemIds.forEach { itemId ->
                val index = queueItems.indexOfFirst { it.itemId == itemId }
                val insertBeforeIndex = queueItems.indexOfFirst { it.itemId == insertBeforeId }
                if (index >= 0 && insertBeforeIndex >= 0) {
                    val indexToMove = if (index > insertBeforeIndex) insertBeforeIndex else (insertBeforeIndex - 1).coerceAtLeast(0)
                    moveMediaItem(index, indexToMove)
                }
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
    }

    private inner class MediaLoadCommands : MediaLoadCommandCallback() {
        override fun onLoad(senderId: String?, loadRequest: MediaLoadRequestData): Task<MediaLoadRequestData?> {
            Log.d(TAG, "onLoad from $senderId ${loadRequest.queueData?.items?.size} ${loadRequest.queueData?.startIndex}")
            mediaQueueManager.clear()
            mediaStatusModifier.clear()
            // Handled by Player methods
            // mediaManager.setDataFromLoad(loadRequest)
            loadRequest.queueData?.let { queueData ->
                val positionMs = queueData.startTime
                val startIndex = queueData.startIndex
                setMediaItems(queueData.items.orEmpty().map(mediaItemConverter::toMediaItem), startIndex, positionMs)
            } ?: loadRequest.mediaInfo?.let { mediaInfo ->
                Log.d(TAG, "load from media info")
                val mediaQueueItem = MediaQueueItem.Builder(mediaInfo)
                    .build()
                val positionMs = loadRequest.currentTime
                setMediaItem(mediaItemConverter.toMediaItem(mediaQueueItem), positionMs)
            }
            prepare()

            playWhenReady = loadRequest.autoplay == true
            return Tasks.forResult(loadRequest)
        }

        override fun onResumeSession(senderId: String?, requestData: MediaResumeSessionRequestData): Task<MediaLoadRequestData?> {
            Log.d(TAG, "onResumeSession $senderId ${requestData.requestId}")
            return super.onResumeSession(senderId, requestData)
        }
    }

    private inner class PlayerComponent : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    EVENT_PLAYBACK_PARAMETERS_CHANGED,
                    EVENT_MEDIA_ITEM_TRANSITION,
                    EVENT_TIMELINE_CHANGED,
                    EVENT_AVAILABLE_COMMANDS_CHANGED,
                )
            ) {
                mediaStatusModifier.setSupportedMediaCommandsFromAvailableCommand(player.availableCommands)
                mediaStatusModifier.setPlaybackRateFromPlaybackParameter(player.playbackParameters)

                if (player.currentMediaItemIndex != C.INDEX_UNSET && player.mediaItemCount > 0) {
                    val currentId = mediaQueueManager.queueItems?.get(state.currentMediaItemIndex)?.itemId
                    if (currentId != mediaQueueManager.currentItemId) {
                        mediaQueueManager.currentItemId = currentId
                    }
                }

                mediaManager.broadcastMediaStatus()
            }
        }
    }

    private inner class EventCallback : CastReceiverContext.EventCallback() {

        override fun onSenderConnected(senderInfo: SenderInfo) {
            Log.d(TAG, "onSenderConnected $senderInfo #sender = ${castReceiver.senders.size}")
        }

        override fun onSenderDisconnected(senderInfo: SenderDisconnectedEventInfo) {
            Log.d(TAG, "onSenderDisconnected $senderInfo #sender = ${castReceiver.senders.size}")
        }

        override fun onStopApplication() {
            Log.d(TAG, "onStopApplication #sender = ${castReceiver.senders.size}")
        }
    }

    companion object {
        private const val TAG = "PillarboxCastReceiver"
    }
}
