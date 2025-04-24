/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations.cast

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import ch.srgssr.androidx.mediarouter.compose.MediaRouteButton
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerView

/**
 * Showcase for cast integration
 */
@Composable
fun CastShowcase() {
    val mainViewModel: CastShowcaseViewModel = viewModel()
    val player by mainViewModel.currentPlayer.collectAsState()
    Box(modifier = Modifier) {
        ExoPlayerView(
            player = player,
            modifier = Modifier
                .background(color = androidx.compose.ui.graphics.Color.Black)
                .fillMaxSize(),
            setupView = {
                setShowShuffleButton(true)
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

        MediaRouteButton(
            modifier = Modifier.align(Alignment.TopEnd),
            routeSelector = MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                .build(),
        )
    }
}
