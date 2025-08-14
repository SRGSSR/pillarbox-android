/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.tv.material3.Button
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
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
import ch.srgssr.pillarbox.demo.shared.ui.player.shouldDisplayArtworkAsState
import ch.srgssr.pillarbox.demo.shared.ui.rememberIsTalkBackEnabled
import ch.srgssr.pillarbox.demo.shared.ui.settings.MetricsOverlayOptions
import ch.srgssr.pillarbox.demo.tv.R
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerError
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerPlaybackRow
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.playlist.PlaylistDrawer
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.settings.PlaybackSettingsDrawer
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.currentPositionAsFlow
import ch.srgssr.pillarbox.player.extension.canSeek
import ch.srgssr.pillarbox.player.extension.getUnixTimeMs
import ch.srgssr.pillarbox.ui.extension.availableCommandsAsState
import ch.srgssr.pillarbox.ui.extension.currentMediaMetadataAsState
import ch.srgssr.pillarbox.ui.extension.currentPositionAsState
import ch.srgssr.pillarbox.ui.extension.durationAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentChapterAsState
import ch.srgssr.pillarbox.ui.extension.isCurrentMediaItemLiveAsState
import ch.srgssr.pillarbox.ui.extension.isPlayingAsState
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.state.rememberCreditState
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import ch.srgssr.pillarbox.demo.shared.R as shareR

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
                    colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                ) {
                    when (drawerMode) {
                        DrawerMode.PLAYLIST -> PlaylistDrawer(player)
                        DrawerMode.SETTINGS -> PlaybackSettingsDrawer(player)
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
                    placeholder = painterResource(shareR.drawable.placeholder),
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val creditState = rememberCreditState(player)

                Column {
                    ChapterInfo(
                        player = player,
                        currentMediaMetadata = mediaMetadata,
                        controlsVisible = controlsVisibilityState.visible,
                    )

                    if (metricsOverlayEnabled) {
                        val currentMetricsFlow = remember(player) {
                            player.currentPositionAsFlow(updateInterval = 500.milliseconds)
                                .map { player.getCurrentMetrics() }
                        }
                        val currentMetrics by currentMetricsFlow.collectAsState(initial = player.getCurrentMetrics())

                        currentMetrics?.let {
                            MetricsOverlay(
                                playbackMetrics = it,
                                overlayOptions = metricsOverlayOptions,
                                modifier = Modifier.padding(MaterialTheme.paddings.small),
                            )
                        }
                    }
                }

                if (!controlsVisibilityState.visible && creditState.isInCredit) {
                    SkipButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(MaterialTheme.paddings.baseline),
                        onClick = creditState::onClick,
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
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black),
                                    ),
                                )
                                .padding(horizontal = MaterialTheme.paddings.baseline),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
                                ) {
                                    IconButton(
                                        onClick = {
                                            drawerMode = DrawerMode.SETTINGS
                                            drawerState.setValue(DrawerValue.Open)
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = stringResource(shareR.string.settings),
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
                                }

                                if (creditState.isInCredit) {
                                    SkipButton(
                                        onClick = creditState::onClick,
                                    )
                                }
                            }

                            PlayerTimeRow(
                                player = player,
                                onSeek = { value ->
                                    controlsVisibilityState.reset()
                                    player.seekTo(value)
                                },
                            )
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
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        AnimatedContent(
            targetState = currentChapter?.mediaMetadata ?: currentMediaMetadata,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            transitionSpec = {
                slideInHorizontally { it }
                    .togetherWith(slideOutHorizontally { -it })
            },
            label = "media_metadata_transition",
        ) { mediaMetadata ->
            MediaMetadataView(mediaMetadata)
        }
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
        Text(text = stringResource(shareR.string.skip))
    }
}

@Composable
private fun PlayerTimeRow(
    player: Player,
    onSeek: (value: Long) -> Unit,
) {
    val durationMs by player.durationAsState()
    val positionMs by player.currentPositionAsState()
    val availableCommands by player.availableCommandsAsState()
    val duration = durationMs.takeIf { it != C.TIME_UNSET }?.milliseconds ?: ZERO
    val formatter = duration.getFormatter()

    @Suppress("Indentation", "Wrapping")
    val onSeekProxy = remember(durationMs, positionMs) {
        { newPosition: Long ->
            if (newPosition in 0..durationMs && newPosition != positionMs) {
                onSeek(newPosition)
            }
        }
    }

    var compactMode by remember {
        mutableStateOf(true)
    }
    val isLive by player.isCurrentMediaItemLiveAsState()
    val window = remember { Window() }
    val positionTime = if (isLive) player.getUnixTimeMs(positionMs, window) else C.TIME_UNSET
    val positionLabel = when (positionTime) {
        C.TIME_UNSET -> formatter(positionMs.milliseconds)

        else -> {
            val localTime = Instant.fromEpochMilliseconds(positionTime).toLocalDateTime(TimeZone.currentSystemDefault()).time
            localTimeFormatter.format(localTime)
        }
    }

    Text(
        text = "$positionLabel / ${formatter(duration)}",
        modifier = Modifier.padding(
            top = MaterialTheme.paddings.baseline,
            bottom = MaterialTheme.paddings.small,
        ),
        color = Color.White,
    )

    PillarboxSlider(
        value = positionMs,
        range = 0..durationMs,
        compactMode = compactMode,
        modifier = Modifier.onFocusChanged { compactMode = !it.hasFocus },
        enabled = availableCommands.canSeek(),
        thumbColorEnabled = MaterialTheme.colorScheme.primary,
        thumbColorDisabled = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        activeTrackColorEnabled = MaterialTheme.colorScheme.primary,
        activeTrackColorDisabled = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        inactiveTrackColorEnabled = MaterialTheme.colorScheme.surfaceVariant,
        inactiveTrackColorDisabled = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        onSeekBack = { onSeekProxy(positionMs - player.seekBackIncrement) },
        onSeekForward = { onSeekProxy(positionMs + player.seekBackIncrement) },
    )
}
