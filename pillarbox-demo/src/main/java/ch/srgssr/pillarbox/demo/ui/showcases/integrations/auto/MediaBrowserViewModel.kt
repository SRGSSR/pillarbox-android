/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations.auto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.srgssr.pillarbox.demo.service.DemoMediaLibraryService
import ch.srgssr.pillarbox.player.session.PillarboxMediaBrowser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Media browser view model
 *
 * @param application
 */
class MediaBrowserViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Player
     */
    val player = callbackFlow {
        val mediaBrowser = PillarboxMediaBrowser.Builder(application, DemoMediaLibraryService::class.java).build()
        trySend(mediaBrowser)
        awaitClose {
            mediaBrowser.release()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
}
