/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Demo item view.
 *
 * @param title The title of the item.
 * @param modifier The [Modifier] to apply to the root of the item.
 * @param subtitle The optional subtitle of the item.
 * @param onClick The action to perform when an item is clicked.
 */
@Composable
fun DemoListItemView(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .minimumInteractiveComponentSize()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun DemoItemPreview() {
    PillarboxTheme {
        Column {
            val itemModifier = Modifier.fillMaxWidth()

            DemoListItemView(
                modifier = itemModifier,
                title = "Title 1",
                subtitle = "Description 1"
            )

            DemoListItemView(
                modifier = itemModifier,
                title = "Title 2",
            )
        }
    }
}
