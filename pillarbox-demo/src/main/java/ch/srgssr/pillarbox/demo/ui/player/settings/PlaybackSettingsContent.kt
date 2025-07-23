/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.settings

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.PlayerSettingsViewModel
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.SettingItem
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.SettingsRoutes
import ch.srgssr.pillarbox.demo.ui.player.metrics.StatsForNerds
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.ui.extension.getPeriodicallyCurrentMetricsAsState

/**
 * Playback settings content
 *
 * @param player The [Player] actions occurred.
 * @param modifier The [Modifier] to apply to the layout.
 */
@Composable
fun PlaybackSettingsContent(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val settingsViewModel: PlayerSettingsViewModel = viewModel(factory = PlayerSettingsViewModel.Factory(player))
    Surface(modifier = modifier) {
        NavHost(navController = navController, startDestination = SettingsRoutes.Main) {
            composable<SettingsRoutes.Main>(
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
                        when (val destination = it.destination) {
                            is SettingsRoutes.MetricsOverlay -> settingsViewModel.setMetricsOverlayEnabled(!destination.enabled)
                            is SettingsRoutes.SmoothSeeking -> settingsViewModel.setSmoothSeekingEnabled(!destination.enabled)
                            else -> navController.navigate(destination) {
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }

            composable<SettingsRoutes.PlaybackSpeed>(
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
                    onSpeedSelected = settingsViewModel::setPlaybackSpeed
                )
            }

            composable<SettingsRoutes.Subtitles>(
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                }
            ) {
                val subtitles by settingsViewModel.subtitles.collectAsState()
                subtitles?.let {
                    TrackSelectionSettings(
                        tracksSetting = it,
                        onResetClick = settingsViewModel::resetSubtitles,
                        onDisabledClick = settingsViewModel::disableSubtitles,
                        onTrackClick = settingsViewModel::selectTrack,
                    )
                }
            }

            composable<SettingsRoutes.AudioTrack>(
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                }
            ) {
                val audioTracks by settingsViewModel.audioTracks.collectAsState()
                audioTracks?.let {
                    TrackSelectionSettings(
                        tracksSetting = it,
                        onResetClick = settingsViewModel::resetAudioTrack,
                        onDisabledClick = settingsViewModel::disableAudioTrack,
                        onTrackClick = settingsViewModel::selectTrack,
                    )
                }
            }

            composable<SettingsRoutes.VideoTrack>(
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                }
            ) {
                val videoTracks by settingsViewModel.videoTracks.collectAsState()
                videoTracks?.let {
                    TrackSelectionSettings(
                        tracksSetting = it,
                        onResetClick = settingsViewModel::resetVideoTrack,
                        onDisabledClick = settingsViewModel::disableVideoTrack,
                        onTrackClick = settingsViewModel::selectTrack,
                    )
                }
            }

            composable<SettingsRoutes.StatsForNerds>(
                exitTransition = {
                    slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
                },
                enterTransition = {
                    slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
                },
            ) {
                if (player !is PillarboxPlayer) {
                    return@composable
                }

                val playbackMetrics by player.getPeriodicallyCurrentMetricsAsState()

                playbackMetrics?.let {
                    StatsForNerds(
                        playbackMetrics = it,
                        modifier = Modifier.padding(MaterialTheme.paddings.baseline),
                    )
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
    LazyColumn(
        modifier = Modifier.semantics {
            collectionInfo = CollectionInfo(rowCount = settings.size, columnCount = 1)
        }
    ) {
        itemsIndexed(items = settings) { index, setting ->
            SettingsItem(
                modifier = Modifier.clickable(
                    enabled = true,
                    role = Role.Button,
                    onClick = { settingsClicked(setting) }
                ),
                index = index,
                title = setting.title,
                secondaryText = setting.subtitle,
                imageVector = setting.icon
            )
        }
    }
}

@Composable
private fun SettingsItem(
    index: Int,
    title: String,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    secondaryText: String? = null
) {
    ListItem(
        modifier = modifier.semantics {
            collectionItemInfo = CollectionItemInfo(
                rowIndex = index,
                rowSpan = 1,
                columnIndex = 1,
                columnSpan = 1,
            )
        },
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
