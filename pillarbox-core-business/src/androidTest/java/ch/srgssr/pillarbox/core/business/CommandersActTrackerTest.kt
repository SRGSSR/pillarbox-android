/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionDataSourceImpl
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
import org.junit.After
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
    private lateinit var commandersAct: CommandersAct
    private val eventHistory = History()

    @Before
    fun setup() {
        CommandersActStreaming.HARD_BEAT_DELAY = HARD_BEAT_DELAY
        CommandersActStreaming.UPTIME_PERIOD = UPTIME_PERIOD
        CommandersActStreaming.POS_PERIOD = POS_PERIOD
        val analyticsConfig = AnalyticsConfig(distributor = AnalyticsConfig.BuDistributor.SRG)
        val commandersActConfig = CommandersAct.Config(virtualSite = "pillarbox-test-android", sourceKey = CommandersAct.Config.SOURCE_KEY_SRG_DEBUG)
        val appContext = getInstrumentation().targetContext
        commandersAct = CommandersAct(config = analyticsConfig, commandersActConfig = commandersActConfig, appContext)
        commandersAct.registerDebugListener(eventHistory)
        eventHistory.ignorePeriodicEvents = true
    }

    @After
    fun tearDown() {
        commandersAct.unregisterDebugListener(eventHistory)
    }

    private suspend fun createPlayerWithUrn(urn: String, playWhenReady: Boolean = true): TestPlayer {
        val context = getInstrumentation().targetContext
        val player = PillarboxPlayer(
            context = context,
            mediaItemSource = MediaCompositionMediaItemSource(mediaCompositionDataSource = MediaCompositionDataSourceImpl(context, IlHost.PROD)),
            mediaItemTrackerProvider = DefaultMediaItemTrackerRepository(commandersAct = commandersAct)
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
            val player = createPlayerWithUrn(VERY_SHORT_URN)
            player.waitForCondition {
                it.playbackState == Player.STATE_ENDED
            }
            player.release()
            Assert.assertEquals(expected, eventHistory.eventNames)
        }
    }

    @Test
    fun testPlayStop() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val expected = listOf(
            CommandersActStreaming.EVENT_PLAY,
            CommandersActStreaming.EVENT_STOP
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(VERY_SHORT_URN)
            player.release()
            Assert.assertEquals(expected, eventHistory.eventNames)
        }
    }

    @Test
    fun testPlaySeekPlay() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val seekPositionMs = 2_000L
        val expectedEvents = listOf(
            History.Event(CommandersActStreaming.EVENT_PLAY, 0L),
            History.Event(CommandersActStreaming.EVENT_SEEK, 0L),
            History.Event(CommandersActStreaming.EVENT_PLAY, seekPositionMs.milliseconds.inWholeSeconds),
            History.Event(CommandersActStreaming.EVENT_STOP)
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(SHORT_URN)
            player.seekTo(seekPositionMs)
            player.release()
            Assert.assertEquals(expectedEvents, eventHistory.events)
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
            History.Event(CommandersActStreaming.EVENT_PLAY, seekPositionMs.milliseconds.inWholeSeconds),
            History.Event(CommandersActStreaming.EVENT_STOP)
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(SHORT_URN, false)
            player.play()
            player.seekTo(seekPositionMs)
            player.release()
            Assert.assertEquals(expected, eventHistory.events)
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
            val player = createPlayerWithUrn("urn:rts:video:6820736")
            delay(2_000)
            player.pause()
            delay(2_000)
            player.seekTo(seekPositionMs)
            delay(2_000)
            player.release()
            Assert.assertEquals(expected, eventHistory.eventNames)
        }
    }

    @Test
    fun testPosTime() = runTest(dispatchTimeoutMs = 120.seconds.inWholeMilliseconds) {
        val expected = listOf(
            CommandersActStreaming.EVENT_PLAY,
            CommandersActStreaming.EVENT_POS,
            CommandersActStreaming.EVENT_POS,
            CommandersActStreaming.EVENT_STOP
        )
        var position = 0L.milliseconds
        val expectedEvent = listOf(
            History.Event(CommandersActStreaming.EVENT_PLAY, position.inWholeSeconds),
            History.Event(CommandersActStreaming.EVENT_POS, (position + HARD_BEAT_DELAY).inWholeSeconds),
            History.Event(CommandersActStreaming.EVENT_POS, (position + POS_PERIOD + HARD_BEAT_DELAY).inWholeSeconds),
            History.Event(CommandersActStreaming.EVENT_STOP)
        )
        eventHistory.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LONG_URN)
            delay(POS_PERIOD + HARD_BEAT_DELAY)
            player.release()
            Assert.assertEquals(expected, eventHistory.eventNames)
            Assert.assertEquals(expectedEvent, eventHistory.events)
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
        val startPos = HARD_BEAT_DELAY.toDouble(DurationUnit.SECONDS).roundToLong()
        val positionsEvents = listOf(
            History.Event(CommandersActStreaming.EVENT_POS, position = startPos),
            History.Event(CommandersActStreaming.EVENT_UPTIME, position = startPos),
            History.Event(CommandersActStreaming.EVENT_POS, position = startPos + POS_PERIOD.inWholeSeconds),
            History.Event(CommandersActStreaming.EVENT_POS, position = startPos + 2 * POS_PERIOD.inWholeSeconds),
            History.Event(CommandersActStreaming.EVENT_UPTIME, position = startPos + UPTIME_PERIOD.inWholeSeconds),
        )

        eventHistory.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LIVE_URN)
            delay(UPTIME_PERIOD + HARD_BEAT_DELAY)
            player.release()
            Assert.assertEquals(expected, eventHistory.eventNames)
            Assert.assertEquals(positionsEvents, eventHistory.events.filter {
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
        val startPos = HARD_BEAT_DELAY.toDouble(DurationUnit.SECONDS).roundToLong()
        val positionsEvents = listOf(
            History.Event(CommandersActStreaming.EVENT_POS, position = startPos),
            History.Event(CommandersActStreaming.EVENT_UPTIME, position = startPos),
            History.Event(CommandersActStreaming.EVENT_POS, position = startPos + POS_PERIOD.inWholeSeconds),
            History.Event(CommandersActStreaming.EVENT_POS, position = startPos + 2 * POS_PERIOD.inWholeSeconds),
            History.Event(CommandersActStreaming.EVENT_UPTIME, position = startPos + UPTIME_PERIOD.inWholeSeconds),
        )

        eventHistory.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LIVE_DVR_URN)
            delay(UPTIME_PERIOD + HARD_BEAT_DELAY)
            player.release()
            Assert.assertEquals(expected, eventHistory.eventNames)
            Assert.assertEquals(positionsEvents, eventHistory.events.filter {
                it.name == CommandersActStreaming.EVENT_POS || it.name == CommandersActStreaming.EVENT_UPTIME
            })
        }
    }

    @Test
    fun testUpTimeLiveWithDvrTimeShift() = runTest(dispatchTimeoutMs = 30_000L) {
        // 2 dvr duration, we seek 70 seconds before.
        val timeshift = (7136.seconds - 70.seconds).inWholeSeconds
        eventHistory.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LIVE_DVR_URN)
            player.seekTo(70.seconds.inWholeMilliseconds)
            delay(UPTIME_PERIOD + HARD_BEAT_DELAY)
            player.release()
            val actualTimeshift = eventHistory.events.first {
                it.name == CommandersActStreaming.EVENT_POS || it.name == CommandersActStreaming.EVENT_UPTIME
            }.timeshift
            Assert.assertFalse(eventHistory.events.isEmpty())
            Assert.assertTrue("Timeshift tolerance", abs(timeshift - actualTimeshift) <= 5)
        }
    }

    @Test
    fun testPauseSeekPause() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val seekPositionMs = 4_000L
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(VERY_SHORT_URN, false)
            player.seekTo(seekPositionMs)
            player.release()
            Assert.assertTrue(eventHistory.eventNames.isEmpty())
        }
    }

    internal class History(var ignorePeriodicEvents: Boolean = true) : CommandersAct.DebugListener {

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
                if (abs(timeshift - other.timeshift) > 100) return false


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

        override fun onEventSent(event: TCEvent) {
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
    }

    companion object {
        private const val TIME_OUT = 20_000L

        // 10 sec
        private const val VERY_SHORT_URN = "urn:rts:video:13444428"

        // More than 30 sec
        private const val SHORT_URN = "urn:rts:video:13444428"
        private const val LONG_URN = "urn:rts:video:6820736"
        private const val LIVE_URN = "urn:srf:video:c4927fcf-e1a0-0001-7edd-1ef01d441651"
        private const val LIVE_DVR_URN = "urn:rts:video:3608506"
        private val HARD_BEAT_DELAY = 3.seconds
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
