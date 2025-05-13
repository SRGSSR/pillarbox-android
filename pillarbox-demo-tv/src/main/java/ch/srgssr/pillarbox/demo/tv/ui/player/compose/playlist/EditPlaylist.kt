/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.playlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.media3.common.MediaItem
import androidx.tv.material3.Icon
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.tv.R
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.DrawerContent

@Composable
internal fun NavigationDrawerScope.EditPlaylist(
    items: List<MediaItem>,
    playerItems: List<MediaItem>,
    modifier: Modifier = Modifier,
    onAddClick: (item: MediaItem) -> Unit,
    onRemoveClick: (index: Int) -> Unit,
) {
    DrawerContent(
        title = { Text(text = stringResource(R.string.edit_playlist)) },
        items = items,
        isItemSelected = { _, item ->
            item in playerItems
        },
        modifier = modifier,
        onItemClick = { _, item ->
            if (item in playerItems) {
                onRemoveClick(playerItems.indexOf(item))
            } else {
                onAddClick(item)
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
