/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.exoplayer.ExoPlayer

class FakeMediaItemTracker : MediaItemTracker<FakeMediaItemTracker.Data> {
    data class Data(val id: String)

    override fun start(player: ExoPlayer, data: Data) {
        println("start $data")
    }

    override fun stop(player: ExoPlayer) {
        // Nothing
        println("stop")
    }

    class Factory(private val fakeMediaItemTracker: FakeMediaItemTracker) : MediaItemTracker.Factory<Data> {
        override fun create(): FakeMediaItemTracker {
            return fakeMediaItemTracker
        }
    }
}
