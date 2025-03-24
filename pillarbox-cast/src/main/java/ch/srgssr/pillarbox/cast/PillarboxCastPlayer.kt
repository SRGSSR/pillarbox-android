/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.annotation.IntRange
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.MediaItemConverter
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.analytics.DefaultAnalyticsCollector
import androidx.media3.exoplayer.util.EventLogger
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.PillarboxPlayer
import com.google.android.gms.cast.MediaError
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.MediaQueue
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
 * A [PillarboxPlayer] implementation that forwards calls to a [CastPlayer].
 *
 * It disables smooth seeking and tracking capabilities as these are not supported or relevant in the context of Cast playback.
 *
 * @param castContext The context from which the cast session is obtained.
 * @param context A [Context] used to populate [getDeviceInfo]. If `null`, [getDeviceInfo] will always return [CastPlayer.DEVICE_INFO_REMOTE_EMPTY].
 * @param mediaItemConverter The [MediaItemConverter] to use.
 * @param seekBackIncrementMs The [seekBack] increment, in milliseconds.
 * @param seekForwardIncrementMs The [seekForward] increment, in milliseconds.
 * @param maxSeekToPreviousPositionMs The maximum position for which [seekToPrevious] seeks to the previous [MediaItem], in milliseconds.
 * @param trackSelector The [CastTrackSelector] to use when selecting tracks from [TrackSelectionParameters].
 */
@Suppress("LongParameterList")
class PillarboxCastPlayer internal constructor(
    private val castContext: CastContext,
    private val mediaItemConverter: MediaItemConverter,
    @IntRange(from = 1) private val seekBackIncrementMs: Long,
    @IntRange(from = 1) private val seekForwardIncrementMs: Long,
    @IntRange(from = 0) private val maxSeekToPreviousPositionMs: Long,
    applicationLooper: Looper = Util.getCurrentOrMainLooper(),
    clock: Clock = Clock.DEFAULT
) : SimpleBasePlayer(applicationLooper) {
    private val sessionListener = SessionListener()
    private val mediaQueueCallback = MediaQueueCallback()
    private val analyticsCollector = DefaultAnalyticsCollector(clock).apply { addListener(EventLogger()) }

    var sessionAvailabilityListener: SessionAvailabilityListener? = null

    var remoteMediaClient: RemoteMediaClient? = null
        set(value) {
            if (field != value) {
                field?.unregisterCallback(sessionListener)
                field?.removeProgressListener(sessionListener)
                field?.mediaQueue?.unregisterCallback(mediaQueueCallback)
                field = value
                field?.registerCallback(sessionListener)
                field?.addProgressListener(sessionListener, 1000L)
                field?.mediaQueue?.registerCallback(mediaQueueCallback)
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

    fun isCastSessionAvailable(): Boolean {
        return remoteMediaClient != null
    }

    override fun getState(): State {
        val remoteMediaClient = remoteMediaClient ?: return State.Builder().build()
        val currentItemIndex = remoteMediaClient.getCurrentMediaItemIndex()
        val isPlayingAd = remoteMediaClient.mediaStatus?.isPlayingAd == true
        val itemCount = remoteMediaClient.mediaQueue.itemCount
        val hasNextItem = !isPlayingAd && currentItemIndex + 1 < itemCount
        val hasPreviousItem = !isPlayingAd && currentItemIndex - 1 >= 0
        val hasNext = hasNextItem // TODO handle like describe in Player.seekToNext
        val hasPrevious = hasPreviousItem // TODO handle like describe in Player.seekToPrevious
        val availableCommands = PERMANENT_AVAILABLE_COMMANDS.buildUpon()
            .addIf(COMMAND_SEEK_TO_DEFAULT_POSITION, !isPlayingAd)
            .addIf(COMMAND_SEEK_TO_MEDIA_ITEM, !isPlayingAd)
            .addIf(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM, hasNextItem)
            .addIf(COMMAND_SEEK_TO_NEXT, hasNext)
            .addIf(COMMAND_SEEK_TO_PREVIOUS, hasPrevious)
            .addIf(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM, hasPreviousItem)
            .build()

        return State.Builder()
            .setAvailableCommands(availableCommands)
            .setPlaybackState(remoteMediaClient.getPlaybackState())
            .setPlaylist(remoteMediaClient.getSimpleDummyPlaylist())
            .setContentPositionMs(remoteMediaClient.getContentPositionMs())
            .setCurrentMediaItemIndex(currentItemIndex)
            .setPlayWhenReady(remoteMediaClient.isPlaying, PLAY_WHEN_READY_CHANGE_REASON_REMOTE)
            .setShuffleModeEnabled(false)
            .setRepeatMode(remoteMediaClient.getRepeatMode())
            .build()
    }

    override fun handleStop() = withRemoteClient {
        stop()
    }

    override fun handleRelease(): ListenableFuture<*> {
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
        val remoteRepeatMode = when (repeatMode) {
            REPEAT_MODE_OFF -> MediaStatus.REPEAT_MODE_REPEAT_OFF
            REPEAT_MODE_ONE -> MediaStatus.REPEAT_MODE_REPEAT_SINGLE
            REPEAT_MODE_ALL -> MediaStatus.REPEAT_MODE_REPEAT_ALL
            else -> MediaStatus.REPEAT_MODE_REPEAT_OFF
        }

        queueSetRepeatMode(remoteRepeatMode, null)
    }

    override fun handleSeek(mediaItemIndex: Int, positionMs: Long, seekCommand: @Player.Command Int) = withRemoteClient {
        Log.d(TAG, "handle seek $mediaItemIndex $positionMs $seekCommand")
        when (seekCommand) {
            COMMAND_SEEK_TO_DEFAULT_POSITION -> Log.d(TAG, "COMMAND_SEEK_TO_DEFAULT_POSITION")
            COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> {
                Log.d(TAG, "COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM")
                queuePrev(null)
            }

            COMMAND_SEEK_TO_PREVIOUS -> {
                Log.d(TAG, "COMMAND_SEEK_TO_PREVIOUS")
                // TODO should handle seek to live edge if it is live instead like it is documented at Player.seekToPrevious()
                queuePrev(null)
            }

            COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> {
                Log.d(TAG, "COMMAND_SEEK_TO_NEXT_MEDIA_ITEM")
                queueNext(null)
            }

            COMMAND_SEEK_TO_NEXT -> {
                Log.d(TAG, "COMMAND_SEEK_TO_NEXT")
                // TODO should handle seek to live edge if it is live instead like it is documented at Player.seekToNext()
                queueNext(null)
            }

            COMMAND_SEEK_TO_MEDIA_ITEM -> {
                Log.d(TAG, "COMMAND_SEEK_TO_MEDIA_ITEM to $mediaItemIndex")
                jumpTo(this, mediaItemIndex, positionMs)
            }

            else -> super.handleSeek(mediaItemIndex, positionMs, seekCommand)
        }
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

    /**
     * TODO optimize if there is more items than the mediaQueue.capacity(20), it will fetch items endlessly.
     */
    private fun RemoteMediaClient.getSimpleDummyPlaylist(): List<MediaItemData> {
        val itemCount = mediaQueue.itemCount
        val itemIds = mediaQueue.itemIds
        val currentMediaIndex = getCurrentMediaItemIndex()

        return (0 until itemCount).map { index ->
            val id = itemIds[index]
            val queueItem = mediaQueue.getItemAtIndex(index, true)
            if (queueItem == null) {
                MediaItemData.Builder(id)
                    .setMediaItem(MediaItem.Builder().build())
                    .setIsPlaceholder(true)
                    .build()
            } else {
                val mediaItem = mediaItemConverter.toMediaItem(queueItem)
                val duration: Long
                val isLive: Boolean
                val isDynamic: Boolean

                if (index == currentMediaIndex) {
                    duration = streamDuration
                    isLive = isLiveStream || mediaInfo?.streamType == MediaInfo.STREAM_TYPE_LIVE
                    isDynamic = mediaStatus?.liveSeekableRange?.isMovingWindow == true
                } else {
                    duration = queueItem.media?.streamDuration ?: MediaInfo.UNKNOWN_DURATION
                    isLive = queueItem.media?.streamType == MediaInfo.STREAM_TYPE_LIVE
                    isDynamic = false
                }

                // FIXME when improving playlist we should also improve data that can be only fetch for the current item.
                MediaItemData.Builder(id)
                    .setMediaItem(mediaItem)
                    .setDurationUs(if (duration == MediaInfo.UNKNOWN_DURATION) C.TIME_UNSET else duration.milliseconds.inWholeMicroseconds)
                    .setIsSeekable(false)
                    .setIsDynamic(isDynamic)
                    .setLiveConfiguration(if (isLive) MediaItem.LiveConfiguration.UNSET else null)
                    .setElapsedRealtimeEpochOffsetMs(C.TIME_UNSET)
                    .setWindowStartTimeMs(C.TIME_UNSET)
                    .setTracks(Tracks.EMPTY)
                    .setManifest(null)
                    .build()
            }
        }
    }

    private fun withRemoteClient(command: RemoteMediaClient.() -> Unit): ListenableFuture<*> {
        remoteMediaClient?.command()

        return Futures.immediateVoidFuture()
    }

    private inner class SessionListener : SessionManagerListener<CastSession>, RemoteMediaClient.Callback(), ProgressListener {

        override fun onProgressUpdated(p: Long, d: Long) {
            // Log.d(TAG, "p=${p.milliseconds} $d=${d.milliseconds}")
        }

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
                    " position = ${remoteMediaClient?.mediaStatus?.streamPosition?.milliseconds}" +
                    " duration = ${remoteMediaClient?.mediaStatus?.mediaInfo?.streamDuration?.milliseconds}"
            )
            invalidateState()
        }

        override fun onMediaError(error: MediaError) {
            Log.e(TAG, "onMediaError: ${error.type} ${error.reason}")
        }

        override fun onQueueStatusUpdated() {
            Log.d(TAG, "onQueueStatusUpdated ${remoteMediaClient?.mediaQueue?.itemCount}")
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
            Log.i(TAG, "onSessionResumeFailed ${session.sessionId} with error = $error")
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            Log.i(TAG, "onSessionResumed ${session.sessionId} wasSuspended = $wasSuspended")
            remoteMediaClient = session.remoteMediaClient
        }

        override fun onSessionResuming(session: CastSession, sessionId: String) {
            Log.i(TAG, "onSessionResuming ${session.sessionId} sessionId = $sessionId")
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {
            Log.i(TAG, "onSessionStartFailed ${session.sessionId} with error = $error")
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

    private inner class MediaQueueCallback : MediaQueue.Callback()

    private companion object {
        private const val TAG = "CastSimplePlayer"
        private val PERMANENT_AVAILABLE_COMMANDS = Player.Commands.Builder()
            .addAll(
                COMMAND_PLAY_PAUSE,
                COMMAND_GET_CURRENT_MEDIA_ITEM,
                COMMAND_GET_TIMELINE,
                COMMAND_STOP,
                COMMAND_RELEASE,
                COMMAND_SET_SHUFFLE_MODE,
                COMMAND_SET_REPEAT_MODE,
            )
            .build()

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
    }
}

private fun RemoteMediaClient.getContentPositionMs(): Long {
    return if (approximateStreamPosition == MediaInfo.UNKNOWN_DURATION) {
        C.TIME_UNSET
    } else {
        approximateStreamPosition
    }
}

private fun RemoteMediaClient.getPlaybackState(): @Player.State Int {
    if (mediaQueue.itemCount == 0) return Player.STATE_IDLE
    return when (playerState) {
        MediaStatus.PLAYER_STATE_IDLE, MediaStatus.PLAYER_STATE_UNKNOWN -> Player.STATE_IDLE
        MediaStatus.PLAYER_STATE_PAUSED, MediaStatus.PLAYER_STATE_PLAYING -> Player.STATE_READY
        MediaStatus.PLAYER_STATE_BUFFERING, MediaStatus.PLAYER_STATE_LOADING -> Player.STATE_BUFFERING
        else -> Player.STATE_IDLE
    }
}

private fun RemoteMediaClient.getCurrentMediaItemIndex(): Int {
    return currentItem?.let { mediaQueue.indexOfItemWithId(it.itemId) } ?: 0
}

private fun RemoteMediaClient.getRepeatMode(): @Player.RepeatMode Int {
    return when (mediaStatus?.queueRepeatMode) {
        MediaStatus.REPEAT_MODE_REPEAT_ALL,
        MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE -> Player.REPEAT_MODE_ALL

        MediaStatus.REPEAT_MODE_REPEAT_OFF -> Player.REPEAT_MODE_OFF
        MediaStatus.REPEAT_MODE_REPEAT_SINGLE -> Player.REPEAT_MODE_ONE
        else -> Player.REPEAT_MODE_OFF
    }
}
