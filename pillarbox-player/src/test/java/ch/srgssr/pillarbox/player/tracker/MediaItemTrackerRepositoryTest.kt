/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import androidx.media3.exoplayer.ExoPlayer
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MediaItemTrackerRepositoryTest {
    private lateinit var trackerRepository: MediaItemTrackerRepository

    @BeforeTest
    fun init() {
        trackerRepository = MediaItemTrackerRepository()
    }

    @Test(expected = AssertionError::class)
    fun `tracker not found`() {
        trackerRepository.getMediaItemTrackerFactory(String::class.java)
    }

    @Test
    fun `retrieve tracker`() {
        val testFactory = TestTracker.Factory()
        trackerRepository.registerFactory(TestTracker::class.java, testFactory)
        val factory = trackerRepository.getMediaItemTrackerFactory(TestTracker::class.java)
        assertEquals(TestTracker.Factory::class.java, factory::class.java)
        assertEquals(testFactory, factory)
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
