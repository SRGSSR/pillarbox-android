/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.ui.player.DefaultVisibilityDelay
import ch.srgssr.pillarbox.demo.shared.ui.player.metrics.MetricsOverlay
import ch.srgssr.pillarbox.demo.shared.ui.player.rememberDelayedControlsVisibility
import ch.srgssr.pillarbox.demo.shared.ui.rememberIsTalkBackEnabled
import ch.srgssr.pillarbox.demo.shared.ui.settings.MetricsOverlayOptions
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerControls
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerError
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerNoContent
import ch.srgssr.pillarbox.demo.ui.player.controls.SkipButton
import ch.srgssr.pillarbox.demo.ui.player.controls.rememberProgressTrackerState
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView
import ch.srgssr.pillarbox.ui.extension.getCurrentCreditAsState
import ch.srgssr.pillarbox.ui.extension.getPeriodicallyCurrentMetricsAsState
import ch.srgssr.pillarbox.ui.extension.hasMediaItemsAsState
import ch.srgssr.pillarbox.ui.extension.isPlayingAsState
import ch.srgssr.pillarbox.ui.extension.playbackStateAsState
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.widget.keepScreenOn
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import kotlin.time.Duration.Companion.ZERO
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
                player.getCurrentPlaybackSessionId()
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
    val talkBackEnabled = rememberIsTalkBackEnabled()
    val isPlaying by player.isPlayingAsState()
    val keepControlDelay = if (!talkBackEnabled && !isSliderDragged && isPlaying) DefaultVisibilityDelay else ZERO
    val controlsVisibility = rememberDelayedControlsVisibility(initialVisible = controlsVisible, initialDelay = keepControlDelay)
    val playbackState by player.playbackStateAsState()
    val isBuffering = playbackState == Player.STATE_BUFFERING
    val controlsStateDescription = if (controlsVisibility.visible) {
        stringResource(R.string.controls_visible)
    } else {
        stringResource(R.string.controls_hidden)
    }
    Box(
        modifier = modifier
            .toggleable(
                value = controlsVisibility.visible,
                enabled = controlsToggleable,
                onValueChange = {
                    controlsVisibility.visible = !controlsVisibility.visible
                }
            )
            .semantics {
                stateDescription = controlsStateDescription
            }
    ) {
        PlayerSurface(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black),
            player = player,
            scaleMode = scaleMode
        ) {
            SurfaceOverlay(
                player = player,
                displayBuffering = isBuffering && !isSliderDragged,
                overlayEnabled = overlayEnabled,
                overlayOptions = overlayOptions,
            )
        }
        val currentCredit by player.getCurrentCreditAsState()
        AnimatedVisibility(
            visible = currentCredit != null && !controlsVisibility.visible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            SkipButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaterialTheme.paddings.baseline),
                onClick = { player.seekTo(currentCredit?.end ?: 0L) },
            )
        }

        DemoControls(
            modifier = Modifier
                .matchParentSize()
                .onFocusChanged { if (it.isFocused) controlsVisibility.reset() }
                .onEnterPressed {
                    controlsVisibility.visible = true
                },
            controlsVisible = controlsVisibility.visible,
            player = player,
            progressTracker = progressTracker,
            interactionSource = interactionSource,
            currentCredit = currentCredit,
            content = content,
        )
    }
}

@Composable
private fun BoxScope.SurfaceOverlay(
    player: Player,
    displayBuffering: Boolean,
    overlayEnabled: Boolean,
    overlayOptions: MetricsOverlayOptions,
) {
    AnimatedVisibility(
        displayBuffering,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        }
    }
    ExoPlayerSubtitleView(player = player)
    if (overlayEnabled && player is PillarboxExoPlayer) {
        val currentMetrics by player.getPeriodicallyCurrentMetricsAsState(500.milliseconds)
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

@Composable
private fun DemoControls(
    modifier: Modifier,
    controlsVisible: Boolean,
    player: Player,
    progressTracker: ProgressTrackerState,
    interactionSource: MutableInteractionSource,
    currentCredit: Credit?,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = controlsVisible
    ) {
        PlayerControls(
            player = player,
            interactionSource = interactionSource,
            progressTracker = progressTracker,
            credit = currentCredit,
            content = content
        )
    }
}

private fun Modifier.onEnterPressed(action: () -> Unit): Modifier {
    return this then Modifier.onPreviewKeyEvent {
        val isEnterKey = it.key == Key.Enter || it.key == Key.DirectionCenter || it.key == Key.NumPadEnter
        val isKeyUp = it.type == KeyEventType.KeyUp

        if (isEnterKey && isKeyUp) {
            action()
            true
        } else {
            false
        }
    }
}
