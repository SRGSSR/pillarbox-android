/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrations.multiplayer

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
import ch.srgssr.pillarbox.ui.ExoplayerView

/**
 * Demo of 2 player swapping view
 */
@Composable
fun PlayerSwap() {
    val multiPlayerViewModel: PlayerSwapViewModel = viewModel()
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
            ExoplayerView(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(4.dp),
                player = playerLeft
            )
            ExoplayerView(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(4.dp),
                player = playerRight
            )
        }
    }
}
