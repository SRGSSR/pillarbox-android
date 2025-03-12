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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.analytics.DefaultAnalyticsCollector
import androidx.media3.exoplayer.util.EventLogger
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.PillarboxPlayer
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.MediaQueue
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

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
    castContext: CastContext,
    private val mediaItemConverter: MediaItemConverter,
    @IntRange(from = 1) private val seekBackIncrementMs: Long,
    @IntRange(from = 1) private val seekForwardIncrementMs: Long,
    @IntRange(from = 0) private val maxSeekToPreviousPositionMs: Long,
    applicationLooper: Looper = Util.getCurrentOrMainLooper(),
    clock: Clock = Clock.DEFAULT
) : SimpleBasePlayer(applicationLooper) {
    private val sessionListener = SessionListener()
    private val mediaQueueCallback = MediaQueueCallback()
    private val analyticsCollector = DefaultAnalyticsCollector(clock).apply { addListener(EventLogger(TAG)) }

    var sessionAvailabilityListener: SessionAvailabilityListener? = null

    var remoteMediaClient: RemoteMediaClient? = null
        set(value) {
            if (field != value) {
                field?.unregisterCallback(sessionListener)
                field?.removeProgressListener(sessionListener)
                field?.mediaQueue?.unregisterCallback(mediaQueueCallback)
                field = value
                field?.registerCallback(sessionListener)
                field?.addProgressListener(sessionListener, PROGRESS_REPORT_PERIOD_MS)
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
        return State.Builder().apply {
            setAvailableCommands(AVAILABLE_COMMAND)
            setPlaybackState(STATE_IDLE)
            setPlayWhenReady(remoteMediaClient?.isPlaying == true, PLAY_WHEN_READY_CHANGE_REASON_REMOTE)
        }.build()
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

    private inner class SessionListener : SessionManagerListener<CastSession>, RemoteMediaClient.Callback(), RemoteMediaClient.ProgressListener {

        // RemoteClient Callback

        override fun onMetadataUpdated() {
            invalidateState()
        }

        override fun onStatusUpdated() {
            invalidateState()
        }

        // ProgressListener

        override fun onProgressUpdated(progressMs: Long, durationMs: Long) {
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
        private val PROGRESS_REPORT_PERIOD_MS = 1000L
        private val AVAILABLE_COMMAND = Player.Commands.Builder()
            .addAll(
                COMMAND_PLAY_PAUSE,
            )
            .build()
    }
}
