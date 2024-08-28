/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import android.util.Log
import ch.srgssr.pillarbox.player.qos.models.QoSMessage
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URL

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
 * QoS message handler that does nothing.
 */
object NoOpQoSMessageHandler : QoSMessageHandler {
    override fun sendEvent(event: QoSMessage) = Unit
}

/**
 * QoS message handler that logs each event in Logcat.
 *
 * @param priority The priority of this message.
 * @param tag The tag to use to log the events in Logcat.
 */
class LogcatQoSMessageHandler(
    private val priority: Int = Log.DEBUG,
    private val tag: String = "LogcatQoSHandler",
) : QoSMessageHandler {
    override fun sendEvent(event: QoSMessage) {
        Log.println(priority, tag, "event=$event")
    }
}

/**
 * QoS message handler that posts each event to the [endpointUrl].
 *
 * @param httpClient The [HttpClient] to use to send the events.
 * @param endpointUrl The endpoint receiving QoS messages.
 * @param coroutineScope The scope used to send the QoS message.
 */
class RemoteQoSMessageHandler(
    private val httpClient: HttpClient,
    private val endpointUrl: URL,
    private val coroutineScope: CoroutineScope,
) : QoSMessageHandler {
    override fun sendEvent(event: QoSMessage) {
        coroutineScope.launch {
            runCatching {
                httpClient.post(endpointUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(event)
                }
            }
        }
    }
}
