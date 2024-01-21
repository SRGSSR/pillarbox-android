/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Demo of 2 player swapping view
 */
@Composable
fun MultiPlayerShowcase() {
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
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Row(modifier = Modifier.fillMaxWidth()) {
                PlayerView(
                    modifier = Modifier
                        .weight(1.0f)
                        .aspectRatio(AspectRatio)
                        .padding(MaterialTheme.paddings.mini),
                    player = playerLeft
                )
                PlayerView(
                    modifier = Modifier
                        .weight(1.0f)
                        .aspectRatio(AspectRatio)
                        .padding(MaterialTheme.paddings.mini),
                    player = playerRight
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                PlayerView(
                    modifier = Modifier
                        .weight(1.0f)
                        .aspectRatio(AspectRatio)
                        .padding(MaterialTheme.paddings.mini),
                    player = playerLeft
                )
                PlayerView(
                    modifier = Modifier
                        .weight(1.0f)
                        .aspectRatio(AspectRatio)
                        .padding(MaterialTheme.paddings.mini),
                    player = playerRight
                )
            }
        }
    }
}

private const val AspectRatio = 16 / 9f