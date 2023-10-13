/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.tracking

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface2

/**
 * Tracking toggle sample
 * Demonstrate how to toggle MediaItem tracking.
 */
@Composable
fun TrackingToggleSample() {
    val context = LocalContext.current
    val player = remember {
        PlayerModule.provideDefaultPlayer(context).apply {
            setMediaItem(DemoItem.OnDemandHorizontalVideo.toMediaItem())
            prepare()
            play()
        }
    }
    var trackingEnabled by remember {
        mutableStateOf(player.trackingEnabled)
    }
    player.trackingEnabled = trackingEnabled
    DisposableEffect(key1 = player) {
        onDispose {
            player.release()
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        val isLandScape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        val playerModifier = if (isLandScape) {
            Modifier
                .aspectRatio(16 / 9f)
                .weight(1f)
                .align(alignment = Alignment.CenterHorizontally)
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        }
        PlayerSurface2(
            player = player,
            modifier = playerModifier
        )

        Row(modifier = Modifier.wrapContentSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Toggle tracking", color = MaterialTheme.colorScheme.onBackground)
            Switch(checked = trackingEnabled, onCheckedChange = {
                trackingEnabled = it
            })
        }
    }
}
