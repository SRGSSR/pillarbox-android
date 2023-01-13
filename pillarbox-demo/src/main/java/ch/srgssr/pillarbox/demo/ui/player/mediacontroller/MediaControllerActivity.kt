/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.mediacontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.ui.ExoPlayerView

/**
 * Media controller activity
 *
 * Using official guide for background playback at https://developer.android.com/guide/topics/media/media3/getting-started/playing-in-background
 *
 * @constructor Create empty Media controller activity
 */
class MediaControllerActivity : ComponentActivity() {
    private val controllerViewModel: MediaControllerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PillarboxTheme {
                Surface {
                    MainView(controllerViewModel)
                }
            }
        }
    }

    @Composable
    private fun MainView(viewModel: MediaControllerViewModel, modifier: Modifier = Modifier) {
        val player = viewModel.player.collectAsState()
        val items = viewModel.items.collectAsState()
        val currentItem = viewModel.currentPlayingItem.collectAsState()
        val currentPlaylist = viewModel.currentPlaylistItems.collectAsState()
        val currentPlaylistIds = remember {
            currentPlaylist.value.map { it.mediaId }
        }
        val shuffleModeEnableState = viewModel.shuffleModeEnabled.collectAsState()
        Column(modifier) {
            ExoPlayerView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ASPECT_RATIO),
                keepScreenOn = true,
                showBuffering = PlayerView.SHOW_BUFFERING_ALWAYS,
                player = player.value
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                if (player.value != null) {
                    playlistView(
                        items = currentPlaylist.value, currentItem = currentItem.value,
                        shuffleModeEnable = shuffleModeEnableState.value,
                        onItemClick = {
                            viewModel.playItem(it)
                        },
                        moveDownClick = viewModel::moveDown,
                        moveUpClick = viewModel::moveUp,
                        deleteClick = viewModel::removeFromPlaylist,
                        shuffleModeEnableChanged = { player.value?.shuffleModeEnabled = it }
                    )
                    item {
                        Divider()
                    }
                }
                libraryItemsView(
                    items = items.value,
                    currentPlaylistId = currentPlaylistIds,
                    onItemClick = viewModel::addMediaItemToPlaylist,
                )
            }
        }
    }

    private fun LazyListScope.playlistView(
        items: List<PlaylistItem>,
        currentItem: Int,
        shuffleModeEnable: Boolean,
        onItemClick: (PlaylistItem) -> Unit,
        moveUpClick: (PlaylistItem) -> Unit,
        moveDownClick: (PlaylistItem) -> Unit,
        deleteClick: (PlaylistItem) -> Unit,
        shuffleModeEnableChanged: (Boolean) -> Unit
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Current Playlist", style = MaterialTheme.typography.h4)
                IconToggleButton(checked = shuffleModeEnable, onCheckedChange = shuffleModeEnableChanged) {
                    if (shuffleModeEnable) {
                        Icon(imageVector = Icons.Default.ShuffleOn, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = null)
                    }
                }
            }
        }
        items(items) { item ->
            val canMoveUp = item.index + 1 < items.size
            val canMoveDown = item.index - 1 >= 0
            PlaylistItemView(
                modifier = Modifier.fillMaxWidth(),
                item = item,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                isPlaying = currentItem == item.index,
                onItemClick = onItemClick,
                moveUpClick = moveUpClick,
                moveDownClick = moveDownClick,
                deleteClick = deleteClick
            )
            Spacer(modifier = Modifier.fillMaxWidth())
        }
    }

    private fun LazyListScope.libraryItemsView(
        items: List<MediaItem>,
        currentPlaylistId: List<String>,
        onItemClick: (MediaItem) -> Unit
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Library items", style = MaterialTheme.typography.h4)
            }
        }
        items(items) { item ->
            LibraryItemView(
                modifier = Modifier.fillMaxWidth(),
                item = item,
                isInPlaylist = currentPlaylistId.contains(item.mediaId),
                onItemClick = onItemClick
            )
            Spacer(modifier = Modifier.fillMaxWidth())
        }
    }

    @Composable
    private fun LibraryItemView(item: MediaItem, isInPlaylist: Boolean, onItemClick: (MediaItem) -> Unit, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onItemClick(item) }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val fontWeight = if (isInPlaylist) FontWeight.Bold else FontWeight.Normal
            Text(text = item.mediaMetadata.title.toString(), overflow = TextOverflow.Ellipsis, fontWeight = fontWeight)
            if (isInPlaylist) {
                Icon(modifier = Modifier.padding(start = 4.dp), imageVector = Icons.Default.Check, contentDescription = "Is Playing")
            }
        }
    }

    @Composable
    private fun PlaylistItemView(
        item: PlaylistItem,
        isPlaying: Boolean,
        canMoveUp: Boolean,
        canMoveDown: Boolean,
        modifier: Modifier = Modifier,
        onItemClick: (PlaylistItem) -> Unit,
        moveUpClick: (PlaylistItem) -> Unit,
        moveDownClick: (PlaylistItem) -> Unit,
        deleteClick: (PlaylistItem) -> Unit
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onItemClick(item) }
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
            val color = if (isPlaying) MaterialTheme.colors.primary else Color.Unspecified
            Text(
                modifier = Modifier.weight(0.5f),
                text = item.title,
                color = color,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontWeight = fontWeight
            )

            val buttonModifier = Modifier // .weight(1f, false)

            IconButton(modifier = buttonModifier, enabled = canMoveUp, onClick = { moveUpClick(item) }) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Move bottom of the " +
                        "list"
                )
            }
            IconButton(modifier = buttonModifier, enabled = canMoveDown, onClick = { moveDownClick(item) }) {
                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move top of the list")
            }
            IconButton(modifier = buttonModifier, onClick = { deleteClick(item) }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }

    @Preview
    @Composable
    private fun PlayListItemPreview() {
        val item = PlaylistItem(
            index = 0, mediaId = "",
            title = "Media Title a bit long very very long indeed and can lead some issue"
        )
        PlaylistItemView(
            item = item,
            isPlaying = true,
            canMoveUp = true,
            canMoveDown = false,
            onItemClick = {},
            moveUpClick = {},
            moveDownClick = {},
            deleteClick = {}
        )
    }

    companion object {
        private const val ASPECT_RATIO = 16 / 9f
    }
}
