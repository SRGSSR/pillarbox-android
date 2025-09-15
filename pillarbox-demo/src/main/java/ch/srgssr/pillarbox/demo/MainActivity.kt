/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Dimension
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.ui.PillarboxSurface
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerSubtitleView
import ch.srgssr.pillarbox.ui.exoplayer.SubtitleTextSize

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            PillarboxTheme {
                // MainNavigation()
                Simple()
                // ExoPlayerDemo()
            }
        }
    }
}

@Composable
private fun Simple() {
    val context = LocalContext.current
    val player = remember {
        PlayerModule.provideDefaultPlayer(context).apply {
            setMediaItem(SamplesSRG.OnDemandHorizontalVideo.toMediaItem())
            // setMediaItem(MediaItem.fromUri("https://rts-vod-amd.akamaized.net/ww/14827306/98923d94-071c-3d48-ac0c-dbababe70a68/master.m3u8"))
            volume = 0f
        }
    }

    var percentWidth by remember { mutableFloatStateOf(1f) }
    var percentHeight by remember { mutableFloatStateOf(1f) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth(percentWidth)
                .fillMaxHeight(percentHeight)
                .clipToBounds()
                .background(color = Color.Black),
            contentAlignment = Alignment.Center
        ) {
            PillarboxSurface(pillarboxPlayer = player, contentScale = ContentScale.Fit, surfaceContent = {
            })
            ExoPlayerSubtitleView(player = player, modifier = Modifier, subtitleTextSize = SubtitleTextSize.Fixed(Dimension.SP, 48f))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp)
                .align(Alignment.BottomCenter)
        ) {
            Slider(percentWidth, onValueChange = { percentWidth = it })
            Slider(percentHeight, onValueChange = { percentHeight = it })
        }
    }

    DisposableEffect(Unit) {
        player.prepare()
        player.play()
        onDispose {
            player.release()
        }
    }
}
