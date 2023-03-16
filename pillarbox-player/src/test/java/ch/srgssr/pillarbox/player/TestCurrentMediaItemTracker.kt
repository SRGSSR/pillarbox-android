/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import ch.srgssr.pillarbox.player.tracker.CurrentMediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerList
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestCurrentMediaItemTracker {

    private lateinit var analyticsCommander: AnalyticsListenerCommander
    private lateinit var currentItemTracker: CurrentMediaItemTracker

    @Before
    fun setUp() {
        analyticsCommander = AnalyticsListenerCommander(mock = mockk(relaxed = false))
        every { analyticsCommander.currentMediaItem } returns null
        currentItemTracker = CurrentMediaItemTracker(analyticsCommander)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun testStartTimeLineChanged() = runTest {
        val tracker = TestTracker()
        val mediaItem = createMediaItem("M1", tracker)
        val eventTime = createEventTime(mediaItem)
        analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        Assert.assertEquals(EventState.START, tracker.eventState.take(1).first().state)

        analyticsCommander.onMediaItemTransition(eventTime, null, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)
        Assert.assertEquals(EventState.END, tracker.eventState.take(1).first().state)
        Assert.assertEquals(0, tracker.startCount)
    }

    @Test
    fun testEndAtEoF() = runTest {
        val tracker = TestTracker()
        val mediaItem = createMediaItem("M1", tracker)
        val eventTime = createEventTime(mediaItem)
        analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        Assert.assertEquals(EventState.START, tracker.eventState.take(1).first().state)

        analyticsCommander.onPlaybackStateChanged(eventTime, Player.STATE_ENDED)
        Assert.assertEquals(EventState.END, tracker.eventState.take(1).first().state)
        Assert.assertEquals(0, tracker.startCount)
    }

    @Test
    fun testEoFRestart() = runTest {
        val tracker = TestTracker()
        val mediaItem = createMediaItem("M1", tracker)
        val eventTime = createEventTime(mediaItem)
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START, EventState.END)
        launch {
            analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
            delay(100)
            analyticsCommander.onPlaybackStateChanged(eventTime, Player.STATE_ENDED)
            delay(100)
            analyticsCommander.onPlaybackStateChanged(eventTime, Player.STATE_READY)
            delay(100)
            analyticsCommander.onPlayerReleased(eventTime)
        }

        Assert.assertEquals(expected, tracker.eventState.take(expected.size).toList().map { it.state })
        Assert.assertEquals(0, tracker.startCount)
    }

    @Test
    fun testMediaTransition() = runTest {
        val tracker = TestTracker()
        val mediaItem = createMediaItem("M1", tracker)
        val eventTime = createEventTime(mediaItem)
        analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        Assert.assertEquals(EventState.START, tracker.eventState.take(1).first().state)

        val tracker2 = TestTracker()
        val mediaItem2 = createMediaItem("M2", tracker2)
        val eventTime2 = createEventTime(mediaItem2)
        analyticsCommander.onMediaItemTransition(eventTime2, mediaItem2, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)
        Assert.assertEquals(EventState.END, tracker.eventState.take(1).first().state)
        Assert.assertEquals(EventState.START, tracker2.eventState.take(1).first().state)

        analyticsCommander.onMediaItemTransition(eventTime2, null, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)
        Assert.assertEquals(EventState.END, tracker2.eventState.take(1).first().state)
        Assert.assertEquals(0, tracker.startCount)
    }

    @Test
    fun testMultipleStart() = runTest {
        val tracker = TestTracker()
        val mediaItem = createMediaItem("M1", tracker)
        val eventTime = createEventTime(mediaItem)
        launch {
            analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
            delay(1_000)
            analyticsCommander.onMediaItemTransition(eventTime, mediaItem, Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED)
            delay(1_000)
            analyticsCommander.onMediaItemTransition(eventTime, null, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)
        }
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        Assert.assertEquals(expected, tracker.eventState.take(3).toList().map { it.state })
        Assert.assertEquals(0, tracker.startCount)
    }

    @Test
    fun testMultipleStop() = runTest {
        val tracker = TestTracker()
        val mediaItem = createMediaItem("M1", tracker)
        val eventTime = createEventTime(mediaItem)
        launch {
            analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
            delay(1_000)
            analyticsCommander.onMediaItemTransition(eventTime, null, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)
            delay(1_000)
            analyticsCommander.onPlayerReleased(eventTime)

        }
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        Assert.assertEquals(expected, tracker.eventState.take(3).toList().map { it.state })
        Assert.assertEquals(0, tracker.startCount)
    }

    companion object {

        fun createMediaItem(mediaId: String, tracker: MediaItemTracker): MediaItem {
            val uri: Uri = mockk(relaxed = true)
            return MediaItem.Builder()
                .setUri(uri)
                .setMediaId(mediaId)
                .setTag(MediaItemTrackerList().apply { append(tracker) })
                .build()
        }

        fun createEventTime(mediaItem: MediaItem): EventTime {
            val timeline = DummyTimeline(mediaItem)
            return EventTime(0, timeline, 0, null, 0, timeline, 0, null, 0, 0)
        }
    }

    private enum class EventState {
        IDLE, START, END
    }

    private data class StartEvent(val state: EventState, val time: Long = System.currentTimeMillis())

    private class TestTracker : MediaItemTracker {
        val eventState = MutableStateFlow(StartEvent(EventState.IDLE))
        var startCount = 0

        override fun start(player: ExoPlayer) {
            startCount++
            eventState.value = StartEvent(EventState.START)
        }

        override fun stop(player: ExoPlayer) {
            startCount--
            eventState.value = StartEvent(EventState.END)
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
