/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.image.ImageOutput
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.cast.DefaultTracksConverter
import ch.srgssr.pillarbox.cast.TracksConverter
import ch.srgssr.pillarbox.cast.receiver.extensions.getItemIndexOrNull
import ch.srgssr.pillarbox.cast.receiver.extensions.insert
import ch.srgssr.pillarbox.cast.receiver.extensions.move
import ch.srgssr.pillarbox.cast.receiver.extensions.remove
import ch.srgssr.pillarbox.cast.receiver.extensions.setMediaItems
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.tv.CastReceiverContext
import com.google.android.gms.cast.tv.SenderDisconnectedEventInfo
import com.google.android.gms.cast.tv.SenderInfo
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.MediaQueueManager

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
 * @param tracksConverter The [TracksConverter] used for conversion
 *  between [androidx.media3.common.Tracks] and [com.google.android.gms.cast.MediaTrack].
 *
 * @see <a href="https://developers.google.com/cast/docs/android_tv_receiver/core_features#configuring_cast_support">
 *     Official documentation Cast Receiver with Android TV</a>
 */
class PillarboxCastReceiverPlayer(
    private val player: PillarboxExoPlayer,
    private val castReceiverContext: CastReceiverContext = CastReceiverContext.getInstance(),
    private val mediaItemConverter: MediaItemConverter = DefaultMediaItemConverter(),
    private val tracksConverter: TracksConverter = DefaultTracksConverter()
) : PillarboxPlayer, ExoPlayer by player {
    private val eventCallback = EventCallback()
    private val mediaManager: MediaManager = castReceiverContext.mediaManager
    private val mediaQueueManager: MediaQueueManager = mediaManager.mediaQueueManager

    private val pillarboxMediaCommand = MediaCommandCallbackImpl(
        player = player,
        mediaManager = mediaManager,
        mediaQueueManager = mediaQueueManager,
        mediaItemConverter = mediaItemConverter,
        tracksConverter = tracksConverter,
    )
    private val pillarboxLoadCommandCallback = MediaLoadCommandCallbackImpl(
        player = player,
        mediaManager = mediaManager,
        mediaItemConverter = mediaItemConverter,
    )

    private val playerListener = PlayerListener(
        mediaManager = mediaManager,
        mediaItemConverter = mediaItemConverter,
        tracksConverter = tracksConverter,
    )

    override var trackingEnabled: Boolean
        get() = player.trackingEnabled
        set(value) {
            player.trackingEnabled = value
        }

    override val isMetricsAvailable: Boolean
        get() = player.isMetricsAvailable

    override val isImageOutputAvailable: Boolean
        get() = player.isImageOutputAvailable

    override val currentPillarboxMetadata: PillarboxMetadata
        get() = player.currentPillarboxMetadata

    init {
        if (BuildConfig.DEBUG) {
            mediaManager.setMediaStatusInterceptor(LogMediaStatusInterceptor)
        }
        castReceiverContext.registerEventCallback(eventCallback)
        mediaManager.setMediaLoadCommandCallback(pillarboxLoadCommandCallback)
        mediaManager.setMediaCommandCallback(pillarboxMediaCommand)
        mediaManager.mediaQueueManager.setQueueStatusLimit(false)
        player.addListener(playerListener)
        mediaManager.setMediaStatusInterceptor {
            val TAG = "MediaStatusInterceptor"
            Log.d(TAG, "-------------------------------------------------------------")
            Log.d(TAG, "mediaStatus.playerState = ${it.mediaStatus.playerState}")
            Log.d(TAG, "mediaStatus.queueItemCount = ${it.mediaStatus.queueItemCount} ${mediaManager.mediaQueueManager.queueItems?.size}")
            Log.d(
                TAG,
                "mediaStatus.queueItems = ${it.mediaStatus.queueItems.map { item ->
                    "[${item.itemId}] ${item.media?.metadata?.getString(
                        MediaMetadata.KEY_TITLE
                    )}"
                }}"
            )
            Log.d(TAG, "-------------------------------------------------------------")
        }
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
        handleSetMediaItems(listOf(mediaItem))
        player.setMediaItem(mediaItem)
    }

    override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) {
        handleSetMediaItems(listOf(mediaItem))
        player.setMediaItem(mediaItem, resetPosition)
    }

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
        handleSetMediaItems(listOf(mediaItem))
        player.setMediaItem(mediaItem, startPositionMs)
    }

    override fun setMediaItems(mediaItems: List<MediaItem>) {
        handleSetMediaItems(mediaItems)
        player.setMediaItems(mediaItems)
    }

    override fun setMediaItems(mediaItems: List<MediaItem>, resetPosition: Boolean) {
        handleSetMediaItems(mediaItems)
        player.setMediaItems(mediaItems, resetPosition)
    }

    override fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int, startPositionMs: Long) {
        handleSetMediaItems(mediaItems, startIndex)
        player.setMediaItems(mediaItems, startIndex, startPositionMs)
    }

    private fun handleSetMediaItems(mediaItems: List<MediaItem>, startIndex: Int = 0) {
        val mediaQueueItem = mediaItems.map(mediaItemConverter::toMediaQueueItem)
        mediaQueueManager.setMediaItems(mediaQueueItem, startIndex)
    }

    override fun setMediaSource(mediaSource: MediaSource) {
        handleMediaSources(listOf(mediaSource))
        player.setMediaSource(mediaSource)
    }

    override fun setMediaSource(mediaSource: MediaSource, resetPosition: Boolean) {
        handleMediaSources(listOf(mediaSource))
        player.setMediaSource(mediaSource, resetPosition)
    }

    override fun setMediaSource(mediaSource: MediaSource, startPositionMs: Long) {
        handleMediaSources(listOf(mediaSource))
        player.setMediaSource(mediaSource, startPositionMs)
    }

    override fun setMediaSources(mediaSources: List<MediaSource>) {
        handleMediaSources(mediaSources)
        player.setMediaSources(mediaSources)
    }

    override fun setMediaSources(mediaSources: List<MediaSource>, resetPosition: Boolean) {
        handleMediaSources(mediaSources)
        player.setMediaSources(mediaSources, resetPosition)
    }

    override fun setMediaSources(mediaSources: List<MediaSource>, startMediaItemIndex: Int, startPositionMs: Long) {
        handleMediaSources(mediaSources, startMediaItemIndex)
        player.setMediaSources(mediaSources, startMediaItemIndex, startPositionMs)
    }

    private fun handleMediaSources(mediaSources: List<MediaSource>, startMediaItemIndex: Int = 0) {
        handleSetMediaItems(mediaSources.map { it.mediaItem }, startMediaItemIndex)
    }

    override fun moveMediaItem(currentIndex: Int, newIndex: Int) {
        moveMediaItems(currentIndex, currentIndex + 1, newIndex)
    }

    override fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        handleMoveMediaItems(fromIndex, toIndex, newIndex)
    }

    private fun handleMoveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        Log.d(TAG, "handleMoveMediaItems fromIndex = $fromIndex toIndex = $toIndex newIndex = $newIndex")
        mediaQueueManager.move(fromIndex, toIndex, newIndex)
        player.moveMediaItems(fromIndex, toIndex, newIndex)
        mediaQueueManager.notifyQueueFullUpdate()
        mediaManager.broadcastMediaStatus()
    }

    override fun addMediaItem(index: Int, mediaItem: MediaItem) {
        addMediaItems(index, listOf(mediaItem))
    }

    override fun addMediaItem(mediaItem: MediaItem) {
        addMediaItems(listOf(mediaItem))
    }

    override fun addMediaItems(index: Int, mediaItems: List<MediaItem>) {
        handleAddMediaItems(index, mediaItems)
    }

    override fun addMediaItems(mediaItems: List<MediaItem>) {
        addMediaItems(Integer.MAX_VALUE, mediaItems)
    }

    private fun handleAddMediaItems(index: Int, mediaItems: List<MediaItem>) {
        Log.d(TAG, "handleAddMediaItems index = $index #items = ${mediaItems.size}")
        val mediaQueueItems = mediaItems.map(mediaItemConverter::toMediaQueueItem)
        mediaQueueManager.insert(mediaQueueItems, index)
        player.addMediaItems(index, mediaItems)
        mediaQueueManager.notifyItemsInserted(mediaQueueItems.map { it.itemId }, mediaQueueManager.getItemIndexOrNull(index))
        mediaManager.broadcastMediaStatus()
    }

    override fun removeMediaItem(index: Int) {
        handleRemoveMediaItems(index, index + 1)
    }

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) {
        handleRemoveMediaItems(fromIndex, toIndex)
    }

    private fun handleRemoveMediaItems(fromIndex: Int, toIndex: Int) {
        Log.d(TAG, "handleRemoveMediaItems fromIndex = $fromIndex toIndex = $toIndex")
        val mediaQueueItems = checkNotNull(mediaQueueManager.queueItems)
        val itemsRemoved: List<Int?> = (fromIndex..toIndex).map { index ->
            mediaQueueItems.getOrNull(index)?.itemId
        }
        mediaQueueManager.remove(fromIndex, toIndex)

        player.removeMediaItems(fromIndex, toIndex)

        mediaQueueManager.notifyItemsRemoved(itemsRemoved.filterNotNull())
        mediaManager.broadcastMediaStatus()
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

    override fun getSecondaryRenderer(index: Int): Renderer? {
        return player.getSecondaryRenderer(index)
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
