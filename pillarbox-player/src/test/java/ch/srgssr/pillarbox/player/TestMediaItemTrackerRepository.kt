/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestMediaItemTrackerRepository {

    private lateinit var trackerRepository: MediaItemTrackerRepository

    @Before
    fun init() {
        trackerRepository = MediaItemTrackerRepository()
    }

    @Test(expected = AssertionError::class)
    fun testNotFoundTracker() {
        trackerRepository.getMediaItemTrackerFactory(String::class.java)
    }

    @Test
    fun testRetrieveTracker() {
        val testFactory = TestTracker.Factory()
        trackerRepository.registerFactory(TestTracker::class.java, testFactory)
        val factory = trackerRepository.getMediaItemTrackerFactory(TestTracker::class.java)
        Assert.assertEquals(TestTracker.Factory::class.java, factory::class.java)
        Assert.assertEquals(testFactory, factory)
    }

    private class TestTracker : MediaItemTracker {

        class Factory : MediaItemTracker.Factory {
            override fun create(): MediaItemTracker {
                return TestTracker()
            }
        }

        override fun start(player: ExoPlayer, initialData: Any?) {
            // Nothing
        }

        override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
            // Nothing
        }
    }
}
