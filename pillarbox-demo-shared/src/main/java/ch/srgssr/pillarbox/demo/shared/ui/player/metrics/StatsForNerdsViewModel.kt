/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.metrics

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.VideoSize
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.ui.getFormatter
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.qos.models.QoETimings
import ch.srgssr.pillarbox.player.qos.models.QoSTimings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import kotlin.time.Duration

/**
 * [ViewModel][androidx.lifecycle.ViewModel] for the "Stats for Nerds" screen.
 */
class StatsForNerdsViewModel(application: Application) : AndroidViewModel(application) {
    private val _indicatedBitRates = MutableStateFlow(BitRates.Empty)

    /**
     * Provides information about the indicated bit rates.
     */
    val indicatedBitRates: StateFlow<BitRates> = _indicatedBitRates

    private val _information = MutableStateFlow(emptyMap<String, String>())

    /**
     * Provides information about the current session.
     */
    val information: StateFlow<Map<String, String>> = _information

    private val _observedBitRates = MutableStateFlow(BitRates.Empty)

    /**
     * Provides information about the observed bit rates.
     */
    val observedBitRates: StateFlow<BitRates> = _observedBitRates

    private val _stalls = MutableStateFlow(Stalls.Empty)

    /**
     * Provides information about stalls.
     */
    val stalls: StateFlow<Stalls> = _stalls

    private val _qoeTimings = MutableStateFlow(emptyMap<String, String>())

    /**
     * Provides information about the startup times, as experienced by the user.
     */
    val qoeTimingsFields: StateFlow<Map<String, String>> = _qoeTimings

    private val _qosTimings = MutableStateFlow(emptyMap<String, String>())

    /**
     * Provides information about the startup times, during the preload phase.
     */
    val qosTimingsFields: StateFlow<Map<String, String>> = _qosTimings

    private val _volumes = MutableStateFlow(DataVolumes.Empty)

    /**
     * Provides information about volumes.
     */
    val volumes: StateFlow<DataVolumes> = _volumes

    /**
     * The latest playback metrics.
     */
    var playbackMetrics: PlaybackMetrics? = null
        set(value) {
            if (field == value) {
                return
            }

            field = value

            if (value == null) {
                return
            }

            _indicatedBitRates.update {
                val newData = (it.data + (value.indicatedBitrate / TO_MEGA))
                    .takeLast(CHART_MAX_POINTS)

                BitRates(data = newData)
            }

            _information.update {
                listOfNotNull(
                    getSessionInformation(R.string.session_id, value.sessionId),
                    getSessionInformation(R.string.media_uri, value.url?.toString()),
                    getSessionInformation(R.string.playback_duration, value.playbackDuration.getFormatter().invoke(value.playbackDuration)),
                    getSessionInformation(R.string.data_volume, value.totalBytesLoaded.toFloat().toFormattedBytes(includeUnit = true)),
                    getSessionInformation(R.string.buffering, value.bufferingDuration.toString()),
                    getSessionInformation(
                        labelRes = R.string.video_size,
                        value = if (value.videoSize != VideoSize.UNKNOWN) {
                            "${value.videoSize.width}x${value.videoSize.height}"
                        } else {
                            null
                        }
                    ),
                ).toMap()
            }

            _observedBitRates.update {
                val newData = (it.data + (value.bandwidth / TO_MEGA))
                    .takeLast(CHART_MAX_POINTS)

                BitRates(data = newData)
            }

            _stalls.update {
                val stallCount = value.stallCount.toFloat()
                val stall = if (it.data.isEmpty()) {
                    stallCount
                } else {
                    stallCount - it.data.sum()
                }
                val newData = (it.data + stall.coerceAtLeast(0f))
                    .takeLast(CHART_MAX_POINTS)

                Stalls(
                    data = newData,
                    total = value.stallCount.toFloat().toFormattedBytes(includeUnit = false)
                )
            }

            _volumes.update {
                val totalBytesLoaded = value.totalBytesLoaded.toFloat()
                val volume = if (it.data.isEmpty()) {
                    totalBytesLoaded / TO_MEGA
                } else {
                    totalBytesLoaded / TO_MEGA - it.data.sum()
                }
                val newData = (it.data + volume.coerceAtLeast(0f))
                    .takeLast(CHART_MAX_POINTS)

                DataVolumes(
                    data = newData,
                    total = totalBytesLoaded.toFormattedBytes(includeUnit = true),
                )
            }
        }

    /**
     * The latest QoE timings.
     */
    var qoeTimings: QoETimings? = null
        set(value) {
            if (field == value) {
                return
            }

            field = value

            if (value == null) {
                return
            }

            _qoeTimings.update {
                listOfNotNull(
                    getLoadDuration(R.string.asset_loading, value.asset),
                    getLoadDuration(R.string.resource_loading, value.metadata),
                    getLoadDuration(R.string.total_load_time, value.total)
                ).toMap()
            }
        }

    /**
     * The latest QoS timings.
     */
    var qosTimings: QoSTimings? = null
        set(value) {
            if (field == value) {
                return
            }

            field = value

            if (value == null) {
                return
            }

            _qosTimings.update {
                listOfNotNull(
                    getLoadDuration(R.string.asset_loading, value.asset),
                    getLoadDuration(R.string.drm_loading, value.drm),
                    getLoadDuration(R.string.resource_loading, value.metadata),
                ).toMap()
            }
        }

    private fun getLoadDuration(
        @StringRes labelRes: Int,
        duration: Duration?,
    ): Pair<String, String>? {
        return if (duration != null) {
            getApplication<Application>().getString(labelRes) to duration.toString()
        } else {
            null
        }
    }

    private fun getSessionInformation(
        @StringRes labelRes: Int,
        value: String?,
    ): Pair<String, String>? {
        return if (value != null) {
            getApplication<Application>().getString(labelRes) to value
        } else {
            null
        }
    }

    private fun Float.toFormattedBytes(
        includeUnit: Boolean,
    ): String {
        val units = listOf("B", "KB", "MB", "GB", "TB")
        val numberFormat = NumberFormat.getNumberInstance()

        var remaining = this
        var unitIndex = 0
        while (remaining >= TO_NEXT_UNIT && unitIndex < units.lastIndex) {
            remaining /= TO_NEXT_UNIT
            unitIndex++
        }

        return if (includeUnit) {
            "${numberFormat.format(remaining)} ${units[unitIndex]}"
        } else {
            numberFormat.format(remaining)
        }
    }

    companion object {
        private const val TO_MEGA = 1_000_000f
        private const val TO_NEXT_UNIT = 1_000f

        /**
         * The aspect ratio to use for the charts.
         */
        const val CHART_ASPECT_RATIO = 16 / 9f

        /**
         * The maximum number of points to display on a chart.
         */
        const val CHART_MAX_POINTS = 90
    }
}
