/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.util.Log
import ch.srgssr.pillarbox.player.qos.models.QoSMessage

/**
 * QoS message handler
 */
interface QoSMessageHandler {
    /**
     * Send event
     *
     * @param event
     */
    fun sendEvent(event: QoSMessage)
}

/**
 * Dummy QoS handler
 */
object DummyQoSHandler : QoSMessageHandler {
    private const val TAG = "DummyQoSHandler"

    override fun sendEvent(event: QoSMessage) {
        Log.d(TAG, "sendEvent($event)")
    }
}
