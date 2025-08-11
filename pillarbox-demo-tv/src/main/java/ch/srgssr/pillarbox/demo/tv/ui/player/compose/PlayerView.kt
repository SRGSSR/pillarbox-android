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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.tv.material3.DrawerValue
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
import ch.srgssr.pillarbox.demo.shared.ui.player.shouldDisplayArtworkAsState
import ch.srgssr.pillarbox.demo.shared.ui.rememberIsTalkBackEnabled
import ch.srgssr.pillarbox.demo.shared.ui.settings.MetricsOverlayOptions
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerError
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerPlaybackRow
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerToolbar
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.SkipButton
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.playlist.PlaylistDrawer
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.settings.PlaybackSettingsDrawer
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.currentPositionAsFlow
import ch.srgssr.pillarbox.player.extension.canSeek
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
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
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
    player: PillarboxPlayer,
    modifier: Modifier = Modifier,
    metricsOverlayEnabled: Boolean,
    metricsOverlayOptions: MetricsOverlayOptions,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val talkBackEnabled = rememberIsTalkBackEnabled()
    val isPlaying by player.isPlayingAsState()
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
            val shouldDisplayArtwork by player.shouldDisplayArtworkAsState()
            val mediaMetadata by player.currentMediaMetadataAsState()
            if (shouldDisplayArtwork) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black),
                    model = mediaMetadata.artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(sharedR.drawable.placeholder),
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val currentCredit by player.getCurrentCreditAsState()

                if (metricsOverlayEnabled && player.isMetricsAvailable) {
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
                    currentMediaMetadata = mediaMetadata,
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
                            val availableCommands by player.availableCommandsAsState()

                            PlayerToolbar(
                                player = player,
                                currentCredit = currentCredit,
                                modifier = Modifier.fillMaxWidth(),
                                onSettingsClick = {
                                    drawerMode = DrawerMode.SETTINGS
                                    drawerState.setValue(DrawerValue.Open)
                                },
                                onPlaylistClick = {
                                    drawerMode = DrawerMode.PLAYLIST
                                    drawerState.setValue(DrawerValue.Open)
                                },
                            )

                            AnimatedVisibility(availableCommands.canSeek()) {
                                PlayerTimeRow(
                                    player = player,
                                    onProgressChange = controlsVisibilityState::reset,
                                )
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
    player: PillarboxPlayer,
    controlsVisible: Boolean,
    currentMediaMetadata: MediaMetadata,
    modifier: Modifier = Modifier,
) {
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
private fun PlayerTimeRow(
    player: Player,
    modifier: Modifier = Modifier,
    progressTracker: ProgressTrackerState = rememberProgressTrackerState(player = player),
    onProgressChange: () -> Unit,
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
    val progressTrackerChangeProxy = { progress: Duration ->
        progressTracker.onChanged(progress)
        onProgressChange()
    }
    val updateProgress = { newProgress: Duration ->
        newProgress.coerceIn(ZERO, duration)
            .takeIf { it != currentProgress }
            ?.let(progressTrackerChangeProxy)
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
                onValueChange = { progressTrackerChangeProxy((it * player.duration).toLong().milliseconds) },
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
