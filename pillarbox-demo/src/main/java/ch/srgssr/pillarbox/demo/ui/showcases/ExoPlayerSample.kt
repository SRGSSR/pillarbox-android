/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.di.Dependencies
import ch.srgssr.pillarbox.ui.ExoPlayerView

/**
 * Exo player sample using [ExoPlayerView]
 */
@Composable
fun ExoPlayerSample() {
    val context = LocalContext.current
    val player = remember {
        Dependencies.provideDefaultPlayer(context).apply {
            setMediaItem(DemoItem.AppleBasic_16_9_TS_HLS.toMediaItem())
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
