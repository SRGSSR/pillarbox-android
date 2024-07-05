/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class HeartbeatTest {
    private var taskRunsCount = 0

    private val task: () -> Unit = { taskRunsCount++ }
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        taskRunsCount = 0
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `verify task exectuion`() = runTest(testDispatcher) {
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
    fun `verify task exectuion with immediate stop`() = runTest(testDispatcher) {
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
}
