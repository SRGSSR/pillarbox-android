/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("UsingMaterialAndMaterial3Libraries") // Using the Material Navigation

package ch.srgssr.pillarbox.demo.ui.player

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.ui.compose.state.rememberRepeatButtonState
import androidx.media3.ui.compose.state.rememberShuffleButtonState
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesAll
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsViewModel
import ch.srgssr.pillarbox.demo.shared.ui.settings.MetricsOverlayOptions
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerBottomToolbar
import ch.srgssr.pillarbox.demo.ui.player.playlist.PlaylistView
import ch.srgssr.pillarbox.demo.ui.player.settings.PlaybackSettingsContent
import ch.srgssr.pillarbox.demo.ui.player.state.rememberFullscreenButtonState
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.ui.state.PipManager

/**
 * Demo player
 *
 * @param player The [Player] to observe.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param pipManager The [PipManager] to drive the Picture-in-Picture button with, or `null` to hide it.
 * @param displayPlaylist If it displays the playlist UI or not.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DemoPlayerView(
    player: PillarboxPlayer,
    modifier: Modifier = Modifier,
    pipManager: PipManager? = null,
    displayPlaylist: Boolean = false,
) {
    val windowSizeClass = calculateWindowSizeClass(checkNotNull(LocalActivity.current))
    val useSidePanel = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium

    if (useSidePanel) {
        var showSettings by remember { mutableStateOf(false) }

        Row(modifier = modifier.displayCutoutPadding()) {
            PlayerContent(
                player = player,
                modifier = Modifier
                    .animateContentSize()
                    .then(if (showSettings) Modifier.weight(0.66f) else Modifier),
                pipManager = pipManager,
                onSettingsClick = { showSettings = !showSettings },
                displayPlaylist = displayPlaylist,
            )

            AnimatedVisibility(
                visible = showSettings,
                modifier = if (showSettings) Modifier.weight(0.33f) else Modifier,
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut() + slideOutHorizontally { it },
            ) {
                PlaybackSettingsContent(player = player)
            }
        }
    } else {
        var showSettingsSheet by remember { mutableStateOf(false) }

        PlayerContent(
            player = player,
            modifier = Modifier.fillMaxSize(),
            pipManager = pipManager,
            onSettingsClick = { showSettingsSheet = true },
            displayPlaylist = displayPlaylist,
        )

        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
            ) {
                PlaybackSettingsContent(player = player)
            }
        }
    }
}

@Composable
private fun PlayerContent(
    player: PillarboxPlayer,
    modifier: Modifier = Modifier,
    appSettingsViewModel: AppSettingsViewModel = viewModel(factory = AppSettingsViewModel.Factory()),
    pipManager: PipManager?,
    onSettingsClick: () -> Unit,
    displayPlaylist: Boolean,
) {
    val shuffleButtonState = rememberShuffleButtonState(player)
    val repeatButtonState = rememberRepeatButtonState(player)
    val fullscreenButtonState = rememberFullscreenButtonState()
    val appSettings by appSettingsViewModel.currentAppSettings.collectAsStateWithLifecycle()
    val isInPictureInPicture = pipManager?.isInPictureInPicture == true
    val isPipTransitioning = pipManager?.isTransitioning == true
    val showControls = !isInPictureInPicture && !isPipTransitioning

    Column(modifier = modifier) {
        var pinchContentScale by remember(fullscreenButtonState.isInFullscreen) {
            mutableStateOf(ContentScale.Fit)
        }
        val scalableModifier = if (fullscreenButtonState.isInFullscreen) {
            Modifier.pointerInput(pinchContentScale) {
                var lastZoomValue = 1f
                detectTransformGestures(true) { _, _, zoom, _ ->
                    lastZoomValue *= zoom
                    pinchContentScale = if (lastZoomValue < 1f) ContentScale.Fit else ContentScale.Crop
                }
            }
        } else {
            Modifier
        }
        PlayerView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(scalableModifier),
            player = player,
            controlsToggleable = showControls,
            controlsVisible = showControls,
            contentScale = pinchContentScale,
            overlayEnabled = appSettings.metricsOverlayEnabled,
            overlayOptions = MetricsOverlayOptions(
                textColor = appSettings.metricsOverlayTextColor.color,
                textStyle = when (appSettings.metricsOverlayTextSize) {
                    AppSettings.TextSize.Small -> MaterialTheme.typography.bodySmall
                    AppSettings.TextSize.Medium -> MaterialTheme.typography.bodyMedium
                    AppSettings.TextSize.Large -> MaterialTheme.typography.bodyLarge
                },
            ),
            pipManager = pipManager,
        ) {
            PlayerBottomToolbar(
                modifier = Modifier.fillMaxWidth(),
                isShuffleEnabled = shuffleButtonState.isEnabled,
                isShuffleOn = shuffleButtonState.shuffleOn,
                onShuffleClick = shuffleButtonState::onClick,
                isRepeatEnabled = repeatButtonState.isEnabled,
                repeatMode = repeatButtonState.repeatModeState,
                onRepeatClick = repeatButtonState::onClick,
                isPictureInPictureEnabled = pipManager?.let { it.isSupported && it.isAllowed } == true,
                isInPictureInPicture = isInPictureInPicture,
                onPictureInPictureClick = { pipManager?.enter() },
                isInFullscreen = fullscreenButtonState.isInFullscreen,
                onFullscreenClick = fullscreenButtonState::onClick,
                onSettingsClick = onSettingsClick,
            )
        }
        if (displayPlaylist && showControls && !fullscreenButtonState.isInFullscreen) {
            PlaylistView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                player = player,
                itemsLibrary = SamplesAll.playlist.items,
            )
        }
    }
}
