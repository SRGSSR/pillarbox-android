/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.playlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.media3.common.MediaItem
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.tv.R
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.DrawerContent
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings

@Composable
internal fun NavigationDrawerScope.EditPlaylist(
    items: List<MediaItem>,
    playerItems: List<MediaItem>,
    modifier: Modifier = Modifier,
    onAddClick: (items: List<MediaItem>) -> Unit,
    onRemoveClick: (index: Int) -> Unit,
    onClearClick: () -> Unit,
) {
    DrawerContent(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.edit_playlist))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
                ) {
                    IconButton(onClick = { onAddClick(items) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = stringResource(R.string.add_all),
                        )
                    }

                    IconButton(onClick = { onClearClick() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.clear),
                        )
                    }
                }
            }
        },
        items = items,
        isItemSelected = { _, item ->
            item in playerItems
        },
        modifier = modifier,
        onItemClick = { _, item ->
            if (item in playerItems) {
                onRemoveClick(playerItems.indexOf(item))
            } else {
                onAddClick(listOf(item))
            }
        },
        leadingContent = { item ->
            AnimatedVisibility(visible = item in playerItems) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                )
            }
        },
        supportingContent = { item ->
            if (item.mediaMetadata.description != null) {
                Text(
                    text = item.mediaMetadata.description.toString(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        },
        content = { item ->
            Text(
                text = item.mediaMetadata.title.toString(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
    )
}
