/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.shared.ui.components.PillarboxSlider
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.ui.ScaleMode
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface

/**
 * Resizable player demo
 * The view allows resizing the player view and changing the scale mode
 */
@Composable
fun ResizablePlayerShowcase() {
    val context = LocalContext.current
    val player = remember {
        PlayerModule.provideDefaultPlayer(context).apply {
            val playlist = Playlist.StreamUrns
            val items = playlist.items.map { it.toMediaItem() }
            setMediaItems(items)
        }
    }
    AdaptivePlayer(player = player, modifier = Modifier.fillMaxSize())
    DisposableEffect(player) {
        player.prepare()
        onDispose {
            player.release()
        }
    }
    LifecycleStartEffect(player) {
        player.play()
        onStopOrDispose {
            player.pause()
        }
    }
}

@Composable
private fun AdaptivePlayer(player: Player, modifier: Modifier = Modifier) {
    var resizeMode by remember { mutableStateOf(ScaleMode.Fit) }
    val (widthPercent, setWidthPercent) = remember { mutableFloatStateOf(1f) }
    val (heightPercent, setHeightPercent) = remember { mutableFloatStateOf(1f) }

    BoxWithConstraints(modifier = modifier) {
        val playerWidth by animateDpAsState(targetValue = maxWidth * widthPercent, label = "player_width")
        val playerHeight by animateDpAsState(targetValue = maxHeight * heightPercent, label = "player_height")

        Box(
            modifier = Modifier.size(width = playerWidth, height = playerHeight),
            contentAlignment = Alignment.Center,
        ) {
            PlayerSurface(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black),
                player = player,
                displayDebugView = true,
                contentAlignment = Alignment.Center,
                scaleMode = resizeMode,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                .padding(MaterialTheme.paddings.baseline)
                .align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
        ) {
            SliderWithLabel(
                label = "W:",
                value = widthPercent,
                onValueChange = setWidthPercent,
            )

            SliderWithLabel(
                label = "H:",
                value = heightPercent,
                onValueChange = setHeightPercent,
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                ScaleMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = mode == resizeMode,
                        onClick = { resizeMode = mode },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ScaleMode.entries.size),
                        label = { Text(mode.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SliderWithLabel(
    modifier: Modifier = Modifier,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = modifier.systemGestureExclusion(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
        )

        PillarboxSlider(
            value = value,
            range = 0f..1f,
            compactMode = false,
            thumbColorEnabled = MaterialTheme.colorScheme.primary,
            thumbColorDisabled = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            activeTrackColorEnabled = MaterialTheme.colorScheme.primary,
            activeTrackColorDisabled = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            inactiveTrackColorEnabled = MaterialTheme.colorScheme.surfaceVariant,
            inactiveTrackColorDisabled = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            onValueChange = onValueChange,
        )
    }
}
