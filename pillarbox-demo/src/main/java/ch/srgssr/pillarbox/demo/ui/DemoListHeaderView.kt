/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

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
    Text(
        modifier = modifier,
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Preview
@Composable
private fun PlaylistHeaderPreview() {
    MaterialTheme {
        DemoListHeaderView(title = "Title of the playlist")
    }
}
