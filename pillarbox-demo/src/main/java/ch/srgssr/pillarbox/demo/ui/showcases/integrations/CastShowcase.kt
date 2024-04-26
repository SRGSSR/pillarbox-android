/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.mediarouter.app.MediaRouteButton
import ch.srgssr.pillarbox.core.business.DefaultPillarbox
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.common.util.concurrent.MoreExecutors

@Composable
fun CastShowcase() {
    val context = LocalContext.current
    val playerController = remember {
        val controller = PlayerManager(
            localPlayer = DefaultPillarbox(context = context).apply {
                addMediaItem(DemoItem.UnifiedStreamingOnDemand_Dash_FragmentedMP4.toMediaItem())
                addMediaItem(DemoItem.OnDemandHLS.toMediaItem())
                addMediaItem(DemoItem.GoogleDashH265.toMediaItem())
            },
            CastContext.getSharedInstance(context.applicationContext, MoreExecutors.directExecutor()).result
        )
        controller
    }
    val player by playerController.player.collectAsState()
    LifecycleResumeEffect(player) {
        player.play()
        onPauseOrDispose {
            player.pause()
        }
    }
    DisposableEffect(playerController) {
        onDispose {
            playerController.release()
        }
    }
    PlayerView(player = player, content = {
        CastButton()
    })
}

@Composable
private fun CastButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val mediaRouterButton = MediaRouteButton(context)
            CastButtonFactory.setUpMediaRouteButton(context, mediaRouterButton)
            mediaRouterButton
        },
        update = {
            CastButtonFactory.setUpMediaRouteButton(context, it)
        }
    )
}
