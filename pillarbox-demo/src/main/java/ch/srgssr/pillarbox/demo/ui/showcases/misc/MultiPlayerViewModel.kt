/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxPlayer
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
        setActivePlayer(_playerOne)
    }

    /**
     * Activate the other player.
     */
    fun activateOtherPlayer() {
        setActivePlayer(getOtherPlayer(_activePlayer.value))
    }

    /**
     * Swap the two players.
     */
    fun swapPlayers() {
        swapPlayers.update { !it }
    }

    override fun onCleared() {
        _playerOne.release()
        _playerTwo.release()
    }

    private fun setActivePlayer(activePlayer: PillarboxPlayer) {
        _activePlayer.update { activePlayer }

        activePlayer.volume = 1f
        getOtherPlayer(activePlayer).volume = 0f
    }

    private fun getOtherPlayer(activePlayer: PillarboxPlayer): PillarboxPlayer {
        return when (activePlayer) {
            _playerOne -> _playerTwo
            _playerTwo -> _playerOne
            else -> error("Unrecognized player")
        }
    }
}
