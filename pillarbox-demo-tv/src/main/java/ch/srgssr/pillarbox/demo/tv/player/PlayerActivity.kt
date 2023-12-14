/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.player

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.media3.session.MediaSession
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.tv.player.compose.TvPlayerView
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * Player activity
 *
 * @constructor Create empty Player activity
 */
class PlayerActivity : ComponentActivity() {
    private lateinit var player: PillarboxPlayer
    private lateinit var mediaSession: MediaSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = PlayerModule.provideDefaultPlayer(this)
        mediaSession = MediaSession.Builder(this, player)
            .build()
        val demoItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(ARG_ITEM, DemoItem::class.java)
        } else {
            intent.getSerializableExtra(ARG_ITEM) as DemoItem?
        }
        demoItem?.let {
            player.setMediaItem(it.toMediaItem())
        }
        player.apply {
            player.prepare()
            player.trackingEnabled = false
            player.playWhenReady = true
        }

        setContent {
            PillarboxTheme {
                TvPlayerView(
                    player = player,
                    modifier = Modifier.fillMaxSize()
                )
            }
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
        mediaSession.release()
        player.stop()
        player.release()
    }

    companion object {
        private const val ARG_ITEM = "demo_item"

        /**
         * Start player with Leanback fragment.
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
