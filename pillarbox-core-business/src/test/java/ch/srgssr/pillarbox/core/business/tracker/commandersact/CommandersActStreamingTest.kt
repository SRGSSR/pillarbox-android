/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.commandersact

import android.content.Context
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.media3.common.C
import androidx.media3.common.DeviceInfo
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType
import ch.srgssr.pillarbox.analytics.commandersact.TCMediaEvent
import io.mockk.Called
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class CommandersActStreamingTest {
    @Test
    fun `commanders act streaming, player not playing initially`() {
        val commandersAct = mockk<CommandersAct>(relaxed = true)

        val commandersActStreaming = CommandersActStreaming(
            commandersAct = commandersAct,
            player = createExoPlayer(isPlaying = false),
            currentData = CommandersActTracker.Data(assets = emptyMap()),
            coroutineContext = EmptyCoroutineContext,
        )

        verify {
            commandersAct wasNot Called
        }

        commandersActStreaming.notifyStop(
            position = 30.seconds,
            isEoF = false,
        )

        verify {
            commandersAct wasNot Called
        }
    }

    @Test
    fun `commanders act streaming, player playing initially, live`() {
        val tcMediaEventSlot = slot<TCMediaEvent>()
        val commandersAct = mockk<CommandersAct> {
            justRun { sendTcMediaEvent(capture(tcMediaEventSlot)) }
        }
        val commandersActStreaming = CommandersActStreaming(
            commandersAct = commandersAct,
            player = createExoPlayer(
                isPlaying = true,
                currentPosition = 2.seconds.inWholeMilliseconds,
                isCurrentMediaItemLive = true,
                volume = 0.5f,
                deviceVolume = 25,
                duration = 45.seconds.inWholeMilliseconds,
            ),
            currentData = CommandersActTracker.Data(
                assets = mapOf(
                    "key1" to "value1",
                ),
                sourceId = "source_id",
            ),
            coroutineContext = EmptyCoroutineContext,
        )

        verify {
            commandersAct.sendTcMediaEvent(any())
        }

        assertTrue(tcMediaEventSlot.isCaptured)

        val tcMediaEventPlay = tcMediaEventSlot.captured
        assertEquals(MediaEventType.Play, tcMediaEventPlay.eventType)
        assertEquals(commandersActStreaming.currentData.assets, tcMediaEventPlay.assets)
        assertEquals(commandersActStreaming.currentData.sourceId, tcMediaEventPlay.sourceId)
        assertFalse(tcMediaEventPlay.isSubtitlesOn)
        assertNull(tcMediaEventPlay.subtitleSelectionLanguage)
        assertEquals(C.LANGUAGE_UNDETERMINED, tcMediaEventPlay.audioTrackLanguage)
        assertEquals(43.seconds, tcMediaEventPlay.timeShift)
        assertEquals(0.25f, tcMediaEventPlay.deviceVolume)
        assertEquals(0.milliseconds.inWholeSeconds, tcMediaEventPlay.mediaPosition.inWholeSeconds)
        assertFalse(tcMediaEventPlay.audioTrackHasAudioDescription)

        commandersActStreaming.notifyStop(
            position = 30.seconds,
            isEoF = true,
        )

        val tcMediaEventStop = tcMediaEventSlot.captured
        assertEquals(MediaEventType.Eof, tcMediaEventStop.eventType)
        assertEquals(commandersActStreaming.currentData.assets, tcMediaEventStop.assets)
        assertEquals(commandersActStreaming.currentData.sourceId, tcMediaEventStop.sourceId)
        assertFalse(tcMediaEventStop.isSubtitlesOn)
        assertNull(tcMediaEventStop.subtitleSelectionLanguage)
        assertEquals(C.LANGUAGE_UNDETERMINED, tcMediaEventStop.audioTrackLanguage)
        assertEquals(15.seconds, tcMediaEventStop.timeShift)
        assertEquals(0.25f, tcMediaEventStop.deviceVolume)
        assertFalse(tcMediaEventPlay.audioTrackHasAudioDescription)
    }

    @Test
    fun `commanders act streaming, player playing initially, not live`() = runTest {
        val tcMediaEventSlot = slot<TCMediaEvent>()
        val commandersAct = mockk<CommandersAct> {
            justRun { sendTcMediaEvent(capture(tcMediaEventSlot)) }
        }
        val commandersActStreaming = CommandersActStreaming(
            commandersAct = commandersAct,
            player = createExoPlayer(
                isPlaying = true,
                duration = 45.seconds.inWholeMilliseconds,
                currentTracks = Tracks(
                    listOf(
                        createTracks(
                            label = "Text",
                            language = "fr",
                            sampleMimeType = MimeTypes.APPLICATION_TTML,
                        ),
                        createTracks(
                            label = "Audio",
                            language = "en",
                            sampleMimeType = MimeTypes.AUDIO_MP4,
                        ),
                    ),
                ),
            ),
            currentData = CommandersActTracker.Data(
                assets = mapOf(
                    "key1" to "value1",
                ),
                sourceId = "source_id",
            ),
            coroutineContext = EmptyCoroutineContext,
        )

        verify {
            commandersAct.sendTcMediaEvent(any())
        }

        assertTrue(tcMediaEventSlot.isCaptured)

        val tcMediaEvent = tcMediaEventSlot.captured
        assertEquals(MediaEventType.Play, tcMediaEvent.eventType)
        assertEquals(commandersActStreaming.currentData.assets, tcMediaEvent.assets)
        assertEquals(commandersActStreaming.currentData.sourceId, tcMediaEvent.sourceId)
        assertTrue(tcMediaEvent.isSubtitlesOn)
        assertEquals("fr", tcMediaEvent.subtitleSelectionLanguage)
        assertEquals("en", tcMediaEvent.audioTrackLanguage)
        assertNull(tcMediaEvent.timeShift)
        assertEquals(0f, tcMediaEvent.deviceVolume)
        assertEquals(0.milliseconds.inWholeSeconds, tcMediaEvent.mediaPosition.inWholeSeconds)
        assertFalse(tcMediaEvent.audioTrackHasAudioDescription)

        commandersActStreaming.notifyStop(
            position = 30.seconds,
            isEoF = false,
        )

        val tcMediaEventStop = tcMediaEventSlot.captured
        assertEquals(MediaEventType.Stop, tcMediaEventStop.eventType)
        assertEquals(commandersActStreaming.currentData.assets, tcMediaEventStop.assets)
        assertEquals(commandersActStreaming.currentData.sourceId, tcMediaEventStop.sourceId)
        assertTrue(tcMediaEvent.isSubtitlesOn)
        assertEquals("fr", tcMediaEvent.subtitleSelectionLanguage)
        assertEquals("en", tcMediaEvent.audioTrackLanguage)
        assertNull(tcMediaEvent.timeShift)
        assertEquals(0f, tcMediaEventStop.deviceVolume)
        assertEquals(30.seconds.inWholeSeconds, tcMediaEventStop.mediaPosition.inWholeSeconds)
        assertFalse(tcMediaEventStop.audioTrackHasAudioDescription)
    }

    @Test
    fun `commanders act streaming, player with audio description`() = runTest {
        val tcMediaEventSlot = slot<TCMediaEvent>()
        val commandersAct = mockk<CommandersAct> {
            justRun { sendTcMediaEvent(capture(tcMediaEventSlot)) }
        }
        val commandersActStreaming = CommandersActStreaming(
            commandersAct = commandersAct,
            player = createExoPlayer(
                isPlaying = true,
                duration = 45.seconds.inWholeMilliseconds,
                currentTracks = Tracks(
                    listOf(
                        createTracks(
                            label = "Text",
                            language = "fr",
                            sampleMimeType = MimeTypes.APPLICATION_TTML,
                        ),
                        createTracks(
                            label = "Audio",
                            language = "en",
                            roleFlags = C.ROLE_FLAG_DESCRIBES_VIDEO,
                            sampleMimeType = MimeTypes.AUDIO_MP4,
                        ),
                    ),
                ),
            ),
            currentData = CommandersActTracker.Data(
                assets = mapOf(
                    "key1" to "value1",
                ),
                sourceId = "source_id",
            ),
            coroutineContext = EmptyCoroutineContext,
        )

        verify {
            commandersAct.sendTcMediaEvent(any())
        }

        assertTrue(tcMediaEventSlot.isCaptured)

        val tcMediaEvent = tcMediaEventSlot.captured
        assertEquals(MediaEventType.Play, tcMediaEvent.eventType)
        assertEquals(commandersActStreaming.currentData.assets, tcMediaEvent.assets)
        assertEquals(commandersActStreaming.currentData.sourceId, tcMediaEvent.sourceId)
        assertTrue(tcMediaEvent.isSubtitlesOn)
        assertEquals("fr", tcMediaEvent.subtitleSelectionLanguage)
        assertEquals("en", tcMediaEvent.audioTrackLanguage)
        assertNull(tcMediaEvent.timeShift)
        assertEquals(0f, tcMediaEvent.deviceVolume)
        assertEquals(0.milliseconds.inWholeSeconds, tcMediaEvent.mediaPosition.inWholeSeconds)
        assertTrue(tcMediaEvent.audioTrackHasAudioDescription)
    }

    private fun createExoPlayer(
        isPlaying: Boolean,
        currentPosition: Long = 0L,
        isCurrentMediaItemLive: Boolean = false,
        deviceInfo: DeviceInfo = DeviceInfo.Builder(DeviceInfo.PLAYBACK_TYPE_LOCAL)
            .setMinVolume(0)
            .setMaxVolume(100)
            .build(),
        @FloatRange(from = 0.0, to = 1.0) volume: Float = 0f,
        @IntRange(from = 0) deviceVolume: Int = 0,
        duration: Long = 0L,
        currentTracks: Tracks = Tracks.EMPTY, // groups, audio
    ): ExoPlayer {
        return mockk<ExoPlayer> {
            val player = this
            val looper = ApplicationProvider.getApplicationContext<Context>().mainLooper

            every { player.playWhenReady } returns true
            every { player.isPlaying } returns isPlaying
            every { player.currentPosition } returns currentPosition
            every { player.isCurrentMediaItemLive } returns isCurrentMediaItemLive
            every { player.deviceInfo } returns deviceInfo
            every { player.volume } returns volume
            every { player.deviceVolume } returns deviceVolume
            every { player.duration } returns duration
            every { player.currentTracks } returns currentTracks
            every { player.applicationLooper } returns looper
        }
    }

    private fun createTracks(
        label: String,
        language: String,
        sampleMimeType: String,
        roleFlags: Int = 0,
    ): Tracks.Group {
        val mediaTrackGroup = listOf(
            Format.Builder()
                .setId("${label}_1")
                .setLabel("$label 1")
                .setLanguage(language)
                .setSampleMimeType(sampleMimeType)
                .build(),
            Format.Builder()
                .setId("${label}_2")
                .setLabel("$label 2")
                .setLanguage(language)
                .setSampleMimeType(sampleMimeType)
                .setSelectionFlags(C.SELECTION_FLAG_FORCED)
                .setRoleFlags(roleFlags)
                .build(),
        )

        return Tracks.Group(
            TrackGroup(*mediaTrackGroup.toTypedArray()),
            false,
            IntArray(mediaTrackGroup.size) { C.FORMAT_HANDLED },
            BooleanArray(mediaTrackGroup.size) {
                // select the second track
                it == 1
            },
        )
    }
}
