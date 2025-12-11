/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.test.utils.FakeClock
import androidx.test.core.app.ApplicationProvider
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Pillarbox ExoPlayer
 *
 * @param context The [Context], by default [ApplicationProvider.getApplicationContext]
 * @param block The block to further configure the [PillarboxExoPlayer].
 * @return [PillarboxExoPlayer] configured for tests.
 */
@PillarboxDsl
fun PillarboxExoPlayer(context: Context = ApplicationProvider.getApplicationContext(), block: Default.Builder.() -> Unit = {}): PillarboxExoPlayer {
    return PillarboxExoPlayer(context, Default) {
        loadControl(PillarboxTestLoadControl())
        clock(FakeClock(true))
        coroutineContext(EmptyCoroutineContext)
        playerStuckDetectionTimeouts(PlayerStuckDetectionTimeouts.DisabledForTest)
        block()
    }
}
