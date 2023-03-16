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
import ch.srgssr.pillarbox.player.tracker.MediaItemMediaItemTrackerRepository
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
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
        currentItemTracker = CurrentMediaItemTracker(analyticsCommander, MediaItemMediaItemTrackerRepository().apply {
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
    fun testStartTimeLineChanged() = runTest {
        val mediaItem = createMediaItem("M1")
        val eventTime = createEventTime(mediaItem)
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        analyticsCommander.onMediaItemTransition(eventTime, null, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testEndAtEoF() = runTest {
        val mediaItem = createMediaItem("M1")
        val eventTime = createEventTime(mediaItem)
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        analyticsCommander.onPlaybackStateChanged(eventTime, Player.STATE_ENDED)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testEoFRestart() = runTest {
        val mediaItem = createMediaItem("M1")
        val eventTime = createEventTime(mediaItem)
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START, EventState.END)
        analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        analyticsCommander.onPlaybackStateChanged(eventTime, Player.STATE_ENDED)
        analyticsCommander.onPlaybackStateChanged(eventTime, Player.STATE_READY)
        analyticsCommander.onPlayerReleased(eventTime)

        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testMediaTransition() = runTest {
        val expected = listOf(EventState.IDLE, EventState.START, EventState.END, EventState.START, EventState.END)
        val mediaItem = createMediaItem("M1")
        val eventTime = createEventTime(mediaItem)

        analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        val mediaItem2 = createMediaItem("M2")
        val eventTime2 = createEventTime(mediaItem2)
        analyticsCommander.onMediaItemTransition(eventTime2, mediaItem2, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)
        analyticsCommander.onMediaItemTransition(eventTime2, null, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)

        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testMultipleStart() = runTest {
        val mediaItem = createMediaItem("M1")
        val eventTime = createEventTime(mediaItem)

        analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        analyticsCommander.onMediaItemTransition(eventTime, mediaItem, Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED)
        analyticsCommander.onMediaItemTransition(eventTime, null, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)

        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        Assert.assertEquals(expected, tracker.stateList)
    }

    @Test
    fun testMultipleStop() = runTest {
        val mediaItem = createMediaItem("M1")
        val eventTime = createEventTime(mediaItem)

        analyticsCommander.onTimelineChanged(eventTime, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
        analyticsCommander.onMediaItemTransition(eventTime, null, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)
        analyticsCommander.onPlayerReleased(eventTime)

        val expected = listOf(EventState.IDLE, EventState.START, EventState.END)
        Assert.assertEquals(expected, tracker.stateList)
    }

    companion object {

        fun createMediaItem(mediaId: String): MediaItem {
            val uri: Uri = mockk(relaxed = true)
            return MediaItem.Builder()
                .setUri(uri)
                .setMediaId(mediaId)
                .setTag(MediaItemTrackerData().apply { putData(TestTracker::class.java, mediaId) })
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

    private class TestTracker : MediaItemTracker {

        val stateList = ArrayList<EventState>().apply { add(EventState.IDLE) }

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
