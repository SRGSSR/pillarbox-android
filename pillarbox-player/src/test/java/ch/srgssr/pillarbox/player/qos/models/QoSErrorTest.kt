/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

import java.lang.RuntimeException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class QoSErrorTest {
    @Test
    fun `throwableConstructor with empty exception`() {
        val throwable = IllegalStateException()
        val qosError = QoSError(
            throwable = throwable,
            playerPosition = 5.minutes.inWholeMilliseconds,
            severity = QoSError.Severity.WARNING,
            url = "",
        )

        val logLines = qosError.log.lineSequence()

        assertTrue(logLines.count() > 1, "Expected log to contain the stacktrace")
        assertEquals("java.lang.IllegalStateException", logLines.first())
        assertTrue(logLines.none { it.startsWith("Caused by: ") }, "Expected log to not contain a cause")
        assertEquals("", qosError.message)
        assertEquals("IllegalStateException", qosError.name)
        assertEquals(QoSError.Severity.WARNING, qosError.severity)
    }

    @Test
    fun `throwableConstructor with detailed exception`() {
        val cause = NullPointerException("Expected 'foo' to be not null")
        val throwable = RuntimeException("Something bad happened", cause)
        val qosError = QoSError(
            throwable = throwable,
            playerPosition = 30.seconds.inWholeMilliseconds,
            severity = QoSError.Severity.FATAL,
            url = "",
        )

        val logLines = qosError.log.lineSequence()

        assertTrue(logLines.count() > 1, "Expected log to contain the stacktrace")
        assertEquals("java.lang.RuntimeException: ${throwable.message}", logLines.first())
        assertTrue(
            logLines.any { it == "Caused by: java.lang.NullPointerException: ${cause.message}" },
            "Expected log to contain a cause",
        )
        assertEquals(throwable.message, qosError.message)
        assertEquals("RuntimeException", qosError.name)
        assertEquals(QoSError.Severity.FATAL, qosError.severity)
    }
}
