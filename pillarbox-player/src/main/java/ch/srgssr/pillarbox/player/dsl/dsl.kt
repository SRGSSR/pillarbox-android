/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("Filename", "UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty")

package ch.srgssr.pillarbox.player.dsl

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.media3.common.C
import androidx.media3.common.util.Clock
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.player.BuildConfig
import ch.srgssr.pillarbox.player.PillarboxBandwidthMeter
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxExoPlayer.Companion.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION
import ch.srgssr.pillarbox.player.PillarboxLoadControl
import ch.srgssr.pillarbox.player.PillarboxRenderersFactory
import ch.srgssr.pillarbox.player.PillarboxTrackSelector
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsCollector
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.UrlAssetLoader
import ch.srgssr.pillarbox.player.monitoring.MonitoringMessageHandler
import ch.srgssr.pillarbox.player.monitoring.models.Message
import ch.srgssr.pillarbox.player.network.PillarboxHttpClient
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

fun PillarboxExoPlayer(
    context: Context,
    coroutineContext: CoroutineContext,
    monitoringMessageHandler: MonitoringMessageHandler,
    block: ExoPlayer.Builder.() -> Unit,
): PillarboxExoPlayer {
    return PillarboxExoPlayer(
        context = context,
        coroutineContext = coroutineContext,
        exoPlayer = ExoPlayer.Builder(context)
            .apply(block)
            .build(),
        monitoringMessageHandler = monitoringMessageHandler,
    )
}

@DslMarker
annotation class PillarboxDsl

@PillarboxDsl
interface MonitoringMessageHandlerFactory<Config> {
    fun createMessageHandler(config: Config): MonitoringMessageHandler
}

interface MonitoringMessageHandlerType<Config, Factory : MonitoringMessageHandlerFactory<Config>> {
    val messageHandlerFactory: Factory
}

object NoOp : MonitoringMessageHandlerType<Nothing, NoOp.Factory> {
    override val messageHandlerFactory = Factory

    object Factory : MonitoringMessageHandlerFactory<Nothing> {
        override fun createMessageHandler(config: Nothing): MonitoringMessageHandler {
            return MessageHandler
        }
    }

    internal object MessageHandler : MonitoringMessageHandler {
        override fun sendEvent(event: Message) {
        }
    }
}

object Logcat : MonitoringMessageHandlerType<Logcat.Config, Logcat.Factory> {
    override val messageHandlerFactory = Factory

    class Config(
        val tag: String = "MonitoringMessageHandler",
        val priority: Int = Log.DEBUG,
    )

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

object Remote : MonitoringMessageHandlerType<Remote.Config, Remote.Factory> {
    override val messageHandlerFactory = Factory

    class Config(
        val endpointUrl: URL,
        val httpClient: HttpClient? = null,
        val coroutineScope: CoroutineScope? = null,
    ) {
        constructor(
            endpointUrl: String,
            httpClient: HttpClient? = null,
            coroutineScope: CoroutineScope? = null,
        ) : this(
            endpointUrl = URL(endpointUrl),
            httpClient = httpClient,
            coroutineScope = coroutineScope,
        )
    }

    object Factory : MonitoringMessageHandlerFactory<Config> {
        override fun createMessageHandler(config: Config): MonitoringMessageHandler {
            return MessageHandler(
                httpClient = config.httpClient ?: PillarboxHttpClient(),
                endpointUrl = config.endpointUrl,
                coroutineScope = config.coroutineScope ?: CoroutineScope(Dispatchers.IO),
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

@PillarboxDsl
abstract class PlayerFactory {
    private val assetLoaders: MutableList<AssetLoader> = mutableListOf()
    private var clock: Clock = Clock.DEFAULT
    private var coroutineContext: CoroutineContext = Dispatchers.Default
    private var loadControl: LoadControl? = null
    private var maxSeekToPreviousPosition: Duration = DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION
    private var monitoring: MonitoringMessageHandler = NoOp.MessageHandler
    private var playbackLooper: Looper? = null
    private var seekBackwardIncrement: Duration = C.DEFAULT_SEEK_BACK_INCREMENT_MS.milliseconds
    private var seekForwardIncrement: Duration = C.DEFAULT_SEEK_FORWARD_INCREMENT_MS.milliseconds

    fun addAssetLoader(assetLoader: AssetLoader) {
        assetLoaders.add(assetLoader)
    }

    operator fun AssetLoader.unaryPlus() {
        addAssetLoader(this)
    }

    @VisibleForTesting
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun clock(clock: Clock) {
        this.clock = clock
    }

    fun coroutineContext(coroutineContext: CoroutineContext) {
        this.coroutineContext = coroutineContext
    }

    fun loadControl(loadControl: LoadControl) {
        this.loadControl = loadControl
    }

    fun maxSeekToPreviousPosition(maxSeekToPreviousPosition: Duration) {
        this.maxSeekToPreviousPosition = maxSeekToPreviousPosition
    }

    fun monitoring(@Suppress("UNUSED_PARAMETER") type: NoOp) {
        monitoring = NoOp.MessageHandler
    }

    fun monitoring(type: Logcat) {
        monitoring(type) {
            Logcat.Config()
        }
    }

    fun <Config, Factory : MonitoringMessageHandlerFactory<Config>> monitoring(
        type: MonitoringMessageHandlerType<Config, Factory>,
        configBuilder: () -> Config,
    ) {
        monitoring = type.messageHandlerFactory.createMessageHandler(configBuilder())
    }

    fun playbackLooper(playbackLooper: Looper) {
        this.playbackLooper = playbackLooper
    }

    fun seekBackwardIncrement(seekBackwardIncrement: Duration) {
        this.seekBackwardIncrement = seekBackwardIncrement
    }

    fun seekForwardIncrement(seekForwardIncrement: Duration) {
        this.seekForwardIncrement = seekForwardIncrement
    }

    fun create(context: Context): PillarboxExoPlayer {
        return PillarboxExoPlayer(
            context = context,
            coroutineContext = coroutineContext,
            exoPlayer = createExoPlayerBuilder(context).build(),
            monitoringMessageHandler = monitoring,
        )
    }

    @CallSuper
    protected open fun createExoPlayerBuilder(context: Context): ExoPlayer.Builder {
        require(seekBackwardIncrement > ZERO) { "Seek backward increment needs to be greater than zero" }
        require(seekForwardIncrement > ZERO) { "Seek forward increment needs to be greater than zero" }

        val mediaSourceFactory = PillarboxMediaSourceFactory(context)
        assetLoaders.forEach { assetLoader ->
            mediaSourceFactory.addAssetLoader(assetLoader)
        }

        return ExoPlayer.Builder(context)
            .setClock(clock)
            .setUsePlatformDiagnostics(false)
            .setSeekForwardIncrementMs(seekForwardIncrement.inWholeMilliseconds)
            .setSeekBackIncrementMs(seekBackwardIncrement.inWholeMilliseconds)
            .setMaxSeekToPreviousPositionMs(maxSeekToPreviousPosition.inWholeMilliseconds)
            .setRenderersFactory(PillarboxRenderersFactory(context))
            .setBandwidthMeter(PillarboxBandwidthMeter(context))
            .setLoadControl(loadControl ?: PillarboxLoadControl())
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(PillarboxTrackSelector(context))
            .setAnalyticsCollector(PillarboxAnalyticsCollector(clock))
            .setDeviceVolumeControlEnabled(true) // Allow the player to control the device volume
            .apply { playbackLooper?.let(::setPlaybackLooper) }
    }
}

interface PlayerConfig<T : PlayerFactory> {
    fun create(): T

    object Default : PlayerConfig<Default.DefaultPlayerFactory> {
        override fun create(): DefaultPlayerFactory {
            return DefaultPlayerFactory()
        }

        class DefaultPlayerFactory : PlayerFactory() {
            init {
                monitoring(NoOp)
            }
        }
    }

    object Sample : PlayerConfig<Sample.SamplePlayerFactory> {
        override fun create(): SamplePlayerFactory {
            return SamplePlayerFactory()
        }

        class SamplePlayerFactory : PlayerFactory() {
            init {
                monitoring(Remote) {
                    Remote.Config(
                        endpointUrl = if (BuildConfig.DEBUG) {
                            "https://dev.monitoring.pillarbox.ch/api/events"
                        } else {
                            "https://monitoring.pillarbox.ch/api/events"
                        },
                    )
                }

                seekBackwardIncrement(10.seconds)
                seekForwardIncrement(30.seconds)
            }

            override fun createExoPlayerBuilder(context: Context): ExoPlayer.Builder {
                // TODO
                // addAssetLoader(SRGAssetLoader(context))

                return super.createExoPlayerBuilder(context)
            }
        }
    }
}

fun pillarbox(context: Context, builder: PlayerConfig.Default.DefaultPlayerFactory.() -> Unit = {}): PillarboxExoPlayer {
    return pillarbox(context, PlayerConfig.Default, builder)
}

fun <T : PlayerFactory> pillarbox(context: Context, type: PlayerConfig<T>, builder: T.() -> Unit = {}): PillarboxExoPlayer {
    return type.create()
        .apply(builder)
        .create(context)
}

fun main(context: Context) {
    pillarbox(context)

    pillarbox(context) {
        addAssetLoader(UrlAssetLoader(DefaultMediaSourceFactory(context)))
        +UrlAssetLoader(DefaultMediaSourceFactory(context))

        loadControl(DefaultLoadControl())

        maxSeekToPreviousPosition(30.seconds)

        monitoring(NoOp)

        monitoring(Logcat)

        monitoring(Logcat) {
            Logcat.Config(
                tag = "Coucou",
                priority = Log.ERROR,
            )
        }

        monitoring(Remote) {
            Remote.Config(
                endpointUrl = URL("https://monitoring.pillarbox.ch/api/events"),
            )
        }

        playbackLooper(Looper.getMainLooper())

        seekBackwardIncrement(5.seconds)
        seekForwardIncrement(10.seconds)
    }

    pillarbox(context, PlayerConfig.Sample)
}
