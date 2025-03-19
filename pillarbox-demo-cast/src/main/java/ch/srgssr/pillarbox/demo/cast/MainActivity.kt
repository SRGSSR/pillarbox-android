/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.common.util.RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL
import androidx.media3.common.util.RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE
import androidx.media3.ui.PlayerView
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import ch.srgssr.androidx.mediarouter.compose.MediaRouteButton
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.demo.cast.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.cast.ui.theme.paddings
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerView
import ch.srgssr.pillarbox.ui.extension.getCurrentMediaItemIndexAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentMediaItemsAsState

/**
 * Activity showing how to use Cast with Pillarbox.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val player by mainViewModel.currentPlayer.collectAsState()

            PillarboxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MainView(player = player, modifier = Modifier)
                        MediaRouteButton(
                            modifier = Modifier.align(Alignment.TopEnd),
                            routeSelector = MediaRouteSelector.Builder()
                                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                                .build(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainView(player: Player, modifier: Modifier) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        ExoPlayerView(
            player = player,
            modifier = Modifier
                .background(color = androidx.compose.ui.graphics.Color.Black)
                .weight(0.5f)
                .fillMaxSize(),
            setupView = {
                setShowShuffleButton(true)
                setRepeatToggleModes(REPEAT_TOGGLE_MODE_ONE or REPEAT_TOGGLE_MODE_ALL)
                setShowSubtitleButton(true)
                setShutterBackgroundColor(Color.BLACK)
                if (player is PillarboxCastPlayer) {
                    artworkDisplayMode = PlayerView.ARTWORK_DISPLAY_MODE_FIT
                    defaultArtwork = ContextCompat.getDrawable(context, R.drawable.ic_cast_128)
                } else {
                    artworkDisplayMode = PlayerView.ARTWORK_DISPLAY_MODE_OFF
                }
            },
        )
        val mediaItems by player.getCurrentMediaItemsAsState()
        val currentMediaItemIndex by player.getCurrentMediaItemIndexAsState()
        PlaylistView(
            items = mediaItems,
            currentMediaItemIndex = currentMediaItemIndex,
            modifier = Modifier
                .padding(MaterialTheme.paddings.small)
                .weight(0.5f)
                .fillMaxWidth()
        ) {
            player.seekToDefaultPosition(it)
            if (player.playbackState == Player.STATE_ENDED || player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
            player.play()
        }
    }
}
