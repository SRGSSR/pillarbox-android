/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/**
 * Playlist header
 *
 * @param title The title of the playlist.
 * @param modifier
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlaylistHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = title,
        style = MaterialTheme.typography.headlineSmall,
    )
}
