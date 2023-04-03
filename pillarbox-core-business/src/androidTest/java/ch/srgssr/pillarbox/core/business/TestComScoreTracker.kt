/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.test.platform.app.InstrumentationRegistry
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.comscore.ComScore
import ch.srgssr.pillarbox.core.business.tracker.ComScoreTracker
import ch.srgssr.pillarbox.player.test.utils.AnalyticsListenerCommander
import ch.srgssr.pillarbox.player.test.utils.TestTimeline
import com.comscore.streaming.StreamingListener
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestComScoreTracker {

    private lateinit var analyticsListenerCommander: AnalyticsListenerCommander

    @Before
    fun setUp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        ComScore.init(
            config = AnalyticsConfig(distributor = AnalyticsConfig.BuDistributor.SRG, "pillarbox-test-android"),
            appContext = appContext, comScoreConfig = ComScore.Config()
        )
        analyticsListenerCommander = AnalyticsListenerCommander(mockk())
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test(timeout = 10_000)
    fun simpleTest() = runBlocking {
        val listener = ComScoreListener()
        val tracker = ComScoreTracker.Factory(listener).create()
        // Simulate initial case
        val timeline = TestTimeline()
        every { analyticsListenerCommander.playbackState } returns Player.STATE_READY
        every { analyticsListenerCommander.playbackParameters } returns PlaybackParameters.DEFAULT
        every { analyticsListenerCommander.isPlaying } returns true
        every { analyticsListenerCommander.currentMediaItemIndex } returns 0
        every { analyticsListenerCommander.currentPosition } returns 0L
        every { analyticsListenerCommander.currentTimeline } returns timeline
        launch {
            tracker.start(analyticsListenerCommander, ComScoreTracker.Data(assets = emptyMap()))
            delay(1_000)
            tracker.stop(analyticsListenerCommander)
        }
        val actual = listener.state.take(3).toList()
        val expected = listOf(0, 2, 0)
        Assert.assertEquals(expected, actual)
    }

    class ComScoreListener : StreamingListener {
        private val _state = MutableStateFlow(0)
        val state = _state.asStateFlow()

        override fun onStateChanged(oldState: Int, newState: Int, eventLabels: MutableMap<String, String>?) {
            _state.value = newState
        }
    }

    companion object {
        // Hypothesis value gathered from logs
        private const val IDLE = 0 //ENDED
        private const val PLAYING = 2
        private const val PAUSE = 3
        private const val BUFFERING = 4 // LOADING?
        private const val U1 = 1
        private const val Y = 6
        private const val YY = 7 // BUFFERING
        private const val X = 9
        private const val Z = 11 // SEEKING
    }
}
