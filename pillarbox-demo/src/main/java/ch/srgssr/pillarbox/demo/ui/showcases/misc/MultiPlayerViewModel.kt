/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.setHandleAudioFocus
import ch.srgssr.pillarbox.player.notification.PillarboxMediaDescriptionAdapter
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession
import ch.srgssr.pillarbox.player.tracks.disableAudioTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * The [ViewModel][androidx.lifecycle.ViewModel] for the [MultiPlayerShowcase].
 *
 * @param application The running [Application].
 */
class MultiPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val notificationManager = PlayerNotificationManager.Builder(application, NOTIFICATION_ID, CHANNEL_ID)
        .setChannelNameResourceId(androidx.media3.session.R.string.default_notification_channel_name)
        .setMediaDescriptionAdapter(PillarboxMediaDescriptionAdapter(null, application))
        .build()
    private val mediaSession: PillarboxMediaSession

    private val _playerOne = PlayerModule.provideDefaultPlayer(application).apply {
        repeatMode = Player.REPEAT_MODE_ONE
        setMediaItem(DemoItem.LiveVideo.toMediaItem())
        prepare()
        play()
    }
    private val _playerTwo = PlayerModule.provideDefaultPlayer(application).apply {
        repeatMode = Player.REPEAT_MODE_ONE
        setMediaItem(DemoItem.DvrVideo.toMediaItem())
        prepare()
        play()
    }

    private val _activePlayer = MutableStateFlow(_playerOne)
    private val swapPlayers = MutableStateFlow(false)

    /**
     * The currently active player.
     */
    val activePlayer: StateFlow<PillarboxPlayer> = _activePlayer

    /**
     * The first player to display.
     */
    val playerOne = swapPlayers.map { swapPlayers ->
        if (swapPlayers) _playerTwo else _playerOne
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), _playerOne)

    /**
     * The second play to display.
     */
    val playerTwo = swapPlayers.map { swapPlayers ->
        if (swapPlayers) _playerOne else _playerTwo
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), _playerTwo)

    init {
        mediaSession = PillarboxMediaSession.Builder(application, _playerTwo)
            .setId("MultiPlayerSession")
            .build()
        notificationManager.setMediaSessionToken(mediaSession.token)
        setActivePlayer(_playerOne)
    }

    /**
     * Set the currently active player.
     *
     * @param activePlayer The new active player.
     */
    fun setActivePlayer(activePlayer: PillarboxExoPlayer) {
        val oldActivePlayer = mediaSession.player as PillarboxExoPlayer
        _activePlayer.update { activePlayer }
        mediaSession.player = activePlayer
        notificationManager.setPlayer(activePlayer)

        oldActivePlayer.disableAudioTrack()
        oldActivePlayer.trackSelectionParameters = oldActivePlayer.trackSelectionParameters.buildUpon().setTrackTypeDisabled(
            C.TRACK_TYPE_AUDIO,
            true
        ).build()
        oldActivePlayer.trackingEnabled = false
        oldActivePlayer.setHandleAudioFocus(false)
        oldActivePlayer.setHandleAudioBecomingNoisy(false)

        activePlayer.trackSelectionParameters = activePlayer.trackSelectionParameters.buildUpon().setTrackTypeDisabled(
            C.TRACK_TYPE_AUDIO,
            false
        ).build()
        activePlayer.trackingEnabled = true
        activePlayer.setHandleAudioFocus(true)
        activePlayer.setHandleAudioBecomingNoisy(true)
    }

    /**
     * Swap the two players.
     */
    fun swapPlayers() {
        swapPlayers.update { !it }
    }

    override fun onCleared() {
        notificationManager.setPlayer(null)
        mediaSession.release()

        _playerOne.release()
        _playerTwo.release()
    }

    private companion object {
        private const val NOTIFICATION_ID = 42
        private const val CHANNEL_ID = "DemoChannel"
    }
}
