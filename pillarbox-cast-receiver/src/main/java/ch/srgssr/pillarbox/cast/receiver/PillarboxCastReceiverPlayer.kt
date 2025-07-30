/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.image.ImageOutput
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.tv.CastReceiverContext
import com.google.android.gms.cast.tv.SenderDisconnectedEventInfo
import com.google.android.gms.cast.tv.SenderInfo
import com.google.android.gms.cast.tv.media.MediaLoadCommandCallback
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.MediaResumeSessionRequestData
import com.google.android.gms.cast.tv.media.MediaStatusModifier
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

/**
 * [PillarboxPlayer] implementation that handles operations that are not currently handled by [androidx.media3.session.MediaSession].
 * It guarantees synchronization between the underlying [player] with all Google Cast senders.
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
 *  )
 *
 *  val mediaSession = PillarboxMediaSession.Builder(this, pillarboxCastReceiverPlayer).build()
 *
 *  castReceiverContext.mediaManager.setSessionTokenFromPillarboxMediaSession(mediaSession)
 * ```
 *
 * @param player The [PillarboxExoPlayer] that plays content.
 * @param castReceiverContext The [CastReceiverContext] used for communication with Google Cast senders.
 * @param mediaItemConverter The [MediaItemConverter] used for conversion between [MediaQueueItem] and [MediaItem].
 *
 * @see <a href="https://developers.google.com/cast/docs/android_tv_receiver/core_features#configuring_cast_support">
 *     Official documentation Cast Receiver with Android TV</a>
 */
class PillarboxCastReceiverPlayer(
    private val player: PillarboxExoPlayer,
    private val castReceiverContext: CastReceiverContext = CastReceiverContext.getInstance(),
    private val mediaItemConverter: MediaItemConverter = DefaultMediaItemConverter(),
) : PillarboxPlayer, ExoPlayer by player {
    private val eventCallback = EventCallback()
    private val mediaLoadCommands = MediaLoadCommands()
    private val mediaManager: MediaManager = castReceiverContext.mediaManager
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

    override val isMetricsAvailable: Boolean
        get() = player.isMetricsAvailable

    override val isSeekParametersAvailable: Boolean
        get() = player.isSeekParametersAvailable

    override val isImageOutputAvailable: Boolean
        get() = player.isImageOutputAvailable

    init {
        castReceiverContext.registerEventCallback(eventCallback)
        mediaManager.setMediaLoadCommandCallback(mediaLoadCommands)
        mediaManager.setMediaCommandCallback(pillarboxMediaCommand)
        mediaManager.mediaQueueManager.setQueueStatusLimit(false)
        addListener(pillarboxMediaCommand)
    }

    override fun setSeekParameters(seekParameters: SeekParameters?) {
        player.setSeekParameters(seekParameters)
    }

    override fun getSeekParameters(): SeekParameters {
        return player.seekParameters
    }

    override fun setImageOutput(imageOutput: ImageOutput?) {
        player.setImageOutput(imageOutput)
    }

    override fun getCurrentMetrics(): PlaybackMetrics? {
        return player.getCurrentMetrics()
    }

    override fun setMediaItem(mediaItem: MediaItem) {
        pillarboxMediaCommand.notifySetMediaItems(listOf(mediaItem), 0)
        player.setMediaItem(mediaItem)
    }

    override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) {
        pillarboxMediaCommand.notifySetMediaItems(listOf(mediaItem), 0)
        player.setMediaItem(mediaItem, resetPosition)
    }

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
        pillarboxMediaCommand.notifySetMediaItems(listOf(mediaItem), 0)
        player.setMediaItem(mediaItem, startPositionMs)
    }

    override fun setMediaItems(mediaItems: List<MediaItem>) {
        pillarboxMediaCommand.notifySetMediaItems(mediaItems, 0)
        player.setMediaItems(mediaItems)
    }

    override fun setMediaItems(mediaItems: List<MediaItem>, resetPosition: Boolean) {
        pillarboxMediaCommand.notifySetMediaItems(mediaItems, 0)
        player.setMediaItems(mediaItems, resetPosition)
    }

    override fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int, startPositionMs: Long) {
        pillarboxMediaCommand.notifySetMediaItems(mediaItems, startIndex)
        player.setMediaItems(mediaItems, startIndex, startPositionMs)
    }

    override fun setMediaSource(mediaSource: MediaSource) {
        handleMediaSources(listOf(mediaSource), 0)
        player.setMediaSource(mediaSource)
    }

    override fun setMediaSource(mediaSource: MediaSource, resetPosition: Boolean) {
        handleMediaSources(listOf(mediaSource), 0)
        player.setMediaSource(mediaSource, resetPosition)
    }

    override fun setMediaSource(mediaSource: MediaSource, startPositionMs: Long) {
        handleMediaSources(listOf(mediaSource), 0)
        player.setMediaSource(mediaSource, startPositionMs)
    }

    override fun setMediaSources(mediaSources: List<MediaSource>) {
        handleMediaSources(mediaSources, 0)
        player.setMediaSources(mediaSources)
    }

    override fun setMediaSources(mediaSources: List<MediaSource>, resetPosition: Boolean) {
        handleMediaSources(mediaSources, 0)
        player.setMediaSources(mediaSources, resetPosition)
    }

    override fun setMediaSources(mediaSources: List<MediaSource>, startMediaItemIndex: Int, startPositionMs: Long) {
        handleMediaSources(mediaSources, startMediaItemIndex)
        player.setMediaSources(mediaSources, startMediaItemIndex, startPositionMs)
    }

    private fun handleMediaSources(mediaSources: List<MediaSource>, startMediaItemIndex: Int) {
        pillarboxMediaCommand.notifySetMediaItems(mediaSources.map { it.mediaItem }, startMediaItemIndex)
    }

    override fun moveMediaItem(currentIndex: Int, newIndex: Int) {
        moveMediaItems(currentIndex, currentIndex + 1, newIndex)
    }

    override fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        handleMoveMediaItems(fromIndex, toIndex, newIndex)
    }

    private fun handleMoveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        Log.d(TAG, "handleMoveMediaItems fromIndex = $fromIndex toIndex = $toIndex newIndex = $newIndex")
        pillarboxMediaCommand.moveMediaItems(fromIndex, toIndex, newIndex)
        debugQueueItems()
    }

    override fun addMediaItem(index: Int, mediaItem: MediaItem) {
        addMediaItems(index, listOf(mediaItem))
    }

    override fun addMediaItem(mediaItem: MediaItem) {
        addMediaItems(listOf(mediaItem))
    }

    override fun addMediaItems(index: Int, mediaItems: List<MediaItem>) {
        handleAddMediaItems(index.coerceAtMost(player.mediaItemCount), mediaItems)
    }

    override fun addMediaItems(mediaItems: List<MediaItem>) {
        addMediaItems(Integer.MAX_VALUE, mediaItems)
    }

    private fun handleAddMediaItems(index: Int, mediaItems: List<MediaItem>) {
        Log.d(TAG, "handleAddMediaItems index = $index #items = ${mediaItems.size}")
        pillarboxMediaCommand.addMediaItems(mediaItems, index)
        debugQueueItems()
    }

    override fun removeMediaItem(index: Int) {
        handleRemoveMediaItems(index, index + 1)
    }

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) {
        handleRemoveMediaItems(fromIndex, toIndex)
    }

    private fun handleRemoveMediaItems(fromIndex: Int, toIndex: Int) {
        Log.d(TAG, "handleRemoveMediaItems fromIndex = $fromIndex toIndex = $toIndex")
        debugQueueItems()
        pillarboxMediaCommand.removeMediaItems(fromIndex, toIndex)
        debugQueueItems()
    }

    override fun replaceMediaItem(index: Int, mediaItem: MediaItem) {
        replaceMediaItems(index, index + 1, listOf(mediaItem))
    }

    override fun replaceMediaItems(fromIndex: Int, toIndex: Int, mediaItems: List<MediaItem>) {
        handleReplaceMediaItems(fromIndex, toIndex, mediaItems)
    }

    private fun handleReplaceMediaItems(fromIndex: Int, toIndex: Int, mediaItems: List<MediaItem>) {
        handleAddMediaItems(toIndex, mediaItems)
        if (fromIndex != toIndex) {
            handleRemoveMediaItems(fromIndex, toIndex)
        }
    }

    override fun release() {
        mediaManager.setMediaCommandCallback(null)
        mediaManager.setMediaLoadCommandCallback(null)
        castReceiverContext.unregisterEventCallback(eventCallback)
        mediaManager.setSessionCompatToken(null)
        player.release()
    }

    private fun debugQueueItems() {
        Log.d(TAG, "MediaItems: ${player.getCurrentMediaItems().map { it.mediaMetadata.title }}")
        Log.d(
            TAG,
            "QueueItems: ${mediaManager.mediaQueueManager.queueItems?.map { item -> item.media?.metadata?.getString(MediaMetadata.KEY_TITLE) }}"
        )
    }

    override fun getSecondaryRenderer(index: Int): Renderer? {
        return player.getSecondaryRenderer(index)
    }

    private inner class MediaLoadCommands : MediaLoadCommandCallback() {
        override fun onLoad(senderId: String?, loadRequest: MediaLoadRequestData): Task<MediaLoadRequestData?> {
            Log.d(TAG, "onLoad from $senderId #items = ${loadRequest.queueData?.items?.size} startIndex = ${loadRequest.queueData?.startIndex}")
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
