/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.media3.common.Player
import androidx.tv.material3.Button
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.tv.extension.onDpadEvent
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerError
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerPlaybackRow
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.settings.PlaybackSettingsDrawer
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.ui.extension.currentMediaMetadataAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentChapterAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentTimeIntervalAsState
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.widget.maintainVisibleOnFocus
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import ch.srgssr.pillarbox.ui.widget.rememberDelayedVisibilityState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Tv player view
 *
 * @param player
 * @param modifier
 */
@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun PlayerView(
    player: Player,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val visibilityState = rememberDelayedVisibilityState(player = player, visible = true)
    val timeInterval by player.getCurrentTimeIntervalAsState()

    LaunchedEffect(drawerState.currentValue) {
        when (drawerState.currentValue) {
            DrawerValue.Closed -> visibilityState.show()
            DrawerValue.Open -> visibilityState.hide()
        }
    }

    PlaybackSettingsDrawer(
        player = player,
        drawerState = drawerState,
        modifier = modifier
    ) {
        val error by player.playerErrorAsState()
        if (error != null) {
            PlayerError(modifier = Modifier.fillMaxSize(), playerError = error!!, onRetry = player::prepare)
        } else {
            val currentChapter by player.getCurrentChapterAsState()
            PlayerSurface(
                player = player,
                modifier = Modifier
                    .fillMaxSize()
                    .onDpadEvent(
                        onEnter = {
                            visibilityState.show()
                            true
                        }
                    )
                    .focusable(true)
            )
            var chapterInfoVisibility by remember {
                mutableStateOf(currentChapter != null)
            }
            LaunchedEffect(currentChapter) {
                chapterInfoVisibility = currentChapter != null
                if (chapterInfoVisibility) {
                    delay(5.seconds)
                    chapterInfoVisibility = false
                }
            }
            AnimatedVisibility(
                visible = !visibilityState.isVisible && chapterInfoVisibility,
                enter = expandVertically { it },
                exit = shrinkVertically { it }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    currentChapter?.let {
                        MediaMetadataView(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .wrapContentHeight()
                                .align(Alignment.BottomStart),
                            mediaMetadata = it.mediaMetadata
                        )
                    }
                }
            }
            AnimatedVisibility(timeInterval != null) {
                Button(
                    onClick = { player.seekTo(timeInterval?.end ?: 0L) },
                    modifier = Modifier.padding(MaterialTheme.paddings.baseline),
                ) {
                    Text(text = stringResource(R.string.skip))
                }
            }
            AnimatedVisibility(
                visible = visibilityState.isVisible,
                enter = expandVertically { it },
                exit = shrinkVertically { it }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .maintainVisibleOnFocus(delayedVisibilityState = visibilityState),
                    contentAlignment = Alignment.Center
                ) {
                    PlayerPlaybackRow(
                        player = player,
                        state = visibilityState
                    )

                    val currentMediaMetadata by player.currentMediaMetadataAsState()
                    MediaMetadataView(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .wrapContentHeight()
                            .align(Alignment.BottomStart),
                        mediaMetadata = currentChapter?.mediaMetadata ?: currentMediaMetadata
                    )

                    IconButton(
                        onClick = { drawerState.setValue(DrawerValue.Open) },
                        modifier = Modifier
                            .padding(MaterialTheme.paddings.baseline)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            }
            BackHandler(enabled = visibilityState.isVisible) {
                visibilityState.hide()
            }
        }
    }
}
