/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.extension.setTrackerData
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class MultiMediaItemTrackerUpdate {
    private lateinit var fakeClock: FakeClock

    @Before
    fun createPlayer() {
        fakeClock = FakeClock(true)
    }

    @After
    fun releasePlayer() {
        clearAllMocks()
    }

    @Test
    fun `Remove one tracker data update other tracker data when initialized both in MediaItemSource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fakeMediaItemTracker = spyk(FakeMediaItemTracker())
        val dummyMediaItemTracker = spyk(DummyTracker())

        val player = PillarboxPlayer(
            context = context,
            clock = fakeClock,
            mediaSourceFactory = PillarboxMediaSourceFactory(context).apply {
                addAssetLoader(object : AssetLoader(DefaultMediaSourceFactory(context)) {
                    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
                        return true
                    }

                    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
                        return Asset(
                            mediaSource = mediaSourceFactory.createMediaSource(mediaItem),
                            trackersData = MediaItemTrackerData.Builder()
                                .putData(DummyTracker::class.java, "DummyItemTracker")
                                .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data("FakeMediaItemTracker"))
                                .build()
                        )
                    }
                })
            },
            mediaItemTrackerProvider = MediaItemTrackerRepository().apply {
                registerFactory(DummyTracker::class.java, DummyTracker.Factory(dummyMediaItemTracker))
                registerFactory(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Factory(fakeMediaItemTracker))
            }
        )
        player.apply {
            player.setMediaItem(MediaItem.fromUri(FakeAssetLoader.URL_MEDIA_1))
            prepare()
            play()
        }
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val currentMediaItem = player.currentMediaItem!!
        val mediaUpdate = currentMediaItem.buildUpon()
            .setTrackerData(
                MediaItemTrackerData.Builder()
                    .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data("New Data"))
                    .build()
            )
            .build()
        player.replaceMediaItem(0, mediaUpdate)

        verify(exactly = 0) {
            dummyMediaItemTracker.update(any())
        }

        verify(exactly = 1) {
            dummyMediaItemTracker.start(any(), any())
        }

        verifyOrder {
            fakeMediaItemTracker.start(any(), any())
            fakeMediaItemTracker.update(FakeMediaItemTracker.Data("New Data"))
        }
        player.release()
    }

    internal class DummyTracker : MediaItemTracker {

        override fun start(player: ExoPlayer, initialData: Any?) {
            // Nothing it is dummy
        }

        override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
            // Nothing is is dummy
        }

        class Factory(private val dummyTracker: DummyTracker = DummyTracker()) : MediaItemTracker.Factory {
            override fun create(): MediaItemTracker {
                return dummyTracker
            }
        }
    }
}
