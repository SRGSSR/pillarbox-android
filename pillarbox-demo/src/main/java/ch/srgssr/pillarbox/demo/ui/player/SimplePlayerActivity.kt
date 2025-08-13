/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.trackPagView
import ch.srgssr.pillarbox.demo.ui.player.state.rememberPictureInPictureButtonState
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.PillarboxPlayer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Simple player activity that can handle picture in picture.
 *
 * This implementation doesn't support background playback, it doesn't stop when the `Activity` starts picture in picture.
 * To have pure background playback with good integration from other devices like Auto, Wear, etc... we need *MediaSessionService*
 *
 * For this demo, only the picture in picture button can enable picture in picture.
 * But feel free to call [enterPictureInPictureMode] whenever you decide, for example, when [onUserLeaveHint]
 */
class SimplePlayerActivity : ComponentActivity() {

    private val playerViewModel by viewModels<SimplePlayerViewModel>()
    private val layoutStyle by lazy { intent.getIntExtra(ARG_LAYOUT, LAYOUT_PLAYLIST) }

    private fun readIntent(intent: Intent) {
        val playlist = IntentCompat.getSerializableExtra(intent, ARG_PLAYLIST, Playlist::class.java)
        playlist?.let { playerViewModel.playUri(it.items) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        readIntent(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lifecycleScope.launch {
                playerViewModel.pictureInPictureRatio.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collectLatest {
                    val params = PictureInPictureParams.Builder()
                        .setAspectRatio(it)
                        .build()
                    setPictureInPictureParams(params)
                }
            }
        }

        setContent {
            PillarboxTheme {
                Scaffold(containerColor = Color.Black) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MainContent(playerViewModel.player)
                    }
                }
            }
        }
    }

    @Composable
    private fun MainContent(player: PillarboxPlayer) {
        val pictureInPictureButtonState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rememberPictureInPictureButtonState {
                PictureInPictureParams.Builder()
                    .setAspectRatio(playerViewModel.pictureInPictureRatio.value)
                    .build()
            }
        } else {
            rememberPictureInPictureButtonState()
        }

        DemoPlayerView(
            player = player,
            isPictureInPictureEnabled = pictureInPictureButtonState.isEnabled,
            isInPictureInPicture = pictureInPictureButtonState.isInPictureInPicture,
            onPictureInPictureClick = pictureInPictureButtonState::onClick,
            displayPlaylist = layoutStyle == LAYOUT_PLAYLIST,
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        SRGAnalytics.trackPagView(DemoPageView("simple player", levels = listOf("app", "pillarbox")))
    }

    override fun onStart() {
        super.onStart()
        playerViewModel.resumePlayback()
    }

    override fun onStop() {
        super.onStop()
        playerViewModel.stopPlayback()
    }

    companion object {
        private const val ARG_PLAYLIST = "ARG_PLAYLIST"
        private const val ARG_LAYOUT = "ARG_LAYOUT"
        private const val LAYOUT_SIMPLE = 1
        private const val LAYOUT_PLAYLIST = 0

        /**
         * Start activity [SimplePlayerActivity] with [playlist]
         */
        fun startActivity(context: Context, playlist: Playlist) {
            val layoutStyle: Int = if (playlist.items.isEmpty() || playlist.items.size > 1) LAYOUT_PLAYLIST else LAYOUT_SIMPLE
            val intent = Intent(context, SimplePlayerActivity::class.java)
            intent.putExtra(ARG_PLAYLIST, playlist)
            intent.putExtra(ARG_LAYOUT, layoutStyle)
            context.startActivity(intent)
        }

        /**
         * Start activity [SimplePlayerActivity] with DemoItem.
         */
        fun startActivity(context: Context, item: DemoItem) {
            startActivity(context, Playlist("UniqueItem", listOf(item), "en-CH"))
        }
    }
}
