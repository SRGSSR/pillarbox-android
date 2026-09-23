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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.media3.common.DeviceInfo
import androidx.media3.common.Player
import androidx.media3.ui.compose.state.rememberPresentationState
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.extension.onDpadEvent
import ch.srgssr.pillarbox.demo.shared.ui.player.DefaultVisibilityDelay
import ch.srgssr.pillarbox.demo.shared.ui.player.metrics.MetricsOverlay
import ch.srgssr.pillarbox.demo.shared.ui.player.rememberDelayedControlsVisibility
import ch.srgssr.pillarbox.demo.shared.ui.player.rememberProgressTrackerState
import ch.srgssr.pillarbox.demo.shared.ui.rememberIsTalkBackEnabled
import ch.srgssr.pillarbox.demo.shared.ui.settings.MetricsOverlayOptions
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerControls
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerError
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerNoContent
import ch.srgssr.pillarbox.demo.ui.player.controls.SkipButton
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.extension.currentMediaMetadataAsState
import ch.srgssr.pillarbox.ui.extension.getDeviceInfoAsState
import ch.srgssr.pillarbox.ui.extension.getPeriodicallyCurrentMetricsAsState
import ch.srgssr.pillarbox.ui.extension.hasMediaItemsAsState
import ch.srgssr.pillarbox.ui.extension.isPlayingAsState
import ch.srgssr.pillarbox.ui.extension.playbackStateAsState
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.state.CreditState
import ch.srgssr.pillarbox.ui.state.rememberCreditState
import ch.srgssr.pillarbox.ui.widget.keepScreenOn
import ch.srgssr.pillarbox.ui.widget.player.PlayerFrame
import coil3.compose.AsyncImage
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

/**
 * Simple player view
 *
 * @param player The [Player] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param contentScale The surface [ContentScale].
 * @param controlsVisible The control visibility.
 * @param controlsToggleable The controls are toggleable.
 * @param progressTracker The progress tracker.
 * @param overlayOptions The [MetricsOverlayOptions].
 * @param overlayEnabled true to display the metrics overlay.
 * @param content The action to display under the slider.
 */
@Composable
fun PlayerView(
    player: PillarboxPlayer,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    controlsVisible: Boolean = true,
    controlsToggleable: Boolean = true,
    progressTracker: ProgressTrackerState = rememberProgressTrackerState(player = player),
    overlayOptions: MetricsOverlayOptions = MetricsOverlayOptions(),
    overlayEnabled: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val presentationState = rememberPresentationState(player, keepContentOnReset = false)
    PlayerFrame(
        modifier = modifier,
        player = player,
        contentScale = contentScale,
        presentationState = presentationState,
        shutter = {
            val deviceInfo by player.getDeviceInfoAsState()
            val mediaMetadata by player.currentMediaMetadataAsState()
            val placeholder = if (deviceInfo.playbackType == DeviceInfo.PLAYBACK_TYPE_REMOTE) {
                androidx.media3.cast.R.drawable.media_route_button_disconnected
            } else {
                R.drawable.placeholder
            }
            val placeHolderPainter = painterResource(placeholder)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black)
            ) {
                AsyncImage(
                    modifier = Modifier
                        .matchParentSize()
                        .background(color = Color.Black)
                        .align(Alignment.Center),
                    model = mediaMetadata.artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    placeholder = placeHolderPainter,
                    error = placeHolderPainter,
                )
            }
        }
    ) {
        val playerError by player.playerErrorAsState()
        playerError?.let {
            val sessionId = remember {
                player.getCurrentPlaybackSessionId()
            }
            PlayerError(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                playerError = it,
                sessionId = sessionId,
                onRetry = player::prepare
            )
            return@PlayerFrame
        }

        val hasMediaItem by player.hasMediaItemsAsState()
        if (!hasMediaItem) {
            PlayerNoContent(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
            return@PlayerFrame
        }

        if (overlayEnabled) {
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

        PlayerOverlay(
            player = player,
            controlsVisible = controlsVisible,
            controlsToggleable = controlsToggleable,
            progressTracker = progressTracker,
            controlsContent = content,
        )
    }
}

@Composable
private fun PlayerOverlay(
    player: PillarboxPlayer,
    controlsVisible: Boolean,
    controlsToggleable: Boolean,
    progressTracker: ProgressTrackerState,
    controlsContent: @Composable ColumnScope.() -> Unit,
) {
    player.keepScreenOn()

    val interactionSource = remember {
        MutableInteractionSource()
    }
    val isSliderDragged by interactionSource.collectIsDraggedAsState()
    val talkBackEnabled = rememberIsTalkBackEnabled()
    val isPlaying by player.isPlayingAsState()
    val keepControlDelay = if (!talkBackEnabled && !isSliderDragged && isPlaying) DefaultVisibilityDelay else ZERO
    val controlsVisibility = rememberDelayedControlsVisibility(initialVisible = controlsVisible, initialDelay = keepControlDelay)
    val controlsStateDescription = if (controlsVisibility.visible) {
        stringResource(R.string.controls_visible)
    } else {
        stringResource(R.string.controls_hidden)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
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
        val creditState = rememberCreditState(player)
        AnimatedVisibility(
            visible = creditState.isInCredit && !controlsVisibility.visible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(MaterialTheme.paddings.baseline),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            SkipButton(onClick = creditState::onClick)
        }

        ProgressIndicator(player, isSliderDragged)

        DemoControls(
            modifier = Modifier
                .matchParentSize()
                .onFocusChanged { if (it.isFocused) controlsVisibility.reset() }
                .onDpadEvent(onEnter = {
                    controlsVisibility.visible = !controlsVisibility.visible
                    true
                }),
            controlsVisible = controlsVisibility.visible,
            player = player,
            progressTracker = progressTracker,
            interactionSource = interactionSource,
            creditState = creditState,
            content = controlsContent,
        )
    }
}

@Composable
private fun ProgressIndicator(player: PillarboxPlayer, isSliderDragging: Boolean) {
    val playbackState by player.playbackStateAsState()
    val isBuffering = playbackState == Player.STATE_BUFFERING
    AnimatedVisibility(
        isBuffering && !isSliderDragging,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        }
    }
}

@Composable
private fun DemoControls(
    modifier: Modifier,
    controlsVisible: Boolean,
    player: PillarboxPlayer,
    progressTracker: ProgressTrackerState,
    interactionSource: MutableInteractionSource,
    creditState: CreditState,
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
            creditState = creditState,
            content = content
        )
    }
}
