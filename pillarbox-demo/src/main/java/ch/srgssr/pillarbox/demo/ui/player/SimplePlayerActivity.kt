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
import androidx.media3.common.util.NotificationUtil
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import ch.srgssr.pillarbox.demo.R
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
    private lateinit var mediaSession: MediaSession
    private lateinit var notificationManager: PlayerNotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
         * Don't work for background playback, after a while(~1min) may receive socket time out, if not charging.
         * This sample just show how to display Notification and usage of MediaSession.
         */
        notificationManager = PlayerNotificationManager.Builder(this, NOTIFICATION_ID, "Pillarbox channel")
            .setChannelImportance(NotificationUtil.IMPORTANCE_LOW)
            .setChannelNameResourceId(R.string.app_name)
            .setChannelDescriptionResourceId(R.string.app_name)
            .build()
        notificationManager.setPlayer(playerViewModel.player)
        mediaSession = MediaSession.Builder(this, playerViewModel.player)
            .build()

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
                DemoPlayerView(player = playerViewModel.player) { notificationEnabled ->
                    if (notificationEnabled) {
                        notificationManager.setPlayer(playerViewModel.player)
                    } else {
                        notificationManager.setPlayer(null)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        notificationManager.setPlayer(null)
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
