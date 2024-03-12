/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Playlist item view
 *
 * @param title Title of the item
 * @param selected true if the item is selected
 * @param onRemoveClick event when remove button is clicked
 * @param modifier The modifier for the layout
 * @param removeEnabled enable delete button
 */
@Composable
fun PlaylistItemView(
    title: String,
    selected: Boolean,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
    removeEnabled: Boolean = true,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        val color = if (selected) MaterialTheme.colorScheme.inversePrimary else Color.Unspecified
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            color = color,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            fontWeight = fontWeight
        )

        IconButton(
            enabled = removeEnabled,
            onClick = onRemoveClick
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete"
            )
        }
    }
}

@Preview
@Composable
private fun PlaylistItemPreview() {
    PillarboxTheme {
        Column {
            PlaylistItemView(
                title = "Title 1",
                selected = true,
                onRemoveClick = {},
            )

            PlaylistItemView(
                title = "Title 2",
                selected = false,
                onRemoveClick = {},
            )

            PlaylistItemView(
                title = "Title 2",
                selected = false,
                onRemoveClick = {},
            )
        }
    }
}
