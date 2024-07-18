/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.metrics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.media3.common.Player
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Remember metrics view model
 *
 * @param player The player.
 */
@Composable
fun rememberMetricsViewModel(player: PillarboxExoPlayer): MetricsViewModel {
    val metricsViewModel = remember(player) {
        MetricsViewModel(player)
    }
    DisposableEffect(metricsViewModel) {
        onDispose {
            metricsViewModel.clear()
        }
    }
    return metricsViewModel
}

/**
 * Metrics view model
 *
 * @param player
 * @constructor Create empty Metrics view model
 */
@Stable
class MetricsViewModel internal constructor(private val player: PillarboxExoPlayer) {
    private val playerComponent = PlayerComponent()

    /**
     * Metrics flow
     */
    val metricsFlow = MutableStateFlow(player.getCurrentMetrics())

    /**
     * Current video format flow
     */
    val currentVideoFormatFlow = MutableStateFlow(player.videoFormat)

    /**
     * Current audio format flow
     */
    val currentAudioFormatFlow = MutableStateFlow(player.audioFormat)

    /**
     * Bitrate estimate flow
     */
    val bitrateEstimateFlow = MutableStateFlow(0L)

    init {
        player.addAnalyticsListener(playerComponent)
    }

    /**
     * Clear
     */
    fun clear() {
        player.removeAnalyticsListener(playerComponent)
    }

    private inner class PlayerComponent : PillarboxAnalyticsListener {

        override fun onBandwidthEstimate(
            eventTime: AnalyticsListener.EventTime,
            totalLoadTimeMs: Int,
            totalBytesLoaded: Long,
            bitrateEstimate: Long
        ) {
            bitrateEstimateFlow.value = bitrateEstimate
        }

        override fun onEvents(p: Player, events: AnalyticsListener.Events) {
            metricsFlow.value = player.getCurrentMetrics()
            currentVideoFormatFlow.value = player.videoFormat
            currentAudioFormatFlow.value = player.audioFormat
        }
    }
}
