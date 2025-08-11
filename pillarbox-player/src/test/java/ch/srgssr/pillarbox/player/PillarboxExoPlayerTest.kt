/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.os.Looper
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper.advance
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.core.business.SRG
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
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

@RunWith(AndroidJUnit4::class)
class PillarboxExoPlayerTest {
    private lateinit var player: ExoPlayer

    @BeforeTest
    fun setup() {
        player = PillarboxExoPlayer(ApplicationProvider.getApplicationContext(), SRG) {
            disableMonitoring()
            clock(FakeClock(true))
            loadControl(PillarboxTestLoadControl())
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

        player.setMediaItem(SRGMediaItem(MEDIA_URN))
        player.addListener(mockListener)

        advance(player).untilPosition(0, MEDIA_DURATION)

        assertEquals(8, chapters.size)
        assertEquals("urn:rts:video:15533242", chapters[0]?.id)
        assertNull(chapters[1])
        assertEquals("urn:rts:video:15533244", chapters[2]?.id)
        assertNull(chapters[3])
        assertEquals("urn:rts:video:15533246", chapters[4]?.id)
        assertNull(chapters[5])
        assertEquals("urn:rts:video:15533248", chapters[6]?.id)
        assertNull(chapters[7])
    }

    @Test
    fun `check that credits are reported`() {
        val credits = mutableListOf<Credit?>()
        val mockListener = mockk<PillarboxPlayer.Listener>(relaxed = true) {
            justRun { onCreditChanged(captureNullable(credits)) }
        }

        player.setMediaItem(SRGMediaItem(MEDIA_URN))
        player.addListener(mockListener)

        advance(player).untilPosition(0, MEDIA_DURATION)

        assertEquals(2, credits.size)
        assertEquals(Credit.Closing(1601320L, 1605800L), credits[0])
        assertNull(credits[1])
    }

    private companion object {
        private const val MEDIA_DURATION = 1613760L
        private const val MEDIA_URN = "urn:rts:video:15532586"
    }
}
