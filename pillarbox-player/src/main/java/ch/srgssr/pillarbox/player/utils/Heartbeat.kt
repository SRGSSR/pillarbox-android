/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * Utility class to trigger a [task] at a regular [intervals][period].
 *
 * @param startDelay The initial delay before the first execution of [task].
 * @param period The period between two executions of [task].
 * @param coroutineContext The coroutine context in which [Heartbeat] is run.
 * @param task The task to execute at regular [intervals][period].
 */
class Heartbeat(
    private val startDelay: Duration = Duration.ZERO,
    private val period: Duration,
    private val coroutineContext: CoroutineContext,
    private val task: () -> Unit,
) {
    private val coroutineScope = CoroutineScope(coroutineContext + CoroutineName("pillarbox-heartbeat"))

    private var job: Job? = null

    /**
     * Start the execution of this heartbeat. Does nothing if it is already running and [restart] is `false`.
     *
     * @param restart `true` to restart the heartbeat if it is already running, `false` otherwise.
     *
     * @see stop
     */
    fun start(restart: Boolean = true) {
        if (job?.isActive == true && !restart) {
            return
        }

        stop()

        job = coroutineScope.launch {
            delay(startDelay)
            while (isActive) {
                task()
                delay(period)
            }
        }
    }

    /**
     * Stop the execution of this heartbeat.
     */
    fun stop() {
        job?.cancel()
        job = null
    }
}
