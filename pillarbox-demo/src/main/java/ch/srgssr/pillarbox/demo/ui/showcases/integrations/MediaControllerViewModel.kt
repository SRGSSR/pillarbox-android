/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import ch.srgssr.pillarbox.demo.service.DemoMediaLibraryService
import ch.srgssr.pillarbox.player.extension.RATIONAL_ONE
import ch.srgssr.pillarbox.player.extension.toRational
import ch.srgssr.pillarbox.player.videoSizeAsFlow
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
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
class MediaControllerViewModel(application: Application) : AndroidViewModel(application), MediaBrowser.Listener {
    private val sessionToken = SessionToken(application, ComponentName(application, DemoMediaLibraryService::class.java))
    private val listenableFuture = MediaBrowser.Builder(application, sessionToken).setListener(this).buildAsync()

    /**
     * Player
     */
    val player = callbackFlow<MediaBrowser> {
        listenableFuture.addListener({
            val mediaBrowser = listenableFuture.get() // or using listenableFuture.await inside a coroutine
            trySend(mediaBrowser)
        }, MoreExecutors.directExecutor())
        awaitClose {
            listenableFuture.cancel(false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    /**
     * Picture in picture enabled
     */
    val pictureInPictureEnabled = MutableStateFlow(false)

    /**
     * Picture in picture aspect ratio
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    var pictureInPictureRatio = player.filterNotNull().flatMapLatest { mediaBrowser ->
        mediaBrowser.videoSizeAsFlow().map { it.toRational() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RATIONAL_ONE)

    override fun onCleared() {
        super.onCleared()
        MediaBrowser.releaseFuture(listenableFuture)
    }
}
