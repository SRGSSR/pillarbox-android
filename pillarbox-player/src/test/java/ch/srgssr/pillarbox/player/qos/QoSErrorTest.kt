/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos

import java.lang.RuntimeException
import kotlin.test.Test
import kotlin.test.assertEquals
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
        )

        val logLines = qosError.log.lineSequence()

        assertEquals(42, logLines.count())
        assertEquals("java.lang.IllegalStateException", logLines.first())
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
        )

        val logLines = qosError.log.lineSequence()

        assertEquals(45, logLines.count())
        assertEquals("java.lang.RuntimeException: ${throwable.message}", logLines.first())
        assertEquals(throwable.message, qosError.message)
        assertEquals("RuntimeException", qosError.name)
        assertEquals(QoSError.Severity.FATAL, qosError.severity)
    }
}
