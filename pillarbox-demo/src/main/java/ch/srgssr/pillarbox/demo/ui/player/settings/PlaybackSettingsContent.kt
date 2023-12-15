/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import android.app.Application
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.PlayerSettingsViewModel
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.SettingItem
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.SettingsRoutes

/**
 * Playback settings content
 *
 * @param player The [Player] actions occurred.
 * @param onDismiss The callback to dismiss the settings.
 */
@Composable
fun PlaybackSettingsContent(
    player: Player,
    onDismiss: () -> Unit,
) {
    val onDismissState = remember {
        onDismiss
    }
    val application = LocalContext.current.applicationContext as Application
    val navController = rememberNavController()
    val settingsViewModel: PlayerSettingsViewModel = viewModel(factory = PlayerSettingsViewModel.Factory(player, application))
    Surface {
        NavHost(navController = navController, startDestination = SettingsRoutes.Settings.route) {
            composable(
                route = SettingsRoutes.Settings.route,
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                }
            ) {
                val settings by settingsViewModel.settings.collectAsState()
                SettingsHome(
                    settings = settings,
                    settingsClicked = {
                        navController.navigate(it.destination.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(
                route = SettingsRoutes.PlaybackSpeed.route,
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                }
            ) {
                val playbackSpeeds by settingsViewModel.playbackSpeeds.collectAsState()
                PlaybackSpeedSettings(
                    playbackSpeeds = playbackSpeeds,
                    onSpeedSelected = { playbackSpeed ->
                        settingsViewModel.setPlaybackSpeed(playbackSpeed)
                        onDismissState()
                    }
                )
            }
            composable(
                route = SettingsRoutes.Subtitles.route,
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                }
            ) {
                val subtitles by settingsViewModel.subtitles.collectAsState()
                TrackSelectionSettings(subtitles.tracks, disabled = subtitles.disabled) { action ->
                    when (action) {
                        is TrackSelectionAction.Disable -> settingsViewModel.disableSubtitles()
                        is TrackSelectionAction.Default -> settingsViewModel.resetSubtitles()
                        is TrackSelectionAction.Selection -> settingsViewModel.setSubtitle(action.group, action.trackIndex)
                    }
                    onDismissState()
                }
            }
            composable(
                route = SettingsRoutes.AudioTrack.route,
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                }
            ) {
                val audioTracks by settingsViewModel.audioTracks.collectAsState()
                TrackSelectionSettings(audioTracks.tracks, disabled = audioTracks.disabled) { action ->
                    when (action) {
                        is TrackSelectionAction.Disable -> settingsViewModel.disableAudioTrack()
                        is TrackSelectionAction.Default -> settingsViewModel.resetAudioTrack()
                        is TrackSelectionAction.Selection -> settingsViewModel.setAudioTrack(action.group, action.trackIndex)
                    }
                    onDismissState()
                }
            }
        }
    }
}

@Composable
private fun SettingsHome(
    settings: List<SettingItem>,
    settingsClicked: (SettingItem) -> Unit,
) {
    LazyColumn {
        items(items = settings) { setting ->
            SettingsItem(
                modifier = Modifier.clickable(
                    enabled = true,
                    role = Role.Button,
                    onClick = { settingsClicked(setting) }
                ),
                title = setting.title,
                secondaryText = setting.subtitle,
                imageVector = setting.icon
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    secondaryText: String? = null
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(text = title)
        },
        trailingContent = {
            Icon(imageVector = imageVector, contentDescription = null)
        },
        supportingContent = secondaryText?.let {
            {
                Text(text = it)
            }
        }
    )
}
