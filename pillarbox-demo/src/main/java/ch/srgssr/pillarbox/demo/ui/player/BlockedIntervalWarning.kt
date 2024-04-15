/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.BlockedInterval
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Display a message when player reach a blocked interval.
 *
 * @param player
 * @param modifier
 * @param visibilityDelay
 */
@Composable
fun BlockedIntervalWarning(
    player: Player,
    modifier: Modifier,
    visibilityDelay: Duration = 5.seconds,
) {
    var currentBlockedInterval: BlockedInterval? by remember(player) {
        mutableStateOf(null)
    }
    DisposableEffect(player) {
        val listener = object : PillarboxPlayer.Listener {
            override fun onBlockIntervalReached(blockedInterval: BlockedInterval) {
                currentBlockedInterval = blockedInterval
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }
    LaunchedEffect(currentBlockedInterval) {
        if (currentBlockedInterval != null) {
            delay(visibilityDelay)
            currentBlockedInterval = null
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = currentBlockedInterval != null
    ) {
        currentBlockedInterval?.let {
            BlockedSegmentInfo(modifier = Modifier.fillMaxWidth(), blockedInterval = it)
        }
    }
}

@Composable
private fun BlockedSegmentInfo(
    blockedInterval: BlockedInterval,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier
            .background(color = Color.Blue.copy(0.8f))
            .padding(MaterialTheme.paddings.baseline),
        text = blockedInterval.reason,
        color = Color.White,
        style = MaterialTheme.typography.labelSmall
    )
}

@Preview(showBackground = true)
@Composable
private fun BlockedSegmentPreview() {
    val blockedSection = BlockedInterval("", 0, 0, "GeoBlock")
    MaterialTheme {
        BlockedSegmentInfo(
            modifier = Modifier.fillMaxWidth(),
            blockedInterval = blockedSection
        )
    }
}
