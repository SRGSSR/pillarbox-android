/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * The live to VOD transition showcase view model.
 * The player reloads the content when it switches from live to VOD, see [ch.srgssr.pillarbox.player.PillarboxBuilder.reloadOnLiveToVodTransition].
 */
class LiveToVodViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * The player.
     */
    val player: PillarboxPlayer = PlayerModule.provideStandardPlayer(application)

    private val content = DemoItem.URL(
        uri = URL_TEST,
        title = "Live to VOD transition",
    )

    init {
        restart()
    }

    /**
     * Play the content from the start.
     */
    fun restart() {
        player.setMediaItem(content.toMediaItem())
        player.prepare()
        player.play()
    }

    override fun onCleared() {
        player.release()
    }

    companion object {
        /**
         * The URL of the metadata of the content to play, in the Pillarbox standard metadata format, served by a local test server.
         * 10.0.2.2 is the address of the host machine from the Android emulator.
         */
        const val URL_TEST = "http://10.0.2.2:8080/metadata"
    }
}
