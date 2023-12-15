/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.player.compose

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.Tracks.Group
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.DrawerState
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.PlayerSettingsViewModel
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.SettingsRoutes
import ch.srgssr.pillarbox.demo.shared.ui.player.settings.TracksSettingItem
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.player.extension.displayName
import ch.srgssr.pillarbox.player.extension.hasAccessibilityRoles

/**
 * Drawer used to display a player's settings.
 *
 * @param player The currently active player.
 * @param drawerState The state of the drawer.
 * @param modifier The [Modifier] to apply to the drawer.
 * @param content The content to display behind the drawer.
 */
@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun PlayerSettingDrawer(
    player: Player,
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerContent = {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Ltr,
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface
                ) {
                    if (it == DrawerValue.Open) {
                        BackHandler {
                            drawerState.setValue(DrawerValue.Closed)
                        }

                        NavigationDrawerNavHost(
                            player = player,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(MaterialTheme.paddings.baseline)
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    shape = MaterialTheme.shapes.large
                                )
                        )
                    }
                }
            },
            modifier = modifier,
            drawerState = drawerState,
            content = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    content()
                }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun NavigationDrawerScope.NavigationDrawerNavHost(
    player: Player,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as Application
    val settingsViewModel = viewModel<PlayerSettingsViewModel>(factory = PlayerSettingsViewModel.Factory(player, application))
    val focusRequester = remember { FocusRequester() }
    val navController = rememberNavController()

    var hasFocus by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = SettingsRoutes.Settings.route,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { hasFocus = it.hasFocus }
            .onGloballyPositioned {
                if (!hasFocus) {
                    focusRequester.requestFocus()
                }
            }
    ) {
        composable(SettingsRoutes.Settings.route) {
            val settings by settingsViewModel.settings.collectAsState()

            GenericSetting(
                title = stringResource(R.string.settings),
                items = settings,
                isItemSelected = { false },
                onItemClick = { setting ->
                    navController.navigate(setting.destination.route)
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

        composable(SettingsRoutes.AudioTrack.route) {
            val audioTracks by settingsViewModel.audioTracks.collectAsState()

            TracksSetting(
                tracksSetting = audioTracks,
                onResetClick = settingsViewModel::resetAudioTrack,
                onDisabledClick = settingsViewModel::disableAudioTrack,
                onTrackClick = settingsViewModel::setAudioTrack
            )
        }

        composable(SettingsRoutes.Subtitles.route) {
            val subtitles by settingsViewModel.subtitles.collectAsState()

            TracksSetting(
                tracksSetting = subtitles,
                onResetClick = settingsViewModel::resetSubtitles,
                onDisabledClick = settingsViewModel::disableSubtitles,
                onTrackClick = settingsViewModel::setSubtitle
            )
        }

        composable(SettingsRoutes.PlaybackSpeed.route) {
            val playbackSpeeds by settingsViewModel.playbackSpeeds.collectAsState()

            GenericSetting(
                title = stringResource(R.string.speed),
                items = playbackSpeeds,
                isItemSelected = { it.isSelected },
                onItemClick = settingsViewModel::setPlaybackSpeed,
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
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun <T> NavigationDrawerScope.GenericSetting(
    title: String,
    items: List<T>,
    isItemSelected: (item: T) -> Boolean,
    modifier: Modifier = Modifier,
    onItemClick: (item: T) -> Unit,
    leadingContent: @Composable (item: T) -> Unit,
    supportingContent: @Composable (item: T) -> Unit = {},
    content: @Composable (item: T) -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = MaterialTheme.paddings.baseline)
            .padding(top = MaterialTheme.paddings.baseline)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        TvLazyColumn(
            contentPadding = PaddingValues(vertical = MaterialTheme.paddings.baseline)
        ) {
            items(items) { item ->
                NavigationDrawerItem(
                    selected = isItemSelected(item),
                    onClick = { onItemClick(item) },
                    leadingContent = { leadingContent(item) },
                    supportingContent = { supportingContent(item) },
                    content = { content(item) }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun NavigationDrawerScope.TracksSetting(
    tracksSetting: TracksSettingItem,
    modifier: Modifier = Modifier,
    onResetClick: () -> Unit,
    onDisabledClick: () -> Unit,
    onTrackClick: (track: Group, trackIndex: Int) -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = MaterialTheme.paddings.baseline)
            .padding(top = MaterialTheme.paddings.baseline)
    ) {
        Text(
            text = tracksSetting.title,
            style = MaterialTheme.typography.titleMedium
        )

        TvLazyColumn(
            contentPadding = PaddingValues(vertical = MaterialTheme.paddings.baseline)
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

            tracksSetting.tracks.forEach { group ->
                items(group.length) { trackIndex ->
                    NavigationDrawerItem(
                        selected = group.isTrackSelected(trackIndex),
                        onClick = { onTrackClick(group, trackIndex) },
                        leadingContent = {
                            AnimatedVisibility(visible = group.isTrackSelected(trackIndex)) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null
                                )
                            }
                        },
                        content = {
                            val format = group.getTrackFormat(trackIndex)
                            val label = buildString {
                                append(format.displayName)

                                if (format.bitrate > Format.NO_VALUE) {
                                    append(" @")
                                    append(format.bitrate)
                                    append(" bit/sec")
                                }

                                if (format.hasAccessibilityRoles()) {
                                    append(" (AD)")
                                }
                            }

                            Text(
                                text = label,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    )
                }
            }
        }
    }
}
