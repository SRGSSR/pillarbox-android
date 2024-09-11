/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.monitoring.models

import androidx.media3.common.Player
import androidx.media3.common.Timeline
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ErrorMessageDataTest {
    private lateinit var player: Player

    @BeforeTest
    fun setUp() {
        player = mockk {
            every { duration } returns DURATION
            every { currentPosition } returns CURRENT_POSITION
            every { currentTimeline } returns Timeline.EMPTY
        }
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `throwableConstructor with empty exception`() {
        val throwable = IllegalStateException()
        val qosErrorMessageData = ErrorMessageData(
            throwable = throwable,
            severity = ErrorMessageData.Severity.WARNING,
            player = player,
            url = URL,
        )

        val logLines = qosErrorMessageData.log.lineSequence()

        assertEquals(DURATION, qosErrorMessageData.duration)
        assertTrue(logLines.count() > 1, "Expected log to contain the stacktrace")
        assertEquals("java.lang.IllegalStateException", logLines.first())
        assertTrue(logLines.none { it.startsWith("Caused by: ") }, "Expected log to not contain a cause")
        assertEquals("", qosErrorMessageData.message)
        assertEquals("IllegalStateException", qosErrorMessageData.name)
        assertEquals(CURRENT_POSITION, qosErrorMessageData.position)
        assertNull(qosErrorMessageData.positionTimestamp)
        assertEquals(ErrorMessageData.Severity.WARNING, qosErrorMessageData.severity)
        assertEquals(URL, qosErrorMessageData.url)
    }

    @Test
    fun `throwableConstructor with detailed exception`() {
        val cause = NullPointerException("Expected 'foo' to be not null")
        val throwable = RuntimeException("Something bad happened", cause)
        val qosErrorMessageData = ErrorMessageData(
            throwable = throwable,
            severity = ErrorMessageData.Severity.FATAL,
            player = player,
            url = URL,
        )

        val logLines = qosErrorMessageData.log.lineSequence()

        assertEquals(DURATION, qosErrorMessageData.duration)
        assertTrue(logLines.count() > 1, "Expected log to contain the stacktrace")
        assertEquals("java.lang.RuntimeException: ${throwable.message}", logLines.first())
        assertTrue(
            logLines.any { it == "Caused by: java.lang.NullPointerException: ${cause.message}" },
            "Expected log to contain a cause",
        )
        assertEquals(throwable.message, qosErrorMessageData.message)
        assertEquals("RuntimeException", qosErrorMessageData.name)
        assertEquals(CURRENT_POSITION, qosErrorMessageData.position)
        assertNull(qosErrorMessageData.positionTimestamp)
        assertEquals(ErrorMessageData.Severity.FATAL, qosErrorMessageData.severity)
        assertEquals(URL, qosErrorMessageData.url)
    }

    private companion object {
        private val DURATION = 10.minutes.inWholeMilliseconds
        private val CURRENT_POSITION = 30.seconds.inWholeMilliseconds
        private const val URL = "https://rts-vod-amd.akamaized.net/ww/14970442/7510ee63-05a4-3d48-8d26-1f1b3a82f6be/master.m3u8"
    }
}
