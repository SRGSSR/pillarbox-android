/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.multiplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.DemoPlaybackControls
import ch.srgssr.pillarbox.demo.ui.player.DemoPlayerSurface

/**
 * Demo of 2 player swapping view
 */
@Composable
fun MultiPlayer() {
    val multiPlayerViewModel: MultiPlayerViewModel = viewModel()
    var swapLeftRight by remember {
        mutableStateOf(false)
    }
    val playerLeft = multiPlayerViewModel.getPlayerLeft(swapLeftRight)
    val playerRight = multiPlayerViewModel.getPlayerRight(swapLeftRight)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { swapLeftRight = !swapLeftRight }) {
            Text(text = "Swap players")
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            PlayerView(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(4.dp),
                player = playerLeft
            )
            PlayerView(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(4.dp),
                player = playerRight
            )
        }
    }
}

@Composable
private fun PlayerView(player: Player, modifier: Modifier) {
    Box(modifier = modifier) {
        DemoPlayerSurface(player = player) {
            DemoPlaybackControls(player = player)
        }
    }
}
