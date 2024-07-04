/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.util.Log

interface QoSMessageHandler {
    fun sendEvent(event: QoSMessage)
}

object DummyQoSHandler : QoSMessageHandler {
    private const val TAG = "DummyQoSHandler"

    override fun sendEvent(event: QoSMessage) {
        Log.d(TAG, "sendEvent($event)")
    }
}
