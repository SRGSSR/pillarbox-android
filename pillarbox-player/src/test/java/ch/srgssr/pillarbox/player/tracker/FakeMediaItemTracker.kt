/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.exoplayer.ExoPlayer

class FakeMediaItemTracker : MediaItemTracker {
    data class Data(val id: String)

    override fun start(player: ExoPlayer, initialData: Any?) {
        require(initialData is Data)
    }

    override fun update(data: Any) {
        require(data is Data)
    }

    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        // Nothing
    }

    class Factory(private val fakeMediaItemTracker: FakeMediaItemTracker) : MediaItemTracker.Factory {
        override fun create(): MediaItemTracker {
            return fakeMediaItemTracker
        }
    }
}

class FakeTrackerProvider(private val fakeMediaItemTracker: FakeMediaItemTracker) : MediaItemTrackerProvider {
    override fun getMediaItemTrackerFactory(trackerClass: Class<*>): MediaItemTracker.Factory {
        return object : MediaItemTracker.Factory {
            override fun create(): MediaItemTracker {
                return fakeMediaItemTracker
            }
        }
    }
}
