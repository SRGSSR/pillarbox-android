/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.demo.shared.data.DemoItem

/**
 * The [ViewModel][androidx.lifecycle.ViewModel] for the [AspectRatioSwitchActivity].
 *
 * It holds a single player shared between the media chooser and the player screens.
 *
 * @param application The running [Application].
 */
class AspectRatioSwitchViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * The player shared between screens.
     */
    val player = PillarboxExoPlayer(application)

    /**
     * Replace the current media item of the [player] with [item] and start playback.
     *
     * @param item The [DemoItem] to play.
     */
    fun play(item: DemoItem) {
        player.setMediaItem(item.toMediaItem())
        player.prepare()
        player.play()
    }

    override fun onCleared() {
        player.release()
    }
}
