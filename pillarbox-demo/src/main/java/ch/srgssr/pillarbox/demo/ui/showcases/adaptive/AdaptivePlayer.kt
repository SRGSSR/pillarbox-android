/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.data.Playlist
import ch.srgssr.pillarbox.demo.di.PlayerModule
import ch.srgssr.pillarbox.demo.ui.player.DemoPlayerSurface
import ch.srgssr.pillarbox.ui.ScaleMode

/**
 * Adaptive player demo
 * The view allow to resize the player view and changing the scale mode
 */
@Composable
fun AdaptivePlayerHome() {
    val context = LocalContext.current
    val player = remember {
        PlayerModule.provideDefaultPlayer(context).apply {
            val playlist = Playlist.StreamUrns
            val items = playlist.items.map { it.toMediaItem() }
            setMediaItems(items)
            prepare()
        }
    }
    player.play()
    DisposableEffect(AdaptivePlayer(player = player, modifier = Modifier.fillMaxSize())) {
        onDispose {
            player.release()
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun AdaptivePlayer(player: Player, modifier: Modifier = Modifier) {
    var resizeMode by remember {
        mutableStateOf(ScaleMode.Fit)
    }
    var widthPercent by remember {
        mutableStateOf(1f)
    }
    var heightPercent by remember {
        mutableStateOf(1f)
    }
    BoxWithConstraints(modifier = modifier.padding(12.dp)) {
        Box(
            modifier = Modifier
                .size(maxWidth * widthPercent, maxHeight * heightPercent)
                .background(color = Color.Black),
            contentAlignment = Alignment.Center
        ) {
            DemoPlayerSurface(
                modifier = Modifier
                    .matchParentSize(),
                player = player,
                scaleMode = resizeMode
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.background.copy(0.5f))
                .align(Alignment.BottomStart)
        ) {
            SliderWithLabel(label = "W: ", value = widthPercent, onValueChange = { widthPercent = it })
            SliderWithLabel(label = "H :", value = heightPercent, onValueChange = { heightPercent = it })
            Row {
                for (mode in ScaleMode.values()) {
                    RadioButtonWithLabel(label = mode.name, selected = mode == resizeMode) {
                        resizeMode = mode
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioButtonWithLabel(modifier: Modifier = Modifier, label: String, selected: Boolean, onClick: (() -> Unit)) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.caption)
    }
}

@Composable
private fun SliderWithLabel(modifier: Modifier = Modifier, label: String, value: Float, onValueChange: (Float) -> Unit) {
    Row(modifier) {
        Text(text = label)
        Slider(
            value = value, onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colors.secondary,
                activeTrackColor = MaterialTheme.colors.secondaryVariant
            )
        )
    }
}
