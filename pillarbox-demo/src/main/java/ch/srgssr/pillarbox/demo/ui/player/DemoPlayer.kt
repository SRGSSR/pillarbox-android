/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.Player
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.srgssr.pillarbox.demo.ui.player.playlist.PlaylistPlayerView
import ch.srgssr.pillarbox.demo.ui.player.settings.PlaybackSettingsContent
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Demo player
 *
 * @param player The [Player] to observe.
 * @param modifier The modifier to be applied to the layout.
 * @param pictureInPicture The picture in picture state.
 * @param pictureInPictureClick he picture in picture button action. If null no button.
 * @param displayPlaylist If it displays playlist ui or not.
 */
@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
@Composable
fun DemoPlayer(
    player: Player,
    modifier: Modifier = Modifier,
    pictureInPicture: Boolean = false,
    pictureInPictureClick: (() -> Unit)? = null,
    displayPlaylist: Boolean = false,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    ModalBottomSheetLayout(
        modifier = modifier,
        bottomSheetNavigator = bottomSheetNavigator
    ) {
        NavHost(navController, startDestination = "player") {
            composable(route = "player") {
                PlayerContent(
                    player = player,
                    pictureInPicture = pictureInPicture,
                    pictureInPictureClick = pictureInPictureClick,
                    displayPlaylist = displayPlaylist,
                ) {
                    navController.navigate(route = "settings") {
                        launchSingleTop = true
                    }
                }
            }
            bottomSheet(route = "settings") {
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
    FullScreenMode(fullScreen = fullScreenState)
    if (displayPlaylist && !pictureInPicture) {
        PlaylistPlayerView(
            player = player,
            fullScreenEnabled = fullScreenState,
            fullScreenClicked = fullScreenToggle,
            pictureInPictureClicked = pictureInPictureClick,
            optionClicked = optionClicked
        )
    } else {
        SimplePlayerView(
            modifier = Modifier.fillMaxSize(),
            player = player,
            controlVisible = !pictureInPicture,
            fullScreenEnabled = fullScreenState,
            fullScreenClicked = fullScreenToggle,
            pictureInPictureClicked = pictureInPictureClick,
            optionClicked = optionClicked
        )
    }
}

@Composable
private fun FullScreenMode(fullScreen: Boolean) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.isSystemBarsVisible = !fullScreen
        /*
         * Swipe doesn't "disable" fullscreen but, buttons are under the navigation bar.
         */
        systemUiController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
