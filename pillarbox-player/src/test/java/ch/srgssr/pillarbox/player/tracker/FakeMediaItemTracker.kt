/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import ch.srgssr.pillarbox.player.PillarboxExoPlayer

class FakeMediaItemTracker : MediaItemTracker<FakeMediaItemTracker.Data> {
    data class Data(val id: String)

    override fun start(player: PillarboxExoPlayer, data: Data) {
        println("start $data")
    }

    override fun stop(player: PillarboxExoPlayer) {
        // Nothing
        println("stop")
    }

    class Factory(private val fakeMediaItemTracker: FakeMediaItemTracker) : MediaItemTracker.Factory<Data> {
        override fun create(): FakeMediaItemTracker {
            return fakeMediaItemTracker
        }
    }
}
