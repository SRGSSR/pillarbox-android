/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations.auto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.trackPagView
import ch.srgssr.pillarbox.demo.ui.player.DemoPlayerView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.ui.state.rememberPipManager

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

    @Composable
    private fun MainView(player: PillarboxPlayer) {
        DemoPlayerView(
            player = player,
            pipManager = rememberPipManager(player = player, autoEnterEnabled = true),
            displayPlaylist = true
        )
    }

    override fun onResume() {
        super.onResume()
        SRGAnalytics.trackPagView(DemoPageView("media browser player", levels = listOf("app", "pillarbox")))
    }
}
