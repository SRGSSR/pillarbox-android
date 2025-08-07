/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("UsingMaterialAndMaterial3Libraries") // Using the Material Navigation

package ch.srgssr.pillarbox.demo.ui.player

import android.app.Activity
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesAll
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsViewModel
import ch.srgssr.pillarbox.demo.shared.ui.settings.MetricsOverlayOptions
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerBottomToolbar
import ch.srgssr.pillarbox.demo.ui.player.playlist.PlaylistView
import ch.srgssr.pillarbox.demo.ui.player.settings.PlaybackSettingsContent
import ch.srgssr.pillarbox.demo.ui.player.state.rememberFullscreenButtonState
import ch.srgssr.pillarbox.player.extension.canSetRepeatMode
import ch.srgssr.pillarbox.player.extension.canSetShuffleMode
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.extension.availableCommandsAsState
import ch.srgssr.pillarbox.ui.extension.repeatModeAsState
import ch.srgssr.pillarbox.ui.extension.shuffleModeEnabledAsState

/**
 * Demo player
 *
 * @param player The [Player] to observe.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param isPictureInPictureEnabled Whether Picture-in-Picture is enabled.
 * @param isInPictureInPicture Whether the [Activity] is currently in Picture-in-Picture mode.
 * @param onPictureInPictureClick The Picture-in-Picture button action.
 * @param displayPlaylist If it displays the playlist UI or not.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DemoPlayerView(
    player: Player,
    modifier: Modifier = Modifier,
    isPictureInPictureEnabled: Boolean = false,
    isInPictureInPicture: Boolean = false,
    onPictureInPictureClick: () -> Unit = {},
    displayPlaylist: Boolean = false,
) {
    val windowSizeClass = calculateWindowSizeClass(checkNotNull(LocalActivity.current))
    val useSidePanel = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
    val availableCommands by player.availableCommandsAsState()
    val shuffleEnabled by player.shuffleModeEnabledAsState()
    val onShuffleClick = if (availableCommands.canSetShuffleMode()) {
        { player.shuffleModeEnabled = !player.shuffleModeEnabled }
    } else {
        null
    }
    val repeatMode by player.repeatModeAsState()
    val onRepeatClick = if (availableCommands.canSetRepeatMode()) {
        {
            player.repeatMode = when (player.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_OFF
                else -> error("Unrecognized repeat mode ${player.repeatMode}")
            }
        }
    } else {
        null
    }

    if (useSidePanel) {
        var showSettings by remember { mutableStateOf(false) }

        Row(modifier = modifier.displayCutoutPadding()) {
            PlayerContent(
                player = player,
                modifier = Modifier
                    .animateContentSize()
                    .then(if (showSettings) Modifier.weight(0.66f) else Modifier),
                shuffleEnabled = shuffleEnabled,
                onShuffleClick = onShuffleClick,
                repeatMode = repeatMode,
                onRepeatClick = onRepeatClick,
                isPictureInPictureEnabled = isPictureInPictureEnabled,
                isInPictureInPicture = isInPictureInPicture,
                onPictureInPictureClick = onPictureInPictureClick,
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
            shuffleEnabled = shuffleEnabled,
            onShuffleClick = onShuffleClick,
            repeatMode = repeatMode,
            onRepeatClick = onRepeatClick,
            isPictureInPictureEnabled = isPictureInPictureEnabled,
            isInPictureInPicture = isInPictureInPicture,
            onPictureInPictureClick = onPictureInPictureClick,
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
    player: Player,
    modifier: Modifier = Modifier,
    appSettingsViewModel: AppSettingsViewModel = viewModel(factory = AppSettingsViewModel.Factory()),
    shuffleEnabled: Boolean,
    onShuffleClick: (() -> Unit)?,
    repeatMode: @Player.RepeatMode Int,
    onRepeatClick: (() -> Unit)?,
    isPictureInPictureEnabled: Boolean,
    isInPictureInPicture: Boolean,
    onPictureInPictureClick: () -> Unit,
    onSettingsClick: () -> Unit,
    displayPlaylist: Boolean,
) {
    val fullscreenButtonState = rememberFullscreenButtonState()
    val appSettings by appSettingsViewModel.currentAppSettings.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        var pinchScaleMode by remember(fullscreenButtonState.isInFullscreen) {
            mutableStateOf(ScaleMode.Fit)
        }
        val scalableModifier = if (fullscreenButtonState.isInFullscreen) {
            Modifier.pointerInput(pinchScaleMode) {
                var lastZoomValue = 1f
                detectTransformGestures(true) { _, _, zoom, _ ->
                    lastZoomValue *= zoom
                    pinchScaleMode = if (lastZoomValue < 1f) ScaleMode.Fit else ScaleMode.Crop
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
            controlsToggleable = !isInPictureInPicture,
            controlsVisible = !isInPictureInPicture,
            scaleMode = pinchScaleMode,
            overlayEnabled = appSettings.metricsOverlayEnabled,
            overlayOptions = MetricsOverlayOptions(
                textColor = appSettings.metricsOverlayTextColor.color,
                textStyle = when (appSettings.metricsOverlayTextSize) {
                    AppSettings.TextSize.Small -> MaterialTheme.typography.bodySmall
                    AppSettings.TextSize.Medium -> MaterialTheme.typography.bodyMedium
                    AppSettings.TextSize.Large -> MaterialTheme.typography.bodyLarge
                },
            ),
        ) {
            PlayerBottomToolbar(
                modifier = Modifier.fillMaxWidth(),
                shuffleEnabled = shuffleEnabled,
                onShuffleClick = onShuffleClick,
                repeatMode = repeatMode,
                onRepeatClick = onRepeatClick,
                isPictureInPictureEnabled = isPictureInPictureEnabled,
                isInPictureInPicture = isInPictureInPicture,
                onPictureInPictureClick = onPictureInPictureClick,
                isInFullscreen = fullscreenButtonState.isInFullscreen,
                onFullscreenClick = fullscreenButtonState::onClick,
                onSettingsClick = onSettingsClick,
            )
        }
        if (displayPlaylist && !isInPictureInPicture && !fullscreenButtonState.isInFullscreen) {
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
