/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.Clock
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsCollector
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.monitoring.Logcat
import ch.srgssr.pillarbox.player.monitoring.Logcat.config
import ch.srgssr.pillarbox.player.monitoring.MonitoringConfigFactory
import ch.srgssr.pillarbox.player.monitoring.MonitoringMessageHandler
import ch.srgssr.pillarbox.player.monitoring.MonitoringMessageHandlerFactory
import ch.srgssr.pillarbox.player.monitoring.MonitoringMessageHandlerType
import ch.srgssr.pillarbox.player.monitoring.NoOp
import ch.srgssr.pillarbox.player.monitoring.Remote
import ch.srgssr.pillarbox.player.monitoring.Remote.config
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

/**
 * Marker for Pillarbox's DSL.
 */
@DslMarker
annotation class PillarboxDsl

/**
 * Builder to create a new instance of [PillarboxExoPlayer].
 */
@PillarboxDsl
@Suppress("TooManyFunctions")
abstract class PillarboxBuilder {
    private val assetLoaders: MutableList<AssetLoader> = mutableListOf()
    private var clock: Clock = Clock.DEFAULT
    private var coroutineContext: CoroutineContext = Dispatchers.Default
    private var loadControl: LoadControl? = null
    private var maxSeekToPreviousPosition: Duration = C.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION_MS.milliseconds

    @VisibleForTesting
    internal var monitoring: MonitoringMessageHandler = NoOp()
    private var playbackLooper: Looper? = null
    private var seekBackwardIncrement: Duration = C.DEFAULT_SEEK_BACK_INCREMENT_MS.milliseconds
    private var seekForwardIncrement: Duration = C.DEFAULT_SEEK_FORWARD_INCREMENT_MS.milliseconds
    private var preloadConfiguration = ExoPlayer.PreloadConfiguration.DEFAULT

    /**
     * Add an [AssetLoader] to the [PillarboxExoPlayer].
     *
     * @param assetLoader The [assetLoader] to add.
     */
    fun addAssetLoader(assetLoader: AssetLoader) {
        assetLoaders.add(assetLoader)
    }

    /**
     * Add an [AssetLoader] to the [PillarboxExoPlayer].
     *
     * @receiver The [AssetLoader] to add.
     */
    operator fun AssetLoader.unaryPlus() {
        addAssetLoader(this)
    }

    /**
     * Set the internal [Clock] used by the player.
     *
     * @param clock The internal clock used by the player.
     */
    @VisibleForTesting
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun clock(clock: Clock) {
        this.clock = clock
    }

    /**
     * Set the coroutine context used by the player.
     *
     * @param coroutineContext The coroutine context used by the player.
     */
    fun coroutineContext(coroutineContext: CoroutineContext) {
        this.coroutineContext = coroutineContext
    }

    /**
     * Set the load control used by the player.
     *
     * @param loadControl The load control used by the player.
     */
    fun loadControl(loadControl: LoadControl) {
        this.loadControl = loadControl
    }

    /**
     * Set the [Player.getMaxSeekToPreviousPosition] value.
     *
     * @param maxSeekToPreviousPosition The [Player.getMaxSeekToPreviousPosition] value.
     */
    fun maxSeekToPreviousPosition(maxSeekToPreviousPosition: Duration) {
        this.maxSeekToPreviousPosition = maxSeekToPreviousPosition
    }

    /**
     * Disable the monitoring for this player
     */
    fun disableMonitoring() {
        monitoring = NoOp()
    }

    /**
     * Make the monitoring logs all events to Logcat, using the default config.
     *
     * @param type [Logcat].
     */
    fun monitoring(type: Logcat) {
        monitoring(type) {
            config()
        }
    }

    /**
     * Make the monitoring sends all events to a remote server.
     *
     * @param endpointUrl The endpoint receiving monitoring messages.
     * @param httpClient The [HttpClient] to use to send the events.
     * @param coroutineScope The scope used to send the monitoring message.
     */
    fun monitoring(
        endpointUrl: String,
        httpClient: HttpClient? = null,
        coroutineScope: CoroutineScope? = null,
    ) {
        monitoring(Remote) {
            config(endpointUrl = endpointUrl, httpClient = httpClient, coroutineScope = coroutineScope)
        }
    }

    /**
     * Configure the monitoring for this player.
     *
     * @param Config The type of the config to create.
     * @param Factory The type of the [MonitoringMessageHandlerFactory].
     * @param type The type of [MonitoringMessageHandler] to use.
     * @param createConfig The configuration builder to create the [MonitoringMessageHandler].
     */
    fun <Config, Factory : MonitoringMessageHandlerFactory<Config>> monitoring(
        type: MonitoringMessageHandlerType<Config, Factory>,
        createConfig: MonitoringConfigFactory<Config>.() -> Config,
    ) {
        monitoring = type(createConfig)
    }

    /**
     * Set the [Looper] to use for playback.
     *
     * @param playbackLooper The [Looper] used for playback.
     */
    fun playbackLooper(playbackLooper: Looper) {
        this.playbackLooper = playbackLooper
    }

    /**
     * Set the seek back increment duration.
     *
     * @param seekBackwardIncrement The seek back increment duration.
     */
    fun seekBackwardIncrement(seekBackwardIncrement: Duration) {
        this.seekBackwardIncrement = seekBackwardIncrement
    }

    /**
     * Set the seek forward increment duration.
     *
     * @param seekForwardIncrement The seek forward increment duration.
     */
    fun seekForwardIncrement(seekForwardIncrement: Duration) {
        this.seekForwardIncrement = seekForwardIncrement
    }

    /**
     *  Set the [ExoPlayer.PreloadConfiguration] used by the player.
     *
     * @param preloadConfiguration The [ExoPlayer.PreloadConfiguration].
     */
    fun preloadConfiguration(preloadConfiguration: ExoPlayer.PreloadConfiguration) {
        this.preloadConfiguration = preloadConfiguration
    }

    /**
     * Create a new instance of [PillarboxExoPlayer].
     *
     * @param context The [Context].
     *
     * @return A new instance of [PillarboxExoPlayer].
     */
    internal fun create(context: Context): PillarboxExoPlayer {
        return PillarboxExoPlayer(
            context = context,
            coroutineContext = coroutineContext,
            exoPlayer = createExoPlayerBuilder(context).build(),
            monitoringMessageHandler = monitoring,
        ).apply {
            preloadConfiguration = this@PillarboxBuilder.preloadConfiguration
        }
    }

    /**
     * Create a new instance of [ExoPlayer.Builder], used internally by [PillarboxExoPlayer].
     *
     * @param context The [Context].
     *
     * @return A new instance of [ExoPlayer.Builder].
     */
    @SuppressLint("VisibleForTests")
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

/**
 * Factory used to create instances of [PillarboxBuilder].
 *
 * @param Builder The type of [PillarboxBuilder] to create.
 */
interface PlayerConfig<Builder : PillarboxBuilder> {
    /**
     * Create a new instance of [Builder].
     */
    fun create(): Builder
}

/**
 * Default implementation used to create simple [PillarboxExoPlayer].
 */
object Default : PlayerConfig<Default.Builder> {
    override fun create(): Builder {
        return Builder()
    }

    /**
     * Default implementation used to create simple [PillarboxExoPlayer].
     */
    class Builder : PillarboxBuilder() {
        init {
            disableMonitoring()
        }
    }
}
