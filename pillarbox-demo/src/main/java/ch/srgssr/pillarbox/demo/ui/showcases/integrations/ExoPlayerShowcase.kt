/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesApple
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerView

/**
 * Exo player sample using [ExoPlayerView]
 */
@Composable
fun ExoPlayerShowcase() {
    val context = LocalContext.current
    val player = remember {
        PlayerModule.provideDefaultPlayer(context).apply {
            setMediaItem(SamplesApple.Basic_16_9.toMediaItem())
            prepare()
            play()
        }
    }
    DisposableEffect(key1 = player) {
        onDispose {
            player.release()
        }
    }
    ExoPlayerView(player = player, useController = true)
}
