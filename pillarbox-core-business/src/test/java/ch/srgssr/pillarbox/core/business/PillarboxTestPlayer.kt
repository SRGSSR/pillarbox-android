/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.content.Context
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.test.utils.FakeClock
import androidx.test.core.app.ApplicationProvider
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PlayerStuckDetectionTimeouts
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Pillarbox ExoPlayer
 *
 * @param context The [Context], by default [ApplicationProvider.getApplicationContext]
 * @param block The block to further configure the [PillarboxExoPlayer].
 * @return [PillarboxExoPlayer] configured for tests.
 */
@PillarboxDsl
fun PillarboxExoPlayer(context: Context = ApplicationProvider.getApplicationContext(), block: SRG.Builder.() -> Unit = {}): PillarboxExoPlayer {
    return PillarboxExoPlayer(context, SRG) {
        loadControl(DefaultLoadControl())
        clock(FakeClock(true))
        coroutineContext(EmptyCoroutineContext)
        playerStuckDetectionTimeouts(PlayerStuckDetectionTimeouts.DisabledForTest)
        disableMonitoring()
        block()
    }.apply {
        // FIXME Investigate why we need to disable the image track in tests
        trackSelectionParameters = trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_IMAGE, true)
            .build()
    }
}
