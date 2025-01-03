/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HeartbeatTest {
    private var taskRunsCount = 0

    private val task: () -> Unit = {
        assertTrue(Looper.getMainLooper().isCurrentThread)

        taskRunsCount++
    }
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        taskRunsCount = 0
    }

    @AfterTest
    fun tearDown() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `verify task execution`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            period = 10.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.start()
        advanceTimeBy(25.seconds)
        heartbeat.stop()
        advanceTimeBy(15.seconds)

        assertEquals(3, taskRunsCount)
    }

    @Test
    fun `verify task execution with start delay`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            startDelay = 5.seconds,
            period = 10.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.start()
        advanceTimeBy(25.seconds)
        heartbeat.stop()
        advanceTimeBy(15.seconds)

        assertEquals(2, taskRunsCount)
    }

    @Test
    fun `verify task doesn't execute if not started`() = runTest(testDispatcher) {
        Heartbeat(
            period = 10.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        advanceTimeBy(15.seconds)

        assertEquals(0, taskRunsCount)
    }

    @Test
    fun `verify task with start delay doesn't execute if not started`() = runTest(testDispatcher) {
        Heartbeat(
            startDelay = 5.seconds,
            period = 10.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        advanceTimeBy(15.seconds)

        assertEquals(0, taskRunsCount)
    }

    @Test
    fun `verify task execution with immediate stop`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            period = 10.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.start()
        heartbeat.stop()
        advanceTimeBy(15.seconds)

        assertEquals(1, taskRunsCount)
    }

    @Test
    fun `verify task execution with start delay and immediate stop`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            startDelay = 5.seconds,
            period = 10.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.start()
        heartbeat.stop()
        advanceTimeBy(15.seconds)

        assertEquals(0, taskRunsCount)
    }

    @Test
    fun `verify task execution with start delay and stop during start delay`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            startDelay = 5.seconds,
            period = 10.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.start()
        advanceTimeBy(2.seconds)
        heartbeat.stop()
        advanceTimeBy(15.seconds)

        assertEquals(0, taskRunsCount)
    }

    @Test
    fun `verify task can be restarted`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            period = 10.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.start()
        advanceTimeBy(25.seconds)
        heartbeat.stop()
        advanceTimeBy(15.seconds)
        heartbeat.start()
        advanceTimeBy(25.seconds)
        heartbeat.stop()
        advanceTimeBy(15.seconds)

        assertEquals(6, taskRunsCount)
    }

    @Test
    fun `verify task with start delay can be restarted`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            startDelay = 5.seconds,
            period = 10.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.start()
        advanceTimeBy(25.seconds)
        heartbeat.stop()
        advanceTimeBy(15.seconds)
        heartbeat.start()
        advanceTimeBy(25.seconds)
        heartbeat.stop()
        advanceTimeBy(15.seconds)

        assertEquals(4, taskRunsCount)
    }

    @Test
    fun `verify not started task can be stopped`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            period = 10.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.stop()
        advanceTimeBy(15.seconds)

        assertEquals(0, taskRunsCount)
    }

    @Test
    fun `verify not started task can be stopped with start delay`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            startDelay = 5.seconds,
            period = 10.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.stop()
        advanceTimeBy(15.seconds)

        assertEquals(0, taskRunsCount)
    }

    @Test
    fun `verify multiple start() restart the heartbeat`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            period = 30.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.start(restart = true)
        advanceTimeBy(10.seconds)
        heartbeat.start(restart = true)
        advanceTimeBy(10.seconds)
        heartbeat.start(restart = true)
        advanceTimeBy(2.minutes)
        heartbeat.stop()
        advanceTimeBy(25.seconds)

        assertEquals(6, taskRunsCount)
    }

    @Test
    fun `verify multiple start() restart the heartbeat with start delay`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            startDelay = 5.seconds,
            period = 30.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.start(restart = true)
        advanceTimeBy(10.seconds)
        heartbeat.start(restart = true)
        advanceTimeBy(10.seconds)
        heartbeat.start(restart = true)
        advanceTimeBy(2.minutes)
        heartbeat.stop()
        advanceTimeBy(25.seconds)

        assertEquals(6, taskRunsCount)
    }

    @Test
    fun `verify multiple start() don't restart the heartbeat`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            period = 30.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.start(restart = false)
        advanceTimeBy(10.seconds)
        heartbeat.start(restart = false)
        advanceTimeBy(10.seconds)
        heartbeat.start(restart = false)
        advanceTimeBy(2.minutes)
        heartbeat.stop()
        advanceTimeBy(25.seconds)

        assertEquals(5, taskRunsCount)
    }

    @Test
    fun `verify multiple start() don't restart the heartbeat with start delay`() = runTest(testDispatcher) {
        val heartbeat = Heartbeat(
            startDelay = 5.seconds,
            period = 30.seconds,
            coroutineContext = coroutineContext,
            task = task,
        )

        heartbeat.start(restart = false)
        advanceTimeBy(10.seconds)
        heartbeat.start(restart = false)
        advanceTimeBy(10.seconds)
        heartbeat.start(restart = false)
        advanceTimeBy(2.minutes)
        heartbeat.stop()
        advanceTimeBy(25.seconds)

        assertEquals(5, taskRunsCount)
    }

    @Test
    fun `verify player is accessible from the task`() {
        val player = PillarboxExoPlayer()
        runTest(testDispatcher) {
            var taskCalled = false

            val heartbeat = Heartbeat(
                period = 10.seconds,
                coroutineContext = coroutineContext,
                task = {
                    player.currentPosition
                    taskCalled = true
                },
            )

            heartbeat.start()
            advanceTimeBy(15.seconds)
            heartbeat.stop()
            advanceTimeBy(15.seconds)

            assertTrue(taskCalled)

            player.release()
        }
    }
}
