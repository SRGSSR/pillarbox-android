/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.os.Looper
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.Player
import androidx.media3.common.util.Clock
import androidx.media3.common.util.ListenerSet
import ch.srgssr.pillarbox.player.PillarboxPlayer
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient

/**
 * A [PillarboxPlayer] implementation that forwards calls to a [CastPlayer].
 *
 * It disables smooth seeking and tracking capabilities as these are not supported or relevant in the context of Cast playback.
 *
 * @param castPlayer The underlying [CastPlayer] instance to which method calls will be forwarded.
 */
// TODO Add all the arguments from the CastPlayer constructor
class PillarboxCastPlayer(
    castContext: CastContext,
    private val castPlayer: CastPlayer = CastPlayer(castContext),
) : PillarboxPlayer, Player by castPlayer {
    private val listeners = ListenerSet<Player.Listener>(Looper.getMainLooper(), Clock.DEFAULT) { listener, flags ->
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

    override var smoothSeekingEnabled: Boolean = false
        set(value) {}

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
        return castPlayer.availableCommands
            .buildUpon()
            .apply {
                add(Player.COMMAND_SET_SHUFFLE_MODE)
            }
            .build()
    }

    private var shuffleModeEnabled = true

    override fun getShuffleModeEnabled(): Boolean {
        return shuffleModeEnabled
    }

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        this.shuffleModeEnabled = shuffleModeEnabled

        listeners.queueEvent(Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED) {
            it.onShuffleModeEnabledChanged(shuffleModeEnabled)
        }
        listeners.flushEvents()

        // TODO Toggle the shuffle mode, keep the current repeat mode, listen to the PendingResult
        // remoteMediaClient?.queueSetRepeatMode(MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE, null)
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
        override fun onSessionEnded(p0: CastSession, p1: Int) {
            remoteMediaClient = null
        }

        override fun onSessionEnding(p0: CastSession) {
        }

        override fun onSessionResumeFailed(p0: CastSession, p1: Int) {
        }

        override fun onSessionResumed(p0: CastSession, p1: Boolean) {
            remoteMediaClient = p0.remoteMediaClient
        }

        override fun onSessionResuming(p0: CastSession, p1: String) {
        }

        override fun onSessionStartFailed(p0: CastSession, p1: Int) {
        }

        override fun onSessionStarted(p0: CastSession, p1: String) {
            remoteMediaClient = p0.remoteMediaClient
        }

        override fun onSessionStarting(p0: CastSession) {
        }

        override fun onSessionSuspended(p0: CastSession, p1: Int) {
            remoteMediaClient = null
        }
    }
}
