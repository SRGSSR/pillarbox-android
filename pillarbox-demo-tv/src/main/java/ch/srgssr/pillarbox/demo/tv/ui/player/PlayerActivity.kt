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
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import ch.srgssr.pillarbox.cast.receiver.PillarboxCastReceiver
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsRepository
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsViewModel
import ch.srgssr.pillarbox.demo.shared.ui.settings.MetricsOverlayOptions
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.PlayerView
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession

/**
 * Player activity
 *
 * @constructor Create empty Player activity
 */
class PlayerActivity : ComponentActivity() {
    private lateinit var player: PillarboxPlayer
    private lateinit var mediaSession: PillarboxMediaSession
    private val appSettingsViewModel by viewModels<AppSettingsViewModel> {
        AppSettingsViewModel.Factory(AppSettingsRepository(this))
    }
    private var pillarboxCastReceiver: PillarboxCastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = PlayerModule.provideDefaultPlayer(this)
        mediaSession = PillarboxMediaSession.Builder(this, player)
            .build()
        pillarboxCastReceiver = PillarboxCastReceiver(mediaSession)
        val demoItem = IntentCompat.getSerializableExtra(intent, ARG_ITEM, DemoItem::class.java)
        demoItem?.let {
            player.setMediaItem(it.toMediaItem())
        }
        player.apply {
            prepare()
            trackingEnabled = false
            playWhenReady = true
        }

        pillarboxCastReceiver?.onNewIntent(intent)

        setContent {
            PillarboxTheme {
                val appSettings by appSettingsViewModel.currentAppSettings.collectAsState()

                Surface(
                    colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                ) {
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (pillarboxCastReceiver?.onNewIntent(intent) == true) {
            return
        } else {
            Log.w(TAG, "Can't handle intent $intent")
        }
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
        pillarboxCastReceiver?.release()
        mediaSession.release()
        player.release()
    }

    companion object {
        private const val ARG_ITEM = "demo_item"
        private const val TAG = "PlayerActivity"

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
