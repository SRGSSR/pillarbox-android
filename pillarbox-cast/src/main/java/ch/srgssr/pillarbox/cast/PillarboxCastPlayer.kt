/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.content.Context
import android.media.MediaRouter2
import android.media.RouteDiscoveryPreference
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.media3.cast.MediaItemConverter
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.C
import androidx.media3.common.DeviceInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.analytics.DefaultAnalyticsCollector
import androidx.media3.exoplayer.util.EventLogger
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer.Companion.DEVICE_INFO_REMOTE_EMPTY
import ch.srgssr.pillarbox.cast.extension.getAvailableCommands
import ch.srgssr.pillarbox.cast.extension.getContentDurationMs
import ch.srgssr.pillarbox.cast.extension.getContentPositionMs
import ch.srgssr.pillarbox.cast.extension.getCurrentMediaItemIndex
import ch.srgssr.pillarbox.cast.extension.getMediaIdFromIndex
import ch.srgssr.pillarbox.cast.extension.getPlaybackRate
import ch.srgssr.pillarbox.cast.extension.getPlaybackState
import ch.srgssr.pillarbox.cast.extension.getRepeatMode
import ch.srgssr.pillarbox.cast.extension.getTracks
import ch.srgssr.pillarbox.cast.extension.getVolume
import ch.srgssr.pillarbox.cast.extension.isMuted
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.PillarboxPlayer
import com.google.android.gms.cast.CastStatusCodes
import com.google.android.gms.cast.MediaError
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.cast.framework.media.RemoteMediaClient.MediaChannelResult
import com.google.android.gms.cast.framework.media.RemoteMediaClient.ProgressListener
import com.google.android.gms.common.api.PendingResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlin.time.Duration.Companion.milliseconds

/**
 * Create a new instance of [PillarboxCastPlayer].
 *
 * **Usage**
 * ```kotlin
 * val player = PillarboxCastPlayer(context, Default) {
 *      mediaItemConverter(MyMediaItemConverter())
 * }
 * ```
 *
 * @param Builder The type of the [PillarboxCastPlayerBuilder].
 * @param context The [Context].
 * @param type The [CastPlayerConfig].
 * @param builder The builder.
 *
 * @return A new instance of [PillarboxCastPlayer].
 */
@PillarboxDsl
fun <Builder : PillarboxCastPlayerBuilder> PillarboxCastPlayer(
    context: Context,
    type: CastPlayerConfig<Builder>,
    builder: Builder.() -> Unit = {},
): PillarboxCastPlayer {
    return type.create()
        .apply(builder)
        .create(context)
}

/**
 * A [PillarboxPlayer] implementation that works with Cast devices.
 *
 * It disables smooth seeking and tracking capabilities as these are not supported or relevant in the context of Cast playback.
 *
 * @param context A [Context] used to populate [getDeviceInfo]. If `null`, [getDeviceInfo] will always return [DEVICE_INFO_REMOTE_EMPTY].
 * @param castContext The context from which the cast session is obtained.
 * @param mediaItemConverter The [MediaItemConverter] to use.
 * @param seekBackIncrementMs The [seekBack] increment, in milliseconds.
 * @param seekForwardIncrementMs The [seekForward] increment, in milliseconds.
 * @param maxSeekToPreviousPositionMs The maximum position for which [seekToPrevious] seeks to the previous [MediaItem], in milliseconds.
 * @param trackSelector The [CastTrackSelector] to use when selecting tracks from [TrackSelectionParameters].
 * @param applicationLooper The [Looper] that must be used for all calls to the player and that is used to call listeners on.
 * @param clock A [Clock] used to generate timestamps.
 */
@Suppress("LongParameterList")
class PillarboxCastPlayer internal constructor(
    context: Context,
    private val castContext: CastContext,
    private val mediaItemConverter: MediaItemConverter,
    @IntRange(from = 1) private val seekBackIncrementMs: Long,
    @IntRange(from = 1) private val seekForwardIncrementMs: Long,
    @IntRange(from = 0) private val maxSeekToPreviousPositionMs: Long,
    private val trackSelector: CastTrackSelector,
    applicationLooper: Looper = Util.getCurrentOrMainLooper(),
    clock: Clock = Clock.DEFAULT
) : SimpleBasePlayer(applicationLooper) {
    private val sessionListener = SessionListener()
    private val analyticsCollector = DefaultAnalyticsCollector(clock).apply { addListener(EventLogger()) }
    private val mediaRouter = if (isMediaRouter2Available()) MediaRouter2Wrapper(context) else null

    private var deviceInfo = if (isMediaRouter2Available()) checkNotNull(mediaRouter).fetchDeviceInfo() else DEVICE_INFO_REMOTE_EMPTY
        set(value) {
            if (field != value) {
                field = value
                invalidateState()
            }
        }

    private var playlistMetadata: MediaMetadata = MediaMetadata.EMPTY
    private var sessionAvailabilityListener: SessionAvailabilityListener? = null
    private var playlistTracker: MediaQueueTracker? = null
    private var trackSelectionParameters: TrackSelectionParameters = TrackSelectionParameters.DEFAULT

    private val positionSupplier: PosSupplier = PosSupplier(0)

    private var remoteMediaClient: RemoteMediaClient? = null
        set(value) {
            if (field != value) {
                field?.unregisterCallback(sessionListener)
                field?.removeProgressListener(positionSupplier)
                playlistTracker?.release()
                playlistTracker = null
                field = value
                playlistTracker = field?.let {
                    MediaQueueTracker(it.mediaQueue, ::invalidateState)
                }
                field?.registerCallback(sessionListener)
                field?.addProgressListener(positionSupplier, 1000L)
                field?.requestStatus()
                invalidateState()
                if (field == null) {
                    sessionAvailabilityListener?.onCastSessionUnavailable()
                } else {
                    sessionAvailabilityListener?.onCastSessionAvailable()
                }
            }
        }

    init {
        castContext.sessionManager.addSessionManagerListener(sessionListener, CastSession::class.java)
        remoteMediaClient = castContext.sessionManager.currentCastSession?.remoteMediaClient
        addListener(analyticsCollector)
        analyticsCollector.setPlayer(this, applicationLooper)
    }

    /**
     * Returns whether a cast session is available.
     */
    fun isCastSessionAvailable(): Boolean {
        return remoteMediaClient != null
    }

    /**
     * Sets a listener for updates on the cast session availability.
     *
     * @param listener The [SessionAvailabilityListener], or null to clear the listener.
     */
    fun setSessionAvailabilityListener(listener: SessionAvailabilityListener?) {
        sessionAvailabilityListener = listener
    }

    override fun getState(): State {
        val remoteMediaClient = remoteMediaClient ?: return State.Builder().build()
        val itemCount = remoteMediaClient.mediaQueue.itemCount
        val playlist = remoteMediaClient.createPlaylist()
        val isLoading = remoteMediaClient.playerState == MediaStatus.PLAYER_STATE_LOADING
        return State.Builder()
            .setAvailableCommands(remoteMediaClient.getAvailableCommands(seekBackIncrementMs, seekForwardIncrementMs))
            .setPlaybackState(if (playlist.isNotEmpty()) remoteMediaClient.getPlaybackState() else STATE_IDLE)
            .setPlaylist(playlist)
            .apply {
                if (itemCount > 0) {
                    setContentPositionMs(positionSupplier)
                } else {
                    setContentPositionMs(C.TIME_UNSET)
                }
            }
            .setCurrentMediaItemIndex(remoteMediaClient.getCurrentMediaItemIndex())
            .setPlayWhenReady(remoteMediaClient.isPlaying, PLAY_WHEN_READY_CHANGE_REASON_REMOTE)
            .setShuffleModeEnabled(false)
            .setRepeatMode(remoteMediaClient.getRepeatMode())
            .setVolume(remoteMediaClient.getVolume().toFloat())
            .setIsDeviceMuted(remoteMediaClient.isMuted())
            .setDeviceInfo(deviceInfo)
            .setMaxSeekToPreviousPositionMs(maxSeekToPreviousPositionMs)
            .setSeekBackIncrementMs(seekBackIncrementMs)
            .setSeekForwardIncrementMs(seekForwardIncrementMs)
            .setTrackSelectionParameters(trackSelectionParameters)
            .setPlaybackParameters(PlaybackParameters(remoteMediaClient.getPlaybackRate()))
            .setPlaylistMetadata(playlistMetadata)
            .setIsLoading(isLoading && playlist.isNotEmpty())
            .build()
    }

    override fun handleSetMediaItems(mediaItems: MutableList<MediaItem>, startIndex: Int, startPositionMs: Long) = withRemoteClient {
        Log.d(TAG, "handleSetMediaItems #${mediaItems.size} startIndex = $startIndex at $startPositionMs")
        if (mediaItems.isNotEmpty()) {
            val mediaQueueItems = mediaItems.map(mediaItemConverter::toMediaQueueItem)
            val startPosition = if (startPositionMs == C.TIME_UNSET) MediaInfo.UNKNOWN_START_ABSOLUTE_TIME else startPositionMs
            queueLoad(mediaQueueItems.toTypedArray(), startIndex, getCastRepeatMode(repeatMode), startPosition, null)
        } else {
            clearMediaItems()
        }
    }

    override fun handleAddMediaItems(index: Int, mediaItems: MutableList<MediaItem>) = withRemoteClient {
        if (mediaQueue.itemCount == 0) {
            handleSetMediaItems(mediaItems, 0, C.TIME_UNSET)
            return@withRemoteClient
        }
        Log.d(TAG, "handleAddMediaItems at $index")
        val mediaQueueItems = mediaItems.map(mediaItemConverter::toMediaQueueItem)
        if (mediaQueueItems.size == 1) {
            queueAppendItem(mediaQueueItems[0], null)
        } else {
            val insertBeforeId = getMediaIdFromIndex(index)
            queueInsertItems(mediaQueueItems.toTypedArray(), insertBeforeId, null)
        }
    }

    override fun handleRemoveMediaItems(fromIndex: Int, toIndex: Int) = withRemoteClient {
        Log.d(TAG, "handleRemoveMediaItems [$fromIndex -> $toIndex[")
        if (toIndex - fromIndex == 1) {
            queueRemoveItem(getMediaIdFromIndex(fromIndex), null)
        } else {
            val itemsToRemove = mediaQueue.itemIds.asList().subList(fromIndex, toIndex)
            queueRemoveItems(itemsToRemove.toIntArray(), null)
        }
    }

    override fun handleMoveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) = withRemoteClient {
        Log.d(TAG, "handleMoveMediaItems [$fromIndex $toIndex[ => $newIndex")
        if (toIndex - fromIndex == 1) {
            val itemId = getMediaIdFromIndex(fromIndex)
            queueMoveItemToNewIndex(itemId, newIndex, null)
        } else {
            val itemsIdToMove = mediaQueue.itemIds.asList().subList(fromIndex, toIndex)
            val insertBeforeId = getMediaIdFromIndex(newIndex + (toIndex - fromIndex))
            queueReorderItems(itemsIdToMove.toIntArray(), insertBeforeId, null)
        }
    }

    override fun handleStop() = withRemoteClient {
        stop()
    }

    override fun handleRelease(): ListenableFuture<*> {
        if (isMediaRouter2Available()) {
            mediaRouter?.release()
        }

        castContext.sessionManager.apply {
            removeSessionManagerListener(sessionListener, CastSession::class.java)
            endCurrentSession(false)
        }
        return Futures.immediateVoidFuture()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean) = withRemoteClient {
        if (playWhenReady) {
            play()
        } else {
            pause()
        }
    }

    override fun handleSetShuffleModeEnabled(shuffleModeEnabled: Boolean) = withRemoteClient {
        queueShuffle(null)
    }

    override fun handleSetRepeatMode(repeatMode: @Player.RepeatMode Int) = withRemoteClient {
        queueSetRepeatMode(getCastRepeatMode(repeatMode), null)
    }

    override fun handleSetVolume(volume: Float) = withRemoteClient {
        setStreamVolume(volume.toDouble())
    }

    override fun handleSetDeviceMuted(muted: Boolean, flags: Int) = withRemoteClient {
        setStreamMute(muted)
    }

    override fun handleSetTrackSelectionParameters(trackSelectionParameters: TrackSelectionParameters) = withRemoteClient {
        this@PillarboxCastPlayer.trackSelectionParameters = trackSelectionParameters
        val mediaTrack = this.mediaStatus?.mediaInfo?.mediaTracks.orEmpty()
        val selectedTrackIds = trackSelector.getActiveMediaTracks(trackSelectionParameters, mediaTrack)
        setActiveMediaTracks(selectedTrackIds)
    }

    override fun handleSeek(mediaItemIndex: Int, positionMs: Long, seekCommand: @Player.Command Int) = withRemoteClient {
        Log.d(TAG, "handle seek $mediaItemIndex $positionMs $seekCommand")
        when (seekCommand) {
            COMMAND_SEEK_TO_DEFAULT_POSITION -> {
                val mediaSeekOptions = MediaSeekOptions.Builder().apply {
                    if (isLiveStream) {
                        this.setIsSeekToInfinite(true)
                    } else {
                        this.setPosition(0)
                    }
                }.build()
                seek(mediaSeekOptions)
            }

            COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM, COMMAND_SEEK_FORWARD, COMMAND_SEEK_BACK -> {
                seekTo(this, positionMs)
            }

            COMMAND_SEEK_TO_PREVIOUS, COMMAND_SEEK_TO_NEXT -> {
                if (mediaItemIndex != currentMediaItemIndex) {
                    if (seekCommand == COMMAND_SEEK_TO_PREVIOUS) queuePrev(null) else queueNext(null)
                } else {
                    seekTo(this, positionMs)
                }
            }

            COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> {
                queuePrev(null)
            }

            COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> {
                queueNext(null)
            }

            COMMAND_SEEK_TO_MEDIA_ITEM -> {
                jumpTo(this, mediaItemIndex, positionMs)
            }

            else -> super.handleSeek(mediaItemIndex, positionMs, seekCommand)
        }
    }

    override fun handleSetPlaybackParameters(playbackParameters: PlaybackParameters) = withRemoteClient {
        val playbackRate = playbackParameters.speed.toDouble().coerceIn(MediaLoadOptions.PLAYBACK_RATE_MIN, MediaLoadOptions.PLAYBACK_RATE_MAX)

        setPlaybackRate(playbackRate)
    }

    override fun handleSetPlaylistMetadata(playlistMetadata: MediaMetadata): ListenableFuture<*> {
        if (this.playlistMetadata != playlistMetadata) {
            this.playlistMetadata = playlistMetadata
            invalidateState()
        }

        return Futures.immediateVoidFuture()
    }

    private fun seekTo(remoteMediaClient: RemoteMediaClient, positionMs: Long): PendingResult<MediaChannelResult> {
        val position = if (positionMs == C.TIME_UNSET) 0 else positionMs
        positionSupplier.position = position
        val mediaSeekOptions = MediaSeekOptions.Builder()
            .setPosition(remoteMediaClient.approximateLiveSeekableRangeStart + position)
            .setIsSeekToInfinite(positionMs == C.TIME_UNSET && remoteMediaClient.isLiveStream)
            .build()
        return remoteMediaClient.seek(mediaSeekOptions)
    }

    private fun jumpTo(remoteMediaClient: RemoteMediaClient, mediaItemIndex: Int, mediaPosition: Long): PendingResult<MediaChannelResult>? {
        if (mediaItemIndex == C.INDEX_UNSET) return null
        val queueMediaItemId = remoteMediaClient.mediaQueue.itemIds[mediaItemIndex]
        return if (mediaPosition == C.TIME_UNSET) {
            remoteMediaClient.queueJumpToItem(queueMediaItemId, null)
        } else {
            remoteMediaClient.queueJumpToItem(queueMediaItemId, mediaPosition, null)
        }
    }

    private fun RemoteMediaClient.createPlaylist(): List<MediaItemData> {
        return playlistTracker?.listCastItemData
            ?.map { castItemData ->
                if (castItemData.item == null) {
                    MediaItemData.Builder(castItemData.id)
                        .setMediaItem(
                            MediaItem.Builder()
                                .setMediaMetadata(MediaMetadata.Builder().setTitle("Item ${castItemData.id}").build())
                                .build()
                        )
                        .setIsPlaceholder(true)
                        .build()
                } else {
                    val queueItem = castItemData.item
                    val mediaItem = mediaItemConverter.toMediaItem(queueItem)
                    val duration: Long
                    val isLive: Boolean
                    val isDynamic: Boolean
                    val tracks: Tracks
                    if (currentItem?.itemId == castItemData.id) {
                        isLive = isLiveStream || mediaInfo?.streamType == MediaInfo.STREAM_TYPE_LIVE
                        isDynamic = mediaStatus?.liveSeekableRange?.isMovingWindow == true
                        duration = getContentDurationMs()
                        tracks = getTracks()
                    } else {
                        duration = queueItem.media?.streamDuration.takeIf { it != MediaInfo.UNKNOWN_DURATION } ?: C.TIME_UNSET
                        isLive = queueItem.media?.streamType == MediaInfo.STREAM_TYPE_LIVE
                        isDynamic = false
                        tracks = Tracks.EMPTY
                    }

                    MediaItemData.Builder(castItemData.id)
                        .setMediaItem(mediaItem)
                        .setDurationUs(if (duration == C.TIME_UNSET) C.TIME_UNSET else duration.milliseconds.inWholeMicroseconds)
                        .setIsSeekable(true)
                        .setIsDynamic(isDynamic)
                        .setLiveConfiguration(if (isLive) MediaItem.LiveConfiguration.UNSET else null)
                        .setElapsedRealtimeEpochOffsetMs(C.TIME_UNSET)
                        .setWindowStartTimeMs(C.TIME_UNSET)
                        .setTracks(tracks)
                        .setManifest(null)
                        .build()
                }
            }
            .orEmpty()
    }

    private fun withRemoteClient(command: RemoteMediaClient.() -> Unit): ListenableFuture<*> {
        remoteMediaClient?.command()

        return Futures.immediateVoidFuture()
    }

    private fun getCastRepeatMode(repeatMode: @Player.RepeatMode Int): Int {
        return when (repeatMode) {
            REPEAT_MODE_ALL -> MediaStatus.REPEAT_MODE_REPEAT_ALL
            REPEAT_MODE_ONE -> MediaStatus.REPEAT_MODE_REPEAT_SINGLE
            else -> MediaStatus.REPEAT_MODE_REPEAT_OFF
        }
    }

    private inner class PosSupplier(var position: Long) : PositionSupplier, ProgressListener {
        override fun get(): Long {
            return position
        }

        override fun onProgressUpdated(position: Long, duration: Long) {
            val playerPosition = position - (remoteMediaClient?.approximateLiveSeekableRangeStart ?: 0L)
            if (playerPosition != this.position) {
                this.position = playerPosition
                invalidateState()
            }
        }
    }

    private inner class SessionListener : SessionManagerListener<CastSession>, RemoteMediaClient.Callback() {
        // RemoteClient Callback

        override fun onMetadataUpdated() {
            Log.d(TAG, "onMetadataUpdated")
            invalidateState()
        }

        override fun onStatusUpdated() {
            Log.d(
                TAG,
                "onStatusUpdated playerState = ${getPlayerStateString(remoteMediaClient!!.playerState)}" +
                    " idleReason = ${getIdleReasonString(remoteMediaClient!!.idleReason)}" +
                    " #items = ${remoteMediaClient?.mediaQueue?.itemCount}" +
                    " #items from status = ${remoteMediaClient?.mediaStatus?.queueItemCount}" +
                    " position = ${remoteMediaClient?.mediaStatus?.streamPosition?.milliseconds}" +
                    " duration = ${remoteMediaClient?.mediaStatus?.mediaInfo?.streamDuration?.milliseconds}"
            )
            positionSupplier.position = remoteMediaClient?.getContentPositionMs() ?: 0
            invalidateState()
        }

        override fun onMediaError(error: MediaError) {
            Log.e(TAG, "onMediaError: ${error.type} ${error.reason}")
        }

        override fun onQueueStatusUpdated() {
            Log.d(TAG, "onQueueStatusUpdated ${remoteMediaClient?.mediaQueue?.itemCount}")
            invalidateState()
        }

        override fun onPreloadStatusUpdated() {
            Log.d(TAG, "onPreloadStatusUpdated")
        }

        override fun onAdBreakStatusUpdated() {
            Log.d(TAG, "onAdBreakStatusUpdated")
        }

        override fun onSendingRemoteMediaRequest() {
            Log.d(TAG, "onSendingRemoteMediaRequest")
        }

        // SessionListener

        override fun onSessionEnded(session: CastSession, error: Int) {
            Log.i(TAG, "onSessionEnded ${session.sessionId} with error = $error")
            remoteMediaClient = null
        }

        override fun onSessionEnding(session: CastSession) {
            Log.i(TAG, "onSessionEnding ${session.sessionId}")
        }

        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            Log.i(TAG, "onSessionResumeFailed ${session.sessionId} with error = ${CastStatusCodes.getStatusCodeString(error)}")
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            Log.i(TAG, "onSessionResumed ${session.sessionId} wasSuspended = $wasSuspended")
            remoteMediaClient = session.remoteMediaClient
        }

        override fun onSessionResuming(session: CastSession, sessionId: String) {
            Log.i(TAG, "onSessionResuming ${session.sessionId} sessionId = $sessionId")
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {
            Log.i(TAG, "onSessionStartFailed ${session.sessionId} with error = ${CastStatusCodes.getStatusCodeString(error)}")
        }

        override fun onSessionStarted(session: CastSession, sessionId: String) {
            Log.i(TAG, "onSessionStarted ${session.sessionId} sessionId = $sessionId")
            remoteMediaClient = session.remoteMediaClient
        }

        override fun onSessionStarting(session: CastSession) {
            Log.i(TAG, "onSessionStarting ${session.sessionId}")
        }

        override fun onSessionSuspended(session: CastSession, reason: Int) {
            Log.i(TAG, "onSessionSuspended ${session.sessionId} with reason = $reason")
            remoteMediaClient = null
        }
    }

    // Based on CastPlayer.Api30Impl from AndroidX Media3 1.6.0
    // https://github.com/androidx/media/blob/1.6.0/libraries/cast/src/main/java/androidx/media3/cast/CastPlayer.java#L1572
    @RequiresApi(Build.VERSION_CODES.R)
    private inner class MediaRouter2Wrapper(context: Context) {
        private val mediaRouter = MediaRouter2.getInstance(context)
        private val transferCallback = TransferCallback()
        private val emptyRouterCallback = RouteCallback()
        private val handler = Handler(Looper.getMainLooper())

        init {
            mediaRouter.registerTransferCallback(handler::post, transferCallback)
            mediaRouter.registerRouteCallback(handler::post, emptyRouterCallback, RouteDiscoveryPreference.Builder(emptyList(), false).build())
        }

        fun release() {
            mediaRouter.unregisterTransferCallback(transferCallback)
            mediaRouter.unregisterRouteCallback(emptyRouterCallback)
            handler.removeCallbacksAndMessages(null)
        }

        fun fetchDeviceInfo(): DeviceInfo {
            val controllers = mediaRouter.controllers
            if (controllers.size != 2) {
                return DEVICE_INFO_REMOTE_EMPTY
            }

            val remoteController = controllers[1]
            val deviceInfo = DeviceInfo.Builder(DeviceInfo.PLAYBACK_TYPE_REMOTE)
                .setMaxVolume(remoteController.volumeMax)
                .setRoutingControllerId(remoteController.id)
                .build()

            return deviceInfo
        }

        private inner class RouteCallback : MediaRouter2.RouteCallback()

        private inner class TransferCallback : MediaRouter2.TransferCallback() {
            override fun onTransfer(oldController: MediaRouter2.RoutingController, newController: MediaRouter2.RoutingController) {
                deviceInfo = fetchDeviceInfo()
            }

            override fun onStop(controller: MediaRouter2.RoutingController) {
                deviceInfo = fetchDeviceInfo()
            }
        }
    }

    private companion object {
        private const val TAG = "CastSimplePlayer"
        private val DEVICE_INFO_REMOTE_EMPTY = DeviceInfo.Builder(DeviceInfo.PLAYBACK_TYPE_REMOTE).build()

        private fun getIdleReasonString(idleReason: Int): String {
            return when (idleReason) {
                MediaStatus.IDLE_REASON_NONE -> "IDLE_REASON_NONE"
                MediaStatus.IDLE_REASON_ERROR -> "IDLE_REASON_ERROR"
                MediaStatus.IDLE_REASON_CANCELED -> "IDLE_REASON_CANCELED"
                MediaStatus.IDLE_REASON_FINISHED -> "IDLE_REASON_FINISHED"
                MediaStatus.IDLE_REASON_INTERRUPTED -> "IDLE_REASON_INTERRUPTED"
                else -> "Unknown idle reason $idleReason"
            }
        }

        private fun getPlayerStateString(state: Int): String {
            return when (state) {
                MediaStatus.PLAYER_STATE_IDLE -> "PLAYER_STATE_IDLE"
                MediaStatus.PLAYER_STATE_LOADING -> "PLAYER_STATE_LOADING"
                MediaStatus.PLAYER_STATE_PLAYING -> "PLAYER_STATE_PLAYING"
                MediaStatus.PLAYER_STATE_PAUSED -> "PLAYER_STATE_PAUSED"
                MediaStatus.PLAYER_STATE_UNKNOWN -> "PLAYER_STATE_UNKNOWN"
                MediaStatus.PLAYER_STATE_BUFFERING -> "PLAYER_STATE_BUFFERING"
                else -> "Unknown player state $state"
            }
        }

        private fun isMediaRouter2Available(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        }
    }
}
