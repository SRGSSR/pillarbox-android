/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("Filename", "UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty")

package ch.srgssr.pillarbox.player.dsl

import android.content.Context
import android.os.Looper
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
import ch.srgssr.pillarbox.player.monitoring.NoOpMonitoringMessageHandler
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import kotlinx.coroutines.Dispatchers
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
interface MessageHandlerFactory {
    fun create(): MonitoringMessageHandler
}

interface MessageHandlerConfig<T : MessageHandlerFactory> {
    fun create(): T

    object NoOp : MessageHandlerConfig<NoOp.NoOpMessageHandlerFactory> {
        override fun create(): NoOpMessageHandlerFactory {
            return NoOpMessageHandlerFactory()
        }

        class NoOpMessageHandlerFactory : MessageHandlerFactory {
            override fun create(): NoOpMonitoringMessageHandler {
                return NoOpMonitoringMessageHandler
            }
        }
    }

    object Http : MessageHandlerConfig<Http.HttpMessageHandlerFactory> {
        override fun create(): HttpMessageHandlerFactory {
            return HttpMessageHandlerFactory()
        }

        class HttpMessageHandlerFactory : MessageHandlerFactory {
            var url: String = ""

            override fun create(): NoOpMonitoringMessageHandler {
                return NoOpMonitoringMessageHandler
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
    private var monitoring: MonitoringMessageHandler = NoOpMonitoringMessageHandler
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

    fun monitoring(type: MessageHandlerConfig.NoOp) {
        monitoring(type) {}
    }

    fun <T : MessageHandlerFactory> monitoring(type: MessageHandlerConfig<T>, builder: T.() -> Unit) {
        monitoring = type.create().apply(builder).create()
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
                monitoring(MessageHandlerConfig.NoOp)
            }
        }
    }

    object Sample : PlayerConfig<Sample.SamplePlayerFactory> {
        override fun create(): SamplePlayerFactory {
            return SamplePlayerFactory()
        }

        class SamplePlayerFactory : PlayerFactory() {
            init {
                monitoring(MessageHandlerConfig.Http) {
                    url(if (BuildConfig.DEBUG) "https://dev.monitoring.pillarbox.ch/api/events" else "https://monitoring.pillarbox.ch/api/events")
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

        monitoring(MessageHandlerConfig.Http) {
            url = "https://monitoring.pillarbox.ch/api/events"
        }

        playbackLooper(Looper.getMainLooper())

        seekBackwardIncrement(5.seconds)
        seekForwardIncrement(10.seconds)
    }

    pillarbox(context, PlayerConfig.Sample)
}
