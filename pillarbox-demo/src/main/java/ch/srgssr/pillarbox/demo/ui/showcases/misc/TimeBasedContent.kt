/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.player.DemoPlayerView
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.player.extension.seekToUtcTime
import kotlinx.datetime.Clock

/**
 * Time-based content that demonstrates how to use timestamp-based api.
 */
@Composable
fun TimeBasedContent() {
    val viewModel: TimeBasedContentViewModel = viewModel()
    val player = viewModel.player
    val timedEvents by viewModel.deltaTimeEvents.collectAsStateWithLifecycle()

    LaunchedEffect(player) {
        player.play()
    }
    Column {
        DemoPlayerView(player = player, modifier = Modifier.weight(1f))
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.paddings.baseline)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small)
        ) {
            item {
                DemoListHeaderView("Timed events")
            }
            item {
                DemoListSectionView {
                    for (it in timedEvents) {
                        DemoListItemView(
                            title = it.name,
                            subtitle = "Delta time ${it.delta}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                        ) {
                            val now = Clock.System.now()
                            player.seekToUtcTime((now + it.delta).toEpochMilliseconds())
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
