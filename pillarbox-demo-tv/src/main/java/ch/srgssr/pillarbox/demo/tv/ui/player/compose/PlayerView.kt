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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.res.stringResource
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.tv.material3.Button
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.extension.onDpadEvent
import ch.srgssr.pillarbox.demo.shared.ui.components.PillarboxSlider
import ch.srgssr.pillarbox.demo.shared.ui.getFormatter
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerError
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerPlaybackRow
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.settings.PlaybackSettingsDrawer
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.player.extension.canSeek
import ch.srgssr.pillarbox.ui.extension.availableCommandsAsState
import ch.srgssr.pillarbox.ui.extension.currentMediaMetadataAsState
import ch.srgssr.pillarbox.ui.extension.currentPositionAsState
import ch.srgssr.pillarbox.ui.extension.durationAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentChapterAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentCreditAsState
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.widget.DelayedVisibilityState
import ch.srgssr.pillarbox.ui.widget.maintainVisibleOnFocus
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import ch.srgssr.pillarbox.ui.widget.rememberDelayedVisibilityState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * TV player view
 *
 * @param player
 * @param modifier
 */
@Composable
fun PlayerView(
    player: Player,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val visibilityState = rememberDelayedVisibilityState(player = player, visible = true)

    LaunchedEffect(drawerState.currentValue) {
        when (drawerState.currentValue) {
            DrawerValue.Closed -> visibilityState.show()
            DrawerValue.Open -> visibilityState.hide()
        }
    }

    BackHandler(enabled = visibilityState.isVisible) {
        visibilityState.hide()
    }

    PlaybackSettingsDrawer(
        player = player,
        drawerState = drawerState,
        modifier = modifier,
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
                            visibilityState.show()
                            true
                        },
                    )
                    .focusable(true),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                val currentCredit by player.getCurrentCreditAsState()

                ChapterInfo(
                    player = player,
                    visibilityState = visibilityState,
                )

                if (!visibilityState.isVisible && currentCredit != null) {
                    SkipButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(MaterialTheme.paddings.baseline),
                        onClick = { player.seekTo(currentCredit?.end ?: 0L) },
                    )
                }

                AnimatedVisibility(
                    visible = visibilityState.isVisible,
                    modifier = Modifier
                        .fillMaxSize()
                        .maintainVisibleOnFocus(delayedVisibilityState = visibilityState),
                ) {
                    Box {
                        PlayerPlaybackRow(
                            player = player,
                            state = visibilityState,
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
                                IconButton(
                                    onClick = { drawerState.setValue(DrawerValue.Open) },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = stringResource(R.string.settings),
                                    )
                                }

                                if (currentCredit != null) {
                                    SkipButton(
                                        onClick = { player.seekTo(currentCredit?.end ?: 0L) },
                                    )
                                }
                            }

                            PlayerTimeRow(
                                player = player,
                                onSeek = { value ->
                                    visibilityState.resetAutoHide()
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
    visibilityState: DelayedVisibilityState,
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
        visible = visibilityState.isVisible || showChapterInfo,
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
        Text(text = stringResource(R.string.skip))
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
        {
                newPosition: Long ->
            if (newPosition in 0..durationMs && newPosition != positionMs) {
                onSeek(newPosition)
            }
        }
    }

    var compactMode by remember {
        mutableStateOf(true)
    }

    Text(
        text = "${formatter(positionMs.milliseconds)} / ${formatter(duration)}",
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
