/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * A showcase demonstrating the transition of a content from live to VOD.
 *
 * Use case: the user watches a live sport event with a delay (they paused it or went back in time). When the event ends, the stream becomes a
 * VOD. The content is then reloaded at the same position, to get its VOD metadata (for example, without commercials).
 *
 * @param liveToVodViewModel The [LiveToVodViewModel].
 */
@Composable
fun LiveToVodShowCase(liveToVodViewModel: LiveToVodViewModel = viewModel()) {
    val player = liveToVodViewModel.player
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.medium)) {
        PlayerView(
            player = player,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.paddings.baseline),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                liveToVodViewModel.restart()
            }) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = stringResource(R.string.play),
                )
            }
        }
    }
}
