/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations.auto

import android.app.PictureInPictureParams
import android.content.pm.PackageManager
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
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.trackPagView
import ch.srgssr.pillarbox.demo.ui.player.DemoPlayerView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Media controller activity that handles a MediaBrowserService to play content with Android Auto.
 *
 * Using official guides for background playback at https://developer.android.com/guide/topics/media/media3/getting-started/playing-in-background
 *
 * @constructor Create empty Media controller activity
 */
class MediaBrowserActivity : ComponentActivity() {
    private val browserViewModel: MediaBrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lifecycleScope.launch {
                browserViewModel.pictureInPictureRatio.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collectLatest {
                    val params = PictureInPictureParams.Builder()
                        .setAspectRatio(it)
                        .build()
                    setPictureInPictureParams(params)
                }
            }
        }

        setContent {
            PillarboxTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val mediaBrowser by browserViewModel.player.collectAsState()
                    mediaBrowser?.let { player ->
                        MainView(player = player)
                    }
                }
            }
        }
    }

    private fun isPictureInPicturePossible(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        }
        return false
    }

    @Composable
    private fun MainView(player: Player) {
        val onPictureInPictureClick: (() -> Unit)? = if (isPictureInPicturePossible()) this::startPictureInPicture else null
        val pictureInPictureEnabled by browserViewModel.pictureInPictureEnabled.collectAsState()
        DemoPlayerView(
            player = player,
            pictureInPictureEnabled = pictureInPictureEnabled,
            onPictureInPictureClick = onPictureInPictureClick,
            displayPlaylist = true
        )
    }

    private fun startPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(browserViewModel.pictureInPictureRatio.value)
                .build()
            enterPictureInPictureMode(params)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            @Suppress("DEPRECATION")
            enterPictureInPictureMode()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        browserViewModel.pictureInPictureEnabled.value = isInPictureInPictureMode
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            browserViewModel.pictureInPictureEnabled.value = isInPictureInPictureMode
        }
    }

    override fun onResume() {
        super.onResume()
        SRGAnalytics.trackPagView(DemoPageView("media browser player", levels = listOf("app", "pillarbox")))
    }
}
