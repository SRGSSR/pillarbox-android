/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.annotation.VisibleForTesting
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
import ch.srgssr.pillarbox.player.qos.DummyQoSHandler
import ch.srgssr.pillarbox.player.qos.PillarboxEventsDispatcher
import ch.srgssr.pillarbox.player.qos.QoSCoordinator
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.tracker.AnalyticsMediaItemTracker
import ch.srgssr.pillarbox.player.tracker.CurrentMediaItemPillarboxDataTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerProvider
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository
import ch.srgssr.pillarbox.player.tracker.TimeRangeTracker
import ch.srgssr.pillarbox.player.utils.PillarboxEventLogger
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Pillarbox player
 *
 * @param context The context.
 * @param coroutineContext The [CoroutineContext].
 * @param exoPlayer The underlying player.
 * @param mediaItemTrackerProvider The [MediaItemTrackerProvider].
 * @param analyticsCollector The [PillarboxAnalyticsCollector].
 * @param metricsCollector The [MetricsCollector].
 */
class PillarboxExoPlayer internal constructor(
    context: Context,
    coroutineContext: CoroutineContext,
    private val exoPlayer: ExoPlayer,
    mediaItemTrackerProvider: MediaItemTrackerProvider,
    analyticsCollector: PillarboxAnalyticsCollector,
    private val metricsCollector: MetricsCollector = MetricsCollector(),
) : PillarboxPlayer, ExoPlayer by exoPlayer {
    private val listeners = ListenerSet<PillarboxPlayer.Listener>(applicationLooper, clock) { listener, flags ->
        listener.onEvents(this, Player.Events(flags))
    }
    private val itemPillarboxDataTracker = CurrentMediaItemPillarboxDataTracker(this)
    private val analyticsTracker = AnalyticsMediaItemTracker(this, mediaItemTrackerProvider)
    internal val sessionManager = PlaybackSessionManager()
    private val window = Window()
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
        QoSCoordinator(
            context = context,
            player = this,
            eventsDispatcher = PillarboxEventsDispatcher(sessionManager),
            metricsCollector = metricsCollector,
            messageHandler = DummyQoSHandler,
            sessionManager = sessionManager,
            coroutineContext = coroutineContext,
        )

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
        seekIncrement: SeekIncrement = SeekIncrement()
    ) : this(
        context = context,
        mediaSourceFactory = mediaSourceFactory,
        loadControl = loadControl,
        mediaItemTrackerProvider = mediaItemTrackerProvider,
        seekIncrement = seekIncrement,
        clock = Clock.DEFAULT,
        coroutineContext = Dispatchers.Default,
    )

    @VisibleForTesting
    constructor(
        context: Context,
        mediaSourceFactory: PillarboxMediaSourceFactory = PillarboxMediaSourceFactory(context),
        loadControl: LoadControl = PillarboxLoadControl(),
        mediaItemTrackerProvider: MediaItemTrackerProvider = MediaItemTrackerRepository(),
        seekIncrement: SeekIncrement = SeekIncrement(),
        clock: Clock,
        coroutineContext: CoroutineContext,
        analyticsCollector: PillarboxAnalyticsCollector = PillarboxAnalyticsCollector(clock),
        metricsCollector: MetricsCollector = MetricsCollector()
    ) : this(
        context,
        coroutineContext,
        ExoPlayer.Builder(context)
            .setClock(clock)
            .setUsePlatformDiagnostics(false)
            .setSeekIncrements(seekIncrement)
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

    override fun setMediaItem(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem.clearTag())
    }

    override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) {
        exoPlayer.setMediaItem(mediaItem.clearTag(), resetPosition)
    }

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
        exoPlayer.setMediaItem(mediaItem.clearTag(), startPositionMs)
    }

    override fun setMediaItems(mediaItems: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItems.map { it.clearTag() })
    }

    override fun setMediaItems(mediaItems: List<MediaItem>, resetPosition: Boolean) {
        exoPlayer.setMediaItems(mediaItems.map { it.clearTag() }, resetPosition)
    }

    override fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int, startPositionMs: Long) {
        exoPlayer.setMediaItems(mediaItems.map { it.clearTag() }, startIndex, startPositionMs)
    }

    override fun addMediaItem(mediaItem: MediaItem) {
        exoPlayer.addMediaItem(mediaItem.clearTag())
    }

    override fun addMediaItem(index: Int, mediaItem: MediaItem) {
        exoPlayer.addMediaItem(index, mediaItem.clearTag())
    }

    override fun addMediaItems(mediaItems: List<MediaItem>) {
        exoPlayer.addMediaItems(mediaItems.map { it.clearTag() })
    }

    override fun addMediaItems(index: Int, mediaItems: List<MediaItem>) {
        exoPlayer.addMediaItems(index, mediaItems.map { it.clearTag() })
    }

    override fun replaceMediaItem(index: Int, mediaItem: MediaItem) {
        exoPlayer.replaceMediaItem(index, mediaItem.clearTag())
    }

    override fun replaceMediaItems(fromIndex: Int, toIndex: Int, mediaItems: List<MediaItem>) {
        exoPlayer.replaceMediaItems(fromIndex, toIndex, mediaItems.map { it.clearTag() })
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
        if (playbackState != Player.STATE_IDLE) {
            stop()
        }
        listeners.release()
        exoPlayer.release()
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
        !isLive() || playbackSpeed == NormalSpeed -> true
        !isSeekable -> false
        isAtDefaultPosition(positionMs) && playbackSpeed > NormalSpeed -> false
        else -> true
    }
}

internal fun Window.isAtDefaultPosition(positionMs: Long): Boolean {
    return positionMs >= defaultPositionMs
}

private const val NormalSpeed = 1.0f

private fun MediaItem.clearTag() = this.buildUpon().setTag(null).build()
