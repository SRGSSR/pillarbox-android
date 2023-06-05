/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.mediacontroller

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import ch.srgssr.pillarbox.analytics.PageView
import ch.srgssr.pillarbox.analytics.SRGPageViewTracker
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerView
import ch.srgssr.pillarbox.demo.ui.player.playlist.PlaylistPlayerView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.ui.rememberPlayerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Media controller activity
 *
 * Using official guide for background playback at https://developer.android.com/guide/topics/media/media3/getting-started/playing-in-background
 *
 * @constructor Create empty Media controller activity
 */
class MediaControllerActivity : ComponentActivity() {
    private val controllerViewModel: MediaControllerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                SRGPageViewTracker.sendPageView(PageView("media controller player", levels = arrayOf("app", "pillarbox")))
            }
        }
        lifecycleScope.launchWhenCreated {
            controllerViewModel.pictureInPictureRatio.collectLatest {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && controllerViewModel.pictureInPictureEnabled.value) {
                    val params = PictureInPictureParams.Builder()
                        .setAspectRatio(controllerViewModel.pictureInPictureRatio.value)
                        .build()
                    setPictureInPictureParams(params)
                }
            }
        }

        setContent {
            PillarboxTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    val mediaBrowser = controllerViewModel.player.collectAsState()
                    mediaBrowser.value?.let { player ->
                        MainView(player = player)
                    }
                }
            }
        }
    }

    @Composable
    private fun MainView(player: Player) {
        val isPictureInPicturePossible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        val pictureInPictureClick: (() -> Unit)? = if (isPictureInPicturePossible) this::startPictureInPicture else null
        var fullScreenState by remember {
            mutableStateOf(false)
        }
        val fullScreenToggle: (Boolean) -> Unit = { fullScreenEnabled ->
            fullScreenState = fullScreenEnabled
        }
        FullScreenMode(fullScreen = fullScreenState)
        val pictureInPicture = controllerViewModel.pictureInPictureEnabled.collectAsState()
        val playerStateful = rememberPlayerState(player = player)
        when {
            pictureInPicture.value -> {
                SimplePlayerView(
                    modifier = Modifier.fillMaxSize(),
                    player = playerStateful,
                    controlVisible = !pictureInPicture.value,
                    fullScreenEnabled = fullScreenState,
                    fullScreenClicked = fullScreenToggle,
                    pictureInPictureClicked = pictureInPictureClick
                )
            }
            else -> {
                PlaylistPlayerView(
                    player = playerStateful,
                    fullScreenEnabled = fullScreenState,
                    fullScreenClicked = fullScreenToggle,
                    pictureInPictureClicked = pictureInPictureClick
                )
            }
        }
    }

    private fun startPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(controllerViewModel.pictureInPictureRatio.value)
                .build()
            enterPictureInPictureMode(params)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            enterPictureInPictureMode()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        controllerViewModel.pictureInPictureEnabled.value = isInPictureInPictureMode
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            controllerViewModel.pictureInPictureEnabled.value = isInPictureInPictureMode
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
}
