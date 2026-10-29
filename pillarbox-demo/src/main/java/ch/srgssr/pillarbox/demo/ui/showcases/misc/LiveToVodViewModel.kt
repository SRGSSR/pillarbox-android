/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.Timeline
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * The live to VOD transition showcase view model.
 * It reloads the content when it switches from live to VOD.
 */
class LiveToVodViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * The player.
     */
    val player: PillarboxPlayer = PlayerModule.provideDefaultPlayer(application)

    private val content = DemoItem.URL(
        uri = URL_TEST,
        title = "Live to VOD transition",
    )

    private val window = Timeline.Window()

    // Whether the content was live at the last timeline update, see onTimelineChanged.
    private var wasLive = false

    init {
        restart()

        val listener = object : PillarboxPlayer.Listener {
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                if (timeline.isEmpty) return
                timeline.getWindow(player.currentMediaItemIndex, window)
                // A placeholder window is used while the content loads, it doesn't know yet whether the content is live.
                if (window.isPlaceholder) return

                // When an HLS live ends (#EXT-X-ENDLIST), Media3 keeps window.isLive true, only window.isDynamic becomes false.
                val isLive = window.isLive && window.isDynamic
                Log.d("testmbo", "onTimelineChanged wasLive? $wasLive isLive: $isLive")

                if (wasLive && !isLive) {
                    // Live to VOD transition
                    Log.d("testmbo", "TESTMBO - LIVE TO VOD transistion")
                    reload()
                } else {
                    wasLive = isLive
                }
            }
        }

        player.addListener(listener)
    }

    /**
     * Play the content from the start.
     */
    fun restart() {
        wasLive = false
        player.setMediaItem(content.toMediaItem())
        player.prepare()
        player.play()
    }

    /**
     * Load [content] again, to get its VOD metadata, keeping the current position and play/pause state.
     */
    private fun reload() {
        wasLive = false
        player.setMediaItem(content.toMediaItem(), player.currentPosition)
        player.prepare()
    }

    override fun onCleared() {
        player.release()
    }

    companion object {
        /**
         * The URL of the content to play, served by a local test server.
         * 10.0.2.2 is the address of the host machine from the Android emulator.
         */
        const val URL_TEST = "http://10.0.2.2:8080/hls/index.m3u8"
    }
}
