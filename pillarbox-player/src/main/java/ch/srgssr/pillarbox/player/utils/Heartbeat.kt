/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * Utility class to trigger a [task] at a regular [intervals][period].
 *
 * @param startDelay The initial delay before the first execution of [task].
 * @param period The period between two executions of [task].
 * @param coroutineContext The coroutine context in which [Heartbeat] is run.
 * @param task The task to execute, on the main [Thread] at regular [intervals][period].
 */
class Heartbeat(
    private val startDelay: Duration = Duration.ZERO,
    private val period: Duration,
    private val coroutineContext: CoroutineContext,
    @MainThread private val task: () -> Unit,
) {
    private val coroutineScope = CoroutineScope(coroutineContext + CoroutineName("pillarbox-heart-beat"))

    private var job: Job? = null

    /**
     * Start the execution of this heartbeat. If it is already running, the current execution is canceled, and the heartbeat is restarted.
     *
     * @see stop
     */
    fun start() {
        stop()

        job = coroutineScope.launch {
            delay(startDelay)

            while (isActive) {
                runBlocking(Dispatchers.Main) {
                    task()
                }

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
