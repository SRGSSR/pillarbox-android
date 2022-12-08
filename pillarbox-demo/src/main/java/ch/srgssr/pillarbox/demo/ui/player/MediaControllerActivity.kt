/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import ch.srgssr.pillarbox.demo.service.DemoMediaSessionService
import ch.srgssr.pillarbox.player.R
import ch.srgssr.pillarbox.player.service.MediaControllerConnection
import kotlinx.coroutines.flow.collectLatest

/**
 * Media controller activity
 *
 * Using official guide for background playback at https://developer.android.com/guide/topics/media/media3/getting-started/playing-in-background
 *
 * @constructor Create empty Media controller activity
 */
class MediaControllerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private val listener = ComponentListener()

    private lateinit var controllerConnection: MediaControllerConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_controller)
        playerView = findViewById(R.id.player_view)
        controllerConnection = MediaControllerConnection(this, ComponentName(this, DemoMediaSessionService::class.java))

        lifecycleScope.launchWhenResumed {
            controllerConnection.mediaController.collectLatest { controller ->
                Log.d("Coucou", "Controller received $controller")
                setPlayer(controller)
            }
        }
    }

    private fun setPlayer(player: Player?) {
        player?.addListener(listener)
        playerView.player = player
    }

    override fun onDestroy() {
        super.onDestroy()
        controllerConnection.release()
    }

    override fun onStart() {
        super.onStart()
        setPlayer(controllerConnection.mediaController.value)
    }

    override fun onStop() {
        super.onStop()
        controllerConnection.mediaController.value?.removeListener(listener)
        playerView.player = null
    }

    private inner class ComponentListener : Player.Listener {
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
            Log.d("Coucou", "${mediaMetadata.title}")
        }
    }
}
