/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesApple
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import kotlin.time.Duration.Companion.minutes

/**
 * Simple example to demonstrate how to start content at a given time (10min).
 */
@Composable
fun StartAtGivenTimeShowcase() {
    val context = LocalContext.current
    val player = remember {
        PlayerModule.provideDefaultPlayer(context).apply {
            setMediaItem(SamplesApple.Basic_16_9.toMediaItem(), 10.minutes.inWholeMilliseconds)
            prepare()
        }
    }
    LifecycleResumeEffect(player) {
        player.play()
        onPauseOrDispose {
            player.pause()
        }
    }
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
    PlayerView(player = player)
}
