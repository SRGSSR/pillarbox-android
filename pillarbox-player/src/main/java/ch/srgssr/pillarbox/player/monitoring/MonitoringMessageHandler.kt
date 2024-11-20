/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring

import android.util.Log
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.monitoring.models.Message
import ch.srgssr.pillarbox.player.network.PillarboxHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
 * Factory used to create instances of [MonitoringMessageHandler].
 *
 * @param Config The config used to create a new [MonitoringMessageHandler].
 */
interface MonitoringMessageHandlerFactory<Config> {
    /**
     * Create a new instance of [MonitoringMessageHandler] using the provided [config].
     *
     * @param config The config used to create a new [MonitoringMessageHandler].
     */
    fun createMessageHandler(config: Config): MonitoringMessageHandler
}

/**
 * Receiver for creating [Config] instances of a specific [type][MonitoringMessageHandlerType].
 *
 * @param Config The config used to create a new [MonitoringMessageHandler].
 */
@PillarboxDsl
class MonitoringConfigFactory<Config> internal constructor()

/**
 * Represents a specific type of [MonitoringMessageHandler].
 *
 * @param Config The config used to create a new [MonitoringMessageHandler].
 * @param Factory The factory used to create a new [MonitoringMessageHandler].
 */
abstract class MonitoringMessageHandlerType<Config, Factory : MonitoringMessageHandlerFactory<Config>> {
    protected abstract val messageHandlerFactory: Factory

    /**
     * Helper method to create a new [MonitoringMessageHandler].
     *
     * @param createConfig The lambda used to create the [Config] for the desired [MonitoringMessageHandler].
     */
    operator fun invoke(createConfig: MonitoringConfigFactory<Config>.() -> Config): MonitoringMessageHandler {
        val config = MonitoringConfigFactory<Config>().createConfig()

        return messageHandlerFactory.createMessageHandler(config)
    }
}

/**
 * Monitoring message handler that does nothing.
 */
object NoOp : MonitoringMessageHandlerType<Nothing, NoOp.Factory>() {
    override val messageHandlerFactory = Factory

    /**
     * Returns the [MonitoringMessageHandler] instance.
     */
    operator fun invoke(): MonitoringMessageHandler {
        return MessageHandler
    }

    /**
     * Factory for creating new [NoOp] handler type.
     */
    object Factory : MonitoringMessageHandlerFactory<Nothing> {
        override fun createMessageHandler(config: Nothing): MonitoringMessageHandler {
            return MessageHandler
        }
    }

    private object MessageHandler : MonitoringMessageHandler {
        override fun sendEvent(event: Message) = Unit
    }
}

/**
 * Monitoring message handler that logs each event in Logcat.
 */
object Logcat : MonitoringMessageHandlerType<Logcat.Config, Logcat.Factory>() {
    override val messageHandlerFactory = Factory

    /**
     * Config class for the [Logcat] handler type.
     *
     * @property tag The tag to use to log the events in Logcat.
     * @property priority The priority of this message.
     */
    class Config internal constructor(
        val tag: String,
        val priority: Int,
    )

    /**
     * Helper method to create a new [Config] instance.
     */
    @Suppress("UnusedReceiverParameter")
    fun MonitoringConfigFactory<Config>.config(
        tag: String = "MonitoringMessageHandler",
        priority: Int = Log.DEBUG,
    ): Config {
        return Config(
            tag = tag,
            priority = priority,
        )
    }

    /**
     * Factory for creating new [Logcat] handler type.
     */
    object Factory : MonitoringMessageHandlerFactory<Config> {
        override fun createMessageHandler(config: Config): MonitoringMessageHandler {
            return MessageHandler(
                priority = config.priority,
                tag = config.tag,
            )
        }
    }

    private class MessageHandler(
        private val priority: Int,
        private val tag: String,
    ) : MonitoringMessageHandler {
        override fun sendEvent(event: Message) {
            Log.println(priority, tag, "event=$event")
        }
    }
}

/**
 * Monitoring message handler that sends each event to a remote server.
 */
object Remote : MonitoringMessageHandlerType<Remote.Config, Remote.Factory>() {
    override val messageHandlerFactory = Factory

    /**
     * Config class for the [Remote] handler type.
     *
     * @property endpointUrl The endpoint receiving monitoring messages.
     * @property httpClient The [HttpClient] to use to send the events.
     * @property coroutineScope The scope used to send the monitoring message.
     */
    class Config internal constructor(
        val endpointUrl: URL,
        val httpClient: HttpClient,
        val coroutineScope: CoroutineScope,
    )

    /**
     * Helper method to create a new [Config] instance.
     */
    @Suppress("UnusedReceiverParameter")
    fun MonitoringConfigFactory<Config>.config(
        endpointUrl: String,
        httpClient: HttpClient? = null,
        coroutineScope: CoroutineScope? = null,
    ): Config {
        return Config(
            endpointUrl = URL(endpointUrl),
            httpClient = httpClient ?: PillarboxHttpClient(),
            coroutineScope = coroutineScope ?: CoroutineScope(Dispatchers.IO),
        )
    }

    /**
     * Factory for creating new [Remote] handler type.
     */
    object Factory : MonitoringMessageHandlerFactory<Config> {
        override fun createMessageHandler(config: Config): MonitoringMessageHandler {
            return MessageHandler(
                httpClient = config.httpClient,
                endpointUrl = config.endpointUrl,
                coroutineScope = config.coroutineScope,
            )
        }
    }

    private class MessageHandler(
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
}
