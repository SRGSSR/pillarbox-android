/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.data.Playlist
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Simple player activity using a SimplePlayerFragment
 *
 * @constructor Create empty Simple player activity
 */
class SimplePlayerActivity : ComponentActivity() {

    private val playerViewModel: SimplePlayerViewModel by viewModels()

    private fun playFromIntent(intent: Intent) {
        intent.extras?.let {
            val playlist: Playlist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_PLAYLIST, Playlist::class.java)!!
            } else {
                it.getSerializable(ARG_PLAYLIST) as Playlist
            }
            playerViewModel.playUri(playlist.items)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playFromIntent(intent)
        lifecycleScope.launchWhenCreated {
            playerViewModel.pictureInPictureRatio.collectLatest {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && playerViewModel.pictureInPictureEnabled.value) {
                    val params = PictureInPictureParams.Builder()
                        .setAspectRatio(playerViewModel.pictureInPictureRatio.value)
                        .build()
                    setPictureInPictureParams(params)
                }
            }
        }

        setContent {
            PillarboxTheme {
                DemoPlayerView(playerViewModel = playerViewModel) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startPictureInPicture()
                    } else {
                        Toast.makeText(this, "PiP not supported", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            playFromIntent(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startPictureInPicture() {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(playerViewModel.pictureInPictureRatio.value)
            .build()
        enterPictureInPictureMode(params)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        playerViewModel.pictureInPictureEnabled.value = isInPictureInPictureMode
    }

    override fun onStart() {
        super.onStart()
        playerViewModel.player.play()
    }

    override fun onStop() {
        super.onStop()
        if (playerViewModel.pauseOnBackground.value) {
            playerViewModel.player.pause()
        }
    }

    companion object {
        private const val ARG_PLAYLIST = "ARG_PLAYLIST"

        /**
         * Start activity [SimplePlayerActivity] with [playlist]
         */
        fun startActivity(context: Context, playlist: Playlist) {
            val intent = Intent(context, SimplePlayerActivity::class.java)
            intent.putExtra(ARG_PLAYLIST, playlist)
            context.startActivity(intent)
        }

        /**
         * Start activity [SimplePlayerActivity] with [demoItem]
         */
        fun startActivity(context: Context, item: DemoItem) {
            startActivity(context, Playlist("", listOf(item)))
        }
    }
}
