/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.srgssr.pillarbox.player.extension.disableAudioTrack
import ch.srgssr.pillarbox.player.extension.disableTextTrack
import ch.srgssr.pillarbox.player.extension.isAudioTrackDisabled
import ch.srgssr.pillarbox.player.extension.isTextTrackDisabled
import ch.srgssr.pillarbox.player.extension.setDefaultAudioTrack
import ch.srgssr.pillarbox.player.extension.setDefaultTextTrack
import ch.srgssr.pillarbox.player.extension.setTrackOverride

private sealed class SettingDestination(val route: String) {
    data object Home : SettingDestination(route = "settings/home")
    data object PlaybackSpeed : SettingDestination(route = "settings/speed")
    data object Subtitles : SettingDestination(route = "settings/subtitles")
    data object Audios : SettingDestination(route = "settings/audios")
}

private fun NavController.navigate(destination: SettingDestination) {
    navigate(route = destination.route) {
        launchSingleTop = true
    }
}

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
    val navController = rememberNavController()
    val settingsViewModel: PlayerSettingsViewModel = viewModel(factory = PlayerSettingsViewModel.Factory(player))
    val currentPlaybackSpeed = settingsViewModel.playbackSpeed.collectAsState().value
    Surface {
        NavHost(navController = navController, startDestination = SettingDestination.Home.route) {
            composable(
                route = SettingDestination.Home.route,
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                }
            ) {
                val hasAudios = settingsViewModel.hasAudio.collectAsState(initial = false)
                val hasSubtitles = settingsViewModel.hasSubtitles.collectAsState(initial = false)
                SettingsHome(
                    settings = createSettingsItems(
                        playbackSeed = currentPlaybackSpeed,
                        hasAudios = hasAudios.value,
                        hasSubtitles = hasSubtitles.value
                    ),
                    settingsClicked = {
                        navController.navigate(it.destination)
                    },
                )
            }
            composable(
                route = SettingDestination.PlaybackSpeed.route,
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                }
            ) {
                PlaybackSpeedSettings(currentSpeed = currentPlaybackSpeed, onSpeedSelected = {
                    player.setPlaybackSpeed(it)
                    onDismissState()
                })
            }
            composable(
                route = SettingDestination.Subtitles.route,
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                }
            ) {
                val textTrackState = settingsViewModel.textTracks.collectAsState()
                val textTrackSelection = settingsViewModel.trackSelectionParameters.collectAsState()
                val disabled = textTrackSelection.value.isTextTrackDisabled
                val context = LocalContext.current
                TrackSelectionSettings(textTrackState.value, disabled = disabled) { action ->
                    when (action) {
                        is TrackSelectionAction.Disable -> {
                            player.disableTextTrack()
                        }

                        is TrackSelectionAction.Default -> {
                            player.setDefaultTextTrack(context)
                        }

                        is TrackSelectionAction.Selection -> {
                            player.setTrackOverride(TrackSelectionOverride(action.group.mediaTrackGroup, action.trackIndex))
                        }
                    }
                    onDismissState()
                }
            }
            composable(
                route = SettingDestination.Audios.route,
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                }
            ) {
                val audioTracks = settingsViewModel.audioTracks.collectAsState()
                val trackSelectionParametersState = settingsViewModel.trackSelectionParameters.collectAsState()
                val disabled = trackSelectionParametersState.value.isAudioTrackDisabled
                val context = LocalContext.current
                TrackSelectionSettings(audioTracks.value, disabled = disabled) { action ->
                    when (action) {
                        is TrackSelectionAction.Disable -> {
                            player.disableAudioTrack()
                        }

                        is TrackSelectionAction.Default -> {
                            player.setDefaultAudioTrack(context)
                        }

                        is TrackSelectionAction.Selection -> {
                            player.setTrackOverride(TrackSelectionOverride(action.group.mediaTrackGroup, action.trackIndex))
                        }
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
                secondaryText = setting.secondaryText,
                imageVector = setting.imageVector
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
            { Text(text = it) }
        }
    )
}

private data class SettingItem(
    val title: String,
    val imageVector: ImageVector,
    val destination: SettingDestination,
    val secondaryText: String? = null,
)

private fun createSettingsItems(
    playbackSeed: Float,
    hasAudios: Boolean,
    hasSubtitles: Boolean
): List<SettingItem> {
    val list = ArrayList<SettingItem>()
    list.add(
        SettingItem(
            title = "Speed",
            imageVector = Icons.Default.Speed,
            destination = SettingDestination.PlaybackSpeed,
            secondaryText = DefaultSpeedLabelProvider(playbackSeed),
        )
    )
    if (hasSubtitles) {
        list.add(
            SettingItem(
                title = "Subtitles",
                imageVector = Icons.Default.Subtitles,
                destination = SettingDestination.Subtitles,
            )
        )
    }
    if (hasAudios) {
        list.add(
            SettingItem(
                title = "Audios",
                imageVector = Icons.Default.Audiotrack,
                destination = SettingDestination.Audios,
            )
        )
    }

    return list
}
