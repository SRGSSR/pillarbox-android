/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.PlayerSettingsViewModel
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.SettingsRoutes
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.TracksSettingItem
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.DrawerContent
import ch.srgssr.pillarbox.demo.tv.ui.player.metrics.StatsForNerds
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.displayName
import ch.srgssr.pillarbox.player.extension.hasAccessibilityRoles
import ch.srgssr.pillarbox.player.extension.isForced
import ch.srgssr.pillarbox.player.tracks.AudioTrack
import ch.srgssr.pillarbox.player.tracks.Track
import ch.srgssr.pillarbox.player.tracks.VideoTrack
import ch.srgssr.pillarbox.ui.extension.getPeriodicallyCurrentMetricsAsState

/**
 * Drawer used to display a player's settings.
 *
 * @param player The currently active player.
 * @param modifier The [Modifier] to apply to this layout.
 */
@Composable
fun NavigationDrawerScope.PlaybackSettingsDrawer(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val settingsViewModel = viewModel<PlayerSettingsViewModel>(factory = PlayerSettingsViewModel.Factory(player))
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SettingsRoutes.Main,
        modifier = modifier,
    ) {
        composable<SettingsRoutes.Main> {
            val settings by settingsViewModel.settings.collectAsState()

            DrawerContent(
                title = { Text(text = stringResource(R.string.settings)) },
                items = settings,
                isItemSelected = { _, _ -> false },
                onItemClick = { _, setting ->
                    val destination = setting.destination

                    if (destination is SettingsRoutes.MetricsOverlay) {
                        settingsViewModel.setMetricsOverlayEnabled(!destination.enabled)
                    } else {
                        navController.navigate(destination)
                    }
                },
                leadingContent = { setting ->
                    Icon(
                        imageVector = setting.icon,
                        contentDescription = null
                    )
                },
                supportingContent = { setting ->
                    setting.subtitle?.let { subtitle ->
                        Text(text = subtitle)
                    }
                },
                content = { setting ->
                    Text(
                        text = setting.title,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            )
        }

        composable<SettingsRoutes.AudioTrack> {
            val audioTracks by settingsViewModel.audioTracks.collectAsState()

            audioTracks?.let {
                TracksSetting(
                    tracksSetting = it,
                    onResetClick = settingsViewModel::resetAudioTrack,
                    onDisabledClick = settingsViewModel::disableAudioTrack,
                    onTrackClick = settingsViewModel::selectTrack,
                )
            }
        }

        composable<SettingsRoutes.VideoTrack> {
            val videoTracks by settingsViewModel.videoTracks.collectAsState()

            videoTracks?.let {
                TracksSetting(
                    tracksSetting = it,
                    onResetClick = settingsViewModel::resetVideoTrack,
                    onDisabledClick = settingsViewModel::disableVideoTrack,
                    onTrackClick = settingsViewModel::selectTrack,
                )
            }
        }

        composable<SettingsRoutes.Subtitles> {
            val subtitles by settingsViewModel.subtitles.collectAsState()

            subtitles?.let {
                TracksSetting(
                    tracksSetting = it,
                    onResetClick = settingsViewModel::resetSubtitles,
                    onDisabledClick = settingsViewModel::disableSubtitles,
                    onTrackClick = settingsViewModel::selectTrack,
                )
            }
        }

        composable<SettingsRoutes.PlaybackSpeed> {
            val playbackSpeeds by settingsViewModel.playbackSpeeds.collectAsState()

            DrawerContent(
                title = { Text(text = stringResource(R.string.speed)) },
                items = playbackSpeeds,
                isItemSelected = { _, item ->
                    item.isSelected
                },
                onItemClick = { _, item ->
                    settingsViewModel.setPlaybackSpeed(item)
                },
                leadingContent = { playbackSpeed ->
                    AnimatedVisibility(visible = playbackSpeed.isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                },
                content = { playbackSpeed ->
                    Text(text = playbackSpeed.speed)
                }
            )
        }

        composable<SettingsRoutes.StatsForNerds> {
            if (player !is PillarboxPlayer) {
                return@composable
            }

            val playbackMetrics by player.getPeriodicallyCurrentMetricsAsState()

            playbackMetrics?.let {
                StatsForNerds(it)
            }
        }
    }
}

@Composable
private fun NavigationDrawerScope.TracksSetting(
    tracksSetting: TracksSettingItem,
    modifier: Modifier = Modifier,
    onResetClick: () -> Unit,
    onDisabledClick: () -> Unit,
    onTrackClick: (track: Track) -> Unit,
) {
    DrawerContent(
        title = { Text(text = tracksSetting.title) },
        modifier = modifier,
    ) {
        item {
            NavigationDrawerItem(
                selected = false,
                onClick = onResetClick,
                leadingContent = {},
                content = {
                    Text(
                        text = stringResource(R.string.reset_to_default),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            )
        }

        item {
            NavigationDrawerItem(
                selected = tracksSetting.disabled,
                onClick = onDisabledClick,
                leadingContent = {
                    AnimatedVisibility(visible = tracksSetting.disabled) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                },
                content = {
                    Text(
                        text = stringResource(R.string.disabled),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            )
        }

        items(tracksSetting.tracks) { track ->
            val format = track.format
            NavigationDrawerItem(
                selected = track.isSelected,
                enabled = track.isSupported && !format.isForced(),
                onClick = { onTrackClick(track) },
                leadingContent = {
                    AnimatedVisibility(visible = track.isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                },
                content = {
                    when (track) {
                        is AudioTrack -> {
                            val text = buildString {
                                append(format.displayName)

                                if (format.bitrate > Format.NO_VALUE) {
                                    append(" @%1$.2f Mbps".format(format.bitrate / 1_000_000f))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = text)
                                if (format.hasAccessibilityRoles()) {
                                    Icon(
                                        imageVector = Icons.Filled.HearingDisabled,
                                        contentDescription = "AD",
                                        modifier = Modifier.padding(start = MaterialTheme.paddings.small),
                                    )
                                }
                            }
                        }

                        is VideoTrack -> {
                            val text = buildString {
                                append(format.width)
                                append("×")
                                append(format.height)

                                if (format.bitrate > Format.NO_VALUE) {
                                    append(" @%1$.2f Mbps".format(format.bitrate / 1_000_000f))
                                }
                            }

                            Text(text = text)
                        }

                        else -> {
                            if (format.hasAccessibilityRoles()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = format.displayName)
                                    Icon(
                                        imageVector = Icons.Default.HearingDisabled,
                                        contentDescription = "Hearing disabled",
                                        modifier = Modifier.padding(start = MaterialTheme.paddings.small),
                                    )
                                }
                            } else {
                                Text(text = format.displayName)
                            }
                        }
                    }
                }
            )
        }
    }
}
