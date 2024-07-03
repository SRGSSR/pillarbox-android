/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager.Session

interface QoSEventsDispatcher {
    interface Listener {
        fun onSessionCreated(session: Session)

        fun onActiveSession(session: Session)

        fun onMediaStart(session: Session)

        fun onSeek(session: Session)

        fun onStall(session: Session)

        fun onSessionFinished(session: Session)

        fun onPlayerReleased()
    }

    fun registerPlayer(player: ExoPlayer)

    fun unregisterPlayer(player: ExoPlayer)

    fun addListener(listener: Listener)

    fun removeListener(listener: Listener)
}

class DummyEventsDispatcher : QoSEventsDispatcher {
    override fun registerPlayer(player: ExoPlayer) = Unit

    override fun unregisterPlayer(player: ExoPlayer) = Unit

    override fun addListener(listener: QoSEventsDispatcher.Listener) = Unit

    override fun removeListener(listener: QoSEventsDispatcher.Listener) = Unit
}
