/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.ui.ExoplayerView

/**
 * Demo player view demonstrate how to integrate PlayerView with Compose
 *
 * doc : https://developer.android.com/jetpack/compose/interop/interop-apis#fragments-in-compose
 *
 * @param playerViewModel
 * @param pipClick picture in picture button click action
 */
@Composable
fun DemoPlayerView(
    playerViewModel: SimplePlayerViewModel,
    pipClick: () -> Unit = {}
) {
    val hideUi = playerViewModel.pictureInPictureEnabled.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        ExoplayerView(
            player = playerViewModel.player,
            useController = !hideUi.value,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
        if (!hideUi.value) {
            DemoControlView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                playerViewModel = playerViewModel, pipClick = pipClick
            )
        }
    }
}

@Composable
private fun DemoControlView(
    modifier: Modifier = Modifier,
    playerViewModel: SimplePlayerViewModel,
    pipClick: () -> Unit = {}
) {
    val pauseOnBackground = playerViewModel.pauseOnBackground.collectAsState()
    Column(modifier = modifier) {
        Row {
            IconButton(onClick = pipClick) {
                Icon(imageVector = Icons.Default.PictureInPicture, contentDescription = "Go in picture in picture")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.wrapContentWidth(),
                text = "Background playback",
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold
            )
            Switch(checked = !pauseOnBackground.value, onCheckedChange = { playerViewModel.togglePauseOnBackground() })
        }
    }
}
