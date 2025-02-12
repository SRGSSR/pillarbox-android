/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.content.Context
import androidx.annotation.IntRange
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.C
import ch.srgssr.pillarbox.player.PillarboxBuilder
import ch.srgssr.pillarbox.player.PillarboxDsl

@PillarboxDsl
abstract class PillarboxCastPlayerBuilder {
    private var mediaItemConverter: MediaItemConverter = DefaultMediaItemConverter()
    private var seekBackIncrementMs: Long = C.DEFAULT_SEEK_BACK_INCREMENT_MS
    private var seekForwardIncrementMs: Long = C.DEFAULT_SEEK_FORWARD_INCREMENT_MS
    private var maxSeekToPreviousPositionMs: Long = C.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION_MS
    private var trackSelector: CastTrackSelector = DefaultCastTrackSelector
    private var onCastSessionAvailable: (PillarboxCastPlayer.() -> Unit)? = null
    private var onCastSessionUnAvailable: (PillarboxCastPlayer.() -> Unit)? = null

    fun seekBackIncrementMs(@IntRange(from = 1) seekBackIncrementMs: Long) {
        this.seekBackIncrementMs = seekBackIncrementMs
    }

    fun seekForwardIncrementMs(@IntRange(from = 1) seekForwardIncrementMs: Long) {
        this.seekForwardIncrementMs = seekBackIncrementMs
    }

    fun maxSeekToPreviousPositionMs(@IntRange(from = 0) maxSeekToPreviousPositionMs: Long) {
        this.maxSeekToPreviousPositionMs = maxSeekToPreviousPositionMs
    }

    fun trackSelector(trackSelector: CastTrackSelector) {
        this.trackSelector = trackSelector
    }

    fun onCastSessionAvailable(onCastSessionAvailable: PillarboxCastPlayer.() -> Unit) {
        this.onCastSessionAvailable = onCastSessionAvailable
    }

    fun onCastSessionUnavailable(onCastSessionUnAvailable: PillarboxCastPlayer.() -> Unit) {
        this.onCastSessionAvailable = onCastSessionUnAvailable
    }

    fun mediaItemConverter(mediaItemConverter: MediaItemConverter) {
        this.mediaItemConverter = mediaItemConverter
    }

    internal fun create(context: Context): PillarboxCastPlayer {
        return PillarboxCastPlayer(
            context.getCastContext(),
            context,
            mediaItemConverter,
            seekBackIncrementMs,
            seekForwardIncrementMs,
            maxSeekToPreviousPositionMs,
            trackSelector,
        ).apply {
            setSessionAvailabilityListener(object : SessionAvailabilityListener {
                override fun onCastSessionAvailable() {
                    onCastSessionAvailable?.invoke(this@apply)
                }

                override fun onCastSessionUnavailable() {
                    onCastSessionUnAvailable?.invoke(this@apply)
                }
            })
        }
    }
}

/**
 * Defines a factory for creating instances of [PillarboxBuilder].
 *
 * @param Builder The type of [PillarboxBuilder] that this factory creates.
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
