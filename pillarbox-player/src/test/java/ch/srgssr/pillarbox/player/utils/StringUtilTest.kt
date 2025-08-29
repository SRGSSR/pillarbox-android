/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import androidx.media3.common.Player
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class StringUtilTest {
    @Test
    fun `media item transition reason string`() {
        assertEquals("MEDIA_ITEM_TRANSITION_REASON_AUTO", StringUtil.mediaItemTransitionReasonString(Player.MEDIA_ITEM_TRANSITION_REASON_AUTO))
        assertEquals(
            "MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED",
            StringUtil.mediaItemTransitionReasonString(Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED)
        )
        assertEquals("MEDIA_ITEM_TRANSITION_REASON_SEEK", StringUtil.mediaItemTransitionReasonString(Player.MEDIA_ITEM_TRANSITION_REASON_SEEK))
        assertEquals("MEDIA_ITEM_TRANSITION_REASON_REPEAT", StringUtil.mediaItemTransitionReasonString(Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT))
        assertEquals("UNKNOWN", StringUtil.mediaItemTransitionReasonString(42))
    }

    @Test
    fun `player state string`() {
        assertEquals("STATE_BUFFERING", StringUtil.playerStateString(Player.STATE_BUFFERING))
        assertEquals("STATE_ENDED", StringUtil.playerStateString(Player.STATE_ENDED))
        assertEquals("STATE_IDLE", StringUtil.playerStateString(Player.STATE_IDLE))
        assertEquals("STATE_READY", StringUtil.playerStateString(Player.STATE_READY))
        assertEquals("UNKNOWN", StringUtil.playerStateString(42))
    }

    @Test
    fun `timeline change reason string`() {
        assertEquals("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED", StringUtil.timelineChangeReasonString(Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED))
        assertEquals("TIMELINE_CHANGE_REASON_SOURCE_UPDATE", StringUtil.timelineChangeReasonString(Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE))
        assertEquals("UNKNOWN", StringUtil.timelineChangeReasonString(42))
    }

    @Test
    fun `discontinuity reason string`() {
        assertEquals("DISCONTINUITY_REASON_AUTO_TRANSITION", StringUtil.discontinuityReasonString(Player.DISCONTINUITY_REASON_AUTO_TRANSITION))
        assertEquals("DISCONTINUITY_REASON_INTERNAL", StringUtil.discontinuityReasonString(Player.DISCONTINUITY_REASON_INTERNAL))
        assertEquals("DISCONTINUITY_REASON_REMOVE", StringUtil.discontinuityReasonString(Player.DISCONTINUITY_REASON_REMOVE))
        assertEquals("DISCONTINUITY_REASON_SEEK", StringUtil.discontinuityReasonString(Player.DISCONTINUITY_REASON_SEEK))
        assertEquals("DISCONTINUITY_REASON_SEEK_ADJUSTMENT", StringUtil.discontinuityReasonString(Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT))
        assertEquals("DISCONTINUITY_REASON_SILENCE_SKIP", StringUtil.discontinuityReasonString(Player.DISCONTINUITY_REASON_SILENCE_SKIP))
        assertEquals("DISCONTINUITY_REASON_SKIP", StringUtil.discontinuityReasonString(Player.DISCONTINUITY_REASON_SKIP))
        assertEquals("UNKNOWN", StringUtil.discontinuityReasonString(42))
    }

    @Test
    fun `player commands string`() {
        assertEquals("[COMMAND_INVALID]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_INVALID)))
        assertEquals("[COMMAND_PLAY_PAUSE]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_PLAY_PAUSE)))
        assertEquals("[COMMAND_PREPARE]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_PREPARE)))
        assertEquals("[COMMAND_STOP]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_STOP)))
        assertEquals("[COMMAND_SEEK_TO_DEFAULT_POSITION]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)))
        assertEquals(
            "[COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM]",
            StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM))
        )
        assertEquals(
            "[COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM]",
            StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM))
        )
        assertEquals("[COMMAND_SEEK_TO_PREVIOUS]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SEEK_TO_PREVIOUS)))
        assertEquals("[COMMAND_SEEK_TO_NEXT_MEDIA_ITEM]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)))
        assertEquals("[COMMAND_SEEK_TO_NEXT]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SEEK_TO_NEXT)))
        assertEquals("[COMMAND_SEEK_TO_MEDIA_ITEM]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SEEK_TO_MEDIA_ITEM)))
        assertEquals("[COMMAND_SEEK_BACK]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SEEK_BACK)))
        assertEquals("[COMMAND_SEEK_FORWARD]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SEEK_FORWARD)))
        assertEquals("[COMMAND_SET_SPEED_AND_PITCH]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SET_SPEED_AND_PITCH)))
        assertEquals("[COMMAND_SET_SHUFFLE_MODE]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SET_SHUFFLE_MODE)))
        assertEquals("[COMMAND_SET_REPEAT_MODE]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SET_REPEAT_MODE)))
        assertEquals("[COMMAND_GET_CURRENT_MEDIA_ITEM]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)))
        assertEquals("[COMMAND_GET_TIMELINE]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_GET_TIMELINE)))
        assertEquals("[COMMAND_GET_METADATA]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_GET_METADATA)))
        assertEquals("[COMMAND_SET_PLAYLIST_METADATA]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SET_PLAYLIST_METADATA)))
        assertEquals("[COMMAND_SET_MEDIA_ITEM]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SET_MEDIA_ITEM)))
        assertEquals("[COMMAND_CHANGE_MEDIA_ITEMS]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_CHANGE_MEDIA_ITEMS)))
        assertEquals("[COMMAND_GET_AUDIO_ATTRIBUTES]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_GET_AUDIO_ATTRIBUTES)))
        assertEquals("[COMMAND_GET_VOLUME]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_GET_VOLUME)))
        assertEquals("[COMMAND_GET_DEVICE_VOLUME]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_GET_DEVICE_VOLUME)))
        assertEquals("[COMMAND_SET_VOLUME]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SET_VOLUME)))
        assertEquals("[COMMAND_SET_DEVICE_VOLUME]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SET_DEVICE_VOLUME)))
        assertEquals(
            "[COMMAND_SET_DEVICE_VOLUME_WITH_FLAGS]",
            StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SET_DEVICE_VOLUME_WITH_FLAGS))
        )
        assertEquals("[COMMAND_ADJUST_DEVICE_VOLUME]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_ADJUST_DEVICE_VOLUME)))
        assertEquals(
            "[COMMAND_ADJUST_DEVICE_VOLUME_WITH_FLAGS]",
            StringUtil.playerCommandsString(playerCommand(Player.COMMAND_ADJUST_DEVICE_VOLUME_WITH_FLAGS))
        )
        assertEquals("[COMMAND_SET_AUDIO_ATTRIBUTES]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SET_AUDIO_ATTRIBUTES)))
        assertEquals("[COMMAND_SET_VIDEO_SURFACE]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SET_VIDEO_SURFACE)))
        assertEquals("[COMMAND_GET_TEXT]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_GET_TEXT)))
        assertEquals(
            "[COMMAND_SET_TRACK_SELECTION_PARAMETERS]",
            StringUtil.playerCommandsString(playerCommand(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS))
        )
        assertEquals("[COMMAND_GET_TRACKS]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_GET_TRACKS)))
        assertEquals("[COMMAND_RELEASE]", StringUtil.playerCommandsString(playerCommand(Player.COMMAND_RELEASE)))
        assertEquals(
            "[COMMAND_GET_TRACKS, COMMAND_RELEASE]",
            StringUtil.playerCommandsString(playerCommand(Player.COMMAND_GET_TRACKS, Player.COMMAND_RELEASE))
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `player commands string invalid command`() {
        StringUtil.playerCommandsString(playerCommand(36))
    }

    private companion object {
        private fun playerCommand(vararg commands: @Player.Command Int): Player.Commands {
            return Player.Commands.Builder().addAll(*commands).build()
        }
    }
}
