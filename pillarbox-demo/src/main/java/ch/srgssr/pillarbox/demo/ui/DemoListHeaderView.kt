/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Demo list header view
 *
 * @param title title of the header
 * @param modifier The Modifier of the layout
 */
@Composable
fun DemoListHeaderView(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview
@Composable
private fun PlaylistHeaderPreview() {
    PillarboxTheme {
        DemoListHeaderView(title = "Title of the playlist")
    }
}
