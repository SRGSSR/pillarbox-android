/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import kotlin.time.Duration.Companion.minutes

/**
 * Simple example to demonstrate how to start content at a given time (10min).
 */
@Composable
fun StartAtGivenTimeExample() {
    val context = LocalContext.current
    val player = remember {
        PlayerModule.provideDefaultPlayer(context).apply {
            setMediaItem(DemoItem.AppleBasic_16_9_TS_HLS.toMediaItem())
            prepare()
            play()
            seekTo(10.minutes.inWholeMilliseconds)
        }
    }
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
    PlayerView(player = player)
}
