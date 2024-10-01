/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.ui.player.metrics.MetricsOverlay
import ch.srgssr.pillarbox.demo.shared.ui.settings.MetricsOverlayOptions
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerControls
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerError
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerNoContent
import ch.srgssr.pillarbox.demo.ui.player.controls.SkipButton
import ch.srgssr.pillarbox.demo.ui.player.controls.rememberProgressTrackerState
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.currentPositionAsFlow
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView
import ch.srgssr.pillarbox.ui.extension.getCurrentCreditAsState
import ch.srgssr.pillarbox.ui.extension.hasMediaItemsAsState
import ch.srgssr.pillarbox.ui.extension.playbackStateAsState
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.widget.ToggleableBox
import ch.srgssr.pillarbox.ui.widget.keepScreenOn
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import ch.srgssr.pillarbox.ui.widget.rememberDelayedVisibilityState
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.milliseconds

/**
 * Simple player view
 *
 * @param player The [Player] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param scaleMode The surface scale mode.
 * @param controlsVisible The control visibility.
 * @param controlsToggleable The controls are toggleable.
 * @param progressTracker The progress tracker.
 * @param overlayOptions The [MetricsOverlayOptions].
 * @param overlayEnabled true to display the metrics overlay.
 * @param content The action to display under the slider.
 */
@Composable
fun PlayerView(
    player: Player,
    modifier: Modifier = Modifier,
    scaleMode: ScaleMode = ScaleMode.Fit,
    controlsVisible: Boolean = true,
    controlsToggleable: Boolean = true,
    progressTracker: ProgressTrackerState = rememberProgressTrackerState(player = player, smoothTracker = true),
    overlayOptions: MetricsOverlayOptions = MetricsOverlayOptions(),
    overlayEnabled: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val playerError by player.playerErrorAsState()
    playerError?.let {
        val sessionId = remember {
            if (player is PillarboxExoPlayer) {
                player.getCurrentPlaybackSessionId() ?: "No session bitch"
            } else {
                null
            }
        }
        PlayerError(
            modifier = modifier,
            playerError = it,
            sessionId = sessionId,
            onRetry = player::prepare
        )
        return
    }

    val hasMediaItem by player.hasMediaItemsAsState()
    if (!hasMediaItem) {
        PlayerNoContent(modifier = modifier)
        return
    }
    player.keepScreenOn()
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val isSliderDragged by interactionSource.collectIsDraggedAsState()
    val visibilityState = rememberDelayedVisibilityState(
        player = player,
        autoHideEnabled = !isSliderDragged,
        visible = controlsVisible
    )
    val currentCredit by player.getCurrentCreditAsState()

    ToggleableBox(
        modifier = modifier,
        toggleable = controlsToggleable,
        visibilityState = visibilityState,
        toggleableContent = {
            PlayerControls(
                player = player,
                interactionSource = interactionSource,
                progressTracker = progressTracker,
                credit = currentCredit,
                content = content
            )
        }
    ) {
        val playbackState by player.playbackStateAsState()
        val isBuffering = playbackState == Player.STATE_BUFFERING
        PlayerSurface(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black),
            player = player,
            scaleMode = scaleMode
        ) {
            if (isBuffering && !isSliderDragged) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                }
            }
            ExoPlayerSubtitleView(player = player)
            if (overlayEnabled && player is PillarboxExoPlayer) {
                val currentMetricsFlow = remember(player) {
                    player.currentPositionAsFlow(updateInterval = 500.milliseconds).map {
                        player.getCurrentMetrics()
                    }
                }
                val currentMetrics by currentMetricsFlow.collectAsStateWithLifecycle(player.getCurrentMetrics())
                currentMetrics?.let {
                    MetricsOverlay(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.TopStart),
                        playbackMetrics = it,
                        overlayOptions = overlayOptions,
                    )
                }
            }
        }

        if (currentCredit != null && !visibilityState.isVisible) {
            SkipButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaterialTheme.paddings.baseline),
                onClick = { player.seekTo(currentCredit?.end ?: 0L) },
            )
        }

        BlockedTimeRangeWarning(
            player = player,
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(2f),
        )

        ChapterInfo(
            player = player,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .zIndex(2f),
            visible = !visibilityState.isVisible
        )
    }
}
