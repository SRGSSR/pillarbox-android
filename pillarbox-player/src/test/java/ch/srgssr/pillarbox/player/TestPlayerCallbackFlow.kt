/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.Commands
import androidx.media3.common.Timeline
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import ch.srgssr.pillarbox.player.test.utils.PlayerListenerCommander
import ch.srgssr.pillarbox.player.utils.StringUtil
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class TestPlayerCallbackFlow {
    private lateinit var player: Player
    private lateinit var dispatcher: CoroutineDispatcher

    @Before
    @OptIn(ExperimentalCoroutinesApi::class)
    fun setUp() {
        dispatcher = UnconfinedTestDispatcher()
        player = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun testCurrentPositionWhilePlaying() = runTest {
        every { player.currentPosition } returns C.TIME_UNSET
        every { player.isPlaying } returns true
        val currentPositionFlow = player.currentPositionAsFlow()
        currentPositionFlow.test {
            Assert.assertEquals(C.TIME_UNSET, awaitItem())
            every { player.currentPosition } returns 1000L
            Assert.assertEquals(1000L, awaitItem())
            every { player.currentPosition } returns 10_000L
            Assert.assertEquals(10_000L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    /**
     * Test current position while not playing
     * We expected a Timeout as the flow doesn't start
     */
    @Test
    fun testCurrentPositionWhileNotPlaying() = runTest {
        every { player.isPlaying } returns false
        every { player.currentPosition } returns 1000L
        player.currentPositionAsFlow().test {
            Assert.assertEquals(1000L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testCurrentBufferedPercentage() = runTest {
        every { player.isPlaying } returns true
        player.currentBufferedPercentageAsFlow().test {
            every { player.bufferedPercentage } returns 0
            Assert.assertEquals(0.0f, awaitItem())
            every { player.bufferedPercentage } returns 75
            Assert.assertEquals(0.75f, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testUpdateCurrentPositionAfterPositionDiscontinuity() = runTest {
        every { player.isPlaying } returns false // disable periodic update
        every { player.currentPosition } returns 0L
        val fakePlayer = PlayerListenerCommander(player)
        val discontinuityTests = mapOf(
            Player.DISCONTINUITY_REASON_SEEK to 1000L,
            Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT to 2000L,
            Player.DISCONTINUITY_REASON_INTERNAL to 3000L,
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION to 4000L,
            Player.DISCONTINUITY_REASON_REMOVE to 0L,
            Player.DISCONTINUITY_REASON_SKIP to C.TIME_UNSET,
        )
        fakePlayer.currentPositionAsFlow().test {
            Assert.assertEquals(0L, awaitItem())
            for ((reason, position) in discontinuityTests) {
                fakePlayer.onPositionDiscontinuity(
                    mockk(),
                    Player.PositionInfo(null, 0, null, null, 0, position, 0, 0, 0),
                    reason
                )
            }

            for ((reason, position) in discontinuityTests) {
                Assert.assertEquals(StringUtil.discontinuityReasonString(reason), position, awaitItem())
            }
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testDuration() = runTest {
        every { player.duration } returns C.TIME_UNSET
        val fakePlayer = PlayerListenerCommander(player)
        val durationFlow = fakePlayer.durationAsFlow()
        durationFlow.test {
            every { player.duration } returns 20_000L
            fakePlayer.onPlaybackStateChanged(Player.STATE_BUFFERING)
            fakePlayer.onPlaybackStateChanged(Player.STATE_READY)
            every { player.duration } returns 30_000L
            fakePlayer.onTimelineChanged(Timeline.EMPTY, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
            every { player.duration } returns 40_000L
            fakePlayer.onTimelineChanged(Timeline.EMPTY, Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE)

            Assert.assertEquals("initial duration", C.TIME_UNSET, awaitItem())
            Assert.assertEquals("State ready", 20_000L, awaitItem())
            Assert.assertEquals("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED", 30_000L, awaitItem())
            Assert.assertEquals("TIMELINE_CHANGE_REASON_SOURCE_UPDATE", 40_000L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testIsPlaying() = runTest {
        every { player.isPlaying } returns false
        val fakePlayer = PlayerListenerCommander(player)
        val isPlayingFlow = fakePlayer.isPlayingAsFlow()
        isPlayingFlow.test {
            fakePlayer.onIsPlayingChanged(true)
            fakePlayer.onIsPlayingChanged(true)
            fakePlayer.onIsPlayingChanged(false)

            Assert.assertEquals("initial isPlaying", false, awaitItem())
            Assert.assertEquals("isPlaying", true, awaitItem())
            Assert.assertEquals("isPlaying", true, awaitItem())
            Assert.assertEquals("isPlaying", false, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testPlaybackState() = runTest {
        every { player.playbackState } returns Player.STATE_IDLE
        val fakePlayer = PlayerListenerCommander(player)
        val playbackStateFlow = fakePlayer.playbackStateAsFlow()
        val playbackStates =
            listOf(
                Player.STATE_BUFFERING,
                Player.STATE_READY,
                Player.STATE_BUFFERING,
                Player.STATE_READY,
                Player.STATE_ENDED
            )
        playbackStateFlow.test {
            for (state in playbackStates) {
                fakePlayer.onPlaybackStateChanged(state)
            }

            Assert.assertEquals("Initial state", Player.STATE_IDLE, awaitItem())
            for (state in playbackStates) {
                Assert.assertEquals(StringUtil.playerStateString(state), state, awaitItem())
            }
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testPlaybackError() = runTest {
        val error = mockk<PlaybackException>()
        val noError: PlaybackException? = null
        every { player.playerError } returns null
        val fakePlayer = PlayerListenerCommander(player)

        fakePlayer.playerErrorAsFlow().test {
            fakePlayer.onPlayerErrorChanged(error)
            fakePlayer.onPlayerErrorChanged(noError)

            Assert.assertEquals("Initial error", null, awaitItem())
            Assert.assertEquals("error", error, awaitItem())
            Assert.assertEquals("error removed", noError, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testAvailableCommands() = runTest {
        val command1 = mockk<Commands>()
        val command2 = mockk<Commands>()
        every { player.availableCommands } returns command1
        val fakePlayer = PlayerListenerCommander(player)
        fakePlayer.availableCommandsAsFlow().test {
            fakePlayer.onAvailableCommandsChanged(command2)
            Assert.assertEquals(command1, awaitItem())
            Assert.assertEquals(command2, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testShuffleModeEnabled() = runTest {
        every { player.shuffleModeEnabled } returns false

        val fakePlayer = PlayerListenerCommander(player)
        fakePlayer.shuffleModeEnabledAsFlow().test {
            fakePlayer.onShuffleModeEnabledChanged(false)
            fakePlayer.onShuffleModeEnabledChanged(false)
            fakePlayer.onShuffleModeEnabledChanged(true)
            fakePlayer.onShuffleModeEnabledChanged(false)

            Assert.assertEquals("initial state", false, awaitItem())
            Assert.assertEquals(false, awaitItem())
            Assert.assertEquals(false, awaitItem())
            Assert.assertEquals(true, awaitItem())
            Assert.assertEquals(false, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testPlaybackSpeed() = runTest {
        val initialPlaybackRate = 1.5f
        val initialParameters: PlaybackParameters = PlaybackParameters.DEFAULT.withSpeed(initialPlaybackRate)
        every { player.playbackParameters } returns initialParameters
        val fakePlayer = PlayerListenerCommander(player)
        fakePlayer.getPlaybackSpeedAsFlow().test {
            fakePlayer.onPlaybackParametersChanged(initialParameters.withSpeed(2.0f))
            fakePlayer.onPlaybackParametersChanged(initialParameters.withSpeed(0.5f))

            Assert.assertEquals("Initial playback speed", initialPlaybackRate, awaitItem())
            Assert.assertEquals(2.0f, awaitItem())
            Assert.assertEquals(0.5f, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testCurrentMediaIndex() = runTest {
        every { player.currentMediaItemIndex } returns 0
        val fakePlayer = PlayerListenerCommander(player)
        val transitionReasonCases = mapOf(
            Player.MEDIA_ITEM_TRANSITION_REASON_SEEK to 10,
            Player.MEDIA_ITEM_TRANSITION_REASON_AUTO to 11,
            Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT to 12,
            Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED to 13,
        )
        fakePlayer.getCurrentMediaItemIndexAsFlow().test {
            every { player.currentMediaItemIndex } returns 78
            fakePlayer.onTimelineChanged(mockk(), Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE)
            every { player.currentMediaItemIndex } returns 2
            fakePlayer.onTimelineChanged(mockk(), Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)

            for ((reason, index) in transitionReasonCases) {
                every { player.currentMediaItemIndex } returns index
                fakePlayer.onMediaItemTransition(mockk(), reason)
            }

            Assert.assertEquals("Initial index", 0, awaitItem())
            Assert.assertEquals("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED", 2, awaitItem())
            for ((reason, index) in transitionReasonCases) {
                Assert.assertEquals(StringUtil.mediaItemTransitionReasonString(reason), index, awaitItem())
            }
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testCurrentMediaItem() = runTest {
        every { player.currentMediaItem } returns null
        val fakePlayer = PlayerListenerCommander(player)
        val transitionReasonCases = mapOf<Int, MediaItem>(
            Player.MEDIA_ITEM_TRANSITION_REASON_SEEK to mockk(),
            Player.MEDIA_ITEM_TRANSITION_REASON_AUTO to mockk(),
            Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT to mockk(),
            Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED to mockk(),
        )
        val mediaItemTimeLinePlaylistChanged: MediaItem = mockk()
        val mediaItemTimeLineSourceUpdate: MediaItem = mockk()
        fakePlayer.currentMediaItemAsFlow().test {
            every { player.currentMediaItem } returns mediaItemTimeLinePlaylistChanged
            fakePlayer.onTimelineChanged(mockk(), Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
            every { player.currentMediaItem } returns mediaItemTimeLineSourceUpdate
            fakePlayer.onTimelineChanged(mockk(), Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE)

            for ((reason, mediaItem) in transitionReasonCases) {
                every { player.currentMediaItem } returns mediaItem
                fakePlayer.onMediaItemTransition(mediaItem, reason)
            }

            Assert.assertNull("Initial", awaitItem())
            Assert.assertEquals("TIMELINE_CHANGE_REASON_SOURCE_UPDATE", mediaItemTimeLineSourceUpdate, awaitItem())
            for ((reason, mediaItem) in transitionReasonCases) {
                Assert.assertEquals(StringUtil.mediaItemTransitionReasonString(reason), mediaItem, awaitItem())
            }
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testCurrentMediaItems() = runTest {
        val item1 = mockk<MediaItem>()
        val item2 = mockk<MediaItem>()
        val item3 = mockk<MediaItem>()

        val list1 = listOf(item1, item2)
        val list2 = listOf(item1, item2, item3)

        every { player.mediaItemCount } returns 2
        every { player.getMediaItemAt(0) } returns item1
        every { player.getMediaItemAt(1) } returns item2
        every { player.getMediaItemAt(2) } returns item3

        val fakePlayer = PlayerListenerCommander(player)

        fakePlayer.getCurrentMediaItemsAsFlow().test {
            every { player.mediaItemCount } returns 3
            fakePlayer.onTimelineChanged(mockk(), Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)
            every { player.mediaItemCount } returns 1
            fakePlayer.onTimelineChanged(mockk(), Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE)

            Assert.assertEquals("Initial list", list1, awaitItem())
            Assert.assertEquals("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED", list2, awaitItem())
            Assert.assertEquals("TIMELINE_CHANGE_REASON_SOURCE_UPDATE list", listOf(item1), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testVideoSize() = runTest {
        val initialSize = VideoSize.UNKNOWN
        val newSize = VideoSize(1200, 1000)
        every { player.videoSize } returns initialSize
        val fakePlayer = PlayerListenerCommander(player)
        fakePlayer.videoSizeAsFlow().test {
            fakePlayer.onVideoSizeChanged(newSize)

            Assert.assertEquals("Initial video size", initialSize, awaitItem())
            Assert.assertEquals("Updated video size", newSize, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get aspect ratio as flow, default aspect ratio`() = runTest {
        val fakePlayer = PlayerListenerCommander(player)

        fakePlayer.getAspectRatioAsFlow(16 / 9f).test {
            assertEquals(16 / 9f, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get aspect ratio as flow, empty tracks`() = runTest {
        val fakePlayer = PlayerListenerCommander(player)

        fakePlayer.getAspectRatioAsFlow(0f).test {
            fakePlayer.onTracksChanged(Tracks.EMPTY)

            assertEquals(0f, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get aspect ratio as flow, video tracks`() = runTest {
        val videoTracks = Tracks(
            listOf(
                createTrackGroup(
                    selectedIndex = 1,
                    createVideoFormat("v1", width = 800, height = 600),
                    createVideoFormat("v2", width = 1440, height = 900),
                    createVideoFormat("v3", width = 1920, height = 1080),
                )
            )
        )

        val fakePlayer = PlayerListenerCommander(player)

        fakePlayer.getAspectRatioAsFlow(0f).test {
            fakePlayer.onTracksChanged(videoTracks)

            assertEquals(0f, awaitItem())
            assertEquals(8 / 5f, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get aspect ratio as flow, video tracks no video size`() = runTest {
        val videoTracks = Tracks(
            listOf(
                createTrackGroup(
                    selectedIndex = 1,
                    createVideoFormat("v1", width = Format.NO_VALUE, height = Format.NO_VALUE),
                    createVideoFormat("v2", width = Format.NO_VALUE, height = Format.NO_VALUE),
                    createVideoFormat("v3", width = Format.NO_VALUE, height = Format.NO_VALUE),
                )
            )
        )

        val fakePlayer = PlayerListenerCommander(player)

        fakePlayer.getAspectRatioAsFlow(0f).test {
            fakePlayer.onTracksChanged(videoTracks)

            assertEquals(0f, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get aspect ratio as flow, video tracks no video size, has video size`() = runTest {
        val videoTracks = Tracks(
            listOf(
                createTrackGroup(
                    selectedIndex = 1,
                    createVideoFormat("v1", width = Format.NO_VALUE, height = Format.NO_VALUE),
                    createVideoFormat("v2", width = Format.NO_VALUE, height = Format.NO_VALUE),
                    createVideoFormat("v3", width = Format.NO_VALUE, height = Format.NO_VALUE),
                )
            )
        )

        val fakePlayer = PlayerListenerCommander(player)

        fakePlayer.getAspectRatioAsFlow(0f).test {
            fakePlayer.onVideoSizeChanged(VideoSize(1920, 1080))
            fakePlayer.onTracksChanged(videoTracks)

            assertEquals(0f, awaitItem())
            assertEquals(16 / 9f, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get aspect ratio as flow, video tracks no video size, no video size`() = runTest {
        val videoTracks = Tracks(
            listOf(
                createTrackGroup(
                    selectedIndex = 1,
                    createVideoFormat("v1", width = Format.NO_VALUE, height = Format.NO_VALUE),
                    createVideoFormat("v2", width = Format.NO_VALUE, height = Format.NO_VALUE),
                    createVideoFormat("v3", width = Format.NO_VALUE, height = Format.NO_VALUE),
                )
            )
        )

        val fakePlayer = PlayerListenerCommander(player)

        fakePlayer.getAspectRatioAsFlow(0f).test {
            fakePlayer.onTracksChanged(videoTracks)

            assertEquals(0f, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get aspect ratio as flow, video tracks without selection`() = runTest {
        val videoTracksWithoutSelection = Tracks(
            listOf(
                createTrackGroup(
                    selectedIndex = -1,
                    createVideoFormat("v1", width = 800, height = 600),
                    createVideoFormat("v2", width = 1440, height = 900),
                    createVideoFormat("v3", width = 1920, height = 1080),
                )
            )
        )

        val fakePlayer = PlayerListenerCommander(player)

        fakePlayer.getAspectRatioAsFlow(0f).test {
            fakePlayer.onTracksChanged(videoTracksWithoutSelection)

            assertEquals(0f, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get aspect ratio as flow, audio tracks`() = runTest {
        val audioTracks = Tracks(
            listOf(
                createTrackGroup(
                    selectedIndex = 1,
                    createAudioFormat("v1"),
                    createAudioFormat("v2"),
                    createAudioFormat("v3"),
                )
            )
        )

        val fakePlayer = PlayerListenerCommander(player)

        fakePlayer.getAspectRatioAsFlow(0f).test {
            fakePlayer.onTracksChanged(audioTracks)

            assertEquals(0f, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `get aspect ratio as flow, changing tracks`() = runTest {
        val videoTracksWithoutSelection = Tracks(
            listOf(
                createTrackGroup(
                    selectedIndex = 1,
                    createVideoFormat("v1", width = 800, height = 600),
                    createVideoFormat("v2", width = 1440, height = 900),
                    createVideoFormat("v3", width = 1920, height = 1080),
                )
            )
        )
        val audioTracks = Tracks(
            listOf(
                createTrackGroup(
                    selectedIndex = 1,
                    createAudioFormat("v1"),
                    createAudioFormat("v2"),
                    createAudioFormat("v3"),
                )
            )
        )

        val fakePlayer = PlayerListenerCommander(player)

        fakePlayer.getAspectRatioAsFlow(0f).test {
            fakePlayer.onTracksChanged(videoTracksWithoutSelection)
            fakePlayer.onTracksChanged(audioTracks)

            assertEquals(0f, awaitItem())
            assertEquals(8 / 5f, awaitItem())
            assertEquals(0f, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    private companion object {
        private const val AUDIO_MIME_TYPE = MimeTypes.AUDIO_MP4
        private const val VIDEO_MIME_TYPE = MimeTypes.VIDEO_H265

        private fun createAudioFormat(label: String): Format {
            return Format.Builder()
                .setId("id:$label")
                .setLabel(label)
                .setLanguage("fr")
                .setContainerMimeType(AUDIO_MIME_TYPE)
                .build()
        }

        private fun createVideoFormat(
            label: String,
            width: Int,
            height: Int,
        ): Format {
            return Format.Builder()
                .setId("id:$label")
                .setLabel(label)
                .setLanguage("fr")
                .setWidth(width)
                .setHeight(height)
                .setContainerMimeType(VIDEO_MIME_TYPE)
                .build()
        }

        private fun createTrackGroup(
            selectedIndex: Int,
            vararg formats: Format,
        ): Tracks.Group {
            val trackGroup = TrackGroup(*formats)
            val trackSupport = IntArray(formats.size) {
                C.FORMAT_HANDLED
            }
            val selected = BooleanArray(formats.size) { index ->
                index == selectedIndex
            }
            return Tracks.Group(trackGroup, false, trackSupport, selected)
        }
    }
}
