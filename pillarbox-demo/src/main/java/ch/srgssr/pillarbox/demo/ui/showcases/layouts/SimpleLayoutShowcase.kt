/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesApple
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface

/**
 * Simple player integration with only using [PlayerSurface] without any controls or UI.
 */
@Composable
fun SimpleLayoutShowcase() {
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
    PlayerSurface(player = player)
}
