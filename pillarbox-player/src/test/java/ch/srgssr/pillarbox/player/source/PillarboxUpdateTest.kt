/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.UpdatableMediaItem
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PillarboxUpdateTest {

    @Test
    fun `from copies the metadata of the update`() {
        val update = createUpdate(STREAM_URL)

        val pillarboxUpdate = PillarboxUpdate.from(update, STREAM_URL.toUri())

        assertEquals(update.pillarboxMetadata, pillarboxUpdate.pillarboxMetadata)
        assertEquals(update.mediaMetadata, pillarboxUpdate.mediaMetadata)
    }

    @Test
    fun `from with the same stream URL does not require a reload`() {
        val pillarboxUpdate = PillarboxUpdate.from(createUpdate(STREAM_URL), STREAM_URL.toUri())

        assertFalse(pillarboxUpdate.reloadRequired)
    }

    @Test
    fun `from with a different stream URL requires a reload`() {
        val pillarboxUpdate = PillarboxUpdate.from(createUpdate(OTHER_STREAM_URL), STREAM_URL.toUri())

        assertTrue(pillarboxUpdate.reloadRequired)
    }

    @Test
    fun `from without a playing stream URL requires a reload`() {
        val pillarboxUpdate = PillarboxUpdate.from(createUpdate(STREAM_URL), null)

        assertTrue(pillarboxUpdate.reloadRequired)
    }

    @Test
    fun `from without stream URL in the update requires a reload`() {
        val update = UpdatableMediaItem(
            pillarboxMetadata = PillarboxMetadata.EMPTY,
            mediaItem = MediaItem.Builder().setMediaId("media-id").build(),
        )

        val pillarboxUpdate = PillarboxUpdate.from(update, STREAM_URL.toUri())

        assertTrue(pillarboxUpdate.reloadRequired)
    }

    private companion object {
        private const val STREAM_URL = "https://example.com/live/master.m3u8"
        private const val OTHER_STREAM_URL = "https://example.com/vod/master.m3u8"

        private fun createUpdate(streamUrl: String): UpdatableMediaItem {
            return UpdatableMediaItem(
                pillarboxMetadata = PillarboxMetadata(blockedTimeRanges = listOf(BlockedTimeRange(0L, 100L))),
                mediaItem = MediaItem.fromUri(streamUrl),
                mediaMetadata = MediaMetadata.Builder().setTitle("Title").build(),
            )
        }
    }
}
