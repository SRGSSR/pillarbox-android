/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.PlayerView
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession

/**
 * Player activity
 *
 * @constructor Create empty Player activity
 */
class PlayerActivity : ComponentActivity() {
    private lateinit var player: PillarboxExoPlayer
    private lateinit var mediaSession: PillarboxMediaSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = PlayerModule.provideDefaultPlayer(this)
        mediaSession = PillarboxMediaSession.Builder(this, player)
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

        setContent {
            PillarboxTheme {
                PlayerView(
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
