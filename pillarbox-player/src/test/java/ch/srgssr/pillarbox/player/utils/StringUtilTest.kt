/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

import androidx.media3.common.Player
import kotlin.test.Test
import kotlin.test.assertEquals

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
        assertEquals("DISCONTINUITY_REASON_SKIP", StringUtil.discontinuityReasonString(Player.DISCONTINUITY_REASON_SKIP))
        assertEquals("UNKNOWN", StringUtil.discontinuityReasonString(42))
    }
}
