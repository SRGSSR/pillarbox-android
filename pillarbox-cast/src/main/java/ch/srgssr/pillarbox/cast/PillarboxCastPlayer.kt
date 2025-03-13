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
 * @param castPlayer The underlying [CastPlayer] instance to which method calls will be forwarded.
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
                field?.mediaQueue?.unregisterCallback(mediaQueueCallback)
                field = value
                field?.registerCallback(sessionListener)
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

    override fun getState(): State {
        if (remoteMediaClient == null) return State.Builder().build()
        return State.Builder().apply {
            setAvailableCommands(AVAILABLE_COMMAND)
            setPlaybackState(remoteMediaClient.computePlaybackState())
            setPlaylist(getSimpleDummyPlaylist())
            setContentPositionMs(remoteMediaClient.getContentPositionMs())
            setCurrentMediaItemIndex(remoteMediaClient.getCurrentMediaItemIndex())
            setPlayWhenReady(remoteMediaClient?.isPlaying == true, PLAY_WHEN_READY_CHANGE_REASON_REMOTE)
        }.build()
    }

    override fun handleStop(): ListenableFuture<*> {
        remoteMediaClient?.stop()
        return Futures.immediateVoidFuture()
    }

    override fun handleRelease(): ListenableFuture<*> {
        castContext.sessionManager.apply {
            removeSessionManagerListener(sessionListener, CastSession::class.java)
            endCurrentSession(false)
        }
        return Futures.immediateVoidFuture()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        val result = if (playWhenReady) {
            remoteMediaClient?.play()
        } else {
            remoteMediaClient?.pause()
        }
        result?.setResultCallback {
            invalidateState()
        }

        return Futures.immediateVoidFuture()
    }

    /**
     * TODO optimize if there is more items than the mediaQueue.capacity(20), it will fetch items endlessly.
     */
    private fun getSimpleDummyPlaylist(): List<MediaItemData> {
        return remoteMediaClient?.let {
            val itemCount = it.mediaQueue.itemCount
            val itemIds = it.mediaQueue.itemIds
            val currentMediaIndex = it.getCurrentMediaItemIndex()
            val playlistItems: List<MediaItemData> = (0 until itemCount).map { index ->
                val id = itemIds[index]
                val queueItem = it.mediaQueue.getItemAtIndex(index, true)
                val mediaItem = if (queueItem == null) {
                    MediaItem.Builder().build()
                } else {
                    mediaItemConverter.toMediaItem(queueItem)
                }
                // FIXME when improving playlist we should also improve data that can be only fetch for the current item.
                val streamDuration = if (currentMediaIndex == index) it.streamDuration else queueItem?.media?.streamDuration
                val duration =
                    (if (streamDuration == MediaInfo.UNKNOWN_DURATION) null else streamDuration?.milliseconds?.inWholeMicroseconds) ?: C.TIME_UNSET
                MediaItemData.Builder(id)
                    .setMediaItem(mediaItem)
                    .setIsPlaceholder(queueItem == null)
                    .setDurationUs(duration)
                    .setIsSeekable(duration != C.TIME_UNSET)
                    .setIsDynamic(false)
                    .setLiveConfiguration(null)
                    .setElapsedRealtimeEpochOffsetMs(C.TIME_UNSET)
                    .setWindowStartTimeMs(C.TIME_UNSET)
                    .setTracks(Tracks.EMPTY)
                    .setManifest(null)
                    .build()
            }
            playlistItems
        } ?: emptyList()
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
                "onStatusUpdated playerState = ${getPlayerStateString(remoteMediaClient!!.playerState)} " +
                    "idleReason = ${getIdleReasonString(remoteMediaClient!!.idleReason)} " +
                    "#items = ${remoteMediaClient?.mediaQueue?.itemCount} " +
                    "position = ${remoteMediaClient?.approximateStreamPosition}"
            )
            invalidateState()
        }

        override fun onMediaError(error: MediaError) {
            Log.d(TAG, "onMediaError: ${error.reason}")
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

    companion object {
        private const val TAG = "CastSimplePlayer"
        private val AVAILABLE_COMMAND = Player.Commands.Builder()
            .addAll(
                COMMAND_PLAY_PAUSE,
                COMMAND_GET_CURRENT_MEDIA_ITEM,
                COMMAND_GET_TIMELINE,
                COMMAND_STOP,
                COMMAND_RELEASE
            )
            .build()

        private fun getIdleReasonString(idleReason: Int): String {
            return when (idleReason) {
                MediaStatus.IDLE_REASON_NONE -> "IDLE_REASON_NONE"
                MediaStatus.IDLE_REASON_ERROR -> "IDLE_REASON_ERROR"
                MediaStatus.IDLE_REASON_CANCELED -> "IDLE_REASON_CANCELED"
                MediaStatus.IDLE_REASON_FINISHED -> "IDLE_REASON_FINISHED"
                MediaStatus.IDLE_REASON_INTERRUPTED -> "IDLE_REASON_INTERRUPTED"
                else -> "Not an IdleReason"
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
                else -> "Not a Player state"
            }
        }
    }
}

private fun RemoteMediaClient?.getContentPositionMs(): Long {
    return if (this == null || approximateStreamPosition == MediaInfo.UNKNOWN_DURATION) {
        return C.TIME_UNSET
    } else {
        approximateStreamPosition
    }
}

private fun RemoteMediaClient?.computePlaybackState(): @Player.State Int {
    if (this == null || mediaQueue.itemCount == 0) return Player.STATE_IDLE
    return when (playerState) {
        MediaStatus.PLAYER_STATE_IDLE, MediaStatus.PLAYER_STATE_UNKNOWN -> Player.STATE_IDLE
        MediaStatus.PLAYER_STATE_PAUSED, MediaStatus.PLAYER_STATE_PLAYING -> Player.STATE_READY
        MediaStatus.PLAYER_STATE_BUFFERING, MediaStatus.PLAYER_STATE_LOADING -> Player.STATE_BUFFERING
        else -> Player.STATE_IDLE
    }
}

private fun RemoteMediaClient?.getCurrentMediaItemIndex(): Int {
    if (this == null) return 0
    return currentItem?.let { mediaQueue?.indexOfItemWithId(it.itemId) } ?: 0
}
