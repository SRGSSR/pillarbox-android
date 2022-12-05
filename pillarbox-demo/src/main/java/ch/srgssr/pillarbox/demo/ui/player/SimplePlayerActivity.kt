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
import androidx.compose.material.Surface
import androidx.lifecycle.lifecycleScope
import androidx.media3.ui.PlayerNotificationManager
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.data.Playlist
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.notification.PillarboxNotificationManager
import kotlinx.coroutines.flow.collectLatest

/**
 * Simple player activity that can handle picture in picture.
 *
 * For this demo, only the picture in picture button can enable picture in picture.
 * But feel free to call [startPictureInPicture] whenever you decide, for example when [onUserLeaveHint]
 */
class SimplePlayerActivity : ComponentActivity() {

    private val playerViewModel: SimplePlayerViewModel by viewModels()
    private lateinit var notificationManager: PlayerNotificationManager

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
        lifecycleScope.launchWhenCreated {
            playerViewModel.displayNotification.collectLatest { enable ->
                displayNotification(enable)
            }
        }
        notificationManager = PillarboxNotificationManager.Builder(this, NOTIFICATION_ID, "Pillarbox channel")
            .setMediaSession(playerViewModel.mediaSession)
            .setChannelNameResourceId(R.string.app_name)
            .setChannelDescriptionResourceId(R.string.app_name)
            .build()

        setContent {
            PillarboxTheme {
                Surface {
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

    override fun onDestroy() {
        super.onDestroy()
        displayNotification(false)
    }

    private fun displayNotification(enable: Boolean) {
        if (enable) {
            notificationManager.setPlayer(playerViewModel.player)
        } else {
            notificationManager.setPlayer(null)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
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
