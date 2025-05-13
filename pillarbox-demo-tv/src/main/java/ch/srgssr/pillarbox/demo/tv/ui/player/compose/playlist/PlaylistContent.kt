/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.media3.common.MediaItem
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.tv.R
import ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls.DrawerContent
import coil3.compose.AsyncImage

@Composable
internal fun NavigationDrawerScope.PlaylistContent(
    mediaItems: List<MediaItem>,
    currentMediaItemIndex: Int,
    modifier: Modifier = Modifier,
    onItemClick: (index: Int, item: MediaItem) -> Unit,
    onEditClick: () -> Unit,
) {
    DrawerContent(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.playlist))

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_playlist),
                    )
                }
            }
        },
        items = mediaItems,
        isItemSelected = { index, _ ->
            index == currentMediaItemIndex
        },
        modifier = modifier,
        onItemClick = onItemClick,
        leadingContent = { item ->
            AsyncImage(
                model = item.mediaMetadata.artworkUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
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
