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
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import ch.srgssr.pillarbox.analytics.SRGAnalytics
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.service.DemoPlaybackService
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.trackPagView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.qos.QosInfo
import ch.srgssr.pillarbox.player.service.PlaybackService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.URL

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

    private lateinit var playerViewModel: SimplePlayerViewModel
    private var layoutStyle: Int = LAYOUT_PLAYLIST

    private fun readIntent(intent: Intent) {
        layoutStyle = intent.getIntExtra(ARG_LAYOUT, LAYOUT_PLAYLIST)

        val playlist = IntentCompat.getSerializableExtra(intent, ARG_PLAYLIST, Playlist::class.java)
        playlist?.let { playerViewModel.playUri(it.items) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ilHost = IntentCompat.getSerializableExtra(intent, ARG_IL_HOST, URL::class.java) ?: IlHost.DEFAULT
        playerViewModel = ViewModelProvider(this, factory = SimplePlayerViewModel.Factory(application, ilHost))[SimplePlayerViewModel::class.java]
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
            val qosInfo by playerViewModel.player.currentQosInfoAsFlow.collectAsState(QosInfo.Empty)

            PillarboxTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainContent(playerViewModel.player)
                }

                val qosInfoString = remember(qosInfo) {
                    val customLoadTimes = qosInfo.loadTime.custom.entries
                    val customLoadTimesSymbol = if (customLoadTimes.isEmpty()) "─" else "┬"

                    buildString {
                        append("┌ Load time: ").appendLine(qosInfo.loadTime.totalLoadTime)
                        append("├─ Ad: ").appendLine(qosInfo.loadTime.ad)
                        append("├$customLoadTimesSymbol Custom: ").appendLine(qosInfo.loadTime.totalCustomLoadTime)

                        customLoadTimes.forEachIndexed { index, (dataType, loadTime) ->
                            val symbol = if (index == customLoadTimes.size - 1) "└" else "├"

                            append("│$symbol─ ").append(dataType).append(": ").appendLine(loadTime)
                        }

                        append("├─ DRM: ").appendLine(qosInfo.loadTime.drm)
                        append("├─ Manifest: ").appendLine(qosInfo.loadTime.manifest)
                        append("├─ Media: ").appendLine(qosInfo.loadTime.media)
                        append("├─ Media initialization: ").appendLine(qosInfo.loadTime.mediaInitialization)
                        append("├─ Media progressive live: ").appendLine(qosInfo.loadTime.mediaProgressiveLive)
                        append("├─ Time synchronization: ").appendLine(qosInfo.loadTime.timeSynchronization)
                        append("└─ Unknown: ").appendLine(qosInfo.loadTime.unknown).appendLine()
                        append("┌ Play time: ").appendLine(qosInfo.playTime)
                        append("├ Video size: ").append(qosInfo.videoSize.width).append("×").appendLine(qosInfo.videoSize.height)
                        append("├ Dropped frames: ").appendLine(qosInfo.droppedFrames)
                        append("└ Errors: ").append(qosInfo.errors)
                    }
                }

                Text(
                    text = qosInfoString,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.DarkGray.copy(alpha = 0.3f))
                        .padding(MaterialTheme.paddings.small),
                    color = Color.White,
                    lineHeight = 17.sp,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    private fun isPictureInPicturePossible(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
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
        private const val ARG_IL_HOST = "ARG_IL_HOST"
        private const val LAYOUT_SIMPLE = 1
        private const val LAYOUT_PLAYLIST = 0

        /**
         * Start activity [SimplePlayerActivity] with [playlist]
         */
        fun startActivity(context: Context, playlist: Playlist, ilHost: URL = IlHost.DEFAULT) {
            val layoutStyle: Int = if (playlist.items.isEmpty() || playlist.items.size > 1) LAYOUT_PLAYLIST else LAYOUT_SIMPLE
            val intent = Intent(context, SimplePlayerActivity::class.java)
            intent.putExtra(ARG_PLAYLIST, playlist)
            intent.putExtra(ARG_LAYOUT, layoutStyle)
            intent.putExtra(ARG_IL_HOST, ilHost)
            context.startActivity(intent)
        }

        /**
         * Start activity [SimplePlayerActivity] with DemoItem.
         */
        fun startActivity(context: Context, item: DemoItem, ilHost: URL = IlHost.DEFAULT) {
            startActivity(context, Playlist("UniqueItem", listOf(item)), ilHost)
        }
    }
}
