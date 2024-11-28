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
 * A utility class that repeatedly executes the given [task] at a specified [period].
 *
 * @param startDelay The initial delay before the first execution of the [task].
 * @param period The time interval between consecutive executions of the [task].
 * @param coroutineContext The coroutine context in which the heartbeat will run.
 * @param task The function to be executed periodically.
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
     * Starts the execution of this heartbeat.
     *
     * If the heartbeat is already running, this function behaves based on the [restart] parameter:
     * - If [restart] is `true`, the current heartbeat execution is stopped and a new one is started.
     * - If [restart] is `false`, the function does nothing and the current heartbeat continues running.
     *
     * @param restart  Indicates whether to restart the heartbeat if it's already running.
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
     * Stops the execution of this heartbeat.
     */
    fun stop() {
        job?.cancel()
        job = null
    }
}
