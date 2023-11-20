/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Demo list header view.
 *
 * @param title The title of the header.
 * @param modifier The [Modifier] of the layout.
 */
@Composable
fun DemoListHeaderView(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier.padding(
            top = 16.dp,
            bottom = 8.dp
        ),
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
@Preview(showBackground = true)
private fun DemoListHeaderViewPreview() {
    PillarboxTheme {
        DemoListHeaderView(title = "Demo list header")
    }
}
