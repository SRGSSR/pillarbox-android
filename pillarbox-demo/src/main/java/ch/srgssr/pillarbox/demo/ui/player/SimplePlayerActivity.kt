/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.service.DemoPlaybackService
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.trackPagView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.service.PlaybackService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Simple player activity that can handle picture in picture.
 *
 * It handles basic background playback, as it will stop playback when the Activity is destroyed!
 * To have pure background playback with good integration from other devices like Auto, Wear, etc... we need *MediaSessionService*
 *
 * For this demo, only the picture in picture button can enable picture in picture.
 * But feel free to call [startPictureInPicture] whenever you decide, for example, when [onUserLeaveHint]
 */
class SimplePlayerActivity : ComponentActivity(), ServiceConnection {

    private val playerViewModel by viewModels<SimplePlayerViewModel>()
    private val layoutStyle by lazy { intent.getIntExtra(ARG_LAYOUT, LAYOUT_PLAYLIST) }

    private fun readIntent(intent: Intent) {
        val playlist = IntentCompat.getSerializableExtra(intent, ARG_PLAYLIST, Playlist::class.java)
        playlist?.let { playerViewModel.playUri(it.items) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK),
        )

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

        // Bind PlaybackService to allow background playback and MediaNotification.
        bindPlaybackService()
        setContent {
            PillarboxTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainContent(playerViewModel.player)
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
    private fun MainContent(player: Player) {
        val pictureInPictureClick: (() -> Unit)? = if (isPictureInPicturePossible()) this::startPictureInPicture else null
        val pictureInPicture by playerViewModel.pictureInPictureEnabled.collectAsState()
        DemoPlayerView(
            player = player,
            pictureInPicture = pictureInPicture,
            pictureInPictureClick = pictureInPictureClick,
            displayPlaylist = layoutStyle == LAYOUT_PLAYLIST,
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readIntent(intent)
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
        handlePictureInPictureChanges(isInPictureInPictureMode)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.N..Build.VERSION_CODES.N_MR1) {
            handlePictureInPictureChanges(isInPictureInPictureMode)
        }
    }

    private fun handlePictureInPictureChanges(enabled: Boolean) {
        // detect if PiP is dismissed by the user
        val isPictureInPictureStopped = lifecycle.currentState == Lifecycle.State.CREATED
        playerViewModel.pictureInPictureEnabled.value = enabled
        if (isPictureInPictureStopped) {
            finishAndRemoveTask()
        }
    }

    override fun onResume() {
        super.onResume()
        SRGAnalytics.trackPagView(DemoPageView("simple player", levels = listOf("app", "pillarbox")))
    }

    override fun onStart() {
        super.onStart()
        playerViewModel.player.play()
    }

    override fun onStop() {
        super.onStop()
        playerViewModel.player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
    }

    private fun bindPlaybackService() {
        val intent = Intent(this, DemoPlaybackService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
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
