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
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Display a message when the player reaches a blocked time range.
 *
 * @param player
 * @param modifier
 * @param visibilityDelay
 */
@Composable
fun BlockedTimeRangeWarning(
    player: Player,
    modifier: Modifier = Modifier,
    visibilityDelay: Duration = 5.seconds,
) {
    var currentBlockedTimeRange: BlockedTimeRange? by remember(player) {
        mutableStateOf(null)
    }
    DisposableEffect(player) {
        val listener = object : PillarboxPlayer.Listener {
            override fun onBlockedTimeRangeReached(blockedTimeRange: BlockedTimeRange) {
                currentBlockedTimeRange = blockedTimeRange
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }
    LaunchedEffect(currentBlockedTimeRange) {
        if (currentBlockedTimeRange != null) {
            delay(visibilityDelay)
            currentBlockedTimeRange = null
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = currentBlockedTimeRange != null
    ) {
        currentBlockedTimeRange?.let {
            BlockedTimeRangeInfo(modifier = Modifier.fillMaxWidth(), blockedTimeRange = it)
        }
    }
}

@Composable
private fun BlockedTimeRangeInfo(
    blockedTimeRange: BlockedTimeRange,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier
            .background(color = Color.Blue.copy(0.8f))
            .padding(MaterialTheme.paddings.baseline),
        text = "Reached a blocked segment! ${blockedTimeRange.reason}",
        color = Color.White,
        style = MaterialTheme.typography.labelSmall
    )
}

@Preview(showBackground = true)
@Composable
private fun BlockedTimeRangeInfoPreview() {
    val blockedTimeRange = BlockedTimeRange(start = 0, end = 0, reason = "GeoBlock")
    PillarboxTheme {
        BlockedTimeRangeInfo(
            modifier = Modifier.fillMaxWidth(),
            blockedTimeRange = blockedTimeRange
        )
    }
}
