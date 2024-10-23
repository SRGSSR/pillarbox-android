/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.Player
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import ch.srgssr.pillarbox.ui.widget.player.SurfaceType

/**
 * Showcase how to display a spherical surface to play 360° video.
 */
@Composable
fun SphericalSurfaceShowcase() {
    val context = LocalContext.current
    val player = remember {
        PlayerModule.provideDefaultPlayer(context = context).apply {
            setMediaItem(SRGMediaItem("urn:rts:video:8414077"))
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    LifecycleStartEffect(player) {
        player.play()
        onStopOrDispose {
            player.pause()
        }
    }

    DisposableEffect(player) {
        player.prepare()
        onDispose {
            player.release()
        }
    }

    PlayerSurface(
        player = player,
        modifier = Modifier.fillMaxSize(),
        surfaceType = SurfaceType.Spherical,
        scaleMode = ScaleMode.Fill,
    )
}
