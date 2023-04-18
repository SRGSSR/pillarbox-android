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
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CommandersActTrackerTest {
    private lateinit var commandersAct: CommandersAct
    private val eventHistory = History()

    @Before
    fun setup() {
        CommandersActStreaming.HARD_BEAT_DELAY = HARD_BEAT_DELAY
        CommandersActStreaming.UPTIME_PERIOD = UPTIME_PERIOD
        CommandersActStreaming.POS_PERIOD = POS_PERIOD
        val analyticsConfig = AnalyticsConfig(distributor = AnalyticsConfig.BuDistributor.SRG, "pillarbox-test-android")
        val commandersActConfig = CommandersAct.Config.SRG_DEBUG
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
            History.Event(name = CommandersActStreaming.EVENT_PLAY),
            History.Event(name = CommandersActStreaming.EVENT_EOF)
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(VERY_SHORT_URN)
            player.waitForCondition {
                it.playbackState == Player.STATE_ENDED
            }
            player.release()
            Assert.assertEquals(expected, eventHistory.eventList)
        }
    }

    @Test
    fun testPlayStop() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val expected = listOf(
            History.Event(name = CommandersActStreaming.EVENT_PLAY),
            History.Event(name = CommandersActStreaming.EVENT_STOP)
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(VERY_SHORT_URN)
            player.release()
            Assert.assertEquals(expected, eventHistory.eventList)
        }
    }

    @Test
    fun testPlaySeekPlay() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val seekPositionMs = 2_000L
        val expected = listOf(
            History.Event(name = CommandersActStreaming.EVENT_PLAY),
            History.Event(name = CommandersActStreaming.EVENT_SEEK),
            History.Event(name = CommandersActStreaming.EVENT_PLAY),
            History.Event(name = CommandersActStreaming.EVENT_STOP)
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(SHORT_URN)
            player.seekTo(seekPositionMs)
            player.release()
            Assert.assertEquals(expected, eventHistory.eventList)
        }
    }

    @Test
    fun testPlayPauseSeekPause() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val seekPositionMs = 4_000L
        val expected = listOf(
            History.Event(name = CommandersActStreaming.EVENT_PLAY),
            History.Event(name = CommandersActStreaming.EVENT_PAUSE),
            History.Event(name = CommandersActStreaming.EVENT_SEEK),
            History.Event(name = CommandersActStreaming.EVENT_PAUSE),
            History.Event(name = CommandersActStreaming.EVENT_STOP)
        )
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn("urn:rts:video:6820736")
            delay(2_000)
            player.pause()
            delay(2_000)
            player.seekTo(seekPositionMs)
            delay(2_000)
            player.release()
            Assert.assertEquals(expected, eventHistory.eventList)
        }
    }

    @Test
    fun testPosTime() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val expected = listOf(
            History.Event(name = CommandersActStreaming.EVENT_PLAY),
            History.Event(name = CommandersActStreaming.EVENT_POS),
            History.Event(name = CommandersActStreaming.EVENT_POS),
            History.Event(name = CommandersActStreaming.EVENT_STOP)
        )
        eventHistory.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(SHORT_URN)
            delay(POS_PERIOD + HARD_BEAT_DELAY)
            player.release()
            Assert.assertEquals(expected, eventHistory.eventList)
        }
    }

    @Test(timeout = 40_000L)
    fun testUpTime() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val expected = listOf(
            History.Event(name = CommandersActStreaming.EVENT_PLAY),
            History.Event(name = CommandersActStreaming.EVENT_POS),
            History.Event(name = CommandersActStreaming.EVENT_UPTIME),
            History.Event(name = CommandersActStreaming.EVENT_POS),
            History.Event(name = CommandersActStreaming.EVENT_POS),
            History.Event(name = CommandersActStreaming.EVENT_UPTIME),
            History.Event(name = CommandersActStreaming.EVENT_STOP)
        )
        eventHistory.ignorePeriodicEvents = false
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(LIVE_URN)
            delay(UPTIME_PERIOD + HARD_BEAT_DELAY)
            player.release()
            Assert.assertEquals(expected, eventHistory.eventList)
        }
    }

    @Test
    fun testPauseSeekPause() = runTest(dispatchTimeoutMs = TIME_OUT) {
        val seekPositionMs = 4_000L
        launch(Dispatchers.Main) {
            val player = createPlayerWithUrn(VERY_SHORT_URN, false)
            player.seekTo(seekPositionMs)
            player.release()
            Assert.assertTrue(eventHistory.eventList.isEmpty())
        }
    }

    internal class History(var ignorePeriodicEvents: Boolean = true) : CommandersAct.DebugListener {

        data class Event(
            val name: String
        )

        val eventList = ArrayList<Event>()

        override fun onEventSent(event: TCEvent) {
            if (event.isPeriodicEvent() && ignorePeriodicEvents) return
            eventList.add(Event(name = event.name))
        }
    }

    companion object {
        private const val TIME_OUT = 20_000L

        // 10 sec
        private const val VERY_SHORT_URN = "urn:rts:video:13444428"

        // More than 30 sec
        private const val SHORT_URN = "urn:rts:video:13444428"
        private const val LIVE_URN = "urn:srf:video:c4927fcf-e1a0-0001-7edd-1ef01d441651"
        private val HARD_BEAT_DELAY = 3.seconds
        private val UPTIME_PERIOD = 6.seconds
        private val POS_PERIOD = 3.seconds

        private fun TCEvent.isPeriodicEvent(): Boolean {
            return name == CommandersActStreaming.EVENT_POS || name == CommandersActStreaming.EVENT_UPTIME
        }
    }
}
