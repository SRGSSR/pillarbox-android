/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.ui.extension.availableCommandsAsState
import ch.srgssr.pillarbox.ui.extension.currentMediaItemIndexAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentMediaItemsAsState
import ch.srgssr.pillarbox.ui.extension.shuffleModeEnabledAsState

/**
 * PlaylistView for a Player
 *
 * @param player The player whose playlist is managed.
 * @param itemsLibrary The list of [DemoItem] that can be added to the playlist through the dialog.
 * @param modifier Modifier of the layout.
 */
@Composable
fun PlaylistView(
    player: Player,
    itemsLibrary: List<DemoItem>,
    modifier: Modifier = Modifier,
) {
    val currentMediaItems by player.getCurrentMediaItemsAsState()
    val currentMediaItemIndex by player.currentMediaItemIndexAsState()
    val shuffleModeEnabled by player.shuffleModeEnabledAsState()
    val availableCommand by player.availableCommandsAsState()

    var addItemDialogState by remember {
        mutableStateOf(false)
    }
    val mediaItemLibrary by rememberUpdatedState(itemsLibrary)
    if (addItemDialogState) {
        MediaItemLibraryDialog(
            items = mediaItemLibrary,
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
        mediaItems = currentMediaItems,
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
        canShuffle = availableCommand.contains(Player.COMMAND_SET_SHUFFLE_MODE),
        shuffleEnabled = shuffleModeEnabled,
        onShuffleToggled = player::setShuffleModeEnabled,
    )
}

@Composable
private fun PlaylistView(
    mediaItems: List<MediaItem>,
    currentMediaItemIndex: Int,
    onItemClick: (MediaItem, Int) -> Unit,
    onRemoveItemIndex: (Int) -> Unit,
    onMoveItemIndex: (from: Int, to: Int) -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onRemoveAll: () -> Unit,
    canShuffle: Boolean,
    shuffleEnabled: Boolean,
    onShuffleToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
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
                IconButton(
                    onClick = onAddToPlaylistClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_to_playlist),
                    )
                }
                IconToggleButton(
                    checked = shuffleEnabled,
                    enabled = canShuffle,
                    onCheckedChange = onShuffleToggled,
                ) {
                    val imageVector = if (shuffleEnabled) Icons.Default.ShuffleOn else Icons.Default.Shuffle

                    Icon(
                        imageVector = imageVector,
                        contentDescription = stringResource(R.string.toggle_shuffle),
                    )
                }
                IconButton(onClick = onRemoveAll) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = stringResource(R.string.clear_playlist),
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MaterialTheme.paddings.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_playlist),
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
            shuffleEnabled = false,
            canShuffle = true,
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
            shuffleEnabled = false,
            canShuffle = true,
        )
    }
}
