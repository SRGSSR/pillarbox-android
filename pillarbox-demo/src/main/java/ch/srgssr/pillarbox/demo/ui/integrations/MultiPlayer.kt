/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrations

import androidx.compose.foundation.background
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
import ch.srg.pillarbox.ui.PlayerView
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerControls
import ch.srgssr.pillarbox.demo.ui.theme.Black50

/**
 * Multi player demo
 *
 * The demo allow to swap player between left and right
 */
@Suppress("MagicNumber")
@Composable
fun MultiPlayerDemo() {
    val multiPlayerViewModel: MultiPlayerViewModel = viewModel()
    var swapLeftRight by remember {
        mutableStateOf(false)
    }
    val playerLeft = if (!swapLeftRight) multiPlayerViewModel.player1 else multiPlayerViewModel.player2
    val playerRight = if (swapLeftRight) multiPlayerViewModel.player1 else multiPlayerViewModel.player2

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { swapLeftRight = !swapLeftRight }) {
            Text(text = "Swap players")
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            PlayerView(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(4.dp),
                player = playerLeft, defaultAspectRatio = 16 / 9f
            ) {
                SimplePlayerControls(
                    modifier = Modifier.background(Black50),
                    player = playerLeft
                )
            }
            PlayerView(
                modifier = Modifier
                    .weight(1.0f)
                    // .fillMaxWidth(1.0f).wrapContentWidth()
                    .padding(4.dp),
                player = playerRight, defaultAspectRatio = 16 / 9f
            ) {
                SimplePlayerControls(
                    modifier = Modifier.background(Black50),
                    player = playerRight
                )
            }
        }
    }
}
