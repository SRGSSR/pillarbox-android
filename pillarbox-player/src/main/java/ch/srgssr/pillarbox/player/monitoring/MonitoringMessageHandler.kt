/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring

import android.util.Log
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.monitoring.models.Message
import ch.srgssr.pillarbox.player.network.RequestSender.send
import ch.srgssr.pillarbox.player.network.RequestSender.toJsonRequestBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import java.net.URL

/**
 * Interface for handling monitoring messages.
 */
interface MonitoringMessageHandler {
    /**
     * Sends a monitoring event.
     *
     * @param event The event to be sent.
     */
    fun sendEvent(event: Message)
}

/**
 * A factory interface responsible for creating instances of [MonitoringMessageHandler].
 *
 * @param Config The type of configuration object used to initialize a new [MonitoringMessageHandler].
 */
interface MonitoringMessageHandlerFactory<Config> {
    /**
     * Creates a new instance of [MonitoringMessageHandler] configured with the provided [config].
     *
     * @param config The configuration used to initialize the [MonitoringMessageHandler].
     * @return A new instance of [MonitoringMessageHandler] configured according to the provided [config].
     */
    fun createMessageHandler(config: Config): MonitoringMessageHandler
}

/**
 * A factory class responsible for creating [Config] instances.
 *
 * @param Config The type of configuration object used to initialize a new [MonitoringMessageHandler].
 */
@PillarboxDsl
class MonitoringConfigFactory<Config> internal constructor()

/**
 * Represents a specific type of [MonitoringMessageHandler].
 *
 * @param Config The type of configuration used to create a new [MonitoringMessageHandler].
 * @param Factory The type of factory responsible to create a new [MonitoringMessageHandler].
 */
abstract class MonitoringMessageHandlerType<Config, Factory : MonitoringMessageHandlerFactory<Config>> {
    protected abstract val messageHandlerFactory: Factory

    /**
     * Creates a new [MonitoringMessageHandler] using the provided configuration.
     *
     * @param createConfig A lambda that returns a [Config] object.
     * @return A new instance of [MonitoringMessageHandler] configured according to the provided [createConfig] lambda.
     */
    operator fun invoke(createConfig: MonitoringConfigFactory<Config>.() -> Config): MonitoringMessageHandler {
        val config = MonitoringConfigFactory<Config>().createConfig()

        return messageHandlerFactory.createMessageHandler(config)
    }
}

/**
 * A monitoring message handler that skips every message.
 */
object NoOp : MonitoringMessageHandlerType<Nothing, NoOp.Factory>() {
    override val messageHandlerFactory = Factory

    /**
     * Returns the [MonitoringMessageHandler] instance.
     *
     * @return The [MonitoringMessageHandler] instance.
     */
    operator fun invoke(): MonitoringMessageHandler {
        return MessageHandler
    }

    /**
     * A factory for creating instances of the [NoOp] message handler.
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
 * A monitoring message handler that logs each event to Logcat.
 */
object Logcat : MonitoringMessageHandlerType<Logcat.Config, Logcat.Factory>() {
    override val messageHandlerFactory = Factory

    /**
     * Configuration class for the [Logcat] handler type.
     *
     * @property tag The tag used to identify log messages in Logcat.
     * @property priority The priority level of the log messages.
     */
    class Config internal constructor(
        val tag: String,
        val priority: Int,
    )

    /**
     * Creates a new [Config] instance for the [MonitoringConfigFactory].
     *
     * @param tag The tag used to identify log messages in Logcat.
     * @param priority The priority level of the log messages.
     * @return A new [Config] instance with the specified configuration.
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
     * A factory for creating instances of the [Logcat] message handler.
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
 * A monitoring message handler that sends each event to a remote server.
 */
object Remote : MonitoringMessageHandlerType<Remote.Config, Remote.Factory>() {
    override val messageHandlerFactory = Factory

    /**
     * Configuration class for the [Remote] handler type.
     *
     * @property endpointUrl The URL of the endpoint responsible for receiving monitoring messages.
     * @property coroutineScope The [CoroutineScope] which manages the coroutine responsible for sending monitoring messages.
     */
    class Config internal constructor(
        val endpointUrl: URL,
        val coroutineScope: CoroutineScope,
    )

    /**
     * Creates a new [Config] instance for the [MonitoringConfigFactory].
     *
     * @param endpointUrl The URL of the endpoint responsible for receiving monitoring messages.
     * @param coroutineScope The [CoroutineScope] which manages the coroutine responsible for sending monitoring messages.
     *
     * @return A new [Config] instance with the specified configuration.
     */
    @Suppress("UnusedReceiverParameter")
    fun MonitoringConfigFactory<Config>.config(
        endpointUrl: String,
        coroutineScope: CoroutineScope? = null,
    ): Config {
        return Config(
            endpointUrl = URL(endpointUrl),
            coroutineScope = coroutineScope ?: CoroutineScope(Dispatchers.IO),
        )
    }

    /**
     * A factory for creating instances of the [Remote] message handler.
     */
    object Factory : MonitoringMessageHandlerFactory<Config> {
        override fun createMessageHandler(config: Config): MonitoringMessageHandler {
            return MessageHandler(
                endpointUrl = config.endpointUrl,
                coroutineScope = config.coroutineScope,
            )
        }
    }

    private class MessageHandler(
        private val endpointUrl: URL,
        private val coroutineScope: CoroutineScope,
    ) : MonitoringMessageHandler {
        override fun sendEvent(event: Message) {
            coroutineScope.launch {
                Request.Builder()
                    .url(endpointUrl)
                    .post(event.toJsonRequestBody())
                    .build()
                    .send<Unit>()
            }
        }
    }
}
