/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.mediacontroller

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import ch.srgssr.pillarbox.demo.service.DemoMediaLibraryService
import ch.srgssr.pillarbox.player.RATIONAL_ONE
import ch.srgssr.pillarbox.player.service.MediaBrowserConnection
import ch.srgssr.pillarbox.player.toRational
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Media controller view model
 *
 * @param application
 */
class MediaControllerViewModel(application: Application) : AndroidViewModel(application), Player.Listener {
    private val controllerConnection = MediaBrowserConnection(application, ComponentName(application, DemoMediaLibraryService::class.java))

    /**
     * Player
     */
    val player = controllerConnection.mediaController

    /**
     * Picture in picture enabled
     */
    val pictureInPictureEnabled = MutableStateFlow(false)

    /**
     * Picture in picture aspect ratio
     */
    var pictureInPictureRatio = MutableStateFlow(RATIONAL_ONE)

    init {
        viewModelScope.launch {
            player.collectLatest {
                it?.addListener(this@MediaControllerViewModel)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.value?.removeListener(this)
        controllerConnection.release()
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        pictureInPictureRatio.value = videoSize.toRational()
    }
}
