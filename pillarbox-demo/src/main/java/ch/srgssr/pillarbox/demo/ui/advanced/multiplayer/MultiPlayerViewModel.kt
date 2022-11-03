/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.advanced.multiplayer

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ch.srg.pillarbox.core.business.MediaCompositionMediaItemSource
import ch.srg.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srg.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srg.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSourceImpl
import ch.srgssr.pillarbox.demo.data.MixedMediaItemSource
import ch.srgssr.pillarbox.demo.service.PlaybackService
import ch.srgssr.pillarbox.player.PillarboxPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Multi player view model
 *
 * Two players,
 *  - main : player with sound and audio focus
 *  - secondary : muted player without audio focus
 *
 *  The main player will continue playing in background.
 *
 * @param application
 */
class MultiPlayerViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * Left player
     */
    val player1 = createPlayer(application)

    /**
     * Right player
     */
    val player2 = createPlayer(application)
    private val _mainPlayer = MutableStateFlow(player1)

    /**
     * Main player
     */
    val mainPlayer: StateFlow<PillarboxPlayer> = _mainPlayer

    private val serviceConnection = object : ServiceConnection {
        private var job: Job? = null
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlaybackService.ServiceBinder
            job = viewModelScope.launch {
                mainPlayer.collectLatest {
                    binder.getPlaybackService().setPlayer(mainPlayer.value)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            job?.cancel() // Right ?
        }
    }

    init {
        application.bindService(Intent(application, PlaybackService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        player1.setMediaItem(itemPlayer1)
        player2.setMediaItem(itemPlayer2)
        configurePlayerAsMainPlayer(player1)
        configurePlayerAsSecondary(player2)
    }

    /**
     * Set main player
     *
     * @param player as the main player
     */
    fun setMainPlayer(player: PillarboxPlayer) {
        val lastMainPlayer = _mainPlayer.value
        if (lastMainPlayer == player) {
            return
        }
        configurePlayerAsSecondary(lastMainPlayer)
        configurePlayerAsMainPlayer(player)
        _mainPlayer.value = player
    }

    /**
     * Resume playback of all players
     */
    fun resume() {
        player1.play()
        player2.play()
    }

    /**
     * Pause playback of the secondary player
     */
    fun pause() {
        if (player1 != mainPlayer.value) {
            player1.pause()
        }
        if (player2 != mainPlayer.value) {
            player2.pause()
        }
    }

    private fun configurePlayerAsMainPlayer(player: PillarboxPlayer) {
        player.volume = 1.0f
        player.setHandleAudioBecomingNoisy(true)
        player.setAudioAttributes(player.audioAttributes, true)
    }

    private fun configurePlayerAsSecondary(player: PillarboxPlayer) {
        player.volume = 0.0f
        player.setHandleAudioBecomingNoisy(false)
        player.setAudioAttributes(player.audioAttributes, false)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(serviceConnection)
        player1.release()
        player2.release()
    }

    companion object {

        private val itemPlayer1 = MediaItem.Builder()
            .setMediaId("urn:rts:video:3608506")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Live player 1")
                    .build()
            )
            .build()

        private val itemPlayer2 = MediaItem.Builder()
            .setMediaId("urn:rts:video:6820736")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("VoD player 2")
                    .build()
            )
            .build()

        private fun createPlayer(context: Context) = PillarboxPlayer(
            context = context,
            mediaItemSource = MixedMediaItemSource(
                MediaCompositionMediaItemSource(MediaCompositionDataSourceImpl(context, IlHost.PROD))
            ),
            dataSourceFactory = AkamaiTokenDataSource.Factory()
        ).apply {
            repeatMode = Player.REPEAT_MODE_ALL
            setWakeMode(C.WAKE_MODE_NETWORK)
            prepare()
            play()
        }
    }
}
