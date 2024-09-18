/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import android.os.Handler
import androidx.annotation.VisibleForTesting
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.Clock
import androidx.media3.common.util.ListenerSet
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsCollector
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.analytics.metrics.MetricsCollector
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.extension.getPlaybackSpeed
import ch.srgssr.pillarbox.player.extension.setPreferredAudioRoleFlagsToAccessibilityManagerSettings
import ch.srgssr.pillarbox.player.extension.setSeekIncrements
import ch.srgssr.pillarbox.player.monitoring.LogcatMonitoringMessageHandler
import ch.srgssr.pillarbox.player.monitoring.Monitoring
import ch.srgssr.pillarbox.player.monitoring.MonitoringMessageHandler
import ch.srgssr.pillarbox.player.monitoring.NoOpMonitoringMessageHandler
import ch.srgssr.pillarbox.player.monitoring.RemoteMonitoringMessageHandler
import ch.srgssr.pillarbox.player.network.PillarboxHttpClient
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.tracker.AnalyticsMediaItemTracker
import ch.srgssr.pillarbox.player.tracker.CurrentMediaItemPillarboxDataTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerProvider
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository
import ch.srgssr.pillarbox.player.tracker.TimeRangeTracker
import ch.srgssr.pillarbox.player.utils.PillarboxEventLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import java.net.URL
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Pillarbox player
 *
 * @param context The context.
 * @param coroutineContext The [CoroutineContext].
 * @param exoPlayer The underlying player.
 * @param mediaItemTrackerProvider The [MediaItemTrackerProvider].
 * @param analyticsCollector The [PillarboxAnalyticsCollector].
 * @param metricsCollector The [MetricsCollector].
 * @param monitoringMessageHandler The class to handle each Monitoring message.
 */
class PillarboxExoPlayer internal constructor(
    context: Context,
    coroutineContext: CoroutineContext,
    private val exoPlayer: ExoPlayer,
    mediaItemTrackerProvider: MediaItemTrackerProvider,
    analyticsCollector: PillarboxAnalyticsCollector,
    private val metricsCollector: MetricsCollector = MetricsCollector(),
    monitoringMessageHandler: MonitoringMessageHandler,
) : PillarboxPlayer, ExoPlayer by exoPlayer {
    private val listeners = ListenerSet<PillarboxPlayer.Listener>(applicationLooper, clock) { listener, flags ->
        listener.onEvents(this, Player.Events(flags))
    }
    private val itemPillarboxDataTracker = CurrentMediaItemPillarboxDataTracker(this)
    private val analyticsTracker = AnalyticsMediaItemTracker(this, mediaItemTrackerProvider)
    internal val sessionManager = PlaybackSessionManager()
    private val window = Window()

    @VisibleForTesting
    internal val monitoring = Monitoring(
        context = context,
        player = this,
        metricsCollector = metricsCollector,
        messageHandler = monitoringMessageHandler,
        sessionManager = sessionManager,
        coroutineContext = coroutineContext,
    )

    override var smoothSeekingEnabled: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                if (!value) {
                    seekEnd()
                }
                clearSeeking()
                listeners.sendEvent(PillarboxPlayer.EVENT_SMOOTH_SEEKING_ENABLED_CHANGED) { listener ->
                    listener.onSmoothSeekingEnabledChanged(value)
                }
            }
        }
    private var pendingSeek: Long? = null
    private var isSeeking: Boolean = false

    /**
     * Enable or disable analytics tracking for the current [MediaItem].
     */
    override var trackingEnabled: Boolean
        set(value) {
            if (analyticsTracker.enabled != value) {
                analyticsTracker.enabled = value
                listeners.sendEvent(PillarboxPlayer.EVENT_TRACKING_ENABLED_CHANGED) { listener ->
                    listener.onTrackingEnabledChanged(value)
                }
            }
        }
        get() = analyticsTracker.enabled

    private val timeRangeTracker = TimeRangeTracker(
        this,
        object : TimeRangeTracker.Callback {
            override fun onBlockedTimeRange(blockedTimeRange: BlockedTimeRange) {
                listeners.sendEvent(PillarboxPlayer.EVENT_BLOCKED_TIME_RANGE_REACHED) { listener ->
                    listener.onBlockedTimeRangeReached(blockedTimeRange)
                }
                handleBlockedTimeRange(blockedTimeRange)
            }

            override fun onChapterChanged(chapter: Chapter?) {
                listeners.sendEvent(PillarboxPlayer.EVENT_CHAPTER_CHANGED) { listener ->
                    listener.onChapterChanged(chapter)
                }
            }

            override fun onCreditChanged(credit: Credit?) {
                listeners.sendEvent(PillarboxPlayer.EVENT_CREDIT_CHANGED) { listener ->
                    listener.onCreditChanged(credit)
                }
            }
        }
    )

    init {
        sessionManager.setPlayer(this)
        metricsCollector.setPlayer(this)
        addListener(analyticsCollector)
        exoPlayer.addListener(ComponentListener())
        itemPillarboxDataTracker.addCallback(timeRangeTracker)
        itemPillarboxDataTracker.addCallback(analyticsTracker)
        if (BuildConfig.DEBUG) {
            addAnalyticsListener(PillarboxEventLogger())
        }
    }

    constructor(
        context: Context,
        mediaSourceFactory: PillarboxMediaSourceFactory = PillarboxMediaSourceFactory(context),
        loadControl: LoadControl = PillarboxLoadControl(),
        mediaItemTrackerProvider: MediaItemTrackerProvider = MediaItemTrackerRepository(),
        seekIncrement: SeekIncrement = SeekIncrement(),
        maxSeekToPreviousPosition: Duration = DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION,
        coroutineContext: CoroutineContext = Dispatchers.Default,
        monitoringMessageHandler: MonitoringMessageHandler = if (BuildConfig.DEBUG) {
            RemoteMonitoringMessageHandler(
                httpClient = PillarboxHttpClient(),
                endpointUrl = URL("https://httpbin.org/post"),
                coroutineScope = CoroutineScope(coroutineContext),
            )
        } else {
            LogcatMonitoringMessageHandler()
        },
    ) : this(
        context = context,
        mediaSourceFactory = mediaSourceFactory,
        loadControl = loadControl,
        mediaItemTrackerProvider = mediaItemTrackerProvider,
        seekIncrement = seekIncrement,
        maxSeekToPreviousPosition = maxSeekToPreviousPosition,
        clock = Clock.DEFAULT,
        coroutineContext = coroutineContext,
        monitoringMessageHandler = monitoringMessageHandler,
    )

    @VisibleForTesting
    constructor(
        context: Context,
        mediaSourceFactory: PillarboxMediaSourceFactory = PillarboxMediaSourceFactory(context),
        loadControl: LoadControl = PillarboxLoadControl(),
        mediaItemTrackerProvider: MediaItemTrackerProvider = MediaItemTrackerRepository(),
        seekIncrement: SeekIncrement = SeekIncrement(),
        maxSeekToPreviousPosition: Duration = DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION,
        clock: Clock,
        coroutineContext: CoroutineContext,
        analyticsCollector: PillarboxAnalyticsCollector = PillarboxAnalyticsCollector(clock),
        metricsCollector: MetricsCollector = MetricsCollector(),
        monitoringMessageHandler: MonitoringMessageHandler = NoOpMonitoringMessageHandler,
    ) : this(
        context,
        coroutineContext,
        ExoPlayer.Builder(context)
            .setClock(clock)
            .setUsePlatformDiagnostics(false)
            .setSeekIncrements(seekIncrement)
            .setMaxSeekToPreviousPositionMs(maxSeekToPreviousPosition.inWholeMilliseconds)
            .setRenderersFactory(
                DefaultRenderersFactory(context)
                    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                    .setEnableDecoderFallback(true)
            )
            .setBandwidthMeter(DefaultBandwidthMeter.getSingletonInstance(context))
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(
                DefaultTrackSelector(
                    context,
                    TrackSelectionParameters.Builder(context)
                        .setPreferredAudioRoleFlagsToAccessibilityManagerSettings(context)
                        .build()
                )
            )
            .setAnalyticsCollector(analyticsCollector)
            .setDeviceVolumeControlEnabled(true) // allow player to control device volume
            .build(),
        mediaItemTrackerProvider = mediaItemTrackerProvider,
        analyticsCollector = analyticsCollector,
        metricsCollector = metricsCollector,
        monitoringMessageHandler = monitoringMessageHandler,
    )

    /**
     * Get current metrics
     * @return `null` if there is no current metrics.
     */
    fun getCurrentMetrics(): PlaybackMetrics? {
        return metricsCollector.getCurrentMetrics()
    }

    /**
     * Get metrics for item [index]
     *
     * @param index The index in the timeline.
     * @return `null` if there are no metrics.
     */
    fun getMetricsFor(index: Int): PlaybackMetrics? {
        if (currentTimeline.isEmpty) return null
        currentTimeline.getWindow(index, window)
        val periodUid = currentTimeline.getUidOfPeriod(window.firstPeriodIndex)
        return sessionManager.getSessionFromPeriodUid(periodUid)?.let { metricsCollector.getMetricsForSession(it) }
    }

    override fun addListener(listener: Player.Listener) {
        exoPlayer.addListener(listener)
        if (listener is PillarboxPlayer.Listener) {
            listeners.add(listener)
        }
    }

    override fun removeListener(listener: Player.Listener) {
        exoPlayer.removeListener(listener)
        if (listener is PillarboxPlayer.Listener) {
            listeners.remove(listener)
        }
    }

    private fun handleBlockedTimeRange(timeRange: BlockedTimeRange) {
        clearSeeking()
        exoPlayer.seekTo(timeRange.end + 1)
    }

    override fun seekTo(positionMs: Long) {
        if (!smoothSeekingEnabled) {
            exoPlayer.seekTo(positionMs)
            return
        }
        smoothSeekTo(positionMs)
    }

    private fun smoothSeekTo(positionMs: Long) {
        if (isSeeking) {
            pendingSeek = positionMs
            return
        }
        isSeeking = true
        exoPlayer.seekTo(positionMs)
    }

    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
        if (!smoothSeekingEnabled) {
            exoPlayer.seekTo(mediaItemIndex, positionMs)
            return
        }
        smoothSeekTo(mediaItemIndex, positionMs)
    }

    private fun smoothSeekTo(mediaItemIndex: Int, positionMs: Long) {
        if (mediaItemIndex != currentMediaItemIndex) {
            clearSeeking()
            exoPlayer.seekTo(mediaItemIndex, positionMs)
            return
        }
        if (isSeeking) {
            pendingSeek = positionMs
            return
        }
        exoPlayer.seekTo(mediaItemIndex, positionMs)
    }

    override fun seekToDefaultPosition() {
        clearSeeking()
        exoPlayer.seekToDefaultPosition()
    }

    override fun seekToDefaultPosition(mediaItemIndex: Int) {
        clearSeeking()
        exoPlayer.seekToDefaultPosition(mediaItemIndex)
    }

    override fun seekBack() {
        clearSeeking()
        exoPlayer.seekBack()
    }

    override fun seekForward() {
        clearSeeking()
        exoPlayer.seekForward()
    }

    override fun seekToNext() {
        clearSeeking()
        exoPlayer.seekToNext()
    }

    override fun seekToPrevious() {
        clearSeeking()
        exoPlayer.seekToPrevious()
    }

    override fun seekToNextMediaItem() {
        clearSeeking()
        exoPlayer.seekToNextMediaItem()
    }

    override fun seekToPreviousMediaItem() {
        clearSeeking()
        exoPlayer.seekToPreviousMediaItem()
    }

    /**
     * Releases the player.
     * This method must be called when the player is no longer required. The player must not be used after calling this method.
     *
     * Release call automatically [stop] if the player is not in [Player.STATE_IDLE].
     */
    override fun release() {
        clearSeeking()
        exoPlayer.release()
        listeners.release()
        itemPillarboxDataTracker.release()
    }

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        if (isPlaybackSpeedPossibleAtPosition(currentPosition, playbackParameters.speed, window)) {
            exoPlayer.playbackParameters = playbackParameters
        } else {
            exoPlayer.playbackParameters = playbackParameters.withSpeed(NormalSpeed)
        }
    }

    override fun setPlaybackSpeed(speed: Float) {
        playbackParameters = playbackParameters.withSpeed(speed)
    }

    private fun seekEnd() {
        isSeeking = false
        pendingSeek?.let { pendingPosition ->
            pendingSeek = null
            seekTo(pendingPosition)
        }
    }

    private fun clearSeeking() {
        isSeeking = false
        pendingSeek = null
    }

    private inner class ComponentListener : Player.Listener {
        private val window = Window()

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            clearSeeking()
        }

        override fun onRenderedFirstFrame() {
            seekEnd()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    if (isSeeking) {
                        seekEnd()
                    }
                }

                Player.STATE_IDLE, Player.STATE_ENDED -> {
                    clearSeeking()
                }

                Player.STATE_BUFFERING -> {
                    // Do nothing
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            clearSeeking()
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                setPlaybackSpeed(NormalSpeed)
                seekToDefaultPosition()
                prepare()
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (!player.isCurrentMediaItemLive || player.getPlaybackSpeed() == NormalSpeed) return
            if (!player.isCurrentMediaItemSeekable) {
                setPlaybackSpeed(NormalSpeed)
                return
            }
            player.currentTimeline.getWindow(currentMediaItemIndex, window)
            if (window.isAtDefaultPosition(currentPosition) && getPlaybackSpeed() > NormalSpeed) {
                exoPlayer.setPlaybackSpeed(NormalSpeed)
            }
        }
    }

    @Suppress("UndocumentedPublicClass")
    companion object {
        /**
         * A default maximum position for which a seek to previous will seek to the previous window.
         */
        val DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION = C.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION_MS.milliseconds
    }
}

/**
 * Return if the playback [speed] is possible at [position].
 * Always return true for none live content or if [Player.getCurrentTimeline] is empty.
 *
 * @param position The position to test the playback speed.
 * @param speed The playback speed
 * @param window optional window for performance purpose
 * @return true if the playback [speed] can be set at [position]
 */
fun Player.isPlaybackSpeedPossibleAtPosition(position: Long, speed: Float, window: Window = Window()): Boolean {
    if (currentTimeline.isEmpty || speed == NormalSpeed || !isCurrentMediaItemLive) {
        return true
    }
    currentTimeline.getWindow(currentMediaItemIndex, window)
    return window.isPlaybackSpeedPossibleAtPosition(position, speed)
}

internal fun Window.isPlaybackSpeedPossibleAtPosition(positionMs: Long, playbackSpeed: Float): Boolean {
    return when {
        !isLive || playbackSpeed == NormalSpeed -> true
        !isSeekable -> false
        isAtDefaultPosition(positionMs) && playbackSpeed > NormalSpeed -> false
        else -> true
    }
}

internal fun Window.isAtDefaultPosition(positionMs: Long): Boolean {
    return positionMs >= defaultPositionMs
}

private const val NormalSpeed = 1.0f

/**
 * Run the task in the same thread as [Player.getApplicationLooper] if it is necessary.
 *
 * @param task The task to run.
 */
fun Player.runOnApplicationLooper(task: () -> Unit) {
    if (applicationLooper.thread != Thread.currentThread()) {
        runBlocking(Handler(applicationLooper).asCoroutineDispatcher("exoplayer")) {
            task()
        }
    } else {
        task()
    }
}
