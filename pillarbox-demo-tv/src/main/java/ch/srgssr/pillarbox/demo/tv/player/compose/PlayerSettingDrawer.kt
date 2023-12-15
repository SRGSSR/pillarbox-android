/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.player.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
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
import ch.srgssr.pillarbox.demo.tv.R
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.player.extension.audio
import ch.srgssr.pillarbox.player.extension.displayName
import ch.srgssr.pillarbox.player.extension.hasAccessibilityRoles
import ch.srgssr.pillarbox.player.extension.setDefaultAudioTrack
import ch.srgssr.pillarbox.player.extension.setDefaultTextTrack
import ch.srgssr.pillarbox.player.extension.setTrackOverride
import ch.srgssr.pillarbox.player.extension.text
import ch.srgssr.pillarbox.player.getCurrentTracksAsFlow
import ch.srgssr.pillarbox.ui.extension.playbackSpeedAsState

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
    val focusRequester = remember { FocusRequester() }
    val navController = rememberNavController()

    var hasFocus by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Routes.SETTINGS,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { hasFocus = it.hasFocus }
            .onGloballyPositioned {
                if (!hasFocus) {
                    focusRequester.requestFocus()
                }
            }
    ) {
        composable(Routes.SETTINGS) {
            GenericSetting(
                title = stringResource(R.string.settings),
                items = getSettings(player),
                isItemSelected = { false },
                onItemClick = {
                    navController.navigate(it.destination)
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

        composable(Routes.AUDIO_TRACK_SETTING) {
            val context = LocalContext.current
            val tracks by player.getCurrentTracksAsFlow().collectAsState(initial = Tracks.EMPTY)

            TracksSetting(
                title = stringResource(R.string.audio_track),
                tracks = tracks.audio,
                onResetClick = {
                    player.setDefaultAudioTrack(context)
                },
                onTrackClick = { group, trackIndex ->
                    player.setTrackOverride(TrackSelectionOverride(group.mediaTrackGroup, trackIndex))
                }
            )
        }

        composable(Routes.SUBTITLE_SETTING) {
            val context = LocalContext.current
            val tracks by player.getCurrentTracksAsFlow().collectAsState(initial = Tracks.EMPTY)

            TracksSetting(
                title = stringResource(R.string.subtitles),
                tracks = tracks.text,
                onResetClick = {
                    player.setDefaultTextTrack(context)
                },
                onTrackClick = { group, trackIndex ->
                    player.setTrackOverride(TrackSelectionOverride(group.mediaTrackGroup, trackIndex))
                }
            )
        }

        composable(Routes.SPEED_SETTING) {
            val speedOptions = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)
            val playbackSpeed by player.playbackSpeedAsState()

            GenericSetting(
                title = stringResource(R.string.speed),
                items = speedOptions,
                isItemSelected = { it == playbackSpeed },
                onItemClick = { speed ->
                    player.setPlaybackSpeed(speed)
                },
                leadingContent = { speed ->
                    AnimatedVisibility(visible = playbackSpeed == speed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                },
                content = { speed ->
                    Text(text = getSpeedLabel(speed))
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
    title: String,
    modifier: Modifier = Modifier,
    tracks: List<Group>,
    onResetClick: () -> Unit,
    onTrackClick: (track: Group, trackIndex: Int) -> Unit
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

            tracks.forEach { group ->
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

@Composable
private fun getSettings(player: Player): List<SettingItem> {
    val tracks by player.getCurrentTracksAsFlow().collectAsState(initial = Tracks.EMPTY)
    val playbackSpeed by player.playbackSpeedAsState()

    return buildList {
        if (tracks.audio.isNotEmpty()) {
            val selectedAudio = tracks.audio
                .filter { it.isSelected }
                .flatMap {
                    (0 until it.length).mapNotNull { trackIndex ->
                        if (it.isTrackSelected(trackIndex)) {
                            it.getTrackFormat(trackIndex).displayName
                        } else {
                            null
                        }
                    }
                }
                .firstOrNull()

            add(
                SettingItem(
                    destination = Routes.AUDIO_TRACK_SETTING,
                    icon = Icons.Default.Audiotrack,
                    subtitle = selectedAudio,
                    title = stringResource(R.string.audio_track)
                )
            )
        }

        if (tracks.text.isNotEmpty()) {
            val selectedSubtitle = tracks.text
                .filter { it.isSelected }
                .flatMap {
                    (0 until it.length).mapNotNull { trackIndex ->
                        if (it.isTrackSelected(trackIndex)) {
                            it.getTrackFormat(trackIndex).displayName
                        } else {
                            null
                        }
                    }
                }
                .firstOrNull()

            add(
                SettingItem(
                    destination = Routes.SUBTITLE_SETTING,
                    icon = Icons.Default.Subtitles,
                    subtitle = selectedSubtitle,
                    title = stringResource(R.string.subtitles)
                )
            )
        }

        add(
            SettingItem(
                destination = Routes.SPEED_SETTING,
                icon = Icons.Default.Speed,
                subtitle = getSpeedLabel(playbackSpeed),
                title = stringResource(R.string.speed)
            )
        )
    }
}

@Composable
private fun getSpeedLabel(speed: Float): String {
    return if (speed == 1f) {
        stringResource(R.string.speed_normal)
    } else {
        stringResource(R.string.speed_value, speed.toString())
    }
}

private data class SettingItem(
    val destination: String,
    val icon: ImageVector,
    val subtitle: String?,
    val title: String
)

private object Routes {
    const val SETTINGS = "settings"
    const val AUDIO_TRACK_SETTING = "settings/audio_track"
    const val SUBTITLE_SETTING = "settings/subtitles"
    const val SPEED_SETTING = "settings/speed"
}
