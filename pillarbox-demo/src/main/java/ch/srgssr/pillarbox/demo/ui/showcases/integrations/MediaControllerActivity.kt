/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.trackPagView
import ch.srgssr.pillarbox.demo.ui.player.DemoPlayerView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
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
                SRGAnalytics.trackPagView(DemoPageView("media controller player", levels = listOf("app", "pillarbox")))
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
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val mediaBrowser by controllerViewModel.player.collectAsState()
                    mediaBrowser?.let { player ->
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
        val pictureInPicture by controllerViewModel.pictureInPictureEnabled.collectAsState()
        DemoPlayerView(
            player = player,
            pictureInPicture = pictureInPicture,
            pictureInPictureClick = pictureInPictureClick,
            displayPlaylist = true
        )
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
}
