/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast.playlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.cast.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.ui.extension.currentMediaItemIndexAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentMediaItemsAsState

/**
 * PlaylistView for a Player
 *
 * @param player The player whose playlist is managed.
 * @param modifier Modifier of the layout.
 * @param playlist The playlist to display.
 */
@Composable
fun EditablePlaylistView(
    player: Player,
    playlist: Playlist,
    modifier: Modifier = Modifier,
) {
    val mediaItems by player.getCurrentMediaItemsAsState()
    val currentMediaItemIndex by player.currentMediaItemIndexAsState()

    var showAddItemDialog by remember {
        mutableStateOf(false)
    }
    val mediaItemLibrary by rememberUpdatedState(playlist)
    if (showAddItemDialog) {
        MediaItemLibraryDialog(
            items = mediaItemLibrary.items,
            onAddClick = { selectedItems ->
                player.addMediaItems(selectedItems.map { it.toMediaItem() })
            },
            onDismissRequest = {
                showAddItemDialog = false
            },
        )
    }

    EditablePlaylistView(
        modifier = modifier,
        mediaItems = mediaItems,
        currentMediaItemIndex = currentMediaItemIndex,
        onRemoveItemIndex = player::removeMediaItem,
        onMoveItemIndex = player::moveMediaItem,
        onItemClick = { _: MediaItem, index: Int ->
            player.seekToDefaultPosition(index)
            player.play()
            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
        },
        onAddToPlaylistClick = {
            showAddItemDialog = true
        },
        onMoveItems = player::moveMediaItems,
        onRemoveAll = player::clearMediaItems,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditablePlaylistView(
    modifier: Modifier = Modifier,
    mediaItems: List<MediaItem>,
    currentMediaItemIndex: Int,
    onItemClick: (MediaItem, Int) -> Unit,
    onRemoveItemIndex: (Int) -> Unit,
    onMoveItemIndex: (from: Int, to: Int) -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onRemoveAll: () -> Unit,
    onMoveItems: (from: Int, to: Int, at: Int) -> Unit = { _, _, _ -> },
) {
    LazyColumn(
        modifier = modifier.semantics {
            collectionInfo = CollectionInfo(rowCount = mediaItems.size, columnCount = 1)
        },
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = {
                    onMoveItems(currentMediaItemIndex, currentMediaItemIndex + 3, currentMediaItemIndex + 1)
                }) {
                    Text("Move!")
                }
                IconButton(onClick = onAddToPlaylistClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to playlist",
                    )
                }
                IconButton(onClick = onRemoveAll) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove all items",
                    )
                }
            }
        }
        if (mediaItems.isNotEmpty()) {
            itemsIndexed(mediaItems) { index, mediaItem ->
                val selected = currentMediaItemIndex == index
                val nextIndex = index + 1
                val previousIndex = index - 1
                val canMoveUp = previousIndex >= 0
                val canMoveDown = nextIndex < mediaItems.size
                PlaylistItemView(
                    modifier = Modifier
                        .semantics {
                            collectionItemInfo = CollectionItemInfo(
                                rowIndex = index,
                                rowSpan = 1,
                                columnIndex = 1,
                                columnSpan = 1,
                            )
                        }
                        .clickable(enabled = index != currentMediaItemIndex) {
                            onItemClick(mediaItem, index)
                        },
                    title = mediaItem.mediaMetadata.title.toString(),
                    selected = selected,
                    moveDownEnabled = canMoveDown,
                    moveUpEnabled = canMoveUp,
                    onMoveUpClick = {
                        onMoveItemIndex(index, previousIndex)
                    },
                    onMoveDownClick = {
                        onMoveItemIndex(index, nextIndex)
                    },
                    onRemoveClick = {
                        onRemoveItemIndex(index)
                    },
                )
            }
        } else {
            item {
                Text(
                    text = "Empty playlist",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PlaylistPreview() {
    val mediaItems = mutableListOf<MediaItem>()
    for (i in 0..50) {
        val mediaItem = MediaItem.Builder()
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Title $i")
                    .setSubtitle("Subtitle $i")
                    .build()
            )
            .build()
        mediaItems.add(mediaItem)
    }
    val currentMediaItemIndex = 2
    PillarboxTheme {
        Surface {
            EditablePlaylistView(
                mediaItems = mediaItems,
                currentMediaItemIndex = currentMediaItemIndex,
                onItemClick = { _, _ -> },
                onRemoveItemIndex = {},
                onMoveItemIndex = { _, _ -> },
                onAddToPlaylistClick = {},
                onRemoveAll = {},
            )
        }
    }
}

@Preview
@Composable
private fun PlaylistPreviewEmptyList() {
    val mediaItems = emptyList<MediaItem>()
    val currentMediaItemIndex = 0
    PillarboxTheme {
        Surface {
            EditablePlaylistView(
                mediaItems = mediaItems,
                currentMediaItemIndex = currentMediaItemIndex,
                onItemClick = { _, _ -> },
                onRemoveItemIndex = {},
                onMoveItemIndex = { _, _ -> },
                onAddToPlaylistClick = {},
                onRemoveAll = {},
            )
        }
    }
}
