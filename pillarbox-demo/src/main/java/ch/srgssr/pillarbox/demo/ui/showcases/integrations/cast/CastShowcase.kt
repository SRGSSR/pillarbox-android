/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations.cast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.media.maestro.MediaRouteButton
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesGoogle
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesUnifiedStreaming
import ch.srgssr.pillarbox.demo.ui.player.DemoPlayerView
import ch.srgssr.pillarbox.demo.ui.player.playlist.PlaylistView

/**
 * Showcase for cast integration
 */
@Composable
fun CastShowcase() {
    val mainViewModel: CastShowcaseViewModel = viewModel()
    val player by mainViewModel.currentPlayer.collectAsStateWithLifecycle()

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = Color.Black),
        ) {
            DemoPlayerView(
                player = player,
                modifier = Modifier.fillMaxSize(),
            )
            MediaRouteButton(
                modifier = Modifier.align(Alignment.TopEnd),
                routeSelector = mainViewModel.routeSelector,
                colors = IconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.White,
                ),
            )
        }

        PlaylistView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            player = player,
            itemsLibrary = SamplesSRG.StreamUrns.items.filter {
                it != SamplesSRG.Unknown && it != SamplesSRG.Expired
            } + SamplesGoogle.All.items + SamplesUnifiedStreaming.DASH.items
        )
    }
}
