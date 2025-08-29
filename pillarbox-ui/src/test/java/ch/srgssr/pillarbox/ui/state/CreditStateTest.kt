/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.state

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper.advance
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.Default
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

@RunWith(AndroidJUnit4::class)
class CreditStateTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var player: PillarboxExoPlayer
    private lateinit var creditState: CreditState

    @BeforeTest
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        player = PillarboxExoPlayer(context, Default) {
            clock(FakeClock(true))
            coroutineContext(EmptyCoroutineContext)
            addAssetLoader(CreditAssetLoader(context))
        }
        player.prepare()
        player.play()

        composeTestRule.setContent {
            creditState = rememberCreditState(player)
        }
    }

    @AfterTest
    fun tearDown() {
        player.release()
    }

    @Test
    fun `credit information are updated during media playback`() {
        player.setMediaItem(MediaItem.fromUri(MEDIA_URL))

        assertNull(creditState.currentCredit)
        assertFalse(creditState.isInCredit)

        advance(player).untilPosition(0, CreditAssetLoader.credit.start)
        advance(player).untilPendingCommandsAreFullyHandled()

        assertEquals(CreditAssetLoader.credit, creditState.currentCredit)
        assertTrue(creditState.isInCredit)

        advance(player).untilPosition(0, CreditAssetLoader.credit.end)
        advance(player).untilPendingCommandsAreFullyHandled()

        assertNull(creditState.currentCredit)
        assertFalse(creditState.isInCredit)
    }

    @Test
    fun `credit information are updated during media transition`() {
        player.setMediaItems(listOf(MediaItem.fromUri(MEDIA_URL), MediaItem.fromUri(MEDIA_URL2)))

        advance(player).untilPosition(0, CreditAssetLoader.credit.start)
        advance(player).untilPendingCommandsAreFullyHandled()

        assertEquals(CreditAssetLoader.credit, creditState.currentCredit)
        assertTrue(creditState.isInCredit)

        player.seekToNextMediaItem()
        advance(player).untilPendingCommandsAreFullyHandled()

        assertNull(creditState.currentCredit)
        assertFalse(creditState.isInCredit)
    }

    @Test
    fun `onClick seeks to the end of the credit while inside the credit`() {
        player.setMediaItem(MediaItem.fromUri(MEDIA_URL))

        advance(player).untilPosition(0, CreditAssetLoader.credit.start)
        advance(player).untilPendingCommandsAreFullyHandled()

        val credit = creditState.currentCredit

        assertNotNull(credit)
        assertTrue(creditState.isInCredit)

        creditState.onClick()
        advance(player).untilPendingCommandsAreFullyHandled()

        assertEquals(credit.end, player.currentPosition)
    }

    @Test
    fun `onClick does nothing while outside the credit`() {
        player.setMediaItem(MediaItem.fromUri(MEDIA_URL))

        assertNull(creditState.currentCredit)
        assertFalse(creditState.isInCredit)

        creditState.onClick()
        advance(player).untilPendingCommandsAreFullyHandled()

        assertEquals(0, player.currentPosition)
    }

    private companion object {
        private const val MEDIA_URL = "https://rts-vod-amd.akamaized.net/ww/14970442/4dcba1d3-8cc8-3667-a7d2-b3b92c4243d9/master.m3u8"
        private const val MEDIA_URL2 = "https://rts-vod-amd.akamaized.net/ww/14827306/98923d94-071c-3d48-ac0c-dbababe70a68/master.m3u8"
    }

    private class CreditAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
        override fun canLoadAsset(mediaItem: MediaItem): Boolean {
            return mediaItem.localConfiguration?.uri?.toString() == MEDIA_URL
        }

        override suspend fun loadAsset(mediaItem: MediaItem): Asset {
            return Asset(
                mediaSource = mediaSourceFactory.createMediaSource(mediaItem),
                pillarboxMetadata = PillarboxMetadata(
                    credits = listOf(credit),
                ),
            )
        }

        companion object {
            val credit = Credit.Closing(start = 25.minutes.inWholeMilliseconds, end = 26.minutes.inWholeMilliseconds)
        }
    }
}
