/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.tracker.CurrentMediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestCurrentMediaItemTracker {

    private lateinit var analyticsCommander: AnalyticsListenerCommander
    private lateinit var currentItemTracker: CurrentMediaItemTracker
    private lateinit var tracker: TestTracker

    @Before
    fun setUp() {
        analyticsCommander = AnalyticsListenerCommander(mock = mockk(relaxed = false))
        every { analyticsCommander.currentMediaItem } returns null
        tracker = TestTracker()
        currentItemTracker = CurrentMediaItemTracker(analyticsCommander, MediaItemTrackerRepository().apply {
            registerFactory(TestTracker::class.java, object : MediaItemTracker.Factory {
                override fun create(): MediaItemTracker {
                    return tracker
                }
            })
        })
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun testAreEqualsDifferentMediaItem() {
        val mediaItem = createMediaItem("M1")
        val mediaItem2 = createMediaItem("M2")
        Assert.assertFalse(CurrentMediaItemTracker.areEquals(mediaItem, mediaItem2))
    }

    @Test
    fun testAreEqualsSameMediaId() {
        val mediaItem = createMediaItem("M1")
        val mediaItem2 = createMediaItem("M1")
        Assert.assertTrue(CurrentMediaItemTracker.areEquals(mediaItem, mediaItem2))
    }

    @Test
    fun testSimpleMediaItemWithoutTracker() = runTest {
        val mediaItem = createMediaItemWithoutTracker("M1")
        val expected = listOf(EventState.IDLE)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testItemWithoutTracker() = runTest {
        val tag = "testItemWithoutTracker"
        val mediaItem = createMediaItemWithoutTracker("M1", tag)
        val expected = listOf(EventState.IDLE)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testItemLoadAsynchWithoutTracker() = runTest {
        val tag = "testItemWithoutTracker"
        val mediaItemEmpty = MediaItem.Builder().setMediaId("M1").build()
        val mediaItemLoaded = createMediaItemWithoutTracker("M1", tag)
        val expected = listOf(EventState.IDLE)
        analyticsCommander.simulateItemStart(mediaItemEmpty)
        analyticsCommander.simulateItemLoaded(mediaItemLoaded)
        analyticsCommander.simulateItemEnd(mediaItemLoaded)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testStartEnd() = runTest {
        val mediaItem = createMediaItem("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testStartAsyncLoadEnd() = runTest {
        val mediaItemEmpty = MediaItem.Builder().setMediaId("M1").build()
        val mediaItemLoaded = createMediaItem("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        analyticsCommander.simulateItemStart(mediaItemEmpty)
        analyticsCommander.simulateItemLoaded(mediaItemLoaded)
        analyticsCommander.simulateItemEnd(mediaItemLoaded)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testStartAsyncLoadRelease() = runTest {
        val mediaItemEmpty = MediaItem.Builder().setMediaId("M1").build()
        val mediaItemLoaded = createMediaItem("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        analyticsCommander.simulateItemStart(mediaItemEmpty)
        analyticsCommander.simulateItemLoaded(mediaItemLoaded)
        analyticsCommander.simulateRelease(mediaItemLoaded)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testStartReleased() = runTest {
        val mediaItem = createMediaItem("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateRelease(mediaItem)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testRelease() = runTest {
        val mediaItem = createMediaItem("M1")
        val expected = listOf(EventState.IDLE)
        analyticsCommander.simulateRelease(mediaItem)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testRestartAfterEnd() = runTest {
        val mediaItem = createMediaItem("M1")
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START, EventState.END)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        analyticsCommander.simulatedReady(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        analyticsCommander.simulateRelease(mediaItem)

        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testMediaTransitionSeekToNext() = runTest {
        val expectedStates = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START, EventState.END)
        val mediaItem = createMediaItem("M1")
        val mediaItem2 = createMediaItem("M2")
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionSeek(mediaItem, mediaItem2)
        analyticsCommander.simulateItemEnd(mediaItem2)
        Assert.assertEquals("Different Item", expectedStates, tracker.stateList)
        tracker.clear()

        val mediaItem3 = createMediaItem("M1")
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionSeek(mediaItem, mediaItem3)
        analyticsCommander.simulateItemEnd(mediaItem3)
        Assert.assertEquals("Different Item but equal", expectedStates, tracker.stateList)
    }

    @Test
    fun testMediaItemTransitionWithAsyncItem() = runTest {
        val expectedStates = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START, EventState.END)
        val mediaItem = createMediaItem("M1")
        val mediaItem2 = MediaItem.Builder().setMediaId("M2").build()
        val mediaItem2Loaded = createMediaItem("M2")
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionSeek(mediaItem, mediaItem2)
        Assert.assertEquals(listOf(EventState.IDLE, EventState.START, EventState.END), tracker.stateList)

        analyticsCommander.simulateItemLoaded(mediaItem2Loaded)
        analyticsCommander.simulateRelease(mediaItem2Loaded)
        Assert.assertEquals(expectedStates, tracker.stateList)
    }


    @Test
    fun testMediaTransitionSameItemAuto() = runTest {
        val expectedStates = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START, EventState.END)
        val mediaItem = createMediaItem("M1")
        val mediaItem2 = createMediaItem("M2")
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionAuto(mediaItem, mediaItem2)
        analyticsCommander.simulateItemEnd(mediaItem2)
        Assert.assertEquals("Different Item", expectedStates, tracker.stateList)
        tracker.clear()

        val mediaItem3 = createMediaItem("M1")
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionAuto(mediaItem, mediaItem3)
        analyticsCommander.simulateItemEnd(mediaItem3)
        Assert.assertEquals("Different Item but equal", expectedStates, tracker.stateList)
    }

    @Test
    fun testMediaTransitionRepeat() = runTest {
        val expectedStates = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START)
        val mediaItem = createMediaItem("M1")

        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemTransitionRepeat(mediaItem)

        Assert.assertEquals(expectedStates, tracker.stateList)
    }

    @Test
    fun testMultipleStart() = runTest {
        val mediaItem = createMediaItem("M1")

        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testMultipleStop() = runTest {
        val mediaItem = createMediaItem("M1")
        analyticsCommander.simulateItemStart(mediaItem)
        analyticsCommander.simulateItemEnd(mediaItem)
        analyticsCommander.simulateRelease(mediaItem)

        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        Assert.assertEquals(expected, tracker.stateList)
    }

    companion object {
        private val uri: Uri = mockk(relaxed = true)

        fun createMediaItem(mediaId: String): MediaItem {
            every { uri.toString() } returns "https://host/media.mp4"
            every { uri.equals(Any()) } returns true
            return MediaItem.Builder()
                .setUri(uri)
                .setMediaId(mediaId)
                .setTag(MediaItemTrackerData().apply { putData(TestTracker::class.java, mediaId) })
                .build()
        }

        fun createMediaItemWithoutTracker(mediaId: String, customTag: String? = null): MediaItem {
            every { uri.toString() } returns "https://host/media.mp4"
            every { uri.equals(Any()) } returns true
            return MediaItem.Builder()
                .setUri(uri)
                .setMediaId(mediaId)
                .setTag(customTag)
                .build()
        }
    }

    private enum class EventState {
        IDLE, START, END
    }

    private class TestTracker : MediaItemTracker {

        val stateList = ArrayList<EventState>().apply { add(EventState.IDLE) }

        fun clear() {
            stateList.clear()
            stateList.add(EventState.IDLE)
        }

        override fun start(player: ExoPlayer) {
            stateList.add(EventState.START)
        }

        override fun stop(player: ExoPlayer) {
            stateList.add(EventState.END)
        }
    }

    private class DummyTimeline(private val mediaItem: MediaItem) : Timeline() {

        override fun getWindowCount(): Int {
            return 1
        }

        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            window.mediaItem = mediaItem
            return window
        }

        override fun getPeriodCount(): Int {
            return 0
        }

        override fun getPeriod(periodIndex: Int, period: Period, setIds: Boolean): Period {
            return Period()
        }

        override fun getIndexOfPeriod(uid: Any): Int {
            return 0
        }

        override fun getUidOfPeriod(periodIndex: Int): Any {
            return Any()
        }

    }
}
