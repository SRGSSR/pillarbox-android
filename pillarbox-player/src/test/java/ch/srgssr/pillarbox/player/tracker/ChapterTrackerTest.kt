/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.SeekIncrement
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ChapterTrackerTest {
    private lateinit var player: PillarboxExoPlayer
    private lateinit var fakeClock: FakeClock
    private lateinit var listener: PillarboxPlayer.Listener

    @BeforeTest
    fun createPlayer() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        listener = spyk(object : PillarboxPlayer.Listener {})
        fakeClock = FakeClock(true)
        player = PillarboxExoPlayer(
            context = context,
            seekIncrement = SeekIncrement(),
            loadControl = DefaultLoadControl(),
            clock = fakeClock,
            mediaSourceFactory = PillarboxMediaSourceFactory(context).apply {
                addAssetLoader(ChapterAssetLoader(context))
            },
        )
        player.addListener(listener)
        player.prepare()
        player.play()
    }

    @AfterTest
    fun releasePlayer() {
        player.removeListener(listener)
        player.release()
        clearAllMocks()
    }

    @Test
    fun `chapter transition while playing`() {
        player.addMediaItem(ChapterAssetLoader.MEDIA_ITEM)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val expectedChapters = listOf(ChapterAssetLoader.CHAPTER_1, ChapterAssetLoader.CHAPTER_2, null)
        val receivedChapters = mutableListOf<Chapter?>()
        verify {
            listener.onChapterChanged(captureNullable(receivedChapters))
        }
        assertEquals(expectedChapters, receivedChapters)
    }

    @Test
    fun `chapter transition after seek inside a chapter`() {
        player.pause()
        val chapter = ChapterAssetLoader.CHAPTER_2
        player.setMediaItem(ChapterAssetLoader.MEDIA_ITEM, chapter.end)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        player.seekTo(chapter.start)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val expectedChapters = listOf(ChapterAssetLoader.CHAPTER_2)
        val receivedChapters = mutableListOf<Chapter>()
        verifyOrder {
            listener.onChapterChanged(capture(receivedChapters))
        }
        assertEquals(expectedChapters, receivedChapters.reversed())
    }

    @Test
    fun `chapter transition after seek back`() {
        player.addMediaItem(ChapterAssetLoader.MEDIA_ITEM_WITH_CHAPTER)
        TestPlayerRunHelper.playUntilPosition(player, 0, ChapterAssetLoader.CHAPTER_3.start + 1_000L)
        player.seekBack()
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val expectedChapters = listOf(ChapterAssetLoader.CHAPTER_3, null, ChapterAssetLoader.CHAPTER_3, null, ChapterAssetLoader.CHAPTER_4, null)
        val receivedChapters = mutableListOf<Chapter?>()

        verify {
            listener.onChapterChanged(captureNullable(receivedChapters))
        }
        assertEquals(expectedChapters, receivedChapters)
    }

    @Test
    fun `chapter transition skip next`() {
        player.addMediaItems(listOf(ChapterAssetLoader.MEDIA_ITEM_WITH_CHAPTER, ChapterAssetLoader.NO_CHAPTER_MEDIA_ITEM))
        TestPlayerRunHelper.playUntilPosition(player, 0, ChapterAssetLoader.CHAPTER_3.start + 1_000L)
        player.seekToNext()
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val expectedChapters = listOf(ChapterAssetLoader.CHAPTER_3, null)
        val receivedChapters = mutableListOf<Chapter?>()

        verify {
            listener.onChapterChanged(captureNullable(receivedChapters))
        }
        assertEquals(expectedChapters, receivedChapters)
    }
}

private class ChapterAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfiguration != null
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val itemBuilder = mediaItem.buildUpon()
        val mediaSource = mediaSourceFactory.createMediaSource(itemBuilder.build())
        return when (mediaItem.mediaId) {
            ID_START_WITH_CHAPTER -> {
                Asset(
                    mediaSource = mediaSource,
                    mediaMetadata = mediaItem.mediaMetadata,
                    chapters = listOf(CHAPTER_1, CHAPTER_2)
                )
            }

            ID_WITH_CHAPTER -> {
                Asset(
                    mediaSource = mediaSource,
                    mediaMetadata = mediaItem.mediaMetadata,
                    chapters = listOf(CHAPTER_3, CHAPTER_4)
                )
            }

            else -> {
                Asset(
                    mediaSource = mediaSource,
                    mediaMetadata = mediaItem.mediaMetadata,
                    chapters = emptyList()
                )
            }
        }
    }

    companion object {
        private const val URL = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8"
        const val ID_START_WITH_CHAPTER = "ID_START_WITH_CHAPTER"
        const val ID_WITH_CHAPTER = "ID_WITH_CHAPTER"

        val MEDIA_ITEM = MediaItem.Builder().setMediaId(ID_START_WITH_CHAPTER).setUri(URL).build()
        val MEDIA_ITEM_WITH_CHAPTER = MediaItem.Builder().setMediaId(ID_WITH_CHAPTER).setUri(URL).build()
        val NO_CHAPTER_MEDIA_ITEM = MediaItem.Builder().setMediaId("NoChapter").setUri(URL).build()

        const val NEAR_END_POSITION_MS = 15_000L // the video has 17 sec duration

        val CHAPTER_1 = Chapter(id = "Chapter1", 0, 5_000L, MediaMetadata.EMPTY)
        val CHAPTER_2 = Chapter(id = "Chapter2", 5_000L, NEAR_END_POSITION_MS, MediaMetadata.EMPTY)

        val CHAPTER_3 = Chapter(id = "Chapter3", 2_000L, 5_000L, MediaMetadata.EMPTY)
        val CHAPTER_4 = Chapter(id = "Chapter4", 10_000L, NEAR_END_POSITION_MS, MediaMetadata.EMPTY)
    }
}
