/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.content.Context
import androidx.annotation.IntRange
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Clock
import androidx.media3.common.util.ListenerSet
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient

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
 * @param castPlayer The underlying [CastPlayer] instance to which method calls will be forwarded.
 */
class PillarboxCastPlayer(
    castContext: CastContext,
    context: Context? = null,
    mediaItemConverter: MediaItemConverter = DefaultMediaItemConverter(),
    @IntRange(from = 1) seekBackIncrementMs: Long = C.DEFAULT_SEEK_BACK_INCREMENT_MS,
    @IntRange(from = 1) seekForwardIncrementMs: Long = C.DEFAULT_SEEK_FORWARD_INCREMENT_MS,
    @IntRange(from = 0) maxSeekToPreviousPositionMs: Long = C.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION_MS,
    private val castPlayer: CastPlayer = CastPlayer(
        context,
        castContext,
        mediaItemConverter,
        seekBackIncrementMs,
        seekForwardIncrementMs,
        maxSeekToPreviousPositionMs,
    ),
) : PillarboxPlayer, Player by castPlayer {
    private val listeners = ListenerSet<Player.Listener>(castPlayer.applicationLooper, Clock.DEFAULT) { listener, flags ->
        listener.onEvents(this, Player.Events(flags))
    }
    private val remoteClientCallback = RemoteClientCallback()
    private val sessionManagerListener = SessionListener()

    private var remoteMediaClient: RemoteMediaClient? = null
        set(value) {
            if (field != value) {
                field?.unregisterCallback(remoteClientCallback)
                value?.registerCallback(remoteClientCallback)
                field = value
            }
        }

    /**
     * Smooth seeking is not supported on [CastPlayer]. By its very nature (ie. being remote), seeking **smoothly** is impossible to achieve.
     */
    override var smoothSeekingEnabled: Boolean = false
        set(value) {}

    /**
     * This flag is not supported on [CastPlayer]. The receiver should implement tracking on its own.
     */
    override var trackingEnabled: Boolean = false
        set(value) {}

    init {
        remoteMediaClient = castContext.sessionManager.currentCastSession?.remoteMediaClient

        castContext.sessionManager.addSessionManagerListener(sessionManagerListener, CastSession::class.java)

        castPlayer.addListener(object : Player.Listener {
            override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
                notifyOnAvailableCommandsChange()
            }
        })
    }

    /**
     * Returns the item that corresponds to the period with the given id, or `null` if no media queue or period with id [periodId] exist.
     *
     * @param periodId The id of the period ([getCurrentTimeline]) that corresponds to the item to get.
     * @return The item that corresponds to the period with the given id, or `null` if no media queue or period with id [periodId] exist.
     */
    fun getItem(periodId: Int): MediaQueueItem? {
        return castPlayer.getItem(periodId)
    }

    /**
     * Returns whether a cast session is available.
     */
    fun isCastSessionAvailable(): Boolean {
        return castPlayer.isCastSessionAvailable
    }

    /**
     * Sets a listener for updates on the cast session availability.
     *
     * @param listener The [SessionAvailabilityListener], or `null` to clear the listener.
     */
    fun setSessionAvailabilityListener(listener: SessionAvailabilityListener?) {
        castPlayer.setSessionAvailabilityListener(listener)
    }

    override fun addListener(listener: Player.Listener) {
        castPlayer.addListener(listener)
        listeners.add(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        castPlayer.removeListener(listener)
        listeners.remove(listener)
    }

    override fun getAvailableCommands(): Player.Commands {
        val isShuffleAvailable = remoteMediaClient?.mediaStatus?.isMediaCommandSupported(MediaStatus.COMMAND_QUEUE_SHUFFLE) == true

        return castPlayer.availableCommands
            .buildUpon()
            .addIf(Player.COMMAND_SET_SHUFFLE_MODE, isShuffleAvailable)
            .build()
    }

    override fun isCommandAvailable(command: Int): Boolean {
        return availableCommands.contains(command)
    }

    /**
     * It is not possible to toggle shuffle mode on and off on a [CastPlayer], thus, this method always returns `false`.
     */
    override fun getShuffleModeEnabled(): Boolean {
        return false
    }

    /**
     * Shuffle in place the list of media items being played, independently of the value of [shuffleModeEnabled].
     *
     * As opposed to the implementation of this method in [PillarboxExoPlayer], the order of the media items can not be reverted to their original
     * state.
     *
     * @param shuffleModeEnabled Unused in this implementation.
     */
    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        remoteMediaClient?.queueShuffle(null)
    }

    private fun notifyOnAvailableCommandsChange() {
        listeners.queueEvent(Player.EVENT_AVAILABLE_COMMANDS_CHANGED) {
            it.onAvailableCommandsChanged(availableCommands)
        }
        listeners.flushEvents()
    }

    private inner class RemoteClientCallback : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            notifyOnAvailableCommandsChange()
        }

        override fun onQueueStatusUpdated() {
            notifyOnAvailableCommandsChange()
        }
    }

    private inner class SessionListener : SessionManagerListener<CastSession> {
        override fun onSessionEnded(session: CastSession, error: Int) {
            remoteMediaClient = null
        }

        override fun onSessionEnding(session: CastSession) {
        }

        override fun onSessionResumeFailed(session: CastSession, error: Int) {
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            remoteMediaClient = session.remoteMediaClient
        }

        override fun onSessionResuming(session: CastSession, sessionId: String) {
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {
        }

        override fun onSessionStarted(session: CastSession, sessionId: String) {
            remoteMediaClient = session.remoteMediaClient
        }

        override fun onSessionStarting(session: CastSession) {
        }

        override fun onSessionSuspended(session: CastSession, reason: Int) {
            remoteMediaClient = null
        }
    }
}
