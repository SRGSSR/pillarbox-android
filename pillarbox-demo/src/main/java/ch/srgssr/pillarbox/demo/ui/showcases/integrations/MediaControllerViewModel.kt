/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Tracks
import ch.srgssr.pillarbox.demo.service.DemoMediaSessionService
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.extension.RATIONAL_ONE
import ch.srgssr.pillarbox.player.extension.toRational
import ch.srgssr.pillarbox.player.session.PillarboxMediaController
import ch.srgssr.pillarbox.player.videoSizeAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Media controller view model
 *
 * @param application
 */
class MediaControllerViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Player
     */
    val player = callbackFlow {
        val mediaBrowser = PillarboxMediaController.Builder(application, DemoMediaSessionService::class.java).build()
        mediaBrowser.addListener(object : PillarboxPlayer.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                for (group in tracks.groups) {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        Log.d("Coucou", "Format $format")
                    }
                }
            }
        })
        trySend(mediaBrowser)
        awaitClose {
            mediaBrowser.release()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * Picture in picture aspect ratio
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val pictureInPictureRatio = player.filterNotNull().flatMapLatest { mediaBrowser ->
        mediaBrowser.videoSizeAsFlow().map { it.toRational() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RATIONAL_ONE)
}
