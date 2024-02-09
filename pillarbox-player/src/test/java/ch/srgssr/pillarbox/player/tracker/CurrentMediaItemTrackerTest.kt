/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.test.utils.AnalyticsListenerCommander
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class CurrentMediaItemTrackerTest {
    private lateinit var analyticsCommander: AnalyticsListenerCommander
    private lateinit var currentItemTracker: CurrentMediaItemTracker
    private lateinit var tracker: TestTracker

    @BeforeTest
    fun setUp() {
        analyticsCommander = AnalyticsListenerCommander(exoplayer = mockk())
        every { analyticsCommander.currentMediaItem } returns null
        every { analyticsCommander.currentPosition } returns 1000L
        tracker = TestTracker()
        currentItemTracker = CurrentMediaItemTracker(
            player = analyticsCommander,
            mediaItemTrackerProvider = MediaItemTrackerRepository().apply {
                registerFactory(
                    TestTracker::class.java,
                    object : MediaItemTracker.Factory {
                        override fun create(): MediaItemTracker {
                            return tracker
                        }
                    }
                )
            }
        )
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `simple MediaItem without tracker`() {
        val mediaItem = createMediaItemWithoutTracker("M1")
        val expected = listOf(EventState.IDLE)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `MediaItem without tracker`() {
        val mediaItem = createMediaItemWithoutTracker("M1", "testItemWithoutTracker")
        val expected = listOf(EventState.IDLE)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `MediaItem load asynchronously without tracker`() {
        val mediaItemEmpty = MediaItem.Builder().setMediaId("M1").build()
        val mediaItemLoaded = createMediaItemWithoutTracker("M1", "testItemWithoutTracker")
        val expected = listOf(EventState.IDLE)
        analyticsCommander.simulateItemStart(mediaItemEmpty)
        analyticsCommander.simulateItemLoaded(mediaItemLoaded)
        analyticsCommander.simulateItemEnd(mediaItemLoaded)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `start end`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.EOF)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `start asynchronous loading end`() {
        val mediaItemEmpty = MediaItem.Builder().setMediaId("M1").build()
        val mediaItemLoaded = createMediaItemWithMediaId("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.EOF)
        analyticsCommander.simulateItemStart(mediaItemEmpty)
        analyticsCommander.simulateItemLoaded(mediaItemLoaded)
        analyticsCommander.simulateItemEnd(mediaItemLoaded)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `start asynchronous loading release`() {
        val mediaItemEmpty = MediaItem.Builder().setMediaId("M1").build()
        val mediaItemLoaded = createMediaItemWithMediaId("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        analyticsCommander.simulateItemStart(mediaItemEmpty)
        analyticsCommander.simulateItemLoaded(mediaItemLoaded)
        analyticsCommander.simulateRelease(mediaItemLoaded)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `start release`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateRelease(mediaItem)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun release() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val expected = listOf(EventState.IDLE)
        analyticsCommander.simulateRelease(mediaItem)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `restart after end`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.EOF, EventState.START, EventState.EOF)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        analyticsCommander.simulatedReady(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        analyticsCommander.simulateRelease(mediaItem)

        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `media transition seek to next`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val mediaItem2 = createMediaItemWithMediaId("M2")
        val expectedStates = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START, EventState.EOF)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionSeek(mediaItem, mediaItem2)
        analyticsCommander.simulateItemEnd(mediaItem2)
        assertEquals(expectedStates, tracker.stateList, "Different Item")
        tracker.clear()

        val mediaItem3 = createMediaItemWithMediaId("M1")
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionSeek(mediaItem, mediaItem3)
        analyticsCommander.simulateItemEnd(mediaItem3)
        assertEquals(expectedStates, tracker.stateList, "Different Item but equal")
    }

    @Test
    fun `media transition with asynchronous item`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val mediaItem2 = MediaItem.Builder().setMediaId("M2").build()
        val mediaItem2Loaded = createMediaItemWithMediaId("M2")
        val expectedStates = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START, EventState.END)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionSeek(mediaItem, mediaItem2)
        assertEquals(listOf(EventState.IDLE, EventState.START, EventState.END), tracker.stateList)

        analyticsCommander.simulateItemLoaded(mediaItem2Loaded)
        analyticsCommander.simulateRelease(mediaItem2Loaded)
        assertEquals(expectedStates, tracker.stateList)
    }

    @Test
    fun `item without tracker toggle analytics`() {
        val mediaItem = createMediaItemWithoutTracker("M1", "testItemWithoutTracker")
        val expected = listOf(EventState.IDLE)
        currentItemTracker.enabled = true
        analyticsCommander.simulateItemStart(mediaItem)
        currentItemTracker.enabled = false
        currentItemTracker.enabled = true
        analyticsCommander.simulateItemEnd(mediaItem)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `media transition same item auto`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val mediaItem2 = createMediaItemWithMediaId("M2")
        val expectedStates = listOf(EventState.IDLE, EventState.START, EventState.EOF, EventState.START, EventState.EOF)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionAuto(mediaItem, mediaItem2)
        analyticsCommander.simulateItemEnd(mediaItem2)
        assertEquals(expectedStates, tracker.stateList, "Different Item")
        tracker.clear()

        val mediaItem3 = createMediaItemWithMediaId("M1")
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionAuto(mediaItem, mediaItem3)
        analyticsCommander.simulateItemEnd(mediaItem3)
        assertEquals(expectedStates, tracker.stateList, "Different Item but equal")
    }

    @Test
    fun `media transition repeat`() {
        val expectedStates = listOf(EventState.IDLE, EventState.START, EventState.EOF, EventState.START)
        val mediaItem = createMediaItemWithMediaId("M1")

        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionRepeat(mediaItem)

        assertEquals(expectedStates, tracker.stateList)
    }

    @Test
    fun `multiple stops`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        analyticsCommander.simulateRelease(mediaItem)

        val expected = listOf(EventState.IDLE, EventState.START, EventState.EOF)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `start end disabled at start of analytics`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val expected = listOf(EventState.IDLE)
        currentItemTracker.enabled = false
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `start end toggle analytics`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START, EventState.EOF)
        currentItemTracker.enabled = true
        analyticsCommander.simulateItemStart(mediaItem)
        every { analyticsCommander.currentMediaItem } returns mediaItem
        currentItemTracker.enabled = false
        currentItemTracker.enabled = true
        analyticsCommander.simulateItemEnd(mediaItem)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `start asynchronously loading toggle analytics`() {
        val mediaItemEmpty = MediaItem.Builder().setMediaId("M1").build()
        val mediaItemLoaded = createMediaItemWithMediaId("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START, EventState.EOF)
        currentItemTracker.enabled = true
        analyticsCommander.simulateItemStart(mediaItemEmpty)
        analyticsCommander.simulateItemLoaded(mediaItemLoaded)
        every { analyticsCommander.currentMediaItem } returns mediaItemLoaded
        currentItemTracker.enabled = false
        currentItemTracker.enabled = true
        analyticsCommander.simulateItemEnd(mediaItemLoaded)
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `start asynchronously loading end disabled at end`() {
        val mediaItemEmpty = MediaItem.Builder().setMediaId("M1").build()
        val mediaItemLoaded = createMediaItemWithMediaId("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.EOF)
        currentItemTracker.enabled = true
        analyticsCommander.simulateItemStart(mediaItemEmpty)
        analyticsCommander.simulateItemLoaded(mediaItemLoaded)
        analyticsCommander.simulateItemEnd(mediaItemLoaded)
        currentItemTracker.enabled = false
        assertEquals(expected, tracker.stateList)
    }

    @Test
    fun `start remove item`() {
        val mediaItem = createMediaItemWithMediaId("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemRemoved(mediaItem)
        assertEquals(expected, tracker.stateList)
    }

    private companion object {
        private val uri = mockk<Uri>()

        private fun createMediaItemWithMediaId(mediaId: String): MediaItem {
            every { uri.toString() } returns "https://host/media.mp4"
            return MediaItem.Builder()
                .setUri(uri)
                .setMediaId(mediaId)
                .setTag(MediaItemTrackerData.Builder().apply { putData(TestTracker::class.java, mediaId) }.build())
                .build()
        }

        private fun createMediaItemWithoutTracker(mediaId: String, customTag: String? = null): MediaItem {
            every { uri.toString() } returns "https://host/media.mp4"
            return MediaItem.Builder()
                .setUri(uri)
                .setMediaId(mediaId)
                .setTag(customTag)
                .build()
        }
    }

    private enum class EventState {
        IDLE, START, END, EOF
    }

    private class TestTracker : MediaItemTracker {
        private val _stateList = mutableListOf(EventState.IDLE)
        val stateList: List<EventState> = _stateList

        fun clear() {
            _stateList.clear()
            _stateList.add(EventState.IDLE)
        }

        override fun start(player: ExoPlayer, initialData: Any?) {
            _stateList.add(EventState.START)
        }

        override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
            when (reason) {
                MediaItemTracker.StopReason.EoF -> _stateList.add(EventState.EOF)
                MediaItemTracker.StopReason.Stop -> _stateList.add(EventState.END)
            }
        }
    }
}
