/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations.cast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import ch.srgssr.media.maestro.MediaRouteButton
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.cast.isCastSessionAvailableAsFlow
import ch.srgssr.pillarbox.cast.isConnected
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.player.extension.getCurrentMediaItems
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel that olds current player and handle local to remote player switch.
 *
 * Best result can be achieved with a PillarboxMediaSessionService and with PillarboxMediaController.
 *
 * @param application The application context.
 */
class CastShowcaseViewModel(application: Application) : AndroidViewModel(application) {
    private val castContext = application.getCastContext()
    private val castPlayer = PillarboxCastPlayer(application)
    private val localPlayer = PillarboxExoPlayer(application)

    /**
     * The current player, it can be either a [PillarboxCastPlayer] or a [PillarboxExoPlayer].
     */
    val currentPlayer = castPlayer.isCastSessionAvailableAsFlow()
        .map { if (it) castPlayer else localPlayer }
        .distinctUntilChanged()
        .onEach { switchPlayer(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            if (castContext.isConnected()) castPlayer else localPlayer
        )

    /**
     * The [MediaRouteSelector] to use on the [MediaRouteButton].
     */
    val routeSelector = castContext.mergedSelector ?: MediaRouteSelector.Builder()
        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
        .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
        .build()

    override fun onCleared() {
        castPlayer.release()
        localPlayer.release()
    }

    private fun switchPlayer(player: Player) {
        val oldPlayer = if (player is PillarboxCastPlayer) localPlayer else castPlayer
        if (oldPlayer == player || (oldPlayer == localPlayer && oldPlayer.playbackState == Player.STATE_IDLE)) return
        player.repeatMode = oldPlayer.repeatMode
        player.playWhenReady = oldPlayer.playWhenReady
        player.setMediaItems(oldPlayer.getCurrentMediaItems(), oldPlayer.currentMediaItemIndex, oldPlayer.currentPosition)
        player.prepare()
        oldPlayer.stop()
        oldPlayer.clearMediaItems()
    }
}
