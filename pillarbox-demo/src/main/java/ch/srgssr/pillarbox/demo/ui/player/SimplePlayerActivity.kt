/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
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
import androidx.lifecycle.lifecycleScope
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.data.Playlist
import ch.srgssr.pillarbox.demo.service.DemoPlaybackService
import ch.srgssr.pillarbox.demo.ui.player.playlist.PlaylistPlayerView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.service.PlaybackService
import ch.srgssr.pillarbox.ui.rememberPlayerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.collectLatest

/**
 * Simple player activity that can handle picture in picture.
 *
 * It handle basic background playback, as it will stop playback when the Activity is destroyed!
 * To have pure background playback with good integration from other device like Auto, Wear, etc... we need *MediaSessionService*
 *
 * For this demo, only the picture in picture button can enable picture in picture.
 * But feel free to call [startPictureInPicture] whenever you decide, for example when [onUserLeaveHint]
 */
class SimplePlayerActivity : ComponentActivity(), ServiceConnection {

    private val playerViewModel: SimplePlayerViewModel by viewModels()
    private var layoutStyle: Int = LAYOUT_PLAYLIST

    private fun readIntent(intent: Intent) {
        intent.extras?.let {
            layoutStyle = it.getInt(ARG_LAYOUT)
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
        readIntent(intent)
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
        // Bind PlaybackService to allow background playback and MediaNotification.
        bindPlaybackService()

        val isPictureInPicturePossible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        val pictureInPictureClick: (() -> Unit)? = if (isPictureInPicturePossible) this::startPictureInPicture else null
        setContent {
            PillarboxTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    /*
                     * Not working when user "leave" fullscreen by touching status or navigation bar.
                     */
                    var fullScreenState by remember {
                        mutableStateOf(false)
                    }
                    val fullScreenToggle: (Boolean) -> Unit = { fullScreenEnabled ->
                        fullScreenState = fullScreenEnabled
                    }
                    FullScreenMode(fullScreen = fullScreenState)
                    val pictureInPicture = playerViewModel.pictureInPictureEnabled.collectAsState()
                    val player = playerViewModel.player
                    val playerState = rememberPlayerState(player = player)
                    when {
                        pictureInPicture.value || layoutStyle == LAYOUT_SIMPLE -> {
                            SimplePlayerView(
                                modifier = Modifier.fillMaxSize(),
                                player = player,
                                playerState = playerState,
                                controlVisible = !pictureInPicture.value,
                                fullScreenEnabled = fullScreenState,
                                fullScreenClicked = fullScreenToggle,
                                pictureInPictureClicked = pictureInPictureClick
                            )
                        }
                        else -> {
                            PlaylistPlayerView(
                                player = player,
                                playerState = playerState,
                                fullScreenEnabled = fullScreenState,
                                fullScreenClicked = fullScreenToggle,
                                pictureInPictureClicked = pictureInPictureClick
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            readIntent(it)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as PlaybackService.ServiceBinder
        binder.setPlayer(playerViewModel.player)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        // Nothing
    }

    private fun startPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(playerViewModel.pictureInPictureRatio.value)
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
        playerViewModel.pictureInPictureEnabled.value = isInPictureInPictureMode
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            playerViewModel.pictureInPictureEnabled.value = isInPictureInPictureMode
        }
    }

    override fun onStart() {
        super.onStart()
        playerViewModel.player.play()
    }

    override fun onStop() {
        super.onStop()
        if (!playerViewModel.pictureInPictureEnabled.value) {
            playerViewModel.player.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
    }

    private fun bindPlaybackService() {
        val intent = Intent(this, DemoPlaybackService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    @Composable
    private fun FullScreenMode(fullScreen: Boolean) {
        val systemUiController = rememberSystemUiController()
        SideEffect {
            systemUiController.isStatusBarVisible = !fullScreen
            systemUiController.isNavigationBarVisible = !fullScreen
        }
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
            val layoutStyle: Int = if (playlist.items.size > 1) LAYOUT_PLAYLIST else LAYOUT_SIMPLE
            val intent = Intent(context, SimplePlayerActivity::class.java)
            intent.putExtra(ARG_PLAYLIST, playlist)
            intent.putExtra(ARG_LAYOUT, layoutStyle)
            context.startActivity(intent)
        }

        /**
         * Start activity [SimplePlayerActivity] with [demoItem]
         */
        fun startActivity(context: Context, item: DemoItem) {
            startActivity(context, Playlist("UniqueItem", listOf(item)))
        }
    }
}
