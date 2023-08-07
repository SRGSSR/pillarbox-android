/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.srgssr.pillarbox.player.extension.isTextTrackDefault
import ch.srgssr.pillarbox.player.extension.isTextTrackDisabled
import ch.srgssr.pillarbox.player.getCurrentTracksAsFlow
import ch.srgssr.pillarbox.player.getPlaybackSpeed
import ch.srgssr.pillarbox.player.getPlaybackSpeedAsFlow
import ch.srgssr.pillarbox.player.getTrackSelectionParametersAsFlow
import kotlinx.coroutines.flow.map

private sealed class SettingDestination(val route: String) {
    data object Home : SettingDestination(route = "settings/home")
    data object PlaybackSpeed : SettingDestination(route = "settings/speed")
    data object Subtitles : SettingDestination(route = "settings/subtitles")
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
    val trackSelectionParameters = player.getTrackSelectionParametersAsFlow().collectAsState(initial = player.trackSelectionParameters)
    val currentPlaybackSpeed = player.getPlaybackSpeedAsFlow().collectAsState(player.getPlaybackSpeed())
    NavHost(navController = navController, startDestination = SettingDestination.Home.route) {
        composable(route = SettingDestination.Home.route) {
            val list = remember(currentPlaybackSpeed.value) {
                listOf(
                    SettingItem(
                        title = "Speed",
                        imageVector = Icons.Default.Speed,
                        destination = SettingDestination.PlaybackSpeed,
                        secondaryText = DefaultSpeedLabelProvider(currentPlaybackSpeed.value),
                    ),
                    SettingItem(
                        title = "Subtitles tracks",
                        imageVector = Icons.Default.Subtitles,
                        destination = SettingDestination.Subtitles,
                    ),
                )
            }
            SettingsHome(
                settings = list,
                settingsClicked = {
                    navController.navigate(it.destination)
                },
            )
        }
        composable(route = SettingDestination.PlaybackSpeed.route) {
            PlaybackSpeedSettings(currentSpeed = currentPlaybackSpeed.value, onSpeedSelected = {
                player.setPlaybackSpeed(it)
                onDismissState()
            })
        }
        composable(route = SettingDestination.Subtitles.route) { entry ->
            val textTracksFlow = remember(player) {
                player.getCurrentTracksAsFlow().map { it.groups.filter { it.type == C.TRACK_TYPE_TEXT } }
            }
            val textTrackState = textTracksFlow.collectAsState(initial = emptyList())
            val context = LocalContext.current
            val disabled = trackSelectionParameters.value.isTextTrackDisabled
            val hasOverrides = trackSelectionParameters.value.isTextTrackDefault
            TextTrackSettings(textTrackState.value, disabled = disabled, default = !hasOverrides) { action ->
                val trackSelectionBuilder = player.trackSelectionParameters.buildUpon()
                when (action) {
                    is SubtitleAction.Disable -> {
                        trackSelectionBuilder
                            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    }

                    is SubtitleAction.Automatic -> {
                        // Default TextTrack parameters
                        trackSelectionBuilder
                            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                            .setIgnoredTextSelectionFlags(0)
                            .setPreferredTextRoleFlags(0)
                            .setPreferredTextLanguageAndRoleFlagsToCaptioningManagerSettings(context)
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    }

                    is SubtitleAction.Selection -> {
                        trackSelectionBuilder
                            .setTrackTypeDisabled(action.group.type, false)
                            .setOverrideForType(TrackSelectionOverride(action.group.mediaTrackGroup, action.trackIndex))
                    }
                }
                onDismissState()
                player.trackSelectionParameters = trackSelectionBuilder.build()
            }
        }
    }
    // val playerSettingsViewModel: PlayerSettingsViewModel = viewModel(factory = PlayerSettingsViewModel.Factory(player))
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SettingsItem(
    title: String,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    secondaryText: String? = null
) {
    ListItem(
        modifier = modifier,
        icon = { Icon(imageVector = imageVector, contentDescription = null) },
        secondaryText = secondaryText?.let {
            { Text(text = it) }
        }
    ) {
        Text(text = title)
    }
}

private data class SettingItem(
    val title: String,
    val imageVector: ImageVector,
    val destination: SettingDestination,
    val secondaryText: String? = null,
)
