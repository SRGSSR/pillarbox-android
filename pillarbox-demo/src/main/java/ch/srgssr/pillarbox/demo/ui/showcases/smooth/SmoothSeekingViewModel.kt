/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.smooth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.exoplayer.SeekParameters
import ch.srgssr.pillarbox.core.business.DefaultPillarbox
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxLoadControl

/**
 * Smooth seeking view model
 *
 * @param application
 */
class SmoothSeekingViewModel(application: Application) : AndroidViewModel(application) {
    private val loadControl = PillarboxLoadControl()

    /**
     * Player
     */
    val player = DefaultPillarbox(
        context = application,
        loadControl = loadControl,
        mediaItemSource = PlayerModule.provideMixedItemSource(application)
    )

    init {
        player.prepare()
        player.play()
        player.setMediaItem(DemoItem.UnifiedStreamingOnDemand_Dash_TrickPlay.toMediaItem())
    }

    /**
     * Set smooth seeking enabled
     *
     * @param smoothSeeking true to enable smoothSeeking.
     */
    fun setSmoothSeekingEnabled(smoothSeeking: Boolean) {
        loadControl.smoothSeeking = smoothSeeking
        player.setSeekParameters(if (smoothSeeking) SeekParameters.CLOSEST_SYNC else SeekParameters.DEFAULT)
    }

    override fun onCleared() {
        player.release()
    }
}
