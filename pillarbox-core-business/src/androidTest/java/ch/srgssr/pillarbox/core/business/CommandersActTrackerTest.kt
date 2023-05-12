/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.test.filters.FlakyTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ch.srgssr.pillarbox.analytics.PageView
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.core.business.tracker.DefaultMediaItemTrackerRepository
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActStreaming
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.test.utils.TestPlayer
import com.tagcommander.lib.serverside.events.TCEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@OptIn(ExperimentalCoroutinesApi::class)
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
            mediaItemSource = MediaCompositionMediaItemSource(mediaCompositionDataSource = LocalMediaCompositionDataSource(context)),
            mediaItemTrackerProvider = DefaultMediaItemTrackerRepository(commandersAct = commandersActDelegate)
        )
        player.volume = 0.0f
        player.setMediaItem(MediaItem.Builder().setMediaId(urn).build())
        player.playWhenReady = playWhenReady
        val testPlayer = TestPlayer(player)
        testPlayer.prepare()
        return testPlayer
    }

    @Test
    fun testStartEoF() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val expected = listOf(
            CommandersActStreaming.EVENT_PLAY,
            CommandersActStreaming.EVENT_EOF
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
    fun testPlayStop() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val expected = listOf(
            CommandersActStreaming.EVENT_PLAY,
            CommandersActStreaming.EVENT_STOP
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Vod)
            player.release()
            Assert.assertEquals(expected, commandersActDelegate.eventNames)
        }
    }

    @Test
    fun testPlaySeekPlay() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val seekPositionMs = 2_000L
        val expectedEvents = listOf(
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_PLAY, 0L),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_SEEK, 0L),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_PLAY, seekPositionMs.milliseconds.inWholeSeconds),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_STOP)
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
    fun testPausePlaySeekPlay() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val seekPositionMs = 2_000L
        val expected = listOf(
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_PLAY, seekPositionMs.milliseconds.inWholeSeconds),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_STOP)
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
    fun testPlayPauseSeekPause() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val seekPositionMs = 4_000L
        val expected = listOf(
            CommandersActStreaming.EVENT_PLAY,
            CommandersActStreaming.EVENT_PAUSE,
            CommandersActStreaming.EVENT_SEEK,
            CommandersActStreaming.EVENT_PAUSE,
            CommandersActStreaming.EVENT_STOP
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
            CommandersActStreaming.EVENT_PLAY,
            CommandersActStreaming.EVENT_POS,
            CommandersActStreaming.EVENT_POS,
            CommandersActStreaming.EVENT_STOP
        )
        var position = 0L.milliseconds
        val expectedEvent = listOf(
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_PLAY, position.inWholeSeconds),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_POS, (position + HEART_BEAT_DELAY).inWholeSeconds),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_POS, (position + POS_PERIOD + HEART_BEAT_DELAY).inWholeSeconds),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_STOP)
        )
        commandersActDelegate.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Vod)
            delay(POS_PERIOD + HEART_BEAT_DELAY + 500L.milliseconds)
            Assert.assertEquals(false, player.player.isCurrentMediaItemLive)
            player.release()
            Assert.assertEquals(expected, commandersActDelegate.eventNames)
            Assert.assertEquals(expectedEvent, commandersActDelegate.events)
        }
    }

    @Test
    fun testUpTime() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val expected = listOf(
            CommandersActStreaming.EVENT_PLAY,
            CommandersActStreaming.EVENT_POS,
            CommandersActStreaming.EVENT_UPTIME,
            CommandersActStreaming.EVENT_POS,
            CommandersActStreaming.EVENT_POS,
            CommandersActStreaming.EVENT_UPTIME,
            CommandersActStreaming.EVENT_STOP
        )
        val startPos = HEART_BEAT_DELAY.toDouble(DurationUnit.SECONDS).roundToLong()
        val positionsEvents = listOf(
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_POS, position = startPos),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_UPTIME, position = startPos),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_POS, position = startPos + POS_PERIOD.inWholeSeconds),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_POS, position = startPos + 2 * POS_PERIOD.inWholeSeconds),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_UPTIME, position = startPos + UPTIME_PERIOD.inWholeSeconds),
        )

        commandersActDelegate.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Live)
            delay(UPTIME_PERIOD + HEART_BEAT_DELAY)
            player.release()
            Assert.assertEquals(expected, commandersActDelegate.eventNames)
            Assert.assertEquals(positionsEvents, commandersActDelegate.events.filter {
                it.name == CommandersActStreaming.EVENT_POS || it.name == CommandersActStreaming.EVENT_UPTIME
            })
        }
    }

    @Test
    fun testUpTimeLiveWithDvr() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val expected = listOf(
            CommandersActStreaming.EVENT_PLAY,
            CommandersActStreaming.EVENT_POS,
            CommandersActStreaming.EVENT_UPTIME,
            CommandersActStreaming.EVENT_POS,
            CommandersActStreaming.EVENT_POS,
            CommandersActStreaming.EVENT_UPTIME,
            CommandersActStreaming.EVENT_STOP
        )
        val startPos = HEART_BEAT_DELAY.toDouble(DurationUnit.SECONDS).roundToLong()
        val positionsEvents = listOf(
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_POS, position = startPos),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_UPTIME, position = startPos),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_POS, position = startPos + POS_PERIOD.inWholeSeconds),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_POS, position = startPos + 2 * POS_PERIOD.inWholeSeconds),
            CommandersActDelegate.Event(CommandersActStreaming.EVENT_UPTIME, position = startPos + UPTIME_PERIOD.inWholeSeconds),
        )

        commandersActDelegate.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Dvr)
            delay(UPTIME_PERIOD + HEART_BEAT_DELAY)
            player.release()
            Assert.assertEquals(expected, commandersActDelegate.eventNames)
            Assert.assertEquals(positionsEvents, commandersActDelegate.events.filter {
                it.name == CommandersActStreaming.EVENT_POS || it.name == CommandersActStreaming.EVENT_UPTIME
            })
        }
    }

    @Test
    fun testUpTimeLiveWithDvrTimeShift() = runTest(dispatchTimeoutMs = 30_000L) {
    fun testUpTimeLiveWithDvrTimeShift() = runTest {
        val seekPosition = 80.seconds
        commandersActDelegate.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LocalMediaCompositionDataSource.Dvr)
            val timeshift = (player.player.duration.milliseconds - seekPosition).inWholeSeconds
            player.seekTo(seekPosition.inWholeMilliseconds)
            delay(UPTIME_PERIOD + HEART_BEAT_DELAY)
            player.release()
            val actualTimeshift = commandersActDelegate.events.first {
                it.name == CommandersActStreaming.EVENT_POS || it.name == CommandersActStreaming.EVENT_UPTIME
            }.timeshift
            Assert.assertFalse(commandersActDelegate.events.isEmpty())
            Assert.assertTrue("Timeshift expected $timeshift but was $actualTimeshift", abs(timeshift - actualTimeshift) <= 15)
        }
    }

    @Test
    fun testPauseSeekPause() = runTest(dispatchTimeoutMs = TIME_OUT) {
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
        override var userId: String? = null,
        override var isLogged: Boolean = false
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


        override fun sendTcEvent(event: TCEvent) {
            if (event.isPeriodicEvent() && ignorePeriodicEvents) return
            eventNames.add(event.name)
            var position: Long = 0L
            var timeshift: Long = 0L
            if (!event.isEndEvent()) {
                position = event.additionalParameters.getData(CommandersActStreaming.MEDIA_POSITION).toLong()
                timeshift = event.additionalParameters.getData(CommandersActStreaming.MEDIA_TIMESHIFT)?.toLong() ?: 0L
            }

            events.add(Event(name = event.name, position = position, timeshift = timeshift))
        }

        override fun sendPageView(pageView: PageView) {
            // Ignored
        }

        override fun sendEvent(event: ch.srgssr.pillarbox.analytics.Event) {
            // Ignored
        }
    }

    companion object {
        private val HEART_BEAT_DELAY = 3.seconds
        private val UPTIME_PERIOD = 6.seconds
        private val POS_PERIOD = 3.seconds

        private fun TCEvent.isPeriodicEvent(): Boolean {
            return name == CommandersActStreaming.EVENT_POS || name == CommandersActStreaming.EVENT_UPTIME
        }

        private fun TCEvent.isEndEvent(): Boolean {
            return name == CommandersActStreaming.EVENT_STOP || name == CommandersActStreaming.EVENT_EOF
        }
    }
}
