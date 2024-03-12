/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.ui.extension.currentMediaItemIndexAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentMediaItemsAsState
import ch.srgssr.pillarbox.ui.extension.shuffleModeEnabledAsState

/**
 * PlaylistView for a Player
 *
 * @param player The player whose playlist is managed.
 * @param modifier Modifier of the layout.
 */
@Composable
fun PlaylistView(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val mediaItems by player.getCurrentMediaItemsAsState()
    val currentMediaItemIndex by player.currentMediaItemIndexAsState()
    val shuffleModeEnabled by player.shuffleModeEnabledAsState()

    var addItemDialogState by remember {
        mutableStateOf(false)
    }
    val mediaItemLibrary = remember {
        Playlist.All
    }
    if (addItemDialogState) {
        MediaItemLibraryDialog(
            items = mediaItemLibrary.items,
            onAddClick = { selectedItems ->
                player.addMediaItems(selectedItems.map { it.toMediaItem() })
            },
            onDismissRequest = {
                addItemDialogState = false
            },
        )
    }

    PlaylistView(
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
            addItemDialogState = true
        },
        onRemoveAll = player::clearMediaItems,
        shuffleEnabled = shuffleModeEnabled,
        onShuffleToggled = player::setShuffleModeEnabled,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistView(
    mediaItems: List<MediaItem>,
    currentMediaItemIndex: Int,
    onItemClick: (MediaItem, Int) -> Unit,
    onRemoveItemIndex: (Int) -> Unit,
    onMoveItemIndex: (from: Int, to: Int) -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onRemoveAll: () -> Unit,
    shuffleEnabled: Boolean,
    onShuffleToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggedIndex by remember { mutableIntStateOf(-1) }

    LazyColumn(modifier = modifier) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    onClick = onAddToPlaylistClick
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
                IconToggleButton(checked = shuffleEnabled, onShuffleToggled) {
                    if (shuffleEnabled) {
                        Icon(imageVector = Icons.Default.ShuffleOn, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = null)
                    }
                }
                IconButton(onClick = onRemoveAll) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                }
            }
        }
        if (mediaItems.isNotEmpty()) {
            itemsIndexed(mediaItems) { index, mediaItem ->
                val dropAndDropTarget = remember {
                    object : DragAndDropTarget {
                        private var referenceY = 0f

                        override fun onDrop(event: DragAndDropEvent): Boolean {
                            val didDragItem = draggedIndex != index

                            draggedIndex = -1
                            referenceY = 0f

                            return didDragItem
                        }

                        override fun onStarted(event: DragAndDropEvent) {
                            referenceY = event.toAndroidDragEvent().y
                        }

                        override fun onEntered(event: DragAndDropEvent) {
                            val currentY = event.toAndroidDragEvent().y
                            if (currentY == referenceY) {
                                return
                            }

                            val moveUp = currentY < referenceY
                            val newIndex = (draggedIndex + if (moveUp) -1 else 1).coerceIn(mediaItems.indices)

                            onMoveItemIndex(draggedIndex, newIndex)

                            draggedIndex = newIndex
                            referenceY = currentY
                        }

                        override fun onExited(event: DragAndDropEvent) {
                            referenceY = event.toAndroidDragEvent().y
                        }
                    }
                }

                PlaylistItemView(
                    modifier = Modifier
                        .dragAndDropTarget(
                            shouldStartDragAndDrop = { draggedIndex != -1 },
                            target = dropAndDropTarget,
                        )
                        .dragAndDropSource {
                            detectTapGestures(
                                onLongPress = {
                                    if (mediaItems.size > 1) {
                                        draggedIndex = index

                                        startTransfer(DragAndDropTransferData(ClipData.newPlainText("", "")))
                                    }
                                },
                                onTap = {
                                    if (index != currentMediaItemIndex) {
                                        onItemClick(mediaItem, index)
                                    }
                                },
                            )
                        },
                    title = mediaItem.mediaMetadata.title.toString(),
                    selected = currentMediaItemIndex == index,
                    onRemoveClick = {
                        onRemoveItemIndex(index)
                    },
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MaterialTheme.paddings.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Empty playlist",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlaylistPreview() {
    val mediaItems = ArrayList<MediaItem>()
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
        PlaylistView(
            modifier = Modifier,
            mediaItems = mediaItems,
            currentMediaItemIndex = currentMediaItemIndex,
            onItemClick = { _, _ -> },
            onRemoveItemIndex = {},
            onMoveItemIndex = { _, _ -> },
            onAddToPlaylistClick = {},
            onRemoveAll = {},
            onShuffleToggled = {},
            shuffleEnabled = false
        )
    }
}

@Preview
@Composable
private fun PlaylistPreviewEmptyList() {
    val mediaItems = emptyList<MediaItem>()
    val currentMediaItemIndex = 0
    PillarboxTheme {
        PlaylistView(
            modifier = Modifier,
            mediaItems = mediaItems,
            currentMediaItemIndex = currentMediaItemIndex,
            onItemClick = { _, _ -> },
            onRemoveItemIndex = {},
            onMoveItemIndex = { _, _ -> },
            onAddToPlaylistClick = {},
            onRemoveAll = {},
            onShuffleToggled = {},
            shuffleEnabled = false
        )
    }
}
