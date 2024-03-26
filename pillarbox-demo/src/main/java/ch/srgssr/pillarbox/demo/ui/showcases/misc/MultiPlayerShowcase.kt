/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.player.controls.PlayerControls
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface

/**
 * Demo displaying two players, that can be swapped.
 * At any given moment, there's always only one player with sound active.
 */
@Composable
fun MultiPlayerShowcase() {
    val multiPlayerViewModel = viewModel<MultiPlayerViewModel>()
    val activePlayer by multiPlayerViewModel.activePlayer.collectAsState()
    val playerOne by multiPlayerViewModel.playerOne.collectAsState()
    val playerTwo by multiPlayerViewModel.playerTwo.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = multiPlayerViewModel::swapPlayers) {
            Text(text = "Swap players")
        }

        val players = remember {
            movableContentOf {
                ActivablePlayer(
                    player = playerOne,
                    isActive = activePlayer == playerOne,
                    modifier = Modifier.weight(1f),
                    onClick = { multiPlayerViewModel.setActivePlayer(playerOne) },
                )

                ActivablePlayer(
                    player = playerTwo,
                    isActive = activePlayer == playerTwo,
                    modifier = Modifier.weight(1f),
                    onClick = { multiPlayerViewModel.setActivePlayer(playerTwo) },
                )
            }
        }

        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Row(modifier = Modifier.fillMaxWidth()) {
                players()
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                players()
            }
        }
    }
}

@Composable
private fun ActivablePlayer(
    player: Player,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    PlayerSurface(
        modifier = modifier
            .padding(MaterialTheme.paddings.mini)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isActive,
                onClick = onClick,
            ),
        player = player,
    ) {
        val inactivePlayerOverlay = Modifier.drawWithContent {
            drawContent()
            drawRect(Color.LightGray.copy(alpha = 0.7f))
        }

        PlayerControls(
            player = player,
            modifier = Modifier
                .fillMaxSize()
                .then(if (isActive) Modifier else inactivePlayerOverlay),
            backgroundColor = Color.Unspecified,
            content = {},
        )
    }
}
