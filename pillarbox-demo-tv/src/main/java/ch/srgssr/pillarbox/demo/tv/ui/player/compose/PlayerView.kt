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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.media3.common.Player
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.rememberDrawerState
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.tv.extension.onDpadEvent
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerError
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.PlayerPlaybackRow
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.settings.PlaybackSettingsDrawer
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.ui.extension.playerErrorAsState
import ch.srgssr.pillarbox.ui.widget.maintainVisibleOnFocus
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import ch.srgssr.pillarbox.ui.widget.rememberDelayedVisibilityState

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
