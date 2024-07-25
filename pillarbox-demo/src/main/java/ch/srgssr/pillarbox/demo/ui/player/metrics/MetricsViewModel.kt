/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.metrics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.media3.common.Player
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.currentPositionAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Remember metrics view model
 *
 * @param player The [PillarboxExoPlayer] to get metrics from.
 * @param coroutineScope The [CoroutineScope] to listen current [player] position.
 */
@Composable
fun rememberMetricsViewModel(
    player: PillarboxExoPlayer,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): MetricsViewModel {
    val metricsViewModel = remember(player) {
        MetricsViewModel(coroutineScope = coroutineScope, player = player)
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
 * @param coroutineScope The [CoroutineScope] to listen current [player] position.
 * @param player The [PillarboxExoPlayer] to get metrics from.
 */
@Stable
class MetricsViewModel internal constructor(
    coroutineScope: CoroutineScope,
    private val player: PillarboxExoPlayer
) {
    private val playerComponent = PlayerComponent()

    /**
     * Metrics flow
     */
    val metricsFlow = MutableStateFlow(player.getCurrentMetrics())

    init {
        player.addAnalyticsListener(playerComponent)
        coroutineScope.launch {
            player.currentPositionAsFlow().collectLatest {
                metricsFlow.value = player.getCurrentMetrics()
            }
        }
    }

    /**
     * Clear
     */
    internal fun clear() {
        player.removeAnalyticsListener(playerComponent)
    }

    private inner class PlayerComponent : PillarboxAnalyticsListener {

        override fun onEvents(p: Player, events: AnalyticsListener.Events) {
            metricsFlow.value = player.getCurrentMetrics()
        }
    }
}
