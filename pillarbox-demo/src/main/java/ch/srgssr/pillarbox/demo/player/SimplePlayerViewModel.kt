/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.demo.data.SwiMediaItemSource
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * Simple player view model than handle a PillarboxPlayer [player]
 */
class SimplePlayerViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * Player as PillarboxPlayer
     */
    val player = PillarboxPlayer(application, SwiMediaItemSource())

    init {
        player.setMediaItem(MediaItem.Builder().setMediaId(SwiMediaItemSource.UNIQUE_SWI_ID).build())
        player.prepare()
    }

    /**
     * Resume playback of [player]
     */
    fun resumePlayback() {
        player.play()
    }

    /**
     * Pause playback of [player]
     */
    fun pausePlayback() {
        player.pause()
    }
}
