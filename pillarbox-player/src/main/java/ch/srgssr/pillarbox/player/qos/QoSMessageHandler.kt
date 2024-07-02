/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

interface QoSMessageHandler {

    fun sendEvent(event: Any)
}

object DummyQoSHandler : QoSMessageHandler {
    override fun sendEvent(event: Any) {
        TODO("Not yet implemented")
    }
}
