/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.tracker.MediaItemMediaItemTrackerRepository
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestMediaItemTrackerRepository {

    private lateinit var trackerRepository: MediaItemMediaItemTrackerRepository

    @Before
    fun init() {
        trackerRepository = MediaItemMediaItemTrackerRepository()
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

        override fun start(player: ExoPlayer) {

        }

        override fun stop(player: ExoPlayer) {

        }
    }
}
