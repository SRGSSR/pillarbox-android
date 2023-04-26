/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.playlist.CurrentPlaylistView
import ch.srgssr.pillarbox.demo.ui.player.playlist.PlaylistActionsView
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.rememberPlayerDisposable
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Demo player view demonstrate how to integrate PlayerView with Compose
 *
 * doc : https://developer.android.com/jetpack/compose/interop/interop-apis#fragments-in-compose
 */
@Composable
fun DemoPlayerView(
    player: Player,
    isPictureInPictureEnabled: Boolean,
    pipClick: () -> Unit = {}
) {
    val playerState = rememberPlayerDisposable(player = player)
    val fullScreen = remember {
        mutableStateOf(false)
    }
    val scaleMode = remember {
        derivedStateOf {
            if (fullScreen.value) {
                ScaleMode.Zoom
            } else {
                ScaleMode.Fit
            }
        }
    }
    FullScreenMode(fullScreen = fullScreen.value)
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        var playerModifier = Modifier.fillMaxWidth()
        if (!isPictureInPictureEnabled && scaleMode.value == ScaleMode.Fit) {
            playerModifier = playerModifier.aspectRatio(ratio = AspectRatio)
        }
        DemoPlayerSurface(
            player = player,
            defaultAspectRatio = AspectRatio,
            scaleMode = scaleMode.value,
            modifier = playerModifier
        ) {
            if (!isPictureInPictureEnabled) {
                DemoPlaybackControls(
                    modifier = Modifier.matchParentSize(),
                    pictureInPictureClicked = pipClick,
                    fullScreenEnabled = fullScreen.value,
                    fullScreenClicked = {
                        fullScreen.value = !fullScreen.value
                    },
                    player = player,
                    playerState = playerState
                )
            }
        }

        if (!isPictureInPictureEnabled) {
            PlaylistActionsView(player = player, playerState = playerState)
            CurrentPlaylistView(player = player)
        }
    }
}

@Composable
private fun FullScreenMode(fullScreen: Boolean) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.isStatusBarVisible = !fullScreen
        systemUiController.isNavigationBarVisible = !fullScreen
    }
}

private const val AspectRatio = 16 / 9f
