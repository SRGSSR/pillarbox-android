/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/**
 * Playlist item view
 *
 * @param modifier
 * @param title Title of the item
 * @param position position of the item
 * @param itemCount number of items in the player
 * @param currentPosition current playing media position
 * @param onRemoveItemIndex called when click on the delete button.
 * @param onMoveItemIndex called when moving the item.
 */
@Composable
fun PlaylistItemView(
    modifier: Modifier = Modifier,
    title: String,
    position: Int,
    itemCount: Int,
    currentPosition: Int,
    onRemoveItemIndex: (Int) -> Unit,
    onMoveItemIndex: (from: Int, to: Int) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isPlaying = currentPosition == position
        val fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
        val color = if (isPlaying) MaterialTheme.colors.primary else Color.Unspecified
        Text(
            modifier = Modifier.weight(0.5f),
            text = title,
            color = color,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            fontWeight = fontWeight
        )

        val buttonModifier = Modifier
        IconButton(modifier = buttonModifier, enabled = position + 1 < itemCount, onClick = { onMoveItemIndex(position, position + 1) }) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Move bottom of the list"
            )
        }
        IconButton(modifier = buttonModifier, enabled = position - 1 >= 0, onClick = { onMoveItemIndex(position, position - 1) }) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Move top of the list"
            )
        }
        IconButton(modifier = buttonModifier, onClick = { onRemoveItemIndex(position) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete"
            )
        }
    }
}
