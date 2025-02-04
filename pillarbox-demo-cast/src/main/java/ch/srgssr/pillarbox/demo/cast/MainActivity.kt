/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import ch.srgssr.pillarbox.cast.widget.CastButton
import ch.srgssr.pillarbox.demo.cast.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerView

/**
 * Activity showing how to use Cast with Pillarbox.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val player: PillarboxPlayer by mainViewModel.currentPlayer

            PillarboxTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        CastButton()
                    },
                ) { innerPadding ->
                    ExoPlayerView(
                        player = player,
                        modifier = Modifier
                            .background(color = androidx.compose.ui.graphics.Color.Black)
                            .padding(innerPadding)
                            .fillMaxSize(),
                        setupView = {
                            setShowShuffleButton(true)
                            setShowSubtitleButton(true)
                            artworkDisplayMode = PlayerView.ARTWORK_DISPLAY_MODE_FIT
                            setShutterBackgroundColor(Color.BLACK)
                            defaultArtwork = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_cast_128)
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MainPreview() {
    PillarboxTheme {
        CastButton()
    }
}
