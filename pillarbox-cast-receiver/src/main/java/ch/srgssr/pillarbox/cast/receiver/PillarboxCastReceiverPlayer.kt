/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.C
import androidx.media3.common.ForwardingSimpleBasePlayer
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.tv.CastReceiverContext
import com.google.android.gms.cast.tv.SenderDisconnectedEventInfo
import com.google.android.gms.cast.tv.SenderInfo
import com.google.android.gms.cast.tv.media.MediaLoadCommandCallback
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.MediaQueueManager
import com.google.android.gms.cast.tv.media.MediaResumeSessionRequestData
import com.google.android.gms.cast.tv.media.MediaStatusModifier
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**c¨
 * [PillarboxPlayer] implementation that handles operations that are not currently handled by [androidx.media3.session.MediaSession].
 * It guarantees synchronization between the underlying [player] with all Google cast senders.
 *
 *  Official documentation Cast Receiver with Android TV @see https://developers.google.com/cast/docs/android_tv_receiver/core_features#configuring_cast_support
 *
 * Usage
 *
 * ```kotlin
 *  val castReceiverContext = CastReceiverContext.getInstance()
 *
 *  val pillarboxCastReceiverPlayer = PillarboxCastReceiverPlayer(
 *      player = PillarboxExoPlayer(...),
 *      castReceiverContext = castReceiverContext,
 *      mediaItemConverter = SRGMediaItemConverter()
 *      )
 *
 *  val mediaSession = PillarboxMediaSession.Builder(this, pillarboxCastReceiverPlayer).build()
 *
 *  castReceiverContext.mediaManager.setSessionTokenFromPillarboxMediaSession(mediaSession)
 * ```
 *
 * @param player The [PillarboxExoPlayer] that plays content.
 * @param castReceiverContext The [CastReceiverContext] used for communication with Google cast senders.
 * @param mediaItemConverter The [MediaItemConverter] used for conversion between [MediaQueueItem] and [MediaItem].
 */
class PillarboxCastReceiverPlayer(
    private val player: PillarboxExoPlayer,
    private val castReceiverContext: CastReceiverContext = CastReceiverContext.getInstance(),
    private val mediaItemConverter: MediaItemConverter = DefaultMediaItemConverter(),
) : ForwardingSimpleBasePlayer(player), PillarboxPlayer {
    private val eventCallback = EventCallback()
    private val mediaLoadCommands = MediaLoadCommands()
    private val mediaManager: MediaManager = castReceiverContext.mediaManager
    private val mediaQueueManager: MediaQueueManager = mediaManager.mediaQueueManager
    private val mediaStatusModifier: MediaStatusModifier = mediaManager.mediaStatusModifier
    private val pillarboxMediaCommand = PillarboxMediaCommandCallback(
        player = player,
        mediaManager = mediaManager,
        mediaItemConverter = mediaItemConverter
    )

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
        castReceiverContext.registerEventCallback(eventCallback)
        mediaManager.setMediaLoadCommandCallback(mediaLoadCommands)
        mediaManager.setMediaCommandCallback(pillarboxMediaCommand)
        mediaQueueManager.setQueueStatusLimit(false)
        addListener(pillarboxMediaCommand)
    }

    override fun handleSetMediaItems(mediaItems: List<MediaItem>, startIndex: Int, startPositionMs: Long): ListenableFuture<*> {
        Log.d(TAG, "handleSetMediaItems startIndex = $startIndex startPositionMs = $startPositionMs")
        pillarboxMediaCommand.setMediaItems(mediaItems, startIndex)
        return super.handleSetMediaItems(mediaItems, startIndex, startPositionMs).apply { debugQueueItems() }
    }

    override fun handleMoveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int): ListenableFuture<*> {
        Log.d(TAG, "handleMoveMediaItems fromIndex = $fromIndex toIndex = $toIndex newIndex = $newIndex")
        mediaQueueManager.queueItems?.let {
            pillarboxMediaCommand.moveItem(fromIndex, toIndex, newIndex)
            mediaQueueManager.notifyQueueFullUpdate()
        }
        mediaManager.broadcastMediaStatus()
        return super.handleMoveMediaItems(fromIndex, toIndex, newIndex).apply { debugQueueItems() }
    }

    override fun handleAddMediaItems(index: Int, mediaItems: List<MediaItem>): ListenableFuture<*> {
        Log.d(TAG, "handleAddMediaItems index = $index #items = ${mediaItems.size}")
        if (mediaQueueManager.queueItems == null) {
            mediaManager.mediaQueueManager.queueItems = mutableListOf<MediaQueueItem>()
        }
        mediaQueueManager.queueItems?.let { queueItems ->
            pillarboxMediaCommand.addMediaItems(mediaItems, index)
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
            mediaQueueManager.notifyItemsRemoved(pillarboxMediaCommand.remove(itemIdRemoved))
        }
        mediaManager.broadcastMediaStatus()
        return super.handleRemoveMediaItems(fromIndex, toIndex).apply { debugQueueItems() }
    }

    override fun handleReplaceMediaItems(fromIndex: Int, toIndex: Int, mediaItems: List<MediaItem>): ListenableFuture<*> {
        if (fromIndex == toIndex) {
            handleAddMediaItems(toIndex, mediaItems)
        }
        handleRemoveMediaItems(fromIndex, toIndex)
        return Futures.immediateVoidFuture()
    }

    override fun handleRelease(): ListenableFuture<*> {
        mediaManager.setMediaCommandCallback(null)
        mediaManager.setMediaLoadCommandCallback(null)
        castReceiverContext.unregisterEventCallback(eventCallback)
        mediaManager.setSessionCompatToken(null)
        return super.handleRelease()
    }

    private fun debugQueueItems() {
        Log.d(TAG, "MediaItems: ${player.getCurrentMediaItems().map { it.mediaMetadata.title }}")
        Log.d(TAG, "QueueItems: ${mediaQueueManager.queueItems?.map { item -> item.media?.metadata?.getString(MediaMetadata.KEY_TITLE) }}")
    }

    private inner class MediaLoadCommands : MediaLoadCommandCallback() {
        override fun onLoad(senderId: String?, loadRequest: MediaLoadRequestData): Task<MediaLoadRequestData?> {
            Log.d(TAG, "onLoad from $senderId ${loadRequest.queueData?.items?.size} ${loadRequest.queueData?.startIndex}")
            mediaQueueManager.clear()
            mediaStatusModifier.clear()

            loadRequest.queueData?.let { queueData ->
                val positionMs = if (queueData.startTime < 0) C.TIME_UNSET else queueData.startTime
                val startIndex = if (queueData.startIndex < 0) C.INDEX_UNSET else queueData.startIndex
                setMediaItems(queueData.items.orEmpty().map(mediaItemConverter::toMediaItem), startIndex, positionMs)
            } ?: loadRequest.mediaInfo?.let { mediaInfo ->
                Log.d(TAG, "load from media info")
                val mediaQueueItem = MediaQueueItem.Builder(mediaInfo)
                    .build()
                val positionMs = if (loadRequest.currentTime < 0) C.TIME_UNSET else loadRequest.currentTime
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

    private inner class EventCallback : CastReceiverContext.EventCallback() {

        override fun onSenderConnected(senderInfo: SenderInfo) {
            Log.d(TAG, "onSenderConnected $senderInfo #sender = ${castReceiverContext.senders.size}")
        }

        override fun onSenderDisconnected(senderInfo: SenderDisconnectedEventInfo) {
            Log.d(TAG, "onSenderDisconnected $senderInfo #sender = ${castReceiverContext.senders.size}")
        }

        override fun onStopApplication() {
            Log.d(TAG, "onStopApplication #sender = ${castReceiverContext.senders.size}")
        }
    }

    private companion object {
        private const val TAG = "PillarboxCastReceiver"
    }
}
