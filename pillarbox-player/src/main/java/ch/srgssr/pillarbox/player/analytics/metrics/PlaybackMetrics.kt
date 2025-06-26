/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.analytics.metrics

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.Format
import androidx.media3.common.VideoSize
import androidx.media3.common.util.Size
import ch.srgssr.pillarbox.player.extension.videoSize
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlin.time.Duration

/**
 * Represents playback metrics, containing information about the current media stream and playback session.
 *
 * @property sessionId A unique identifier for the playback session.
 * @property bandwidth The device-measured network bandwidth, in bits per second.
 * @property indicatedBitrate The bitrate of the video and audio format, in bits per second.
 * @property playbackDuration The total duration spent playing the media.
 * @property bufferingDuration The total duration spent buffering the media.
 * @property stallCount The number of times playback stalled, excluding stalls caused by seeking.
 * @property stallDuration The total duration of the stalls.
 * @property loadDuration The [LoadDuration] containing detailed timings for different stages of media loading.
 * @property totalLoadTime The total time taken to load the media for bandwidth calculation.
 * @property totalBytesLoaded The total number of bytes loaded for bandwidth calculation.
 * @property url The last URL loaded by the player.
 * @property videoFormat The [Format] of the currently selected video track.
 * @property audioFormat The [Format] of the currently selected audio track.
 * @property surfaceSize The size of the surface used for rendering the video. If no surface is connected, this will be [Size.ZERO].
 * @property totalDroppedFrames The total number of video frames dropped.
 */
@Parcelize
data class PlaybackMetrics(
    val sessionId: String,
    val bandwidth: Long,
    val indicatedBitrate: Long,
    val playbackDuration: Duration,
    val bufferingDuration: Duration,
    val stallCount: Int,
    val stallDuration: Duration,
    val loadDuration: LoadDuration,
    val totalLoadTime: Duration,
    val totalBytesLoaded: Long,
    val url: Uri?,
    @TypeParceler<Format?, FormatParceler>
    val videoFormat: Format?,
    @TypeParceler<Format?, FormatParceler>
    val audioFormat: Format?,
    @TypeParceler<Size, SizeParceler>
    val surfaceSize: Size,
    val totalDroppedFrames: Int,
) : Parcelable {

    /**
     * Represents the video size of [videoFormat], if applicable. Otherwise [VideoSize.UNKNOWN].
     */
    @IgnoredOnParcel
    val videoSize: VideoSize = videoFormat?.videoSize ?: VideoSize.UNKNOWN

    /**
     * Represents the timings spent in different stages of the loading process, until the current media started to play.
     *
     * @property source The time spent loading the media source.
     * @property manifest The time spent loading the main manifest, if applicable.
     * @property asset The time spent loading the asset.
     * @property drm The time spent loading the DRM.
     * @property timeToReady The total time elapsed from the moment the [MediaItem][androidx.media3.common.MediaItem] became the current item until it
     * was ready to play.
     */
    @Parcelize
    data class LoadDuration(
        val source: Duration? = null,
        val manifest: Duration? = null,
        val asset: Duration? = null,
        val drm: Duration? = null,
        val timeToReady: Duration? = null
    ) : Parcelable
}

internal object FormatParceler : Parceler<Format?> {
    override fun create(parcel: Parcel): Format? {
        return Format.fromBundle(parcel.readBundle(Format::class.java.classLoader) ?: Bundle.EMPTY)
    }

    override fun Format?.write(parcel: Parcel, flags: Int) {
        this?.let {
            parcel.writeBundle(toBundle())
        }
    }
}

internal object SizeParceler : Parceler<Size> {
    override fun create(parcel: Parcel): Size {
        return Size(parcel.readInt(), parcel.readInt())
    }

    override fun Size.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(width)
        parcel.writeInt(height)
    }
}
