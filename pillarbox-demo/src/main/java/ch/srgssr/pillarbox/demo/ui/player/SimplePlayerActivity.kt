/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.data.Playlist
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Simple player activity using a SimplePlayerFragment
 *
 * @constructor Create empty Simple player activity
 */
class SimplePlayerActivity : ComponentActivity() {

    private val playerViewModel: SimplePlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            intent.extras?.let {
                val playlist: Playlist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getSerializable(ARG_PLAYLIST, Playlist::class.java)!!
                } else {
                    it.getSerializable(ARG_PLAYLIST) as Playlist
                }
                playerViewModel.playUri(playlist.items)
            }
        }
        setContent {
            PillarboxTheme {
                DemoPlayerView(playerViewModel = playerViewModel)
            }
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
