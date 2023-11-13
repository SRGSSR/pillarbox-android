/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Demo item view
 *
 * @param title Title
 * @param modifier
 * @param subtitle Optional subtitle
 * @param enabled true if the item is clickable
 * @param onClick click event
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoItemView(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = subtitle ?: "", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
        }
    }
}

@Preview
@Composable
private fun DemoItemPreview() {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                val itemModifier = Modifier
                    .fillMaxWidth()
                DemoItemView(
                    modifier = itemModifier,
                    title = "Title 1",
                    subtitle = "Description 1"
                )
                DemoItemView(
                    modifier = itemModifier,
                    title = "Title 2",
                    subtitle = "Description 2"
                )

                DemoItemView(
                    modifier = itemModifier,
                    title = "Title 3",
                )

                DemoItemView(
                    modifier = itemModifier,
                    title = "Title 4",
                )
            }
        }
    }
}
