/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.srgssr.pillarbox.demo.ui.ShowSystemUi
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerBottomToolbar
import ch.srgssr.pillarbox.demo.ui.player.playlist.PlaylistView
import ch.srgssr.pillarbox.demo.ui.player.settings.PlaybackSettingsContent
import ch.srgssr.pillarbox.ui.ScaleMode
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator

/**
 * Demo player
 *
 * @param player The [Player] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param pictureInPicture The picture in picture state.
 * @param pictureInPictureClick he picture in picture button action. If null no button.
 * @param displayPlaylist If it displays playlist ui or not.
 */
@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun DemoPlayerView(
    player: Player,
    modifier: Modifier = Modifier,
    pictureInPicture: Boolean = false,
    pictureInPictureClick: (() -> Unit)? = null,
    displayPlaylist: Boolean = false,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    LaunchedEffect(bottomSheetNavigator.navigatorSheetState.isVisible) {
        if (!bottomSheetNavigator.navigatorSheetState.isVisible) {
            navController.popBackStack(route = RoutePlayer, false)
        }
    }
    ModalBottomSheetLayout(
        modifier = modifier,
        bottomSheetNavigator = bottomSheetNavigator
    ) {
        NavHost(navController, startDestination = RoutePlayer) {
            composable(route = "player") {
                PlayerContent(
                    player = player,
                    pictureInPicture = pictureInPicture,
                    pictureInPictureClick = pictureInPictureClick,
                    displayPlaylist = displayPlaylist,
                ) {
                    navController.navigate(route = RouteSettings) {
                        launchSingleTop = true
                    }
                }
            }
            bottomSheet(route = RouteSettings) {
                LaunchedEffect(pictureInPicture) {
                    if (pictureInPicture) {
                        navController.popBackStack()
                    }
                }
                PlaybackSettingsContent(player = player) {
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
private fun PlayerContent(
    player: Player,
    pictureInPicture: Boolean = false,
    pictureInPictureClick: (() -> Unit)? = null,
    displayPlaylist: Boolean = false,
    optionClicked: (() -> Unit)? = null
) {
    var fullScreenState by remember {
        mutableStateOf(false)
    }
    val fullScreenToggle: (Boolean) -> Unit = { fullScreenEnabled ->
        fullScreenState = fullScreenEnabled
    }
    ShowSystemUi(isShowed = !fullScreenState)
    Column(modifier = Modifier.fillMaxSize()) {
        var pinchScaleMode by remember(fullScreenState) {
            mutableStateOf(ScaleMode.Fit)
        }
        val playerModifier = Modifier
            .fillMaxWidth()
            .weight(1.0f)
        val scalableModifier = if (fullScreenState) {
            playerModifier.then(
                Modifier.pointerInput(pinchScaleMode) {
                    var lastZoomValue = 1.0f
                    detectTransformGestures(true) { _, _, zoom, _ ->
                        lastZoomValue *= zoom
                        pinchScaleMode = if (lastZoomValue < 1.0f) ScaleMode.Fit else ScaleMode.Crop
                    }
                }
            )
        } else {
            playerModifier
        }
        PlayerView(
            modifier = scalableModifier,
            player = player,
            controlsToggleable = !pictureInPicture,
            controlsVisible = !pictureInPicture,
            scaleMode = pinchScaleMode
        ) {
            PlayerBottomToolbar(
                modifier = Modifier.fillMaxWidth(),
                fullScreenClicked = fullScreenToggle,
                fullScreenEnabled = fullScreenState,
                pictureInPictureClicked = pictureInPictureClick,
                optionClicked = optionClicked
            )
        }
        if (displayPlaylist && !pictureInPicture && !fullScreenState) {
            PlaylistView(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                player = player
            )
        }
    }
}

private const val RoutePlayer = "player"
private const val RouteSettings = "settings"
