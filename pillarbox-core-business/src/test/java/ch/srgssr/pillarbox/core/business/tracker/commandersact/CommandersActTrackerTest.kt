/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.commandersact

import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType
import ch.srgssr.pillarbox.analytics.commandersact.TCMediaEvent
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.runner.RunWith
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class CommandersActTrackerTest {
    @Test(expected = IllegalArgumentException::class)
    fun `start() requires a non-null initial data`() {
        val player = mockk<ExoPlayer>(relaxed = true)
        val commandersActs = mockk<CommandersAct>(relaxed = true)
        val commandersActTracker = CommandersActTracker(commandersActs, EmptyCoroutineContext)

        commandersActTracker.start(
            player = player,
            initialData = null,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `start() requires an instance of CommandersActTracker#Data instance for the initial data`() {
        val player = mockk<ExoPlayer>(relaxed = true)
        val commandersActs = mockk<CommandersAct>(relaxed = true)
        val commandersActTracker = CommandersActTracker(commandersActs, EmptyCoroutineContext)

        commandersActTracker.start(
            player = player,
            initialData = "My data",
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `update() requires an instance of CommandersActTracker#Data instance for the data`() {
        val commandersActs = mockk<CommandersAct>(relaxed = true)
        val commandersActTracker = CommandersActTracker(commandersActs, EmptyCoroutineContext)

        commandersActTracker.update(data = "My data")
    }

    @Test
    fun `commanders act tracker`() {
        val player = mockk<ExoPlayer>(relaxed = true) {
            every { isPlaying } returns true
        }
        val commandersAct = mockk<CommandersAct>(relaxed = true)
        val commandersActTracker = CommandersActTracker(commandersAct, EmptyCoroutineContext)
        val commandersActStreamingSlot = slot<CommandersActStreaming>()
        val tcMediaEventSlots = mutableListOf<TCMediaEvent>()

        commandersActTracker.start(
            player = player,
            initialData = CommandersActTracker.Data(emptyMap()),
        )

        verify {
            commandersAct.enableRunningInBackground()
            commandersAct.sendTcMediaEvent(any())

            player.isPlaying
            player.addAnalyticsListener(capture(commandersActStreamingSlot))
        }

        assertTrue(commandersActStreamingSlot.isCaptured)

        val commandersActStreaming = commandersActStreamingSlot.captured
        val newData = CommandersActTracker.Data(
            assets = mapOf(
                "key1" to "value1",
            ),
        )

        commandersActTracker.update(
            data = newData,
        )

        assertEquals(newData, commandersActStreaming.currentData)

        commandersActTracker.stop(
            player = player,
            reason = MediaItemTracker.StopReason.EoF,
            positionMs = 30.seconds.inWholeMilliseconds,
        )

        verify {
            player.removeAnalyticsListener(commandersActStreaming)
            commandersAct.sendTcMediaEvent(capture(tcMediaEventSlots))
        }

        val tcMediaEvent = tcMediaEventSlots.last()
        assertEquals(MediaEventType.Eof, tcMediaEvent.eventType)
        assertEquals(commandersActStreaming.currentData.assets, tcMediaEvent.assets)
        assertEquals(commandersActStreaming.currentData.sourceId, tcMediaEvent.sourceId)
        assertFalse(tcMediaEvent.isSubtitlesOn)
        assertNull(tcMediaEvent.subtitleSelectionLanguage)
        assertEquals(C.LANGUAGE_UNDETERMINED, tcMediaEvent.audioTrackLanguage)
        assertNull(tcMediaEvent.timeShift)
        assertEquals(0f, tcMediaEvent.deviceVolume)
        assertEquals(30.seconds, tcMediaEvent.mediaPosition)
    }
}
