/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Demo of 2 player swapping view
 */
@Composable
fun MultiPlayerShowcase() {
    var swapLeftRight by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val playerOne = remember {
        PlayerModule.provideDefaultPlayer(context).apply {
            repeatMode = Player.REPEAT_MODE_ONE
            setMediaItem(DemoItem.LiveVideo.toMediaItem())
            prepare()
        }
    }
    val playerTwo = remember {
        PlayerModule.provideDefaultPlayer(context).apply {
            repeatMode = Player.REPEAT_MODE_ONE
            setMediaItem(DemoItem.DvrVideo.toMediaItem())
            prepare()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            playerOne.release()
            playerTwo.release()
        }
    }
    LifecycleResumeEffect(Unit) {
        playerOne.play()
        playerTwo.play()
        onPauseOrDispose {
            playerOne.pause()
            playerTwo.pause()
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { swapLeftRight = !swapLeftRight }) {
            Text(text = "Swap players")
        }
        val playerOneView = remember {
            movableContentOf {
                PlayerView(
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(MaterialTheme.paddings.mini),
                    player = playerOne,
                )
            }
        }
        val playerTwoView = remember {
            movableContentOf {
                PlayerView(
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(MaterialTheme.paddings.mini),
                    player = playerTwo,
                )
            }
        }
        val players = remember {
            movableContentOf {
                if (swapLeftRight) {
                    playerTwoView()
                    playerOneView()
                } else {
                    playerOneView()
                    playerTwoView()
                }
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

private const val AspectRatio = 16 / 9f
