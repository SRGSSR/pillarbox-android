/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.test.filters.FlakyTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActPageView
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType
import ch.srgssr.pillarbox.analytics.commandersact.TCMediaEvent
import ch.srgssr.pillarbox.core.business.tracker.DefaultMediaItemTrackerRepository
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActStreaming
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.test.utils.TestPlayer
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class CommandersActTrackerTest {
    private lateinit var commandersActDelegate: CommandersActDelegate

    @Before
    fun setup() {
        CommandersActStreaming.HEART_BEAT_DELAY = HEART_BEAT_DELAY
        CommandersActStreaming.UPTIME_PERIOD = UPTIME_PERIOD
        CommandersActStreaming.POS_PERIOD = POS_PERIOD
        commandersActDelegate = CommandersActDelegate()
    }

    private suspend fun createPlayerWithUrn(urn: String, playWhenReady: Boolean = true): TestPlayer {
        val context = getInstrumentation().targetContext
        val player = PillarboxPlayer(
            context = context,
            mediaItemSource = MediaCompositionMediaItemSource(
                mediaCompositionDataSource = LocalMediaCompositionDataSource(context),
            ),
            mediaItemTrackerProvider = DefaultMediaItemTrackerRepository(
                trackerRepository = MediaItemTrackerRepository(),
                commandersAct = commandersActDelegate
            )
        )
        player.volume = 0.0f
        player.setMediaItem(MediaItem.Builder().setMediaId(urn).build())
        player.playWhenReady = playWhenReady
        val testPlayer = TestPlayer(player)
        testPlayer.prepare()
        return testPlayer
    }

    @Test
    fun testStartEoF() = runTest {
        val expected = listOf(
            MediaEventType.Play.toString(),
            MediaEventType.Eof.toString()
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.VodShort)
            player.waitForCondition {
                it.playbackState == Player.STATE_ENDED || it.playbackState == Player.STATE_IDLE
            }
            player.release()
            Assert.assertEquals(expected, commandersActDelegate.eventNames)
        }
    }

    @Test
    fun testPlayStop() = runTest {
        val expected = listOf(
            MediaEventType.Play.toString(),
            MediaEventType.Stop.toString()
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Vod)
            player.release()
            Assert.assertEquals(expected, commandersActDelegate.eventNames)
        }
    }

    @Test
    fun testPlaySeekPlay() = runTest {
        val seekPositionMs = 2_000L
        val expectedEvents = listOf(
            CommandersActDelegate.Event(MediaEventType.Play.toString(), 0L),
            CommandersActDelegate.Event(MediaEventType.Seek.toString(), 0L),
            CommandersActDelegate.Event(MediaEventType.Play.toString(), seekPositionMs.milliseconds.inWholeSeconds),
            CommandersActDelegate.Event(MediaEventType.Stop.toString())
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Vod)
            player.seekTo(seekPositionMs)
            player.release()
            Assert.assertEquals(expectedEvents, commandersActDelegate.events)
        }
    }

    /**
     * Test pause play seek play
     * Seek event is not send but play event position should be the seek position.
     */
    @Test
    fun testPausePlaySeekPlay() = runTest {
        val seekPositionMs = 2_000L
        val expected = listOf(
            CommandersActDelegate.Event(MediaEventType.Play.toString()),
            CommandersActDelegate.Event(MediaEventType.Seek.toString()),
            CommandersActDelegate.Event(MediaEventType.Play.toString(), seekPositionMs.milliseconds.inWholeSeconds),
            CommandersActDelegate.Event(MediaEventType.Stop.toString())
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Vod, false)
            player.play()
            player.seekTo(seekPositionMs)
            player.release()
            Assert.assertEquals(expected, commandersActDelegate.events)
        }
    }

    @Test
    fun testPlayPauseSeekPause() = runTest {
        val seekPositionMs = 4_000L
        val expected = listOf(
            MediaEventType.Play.toString(),
            MediaEventType.Pause.toString(),
            MediaEventType.Stop.toString()
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Vod)
            delay(2_000)
            player.pause()
            delay(2_000)
            player.seekTo(seekPositionMs)
            delay(2_000)
            player.release()
            Assert.assertEquals(expected, commandersActDelegate.eventNames)
        }
    }

    @FlakyTest(detail = "POS and UPTIME not always send due to timers")
    @Test
    fun testPosTime() = runTest {
        val expected = listOf(
            MediaEventType.Pos.toString(),
            MediaEventType.Pos.toString(),
        )
        commandersActDelegate.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Vod)
            delay(POS_PERIOD + HEART_BEAT_DELAY + DELTA_PERIOD)
            Assert.assertEquals(false, player.player.isCurrentMediaItemLive)
            player.release()
            val sent = commandersActDelegate.eventNames.filter { it == MediaEventType.Pos.toString() }
            Assert.assertTrue(sent.size >= expected.size)
        }
    }

    @FlakyTest(detail = "POS and UPTIME not always send due to timers")
    @Test
    fun testUpTime() = runTest {
        val expected = listOf(
            MediaEventType.Uptime.toString(),
            MediaEventType.Uptime.toString(),
        )

        commandersActDelegate.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Live)
            delay(UPTIME_PERIOD + HEART_BEAT_DELAY + DELTA_PERIOD)
            player.release()
            val sent = commandersActDelegate.eventNames.filter { it == MediaEventType.Uptime.toString() }
            Assert.assertTrue(sent.size >= expected.size)
        }
    }

    @FlakyTest(detail = "POS and UPTIME not always send due to timers")
    @Test
    fun testUpTimeLiveWithDvr() = runTest {
        val expected = listOf(
            MediaEventType.Uptime.toString(),
            MediaEventType.Uptime.toString(),
        )
        commandersActDelegate.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Dvr)
            delay(UPTIME_PERIOD + HEART_BEAT_DELAY + DELTA_PERIOD)
            player.release()
            val sent = commandersActDelegate.eventNames.filter { it == MediaEventType.Uptime.toString() }
            Assert.assertTrue(sent.size >= expected.size)
        }
    }

    @FlakyTest
    @Test
    fun testUpTimeLiveWithDvrTimeShift() = runTest {
        val seekPosition = 80.seconds
        commandersActDelegate.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Dvr)
            val timeshift = (player.player.duration.milliseconds - seekPosition).inWholeSeconds
            player.seekTo(seekPosition.inWholeMilliseconds)
            delay(UPTIME_PERIOD + HEART_BEAT_DELAY + DELTA_PERIOD)
            player.release()
            val actualTimeshift = commandersActDelegate.events.first {
                it.name == MediaEventType.Pos.toString() || it.name == MediaEventType.Uptime.toString()
            }.timeshift
            Assert.assertFalse(commandersActDelegate.events.isEmpty())
            Assert.assertTrue("Timeshift expected $timeshift but was $actualTimeshift", abs(timeshift - actualTimeshift) <= 15)
        }
    }

    @Test
    fun testPauseSeekPause() = runTest {
        val seekPositionMs = 4_000L
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Vod, false)
            player.seekTo(seekPositionMs)
            player.release()
            Assert.assertTrue(commandersActDelegate.eventNames.isEmpty())
        }
    }

    internal class CommandersActDelegate(
        var ignorePeriodicEvents: Boolean = true,
    ) :
        CommandersAct {
        data class Event(
            val name: String,
            val position: Long = 0L,
            val timeshift: Long = 0L
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Event

                if (name != other.name) return false
                if (abs(position - other.position) > 1) return false
                return true
            }

            override fun hashCode(): Int {
                var result = name.hashCode()
                result = 31 * result + position.hashCode()
                return result
            }
        }

        val eventNames = ArrayList<String>()
        val events = ArrayList<Event>()

        override fun sendTcMediaEvent(event: TCMediaEvent) {
            if (event.isPeriodicEvent() && ignorePeriodicEvents) return
            eventNames.add(event.name)
            var position = 0L
            var timeshift = 0L
            if (!event.isEndEvent()) {
                position = event.mediaPosition.inWholeSeconds
                timeshift = event.timeShift?.inWholeSeconds ?: 0L
            }
            events.add(Event(name = event.name, position = position, timeshift = timeshift))
        }

        override fun putPermanentData(labels: Map<String, String>) {
            // Nothing
        }

        override fun removePermanentData(label: String) {
            // Nothing
        }

        override fun getPermanentDataLabel(label: String): String? {
            // Nothing
            return null
        }

        override fun sendPageView(pageView: CommandersActPageView) {
            // Ignored
        }

        override fun setConsentServices(consentServices: List<String>) {
            // Nothing
        }

        override fun sendEvent(event: ch.srgssr.pillarbox.analytics.commandersact.CommandersActEvent) {
            // Ignored
        }
    }

    companion object {
        private val HEART_BEAT_DELAY = 3.seconds
        private val UPTIME_PERIOD = 6.seconds
        private val POS_PERIOD = 3.seconds
        private val DELTA_PERIOD = 500.milliseconds

        private fun TCMediaEvent.isPeriodicEvent(): Boolean {
            return eventType == MediaEventType.Pos || eventType == MediaEventType.Uptime
        }

        private fun TCMediaEvent.isEndEvent(): Boolean {
            return eventType == MediaEventType.Stop || eventType == MediaEventType.Eof
        }
    }
}
