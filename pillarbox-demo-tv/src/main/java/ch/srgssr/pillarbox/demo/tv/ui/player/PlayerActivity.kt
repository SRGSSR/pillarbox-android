/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.tv.material3.MaterialTheme
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsRepository
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsViewModel
import ch.srgssr.pillarbox.demo.shared.ui.settings.MetricsOverlayOptions
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.PlayerView
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.tv.CastReceiverContext
import com.google.android.gms.cast.tv.media.MediaCommandCallback
import com.google.android.gms.cast.tv.media.MediaLoadCommandCallback
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.QueueUpdateRequestData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource

/**
 * Player activity
 *
 * @constructor Create empty Player activity
 */
class PlayerActivity : ComponentActivity() {
    private lateinit var player: PillarboxExoPlayer
    private lateinit var mediaSession: PillarboxMediaSession
    private val appSettingsViewModel by viewModels<AppSettingsViewModel> {
        AppSettingsViewModel.Factory(AppSettingsRepository(this))
    }
    private var mediaManager: MediaManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = PlayerModule.provideDefaultPlayer(this)
        mediaSession = PillarboxMediaSession.Builder(this, player)
            .setCallback(object : PillarboxMediaSession.Callback {
            })
            .build()
        val demoItem = IntentCompat.getSerializableExtra(intent, ARG_ITEM, DemoItem::class.java)
        demoItem?.let {
            player.setMediaItem(it.toMediaItem())
        }
        player.apply {
            player.prepare()
            player.trackingEnabled = false
            player.playWhenReady = true
        }

        mediaManager = CastReceiverContext.getInstance().mediaManager
        mediaManager?.setSessionCompatToken(mediaSession.mediaSession.sessionCompatToken)

        mediaManager?.setMediaCommandCallback(object : MediaCommandCallback() {
            override fun onQueueUpdate(p0: String?, requestData: QueueUpdateRequestData): Task<Void?> {
                Log.d("PlayerActivity", "onQueueUpdate currentItemId = ${requestData.currentItemId} jump = ${requestData.jump}")
                var newItemId = MediaQueueItem.INVALID_ITEM_ID
                if (requestData.jump != null) {
                    newItemId = requestData.jump!!
                } else if (requestData.currentItemId != null) {
                    newItemId = requestData.currentItemId!!
                }
                if (newItemId != MediaQueueItem.INVALID_ITEM_ID) {
                    mediaManager?.mediaQueueManager?.currentItemId = newItemId
                    player.seekTo(newItemId - 1, 0L)
                    mediaManager?.broadcastMediaStatus()
                }
                return super.onQueueUpdate(p0, requestData)
            }
        })
        mediaManager?.setMediaLoadCommandCallback(object : MediaLoadCommandCallback() {
            override fun onLoad(senderId: String?, loadRequest: MediaLoadRequestData): Task<MediaLoadRequestData?> {
                val mediaInfo = loadRequest.mediaInfo
                val queueData = loadRequest.queueData
                queueData?.let {
                    val mediaItems = it.items.orEmpty().mapNotNull {
                        val mediaInfo = it.media
                        if (mediaInfo == null) return@mapNotNull null
                        MediaItem.Builder().setUri(mediaInfo.contentUrl)
                            .setMediaId(mediaInfo.contentId)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle("Item from cast ${it.itemId}")
                                    .build()
                            )
                            .build()
                    }
                    val currentIndex = it.startIndex
                    val position = it.startTime
                    player.setMediaItems(mediaItems, currentIndex, position)
                } ?: {
                    mediaInfo?.let {
                        val mediaItem = MediaItem.Builder().setUri(mediaInfo.contentUrl)
                            .setMediaId(mediaInfo.contentId)
                            .build()
                        player.setMediaItem(
                            mediaItem
                        )
                    }
                }

                player.prepare()
                player.play()
                mediaManager?.mediaStatusModifier?.clear()
                mediaManager?.setDataFromLoad(loadRequest)
                mediaManager?.broadcastMediaStatus()

                return TaskCompletionSource<MediaLoadRequestData>().apply { setResult(loadRequest) }.task
            }
        })
        mediaManager?.onNewIntent(intent)

        setContent {
            PillarboxTheme {
                val appSettings by appSettingsViewModel.currentAppSettings.collectAsState()

                PlayerView(
                    player = player,
                    modifier = Modifier.fillMaxSize(),
                    metricsOverlayEnabled = appSettings.metricsOverlayEnabled,
                    metricsOverlayOptions = MetricsOverlayOptions(
                        textColor = appSettings.metricsOverlayTextColor.color,
                        textStyle = when (appSettings.metricsOverlayTextSize) {
                            AppSettings.TextSize.Small -> MaterialTheme.typography.bodySmall
                            AppSettings.TextSize.Medium -> MaterialTheme.typography.bodyMedium
                            AppSettings.TextSize.Large -> MaterialTheme.typography.bodyLarge
                        },
                    ),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (mediaManager?.onNewIntent(intent) == true) {
            return
        } else {
            Log.w("Coucou", "can't handle so much intent $intent")
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        player.play()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        player.stop()
        player.release()
        mediaManager?.setSessionCompatToken(null)
    }

    companion object {
        private const val ARG_ITEM = "demo_item"

        /**
         * Start player.
         *
         * @param context
         * @param demoItem The item to play.
         */
        fun startPlayer(context: Context, demoItem: DemoItem) {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra(ARG_ITEM, demoItem)
            context.startActivity(intent)
        }
    }
}
