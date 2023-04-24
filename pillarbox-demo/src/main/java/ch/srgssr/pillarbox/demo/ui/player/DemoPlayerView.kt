/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ch.srgssr.pillarbox.demo.ui.player.playlist.CurrentPlaylistView
import ch.srgssr.pillarbox.demo.ui.player.playlist.PlaylistActionsView
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.rememberPlayerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Demo player view demonstrate how to integrate PlayerView with Compose
 *
 * doc : https://developer.android.com/jetpack/compose/interop/interop-apis#fragments-in-compose
 *
 * @param playerViewModel
 * @param pipClick picture in picture button click action
 */
@Composable
fun DemoPlayerView(
    playerViewModel: SimplePlayerViewModel,
    pipClick: () -> Unit = {}
) {
    val player = playerViewModel.player
    val playerState = rememberPlayerState(player = player)
    val hideUi = playerViewModel.pictureInPictureEnabled.collectAsState()
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
        DemoPlayerSurface(
            player = player,
            defaultAspectRatio = 1.0f,
            scaleMode = scaleMode.value,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        ) {
            if (!hideUi.value) {
                DemoPlaybackControls(modifier = Modifier.matchParentSize(), player = player)
            }
        }

        if (!hideUi.value) {
            PlaylistActionsView(player = player, playerState = playerState)
            CurrentPlaylistView(player = player, playerState = playerState)
        }
    }
}

@Composable
private fun DemoControlView(
    modifier: Modifier = Modifier,
    playerViewModel: SimplePlayerViewModel,
    isFullScreen: Boolean,
    fullScreenClick: (Boolean) -> Unit,
    pipClick: () -> Unit = {}
) {
    val pauseOnBackground = playerViewModel.pauseOnBackground.collectAsState()
    Column(modifier = modifier) {
        Row {
            IconButton(onClick = pipClick) {
                Icon(imageVector = Icons.Default.PictureInPicture, contentDescription = "Go in picture in picture")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.wrapContentWidth(),
                text = "Background playback",
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold
            )
            Switch(checked = !pauseOnBackground.value, onCheckedChange = { playerViewModel.togglePauseOnBackground() })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.wrapContentWidth(),
                text = "Fullscreen",
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold
            )
            Switch(checked = isFullScreen, onCheckedChange = fullScreenClick)
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
