/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.ui.extension.currentMediaItemIndexAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentMediaItemsAsState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

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
    val mediaItems by player.getCurrentMediaItemsAsState()
    val currentMediaItemIndex by player.currentMediaItemIndexAsState()
    val mediaItemLibrary by rememberUpdatedState(itemsLibrary)

    var showAddItemsDialog by remember { mutableStateOf(false) }

    if (showAddItemsDialog) {
        MediaItemLibraryDialog(
            items = mediaItemLibrary,
            onAddClick = { selectedItems ->
                val items = selectedItems.map { it.toMediaItem() }
                // Because CastPlayer doesn't support addMediaItems when empty.
                // See https://github.com/androidx/media/issues/2402
                if (player.mediaItemCount == 0) {
                    player.setMediaItems(items)
                } else {
                    player.addMediaItems(items)
                }
            },
            onDismissRequest = { showAddItemsDialog = false },
        )
    }

    AnimatedContent(
        targetState = mediaItems.isEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            EmptyPlaylist(
                modifier = Modifier.padding(MaterialTheme.paddings.baseline),
                onAddClick = { showAddItemsDialog = true },
            )
        } else {
            PlaylistView(
                mediaItems = mediaItems,
                currentMediaItemIndex = currentMediaItemIndex,
                onItemClick = { index ->
                    player.seekToDefaultPosition(index)
                    player.play()
                    if (player.playbackState == Player.STATE_IDLE) {
                        player.prepare()
                    }
                },
                onRemoveItem = player::removeMediaItem,
                onMoveItem = player::moveMediaItem,
                onAddClick = { showAddItemsDialog = true },
                onRemoveAllClick = player::clearMediaItems,
            )
        }
    }
}

@Composable
private fun PlaylistView(
    mediaItems: List<MediaItem>,
    currentMediaItemIndex: Int,
    onItemClick: (index: Int) -> Unit,
    onRemoveItem: (index: Int) -> Unit,
    onMoveItem: (from: Int, to: Int) -> Unit,
    onAddClick: () -> Unit,
    onRemoveAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMoveItem(from.index, to.index)
    }

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.semantics {
                collectionInfo = CollectionInfo(rowCount = mediaItems.size, columnCount = 1)
            },
            state = lazyListState,
        ) {
            itemsIndexed(
                items = mediaItems,
                key = { index, item -> System.identityHashCode(item) },
            ) { index, mediaItem ->
                PlaylistItem(
                    index = index,
                    mediaItem = mediaItem,
                    currentMediaItemIndex = currentMediaItemIndex,
                    reorderableLazyListState = reorderableLazyListState,
                    onItemClick = onItemClick,
                    onRemoveItem = onRemoveItem,
                )
            }
        }

        AnimatedVisibility(
            visible = !lazyListState.isScrollInProgress,
            modifier = Modifier
                .padding(MaterialTheme.paddings.baseline)
                .align(Alignment.BottomEnd),
            enter = fadeIn() + slideInHorizontally { it / 2 },
            exit = fadeOut() + slideOutHorizontally { it / 2 },
        ) {
            PlaylistToolbar(
                onAddClick = onAddClick,
                onRemoveAllClick = onRemoveAllClick,
            )
        }
    }
}

@Composable
private fun LazyItemScope.PlaylistItem(
    index: Int,
    mediaItem: MediaItem,
    currentMediaItemIndex: Int,
    reorderableLazyListState: ReorderableLazyListState,
    onItemClick: (index: Int) -> Unit,
    onRemoveItem: (index: Int) -> Unit,
) {
    val swipeToDismissState = rememberSwipeToDismissBoxState()

    if (swipeToDismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
        onRemoveItem(index)
    }

    SwipeToDismissBox(
        state = swipeToDismissState,
        backgroundContent = {
            if (swipeToDismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.error),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = MaterialTheme.paddings.baseline),
                        tint = MaterialTheme.colorScheme.onError,
                    )
                }
            }
        },
        modifier = Modifier.semantics {
            collectionItemInfo = CollectionItemInfo(
                rowIndex = index,
                rowSpan = 1,
                columnIndex = 1,
                columnSpan = 1,
            )
        },
        enableDismissFromStartToEnd = false,
    ) {
        ReorderableItem(
            state = reorderableLazyListState,
            key = System.identityHashCode(mediaItem),
        ) {
            val selected = index == currentMediaItemIndex
            val color by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            )
            val containerColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.background,
            )

            ListItem(
                headlineContent = {
                    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal

                    Text(
                        text = mediaItem.mediaMetadata.title.toString(),
                        fontWeight = fontWeight,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                modifier = Modifier
                    .longPressDraggableHandle()
                    .clickable(enabled = !selected) {
                        onItemClick(index)
                    },
                supportingContent = if (mediaItem.mediaMetadata.description != null) {
                    {
                        Text(
                            text = mediaItem.mediaMetadata.description.toString(),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    }
                } else {
                    null
                },
                colors = ListItemDefaults.colors(
                    containerColor = containerColor,
                    headlineColor = color,
                    supportingColor = color,
                )
            )
        }
    }
}

@Composable
private fun PlaylistToolbar(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onRemoveAllClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
        horizontalAlignment = Alignment.End,
    ) {
        SmallFloatingActionButton(onClick = onRemoveAllClick) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = stringResource(R.string.clear_playlist),
            )
        }

        FloatingActionButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_to_playlist),
            )
        }
    }
}

@Composable
private fun EmptyPlaylist(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
) {
    Box(modifier = modifier) {
        Text(
            text = stringResource(R.string.empty_playlist),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.bodyLarge,
        )

        ExtendedFloatingActionButton(
            text = { Text(text = stringResource(R.string.add_to_playlist)) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            },
            onClick = onAddClick,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Preview
@Composable
private fun PlaylistViewPreview() {
    val (currentMediaItemIndex, setCurrentMediaItemIndex) = remember { mutableIntStateOf(2) }
    val mediaItems = remember { mutableStateListOf<MediaItem>() }

    repeat(50) { index ->
        val mediaItem = MediaItem.Builder()
            .setMediaId("$index")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Title $index")
                    .setDescription("Description $index")
                    .build()
            )
            .build()

        mediaItems.add(mediaItem)
    }

    PillarboxTheme {
        Surface {
            PlaylistView(
                mediaItems = mediaItems,
                currentMediaItemIndex = currentMediaItemIndex,
                onItemClick = setCurrentMediaItemIndex,
                onRemoveItem = { index ->
                    mediaItems.removeAt(index)

                    if (index < currentMediaItemIndex) {
                        setCurrentMediaItemIndex(currentMediaItemIndex - 1)
                    }
                },
                onMoveItem = { _, _ -> },
                onAddClick = {},
                onRemoveAllClick = { mediaItems.clear() },
            )
        }
    }
}

@Preview
@Composable
private fun PlaylistItemPreview() {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { _, _ -> }
    val mediaItems = buildList {
        repeat(2) { index ->
            val mediaItem = MediaItem.Builder()
                .setMediaId("$index")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("Title $index")
                        .setDescription("Description $index")
                        .build()
                )
                .build()

            add(mediaItem)
        }
    }

    PillarboxTheme {
        Surface {
            LazyColumn {
                itemsIndexed(mediaItems) { index, mediaItem ->
                    PlaylistItem(
                        index = index,
                        mediaItem = mediaItem,
                        currentMediaItemIndex = 0,
                        reorderableLazyListState = reorderableLazyListState,
                        onItemClick = {},
                        onRemoveItem = {},
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlaylistToolbarPreview() {
    PillarboxTheme {
        Surface {
            PlaylistToolbar(
                onAddClick = {},
                onRemoveAllClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun EmptyPlaylistPreview() {
    PillarboxTheme {
        Surface {
            EmptyPlaylist(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.paddings.baseline),
                onAddClick = {},
            )
        }
    }
}
