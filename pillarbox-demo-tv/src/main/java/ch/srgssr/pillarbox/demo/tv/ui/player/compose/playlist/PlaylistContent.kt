/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.media3.common.MediaItem
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.NavigationDrawerItem
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
    onItemMoved: (fromIndex: Int, toIndex: Int) -> Unit,
) {
    var movingItemIndex by remember { mutableIntStateOf(-1) }

    DrawerContent(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.hasFocus) {
                            movingItemIndex = -1
                        }
                    },
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
        modifier = modifier,
        listContent = {
            itemsIndexed(mediaItems) { index, item ->
                NavigationDrawerItem(
                    selected = index == currentMediaItemIndex,
                    onClick = {
                        if (movingItemIndex < 0) {
                            onItemClick(index, item)
                        } else {
                            movingItemIndex = -1
                        }
                    },
                    leadingContent = {
                        if (index == movingItemIndex) {
                            Icon(
                                imageVector = Icons.Default.UnfoldMore,
                                contentDescription = null,
                            )
                        } else {
                            AsyncImage(
                                model = item.mediaMetadata.artworkUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    },
                    modifier = Modifier.onPreviewKeyEvent { key ->
                        if (key.type == KeyEventType.KeyDown) {
                            if (key.key == Key.DirectionUp && movingItemIndex > 0) {
                                onItemMoved(movingItemIndex, --movingItemIndex)
                            } else if (key.key == Key.DirectionDown && movingItemIndex in mediaItems.indices) {
                                onItemMoved(movingItemIndex, ++movingItemIndex)
                            }
                        }

                        false
                    },
                    onLongClick = { movingItemIndex = index },
                    supportingContent = if (item.mediaMetadata.description != null) {
                        {
                            Text(
                                text = item.mediaMetadata.description.toString(),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        }
                    } else {
                        null
                    },
                    content = {
                        Text(
                            text = item.mediaMetadata.title.toString(),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    },
                )
            }
        },
    )
}
