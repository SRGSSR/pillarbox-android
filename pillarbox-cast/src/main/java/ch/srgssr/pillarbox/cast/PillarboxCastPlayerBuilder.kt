/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.content.Context
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.PillarboxDsl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A builder class for creating instances of [PillarboxCastPlayer].
 *
 * This builder provides a fluent API for configuring various aspects of the player, such as seek increments, item converters, ...
 */
@PillarboxDsl
abstract class PillarboxCastPlayerBuilder {
    private var mediaItemConverter: MediaItemConverter = DefaultMediaItemConverter()
    private var seekBackIncrement: Duration = C.DEFAULT_SEEK_BACK_INCREMENT_MS.milliseconds
    private var seekForwardIncrement: Duration = C.DEFAULT_SEEK_FORWARD_INCREMENT_MS.milliseconds
    private var maxSeekToPreviousPosition: Duration = C.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION_MS.milliseconds
    private var trackSelector: CastTrackSelector = DefaultCastTrackSelector
    private var onCastSessionAvailable: (PillarboxCastPlayer.() -> Unit)? = null
    private var onCastSessionUnavailable: (PillarboxCastPlayer.() -> Unit)? = null
    private var tracksConverter: TracksConverter = DefaultTracksConverter()

    /**
     * @param seekBackIncrement The [PillarboxCastPlayer.seekBack] increment.
     */
    fun seekBackIncrement(seekBackIncrement: Duration) {
        check(seekBackIncrement > Duration.ZERO)
        this.seekBackIncrement = seekBackIncrement
    }

    /**
     * @param seekForwardIncrement The [PillarboxCastPlayer.seekForward] increment.
     */
    fun seekForwardIncrement(seekForwardIncrement: Duration) {
        check(seekForwardIncrement > Duration.ZERO)
        this.seekForwardIncrement = seekForwardIncrement
    }

    /**
     * @param maxSeekToPreviousPosition The maximum position for which [PillarboxCastPlayer.seekToPrevious] seeks to the previous [MediaItem].
     */
    fun maxSeekToPreviousPosition(maxSeekToPreviousPosition: Duration) {
        check(maxSeekToPreviousPosition >= Duration.ZERO)
        this.maxSeekToPreviousPosition = maxSeekToPreviousPosition
    }

    /**
     * Track selector
     *
     * @param trackSelector The [CastTrackSelector] to use.
     */
    fun trackSelector(trackSelector: CastTrackSelector) {
        this.trackSelector = trackSelector
    }

    /**
     * On cast session available
     *
     * @param onCastSessionAvailable The method to invoke when [SessionAvailabilityListener.onCastSessionAvailable] is called.
     */
    fun onCastSessionAvailable(onCastSessionAvailable: PillarboxCastPlayer.() -> Unit) {
        this.onCastSessionAvailable = onCastSessionAvailable
    }

    /**
     * On cast session unavailable
     *
     * @param onCastSessionUnavailable The method to invoke when [SessionAvailabilityListener.onCastSessionUnavailable] is called.
     */
    fun onCastSessionUnavailable(onCastSessionUnavailable: PillarboxCastPlayer.() -> Unit) {
        this.onCastSessionUnavailable = onCastSessionUnavailable
    }

    /**
     * Media item converter
     *
     * @param mediaItemConverter The [MediaItemConverter] to use.
     */
    fun mediaItemConverter(mediaItemConverter: MediaItemConverter) {
        this.mediaItemConverter = mediaItemConverter
    }

    /**
     * Tracks converter
     *
     * @param tracksConverter The [TracksConverter] to use.
     */
    fun tracksConverter(tracksConverter: TracksConverter) {
        this.tracksConverter = tracksConverter
    }

    internal fun create(context: Context): PillarboxCastPlayer {
        return PillarboxCastPlayer(
            context = context,
            castContext = context.getCastContext(),
            mediaItemConverter = mediaItemConverter,
            seekBackIncrementMs = seekBackIncrement.inWholeMilliseconds,
            seekForwardIncrementMs = seekForwardIncrement.inWholeMilliseconds,
            maxSeekToPreviousPositionMs = maxSeekToPreviousPosition.inWholeMilliseconds,
            trackSelector = trackSelector,
            tracksConverter = tracksConverter,
        ).apply {
            if (onCastSessionAvailable == null && onCastSessionUnavailable == null) return@apply
            setSessionAvailabilityListener(object : SessionAvailabilityListener {
                override fun onCastSessionAvailable() {
                    onCastSessionAvailable?.invoke(this@apply)
                }

                override fun onCastSessionUnavailable() {
                    onCastSessionUnavailable?.invoke(this@apply)
                }
            })
        }
    }
}

/**
 * Defines a factory for creating instances of [PillarboxCastPlayerBuilder].
 *
 * @param Builder The type of [PillarboxCastPlayerBuilder] that this factory creates.
 */
interface CastPlayerConfig<Builder : PillarboxCastPlayerBuilder> {
    /**
     * Creates a new instance of the [Builder] class.
     *
     * @return A new instance of the [Builder].
     */
    fun create(): Builder
}

/**
 * Default configuration for creating a [PillarboxCastPlayer].
 */
object Default : CastPlayerConfig<Default.Builder> {
    override fun create(): Builder {
        return Builder
    }

    /**
     * A builder class for creating and configuring a [PillarboxCastPlayer].
     */
    object Builder : PillarboxCastPlayerBuilder()
}
