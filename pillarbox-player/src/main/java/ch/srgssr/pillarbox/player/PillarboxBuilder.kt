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
 * Marks a class or function as part of the Pillarbox DSL (Domain Specific Language).
 *
 * This annotation serves as a marker for the Kotlin compiler, enabling DSL-specific features like type-safe builders and improved code completion.
 * Applying this annotation to a class or function indicates that it's intended to be used within the context of the Pillarbox DSL.
 *
 * This annotation is primarily intended for internal use within the Pillarbox library.
 */
@DslMarker
annotation class PillarboxDsl

/**
 * A builder class for creating instances of [PillarboxExoPlayer].
 *
 * This builder provides a fluent API for configuring various aspects of the player, such as asset loaders, coroutine context, seek increments, ...
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
     * Registers a custom [AssetLoader] with the [PillarboxExoPlayer].
     *
     * @param assetLoader The [AssetLoader] to add.
     */
    fun addAssetLoader(assetLoader: AssetLoader) {
        assetLoaders.add(assetLoader)
    }

    /**
     * Registers a custom [AssetLoader] with the [PillarboxExoPlayer].
     *
     * @receiver The [AssetLoader] to add.
     */
    operator fun AssetLoader.unaryPlus() {
        addAssetLoader(this)
    }

    /**
     * Sets the internal [Clock] used by the player.
     *
     * **Note:** this function is intended for internal use and should not be called by applications.
     *
     * @param clock The [Clock] instance to be used by the player.
     */
    @VisibleForTesting
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun clock(clock: Clock) {
        this.clock = clock
    }

    /**
     * Sets the [CoroutineContext] used by the player.
     *
     * @param coroutineContext The [CoroutineContext] to be used by the player.
     */
    fun coroutineContext(coroutineContext: CoroutineContext) {
        this.coroutineContext = coroutineContext
    }

    /**
     * Sets the [LoadControl] used by the player.
     *
     * @param loadControl The [LoadControl] to be used by the player.
     */
    fun loadControl(loadControl: LoadControl) {
        this.loadControl = loadControl
    }

    /**
     * Sets the maximum duration the player can seek backward when using [Player.seekToPrevious].
     *
     * @param maxSeekToPreviousPosition The maximum duration to seek backward.
     */
    fun maxSeekToPreviousPosition(maxSeekToPreviousPosition: Duration) {
        this.maxSeekToPreviousPosition = maxSeekToPreviousPosition
    }

    /**
     * Disables the monitoring for this player.
     */
    fun disableMonitoring() {
        monitoring = NoOp()
    }

    /**
     * Logs all monitoring events to Logcat.
     *
     * @param type [Logcat].
     */
    fun monitoring(type: Logcat) {
        monitoring(type) {
            config()
        }
    }

    /**
     * Configures the monitoring to send all events to a remote server.
     *
     * @param endpointUrl The URL of the endpoint responsible for receiving monitoring messages.
     * @param httpClient The [HttpClient] instance used for transmitting events to the endpoint.
     * @param coroutineScope The [CoroutineScope] which manages the coroutine responsible for sending monitoring messages.
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
     * Configures monitoring for this player.
     *
     * @param Config The type of the configuration object used to setup the monitoring handler.
     * @param Factory The type of the [MonitoringMessageHandlerFactory] used to create the monitoring handler.
     * @param type The type of [MonitoringMessageHandler] to create.
     * @param createConfig A lambda that returns a configuration of type [Config].
     */
    fun <Config, Factory : MonitoringMessageHandlerFactory<Config>> monitoring(
        type: MonitoringMessageHandlerType<Config, Factory>,
        createConfig: MonitoringConfigFactory<Config>.() -> Config,
    ) {
        monitoring = type(createConfig)
    }

    /**
     * Sets the [Looper] used by the player.
     *
     * @param playbackLooper The [Looper] to be used by the player.
     */
    fun playbackLooper(playbackLooper: Looper) {
        this.playbackLooper = playbackLooper
    }

    /**
     * Sets the duration by which the player seeks backward when performing a "seek backward" operation.
     *
     * @param seekBackwardIncrement The duration to seek backward by.
     */
    fun seekBackwardIncrement(seekBackwardIncrement: Duration) {
        this.seekBackwardIncrement = seekBackwardIncrement
    }

    /**
     * Sets the duration by which the player seeks forward when performing a "seek forward" action.
     *
     * @param seekForwardIncrement The duration to seek forward by.
     */
    fun seekForwardIncrement(seekForwardIncrement: Duration) {
        this.seekForwardIncrement = seekForwardIncrement
    }

    /**
     * Sets the [ExoPlayer.PreloadConfiguration] used by the player.
     *
     * @param preloadConfiguration The [ExoPlayer.PreloadConfiguration] to be used by the player.
     */
    fun preloadConfiguration(preloadConfiguration: ExoPlayer.PreloadConfiguration) {
        this.preloadConfiguration = preloadConfiguration
    }

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
     * Creates the instance of [ExoPlayer.Builder], that will be used internally by [PillarboxExoPlayer].
     *
     * Subclasses can override this method to customize the [ExoPlayer.Builder] further, but they **MUST** ensure to call the super implementation.
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
 * Defines a factory for creating instances of [PillarboxBuilder].
 *
 * @param Builder The type of [PillarboxBuilder] that this factory creates.
 */
interface PlayerConfig<Builder : PillarboxBuilder> {
    /**
     * Creates a new instance of the [Builder] class.
     *
     * @return A new instance of the [Builder].
     */
    fun create(): Builder
}

/**
 * Default configuration for creating a [PillarboxExoPlayer], which closely matches an [ExoPlayer].
 */
object Default : PlayerConfig<Default.Builder> {
    override fun create(): Builder {
        return Builder()
    }

    /**
     * A builder class for creating and configuring a [PillarboxExoPlayer].
     */
    class Builder : PillarboxBuilder() {
        init {
            disableMonitoring()
        }
    }
}
