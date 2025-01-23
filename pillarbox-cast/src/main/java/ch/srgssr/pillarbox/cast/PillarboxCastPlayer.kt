/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.ForwardingPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastContext

/**
 * A [PillarboxPlayer] implementation that forwards calls to a [CastPlayer].
 *
 * It disables smooth seeking and tracking capabilities as these are not supported or relevant in the context of Cast playback.
 *
 * @param castPlayer The underlying [CastPlayer] instance to which method calls will be forwarded.
 */
class PillarboxCastPlayer(private val castPlayer: CastPlayer) : PillarboxPlayer, ForwardingPlayer(castPlayer) {
    override var smoothSeekingEnabled: Boolean = false
        set(value) {
            field = false
        }

    override var trackingEnabled: Boolean = false
        set(value) {
            field = false
        }

    constructor(context: CastContext) : this(CastPlayer(context))

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
}
