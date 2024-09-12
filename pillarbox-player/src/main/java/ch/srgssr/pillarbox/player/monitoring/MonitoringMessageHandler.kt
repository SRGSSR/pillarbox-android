/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring

import android.util.Log
import ch.srgssr.pillarbox.player.monitoring.models.Message
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URL

/**
 * Monitoring message handler
 */
interface MonitoringMessageHandler {
    /**
     * Send event
     *
     * @param event
     */
    fun sendEvent(event: Message)
}

/**
 * Monitoring message handler that does nothing.
 */
object NoOpMonitoringMessageHandler : MonitoringMessageHandler {
    override fun sendEvent(event: Message) = Unit
}

/**
 * Monitoring message handler that logs each event in Logcat.
 *
 * @param priority The priority of this message.
 * @param tag The tag to use to log the events in Logcat.
 */
class LogcatMonitoringMessageHandler(
    private val priority: Int = Log.DEBUG,
    private val tag: String = "LogcatMonitoringHandler",
) : MonitoringMessageHandler {
    override fun sendEvent(event: Message) {
        Log.println(priority, tag, "event=$event")
    }
}

/**
 * Monitoring message handler that posts each event to the [endpointUrl].
 *
 * @param httpClient The [HttpClient] to use to send the events.
 * @param endpointUrl The endpoint receiving monitoring messages.
 * @param coroutineScope The scope used to send the monitoring message.
 */
class RemoteMonitoringMessageHandler(
    private val httpClient: HttpClient,
    private val endpointUrl: URL,
    private val coroutineScope: CoroutineScope,
) : MonitoringMessageHandler {
    override fun sendEvent(event: Message) {
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
