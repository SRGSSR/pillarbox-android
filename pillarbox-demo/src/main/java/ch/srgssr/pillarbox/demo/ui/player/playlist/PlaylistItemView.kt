/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
 * @param onMoveUpClick event when move up is clicked
 * @param onMoveDownClick event when move down is clicked
 * @param modifier The modifier for the layout
 * @param moveUpEnabled enable move up button
 * @param moveDownEnabled enable move down button
 * @param removeEnabled enable delete button
 */
@Composable
fun PlaylistItemView(
    title: String,
    selected: Boolean,
    onRemoveClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit,
    modifier: Modifier = Modifier,
    moveUpEnabled: Boolean = true,
    moveDownEnabled: Boolean = true,
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

        val buttonModifier = Modifier
        IconButton(
            modifier = buttonModifier,
            enabled = moveDownEnabled,
            onClick = onMoveDownClick
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Move bottom of the list"
            )
        }
        IconButton(
            modifier = buttonModifier,
            enabled = moveUpEnabled,
            onClick = onMoveUpClick
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Move top of the list"
            )
        }
        IconButton(
            modifier = buttonModifier,
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
                onMoveDownClick = {},
                onMoveUpClick = {},
                onRemoveClick = {},
                moveUpEnabled = false
            )

            PlaylistItemView(
                title = "Title 2",
                selected = false,
                onMoveDownClick = {},
                onMoveUpClick = {},
                onRemoveClick = {},
            )

            PlaylistItemView(
                title = "Title 2",
                selected = false,
                onMoveDownClick = {},
                onMoveUpClick = {},
                onRemoveClick = {},
                moveDownEnabled = false
            )
        }
    }
}
