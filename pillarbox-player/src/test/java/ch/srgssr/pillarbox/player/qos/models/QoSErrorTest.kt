/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

import androidx.media3.common.Player
import androidx.media3.common.Timeline
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class QoSErrorTest {
    @Test
    fun `throwableConstructor with empty exception`() {
        val player = createPlayer()
        val throwable = IllegalStateException()
        val qosError = QoSError(
            throwable = throwable,
            severity = QoSError.Severity.WARNING,
            player = player,
            url = URL,
        )

        val logLines = qosError.log.lineSequence()

        assertEquals(DURATION, qosError.duration)
        assertTrue(logLines.count() > 1, "Expected log to contain the stacktrace")
        assertEquals("java.lang.IllegalStateException", logLines.first())
        assertTrue(logLines.none { it.startsWith("Caused by: ") }, "Expected log to not contain a cause")
        assertEquals("", qosError.message)
        assertEquals("IllegalStateException", qosError.name)
        assertEquals(CURRENT_POSITION, qosError.position)
        assertNull(qosError.positionTimestamp)
        assertEquals(QoSError.Severity.WARNING, qosError.severity)
        assertEquals(URL, qosError.url)
    }

    @Test
    fun `throwableConstructor with detailed exception`() {
        val player = createPlayer()
        val cause = NullPointerException("Expected 'foo' to be not null")
        val throwable = RuntimeException("Something bad happened", cause)
        val qosError = QoSError(
            throwable = throwable,
            severity = QoSError.Severity.FATAL,
            player = player,
            url = URL,
        )

        val logLines = qosError.log.lineSequence()

        assertEquals(DURATION, qosError.duration)
        assertTrue(logLines.count() > 1, "Expected log to contain the stacktrace")
        assertEquals("java.lang.RuntimeException: ${throwable.message}", logLines.first())
        assertTrue(
            logLines.any { it == "Caused by: java.lang.NullPointerException: ${cause.message}" },
            "Expected log to contain a cause",
        )
        assertEquals(throwable.message, qosError.message)
        assertEquals("RuntimeException", qosError.name)
        assertEquals(CURRENT_POSITION, qosError.position)
        assertNull(qosError.positionTimestamp)
        assertEquals(QoSError.Severity.FATAL, qosError.severity)
        assertEquals(URL, qosError.url)
    }

    private companion object {
        private val DURATION = 10.minutes.inWholeMilliseconds
        private val CURRENT_POSITION = 30.seconds.inWholeMilliseconds
        private const val URL = "https://rts-vod-amd.akamaized.net/ww/14970442/7510ee63-05a4-3d48-8d26-1f1b3a82f6be/master.m3u8"

        private fun createPlayer(): Player {
            return mockk {
                every { duration } returns DURATION
                every { currentPosition } returns CURRENT_POSITION
                every { currentTimeline } returns Timeline.EMPTY
            }
        }
    }
}
