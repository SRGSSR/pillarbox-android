/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.tv.material3.Button
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import ch.srgssr.pillarbox.demo.shared.extension.onDpadEvent
import ch.srgssr.pillarbox.demo.shared.ui.components.PillarboxSlider
import ch.srgssr.pillarbox.demo.shared.ui.getFormatter
import ch.srgssr.pillarbox.demo.shared.ui.localTimeFormatter
import ch.srgssr.pillarbox.demo.shared.ui.player.DefaultVisibilityDelay
import ch.srgssr.pillarbox.demo.shared.ui.player.metrics.MetricsOverlay
import ch.srgssr.pillarbox.demo.shared.ui.player.rememberDelayedControlsVisibility
import ch.srgssr.pillarbox.demo.shared.ui.player.rememberProgressTrackerState
import ch.srgssr.pillarbox.demo.shared.ui.rememberIsTalkBackEnabled
import ch.srgssr.pillarbox.demo.shared.ui.settings.MetricsOverlayOptions
import ch.srgssr.pillarbox.demo.tv.R
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerError
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerPlaybackRow
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.playlist.PlaylistDrawer
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.settings.PlaybackSettingsDrawer
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.currentPositionAsFlow
import ch.srgssr.pillarbox.player.extension.canSeek
import ch.srgssr.pillarbox.player.extension.canSetRepeatMode
import ch.srgssr.pillarbox.player.extension.canSetShuffleMode
import ch.srgssr.pillarbox.player.extension.getUnixTimeMs
import ch.srgssr.pillarbox.ui.ProgressTrackerState
import ch.srgssr.pillarbox.ui.extension.availableCommandsAsState
import ch.srgssr.pillarbox.ui.extension.currentBufferedPercentageAsState
import ch.srgssr.pillarbox.ui.extension.currentMediaMetadataAsState
import ch.srgssr.pillarbox.ui.extension.durationAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentChapterAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentCreditAsState
import ch.srgssr.pillarbox.ui.extension.isCurrentMediaItemLiveAsState
import ch.srgssr.pillarbox.ui.extension.isPlayingAsState
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.extension.repeatModeAsState
import ch.srgssr.pillarbox.ui.extension.shuffleModeEnabledAsState
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import ch.srgssr.pillarbox.demo.shared.R as sharedR

private enum class DrawerMode {
    PLAYLIST,
    SETTINGS,
}

/**
 * TV player view
 *
 * @param player
 * @param modifier
 * @param metricsOverlayEnabled
 * @param metricsOverlayOptions
 */
@Suppress("CyclomaticComplexMethod")
@Composable
fun PlayerView(
    player: Player,
    modifier: Modifier = Modifier,
    metricsOverlayEnabled: Boolean,
    metricsOverlayOptions: MetricsOverlayOptions,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val talkBackEnabled = rememberIsTalkBackEnabled()
    val isPlaying by player.isPlayingAsState()
    val availableCommands by player.availableCommandsAsState()
    val keepControlDelay = if (!talkBackEnabled && isPlaying) DefaultVisibilityDelay else ZERO
    val controlsVisibilityState = rememberDelayedControlsVisibility(initialVisible = true, keepControlDelay)

    var drawerMode by remember { mutableStateOf(DrawerMode.SETTINGS) }

    LaunchedEffect(drawerState.currentValue) {
        controlsVisibilityState.visible = when (drawerState.currentValue) {
            DrawerValue.Closed -> true
            DrawerValue.Open -> false
        }
    }

    BackHandler(enabled = controlsVisibilityState.visible) {
        controlsVisibilityState.visible = false
    }

    ModalNavigationDrawer(
        drawerContent = { drawerValue ->
            if (drawerValue == DrawerValue.Open) {
                BackHandler {
                    drawerState.setValue(DrawerValue.Closed)
                }

                var hasFocus by remember { mutableStateOf(false) }

                val focusRequester = remember { FocusRequester() }
                val modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
                    .padding(MaterialTheme.paddings.baseline)
                    .focusRequester(focusRequester)
                    .onFocusChanged { hasFocus = it.hasFocus }
                    .onGloballyPositioned {
                        if (!hasFocus) {
                            focusRequester.requestFocus()
                        }
                    }

                Surface(
                    modifier = modifier,
                    shape = MaterialTheme.shapes.large,
                    colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                        when (drawerMode) {
                            DrawerMode.PLAYLIST -> PlaylistDrawer(player)
                            DrawerMode.SETTINGS -> PlaybackSettingsDrawer(player)
                        }
                    }
                }
            }
        },
        modifier = modifier,
        drawerState = drawerState,
    ) {
        val error by player.playerErrorAsState()
        if (error != null) {
            PlayerError(
                modifier = Modifier.fillMaxSize(),
                playerError = error!!,
                onRetry = player::prepare,
            )
        } else {
            PlayerSurface(
                player = player,
                modifier = Modifier
                    .fillMaxSize()
                    .onDpadEvent(
                        eventType = KeyEventType.KeyUp,
                        onEnter = {
                            controlsVisibilityState.visible = true
                            true
                        },
                    )
                    .focusable(true),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                val currentCredit by player.getCurrentCreditAsState()

                if (metricsOverlayEnabled && player is PillarboxExoPlayer) {
                    val currentMetricsFlow = remember(player) {
                        player.currentPositionAsFlow(updateInterval = 500.milliseconds)
                            .map { player.getCurrentMetrics() }
                    }
                    val currentMetrics by currentMetricsFlow.collectAsState(initial = player.getCurrentMetrics())

                    currentMetrics?.let {
                        MetricsOverlay(
                            playbackMetrics = it,
                            overlayOptions = metricsOverlayOptions,
                            modifier = Modifier.padding(MaterialTheme.paddings.baseline),
                        )
                    }
                }

                ChapterInfo(
                    player = player,
                    controlsVisible = controlsVisibilityState.visible,
                )

                if (!controlsVisibilityState.visible && currentCredit != null) {
                    SkipButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(MaterialTheme.paddings.baseline),
                        onClick = { player.seekTo(currentCredit?.end ?: 0L) },
                    )
                }

                AnimatedVisibility(
                    visible = controlsVisibilityState.visible,
                    modifier = Modifier
                        .fillMaxSize()
                        .onFocusChanged {
                            if (it.isFocused) {
                                controlsVisibilityState.reset()
                            }
                        },
                ) {
                    Box {
                        PlayerPlaybackRow(
                            player = player,
                            modifier = Modifier.align(Alignment.Center),
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(MaterialTheme.paddings.baseline),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
                            ) {
                                val repeatMode by player.repeatModeAsState()
                                val shuffleEnabled by player.shuffleModeEnabledAsState()
                                val canSetRepeatMode = availableCommands.canSetRepeatMode()
                                val canSetShuffleMode = availableCommands.canSetShuffleMode()
                                val iconButtonColors = IconButtonDefaults.colors()
                                val activeIconButtonColors = IconButtonDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                )

                                IconButton(
                                    onClick = {
                                        drawerMode = DrawerMode.SETTINGS
                                        drawerState.setValue(DrawerValue.Open)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = stringResource(sharedR.string.settings),
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        drawerMode = DrawerMode.PLAYLIST
                                        drawerState.setValue(DrawerValue.Open)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                                        contentDescription = stringResource(R.string.playlist),
                                    )
                                }

                                if (canSetRepeatMode) {
                                    IconButton(
                                        onClick = {
                                            player.repeatMode = when (player.repeatMode) {
                                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                                                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                                                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_OFF
                                                else -> error("Unrecognized repeat mode ${player.repeatMode}")
                                            }
                                        },
                                        colors = if (repeatMode == Player.REPEAT_MODE_OFF) iconButtonColors else activeIconButtonColors,
                                    ) {
                                        val repeatIcon = when (repeatMode) {
                                            Player.REPEAT_MODE_ALL,
                                            Player.REPEAT_MODE_OFF -> Icons.Default.Repeat

                                            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                                            else -> error("Unrecognized repeat mode ${player.repeatMode}")
                                        }

                                        Icon(
                                            imageVector = repeatIcon,
                                            contentDescription = stringResource(sharedR.string.repeat_mode),
                                        )
                                    }
                                }

                                if (canSetShuffleMode) {
                                    IconButton(
                                        onClick = { player.shuffleModeEnabled = !shuffleEnabled },
                                        colors = if (shuffleEnabled) activeIconButtonColors else iconButtonColors,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shuffle,
                                            contentDescription = stringResource(sharedR.string.shuffle),
                                        )
                                    }
                                }

                                if (currentCredit != null) {
                                    Spacer(modifier = Modifier.weight(1f))

                                    SkipButton(
                                        onClick = { player.seekTo(currentCredit?.end ?: 0L) },
                                    )
                                }
                            }

                            AnimatedVisibility(availableCommands.canSeek()) {
                                PlayerTimeRow(player)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterInfo(
    player: Player,
    controlsVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentMediaMetadata by player.currentMediaMetadataAsState()
    val currentChapter by player.getCurrentChapterAsState()

    var showChapterInfo by remember {
        mutableStateOf(currentChapter?.mediaMetadata != null)
    }

    LaunchedEffect(currentChapter) {
        showChapterInfo = currentChapter?.mediaMetadata != null
        if (showChapterInfo) {
            delay(5.seconds)
            showChapterInfo = false
        }
    }

    AnimatedVisibility(
        visible = controlsVisible || showChapterInfo,
        modifier = modifier,
        enter = slideInVertically(),
        exit = slideOutVertically(),
    ) {
        MediaMetadataView(currentChapter?.mediaMetadata ?: currentMediaMetadata)
    }
}

@Composable
private fun SkipButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(text = stringResource(sharedR.string.skip))
    }
}

@Composable
private fun PlayerTimeRow(
    player: Player,
    modifier: Modifier = Modifier,
    progressTracker: ProgressTrackerState = rememberProgressTrackerState(player = player),
) {
    val window = remember { Window() }
    val durationMs by player.durationAsState()
    val duration = if (durationMs == C.TIME_UNSET) ZERO else durationMs.milliseconds
    val currentProgress by progressTracker.progress.collectAsState()
    val currentProgressPercent = currentProgress.inWholeMilliseconds / player.duration.coerceAtLeast(1).toFloat()
    val bufferPercentage by player.currentBufferedPercentageAsState()
    val formatter = duration.getFormatter()
    var compactMode by remember { mutableStateOf(true) }

    val isLive by player.isCurrentMediaItemLiveAsState()
    val timePosition = if (isLive) player.getUnixTimeMs(currentProgress.inWholeMilliseconds, window) else C.TIME_UNSET
    val positionLabel = when (timePosition) {
        C.TIME_UNSET -> formatter(currentProgress)

        else -> {
            val localTime = Instant.fromEpochMilliseconds(timePosition).toLocalDateTime(TimeZone.currentSystemDefault()).time
            localTimeFormatter.format(localTime)
        }
    }
    val updateProgress = { newProgress: Duration ->
        newProgress.coerceIn(ZERO, duration)
            .takeIf { it != currentProgress }
            ?.let(progressTracker::onChanged)
    }

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.large,
            )
            .padding(top = MaterialTheme.paddings.baseline)
            .padding(horizontal = MaterialTheme.paddings.baseline)
            .padding(bottom = MaterialTheme.paddings.small),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.mini),
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            PillarboxSlider(
                value = currentProgressPercent,
                range = 0f..1f,
                compactMode = compactMode,
                modifier = Modifier.onFocusChanged { compactMode = !it.hasFocus },
                secondaryValue = bufferPercentage,
                thumbColorEnabled = Color.White,
                thumbColorDisabled = Color.White,
                activeTrackColorEnabled = Color.Red,
                activeTrackColorDisabled = Color.Red,
                inactiveTrackColorEnabled = Color.White,
                inactiveTrackColorDisabled = Color.White,
                secondaryTrackColorEnabled = Color.Gray,
                secondaryTrackColorDisabled = Color.Gray,
                onValueChange = { progressTracker.onChanged((it * player.duration).toLong().milliseconds) },
                onValueChangeFinished = progressTracker::onFinished,
                onSeekBack = { updateProgress(currentProgress - player.seekBackIncrement.milliseconds) },
                onSeekForward = { updateProgress(currentProgress + player.seekForwardIncrement.milliseconds) },
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelSmall) {
                    Text(text = positionLabel)

                    Text(text = formatter(duration))
                }
            }
        }
    }
}
