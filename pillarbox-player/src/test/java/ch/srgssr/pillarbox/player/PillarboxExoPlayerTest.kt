/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper.advance
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.extension.setChapters
import ch.srgssr.pillarbox.player.extension.setCredits
import io.mockk.clearAllMocks
import io.mockk.justRun
import io.mockk.mockk
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@RunWith(AndroidJUnit4::class)
class PillarboxExoPlayerTest {
    private lateinit var player: ExoPlayer

    @BeforeTest
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        player = PillarboxExoPlayer {
            disableMonitoring()
            addAssetLoader(TestAssetLoader(context))
        }
        player.prepare()
        player.play()
    }

    @AfterTest
    fun tearDown() {
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
        clearAllMocks()
    }

    /**
     * Tests inspired by https://github.com/androidx/media/blob/839c4a90f2ab36e48be73e1b5e907f3283dce72e/libraries/common/src/test/java/androidx/media3/common/ForwardingSimpleBasePlayerTest.java#L60-L78
     */
    @Test
    fun `check that all default methods are implemented`() {
        val defaultMethods = ExoPlayer::class.java.declaredMethods.filter { it.isDefault }
        for (method in defaultMethods) {
            val name = method.name
            val parameters = method.parameterTypes
            assertEquals(PillarboxExoPlayer::class.java, PillarboxExoPlayer::class.java.getDeclaredMethod(name, *parameters).declaringClass)
        }
    }

    @Test
    fun `check that chapters are reported`() {
        val chapters = mutableListOf<Chapter?>()
        val mockListener = mockk<PillarboxPlayer.Listener>(relaxed = true) {
            justRun { onChapterChanged(captureNullable(chapters)) }
        }

        player.setMediaItem(MediaItem.fromUri(MEDIA_URL))
        player.addListener(mockListener)

        advance(player).untilState(Player.STATE_ENDED)
        advance(player).untilPendingCommandsAreFullyHandled()

        assertEquals(8, chapters.size)
        assertEquals(TestAssetLoader.chapters[0], chapters[0])
        assertNull(chapters[1])
        assertEquals(TestAssetLoader.chapters[1], chapters[2])
        assertNull(chapters[3])
        assertEquals(TestAssetLoader.chapters[2], chapters[4])
        assertNull(chapters[5])
        assertEquals(TestAssetLoader.chapters[3], chapters[6])
        assertNull(chapters[7])
    }

    @Test
    fun `check that credits are reported`() {
        val credits = mutableListOf<Credit?>()
        val mockListener = mockk<PillarboxPlayer.Listener>(relaxed = true) {
            justRun { onCreditChanged(captureNullable(credits)) }
        }

        player.setMediaItem(MediaItem.fromUri(MEDIA_URL))
        player.addListener(mockListener)

        advance(player).untilState(Player.STATE_ENDED)
        advance(player).untilPendingCommandsAreFullyHandled()

        assertEquals(2 * TestAssetLoader.credits.size, credits.size)

        for (i in TestAssetLoader.credits.indices) {
            // First we enter the credit
            assertEquals(TestAssetLoader.credits[i], credits[2 * i])
            // Then we leave it
            assertNull(credits[(2 * i) + 1])
        }
    }

    private companion object {
        private const val MEDIA_URL = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8"
    }

    private class TestAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
        override fun canLoadAsset(mediaItem: MediaItem): Boolean {
            return mediaItem.localConfiguration?.uri?.toString() == MEDIA_URL
        }

        override suspend fun loadAsset(mediaItem: MediaItem): Asset {
            return Asset(
                mediaSource = mediaSourceFactory.createMediaSource(mediaItem),
                mediaMetadata = mediaItem.mediaMetadata.buildUpon()
                    .setChapters(chapters)
                    .setCredits(credits)
                    .build(),
            )
        }

        companion object {
            val chapters = listOf(
                createChapter(id = "chapter_1", start = 2.minutes, end = 10.minutes),
                createChapter(id = "chapter_2", start = 9.minutes, end = 15.minutes), // Chapter overlaps with the previous one
                createChapter(id = "chapter_3", start = 15.minutes, end = 20.minutes), // Chapter continues after the previous one
                createChapter(id = "chapter_4", start = 21.minutes, end = 25.minutes), // Chapter starts after a delay
            )

            val credits = listOf(
                Credit.Opening(start = 1.minutes.inWholeMilliseconds, end = 2.minutes.inWholeMilliseconds),
                Credit.Closing(start = 25.minutes.inWholeMilliseconds, end = 26.minutes.inWholeMilliseconds),
            )

            private fun createChapter(id: String, start: Duration, end: Duration): Chapter {
                return Chapter(
                    id = id,
                    start = start.inWholeMilliseconds,
                    end = end.inWholeMilliseconds,
                    mediaMetadata = MediaMetadata.EMPTY,
                )
            }
        }
    }
}
