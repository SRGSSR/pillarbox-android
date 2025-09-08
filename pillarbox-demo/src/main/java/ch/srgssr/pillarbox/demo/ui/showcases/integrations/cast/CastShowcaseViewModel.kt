/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations.cast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import ch.srgssr.media.maestro.MediaRouteButton
import ch.srgssr.pillarbox.cast.CastPlayerSynchronizer
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer

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
    private val castPlayerSynchronizer = CastPlayerSynchronizer(
        castContext = castContext,
        coroutineScope = viewModelScope,
        castPlayer = castPlayer,
        localPlayer = localPlayer,
        playerSynchronizer = object : CastPlayerSynchronizer.DefaultPlayerSynchronizer() {
            override fun onPlayerChanged(oldPlayer: PillarboxPlayer, newPlayer: PillarboxPlayer) {
                super.onPlayerChanged(oldPlayer, newPlayer)
                newPlayer.prepare()
                oldPlayer.stop()
                oldPlayer.clearMediaItems()
            }
        }
    )

    /**
     * The current player, it can be either a [PillarboxCastPlayer] or a [PillarboxExoPlayer].
     */
    val currentPlayer = castPlayerSynchronizer.currentPlayer

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
}
