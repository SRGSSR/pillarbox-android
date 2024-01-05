/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.TestExoPlayerBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// TODO Move this in "pillarbox-player-testutils"
private class PlayerIdlingResource(
    @Player.State private val expectedPlaybackState: Int
) : IdlingResource {
    private var callback: IdlingResource.ResourceCallback? = null

    @Player.State
    var playbackState: Int = Player.STATE_IDLE
        set(value) {
            field = value

            if (playbackState == expectedPlaybackState) {
                callback?.onTransitionToIdle()
            }
        }

    override fun getName(): String {
        return "PlayerIdlingResource"
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }

    override fun isIdleNow(): Boolean {
        return playbackState == expectedPlaybackState
    }
}

// TODO Move this in "pillarbox-player-testutils"
class ExoPlayerRule(
    private val mediaUri: String,
    @Player.State private val waitForPlaybackState: Int
) : ExternalResource() {
    private val playerIdlingResource = PlayerIdlingResource(waitForPlaybackState)

    lateinit var clock: FakeClock
        private set

    lateinit var player: Player
        private set

    override fun before() {
        Looper.prepare()
        IdlingRegistry.getInstance().register(playerIdlingResource)

        setupClock()
        setupPlayer()

        Espresso.onIdle()
    }

    override fun after() {
        player.release()

        IdlingRegistry.getInstance().unregister(playerIdlingResource)
        Looper.myLooper()?.quit()
    }

    private fun setupClock() {
        clock = FakeClock(true)
    }

    private fun setupPlayer() {
        player = TestExoPlayerBuilder(ApplicationProvider.getApplicationContext())
            .setClock(clock)
            .build()
        player.addListener(
            object : Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    playerIdlingResource.playbackState = playbackState
                }
            }
        )
        player.setMediaItem(MediaItem.fromUri(mediaUri))
        player.prepare()
        player.play()
    }
}

@RunWith(AndroidJUnit4::class)
class TestSimpleProgressTrackerState {
    @get:Rule
    val playerRule = ExoPlayerRule(
        mediaUri = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd",
        waitForPlaybackState = Player.STATE_READY
    )

    @Test
    fun progressWithoutManualChanges() = runTest {
        val progressTrackerState = SimpleProgressTrackerState(playerRule.player, this)

        val playerPositions = (0L..50L step 5L).toList()
        playerPositions.forEach { playerPosition ->
            playerRule.clock.advanceTime(playerPosition)
        }

        val actualProgress = mutableListOf<Duration>()
        launch(coroutineContext) {
            progressTrackerState.progress
                .toList(actualProgress)
        }.join()

        assertEquals(playerPositions.map { it.milliseconds }, actualProgress)
    }
}
