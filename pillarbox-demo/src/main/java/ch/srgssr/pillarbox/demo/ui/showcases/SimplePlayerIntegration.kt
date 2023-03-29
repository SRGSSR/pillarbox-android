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
import ch.srgssr.pillarbox.demo.di.PlayerModule
import ch.srgssr.pillarbox.ui.PlayerSurface

/**
 * Simple player integration with only using [PlayerSurface] without any controls or UI.
 */
@Composable
fun SimplePlayerIntegration() {
    val context = LocalContext.current
    val player = remember {
        PlayerModule.provideDefaultPlayer(context).apply {
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
    PlayerSurface(player = player)
}
