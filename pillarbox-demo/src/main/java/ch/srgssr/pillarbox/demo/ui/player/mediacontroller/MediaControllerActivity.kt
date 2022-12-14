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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

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
        Column(modifier) {
            PlayerView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ASPECT_RATIO),
                player = player.value
            )
            if (player.value != null) {
                PlaylistView(
                    items = items.value, currentItem = currentItem.value, currentPlaylistId = currentPlaylist.value.map { it.mediaId },
                    onItemClick = {
                        viewModel.playItem(it)
                    },
                    toggleClick = { mediaItem, state ->
                        if (state) {
                            viewModel.addItemToPlaylist(mediaItem, false)
                        } else {
                            viewModel.removeItem(mediaItem)
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun PlaylistView(
        items: List<MediaItem>,
        currentItem: MediaItem,
        currentPlaylistId: List<String>,
        modifier: Modifier = Modifier,
        onItemClick: (MediaItem) -> Unit,
        toggleClick: (MediaItem, Boolean) -> Unit
    ) {

        LazyColumn(modifier = modifier.fillMaxWidth()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Playlist items", style = MaterialTheme.typography.h4)
                }
            }
            items(items) { item ->
                PlaylistItemView(
                    modifier = Modifier.fillMaxWidth(),
                    item = item,
                    isPlaying = currentItem.mediaId == item.mediaId,
                    isInPlaylist = currentPlaylistId.contains(item.mediaId),
                    onItemClick = onItemClick,
                    toggleClick = toggleClick
                )
                Spacer(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    @Composable
    private fun PlaylistItemView(
        item: MediaItem,
        isPlaying: Boolean,
        isInPlaylist: Boolean,
        modifier: Modifier = Modifier,
        onItemClick: (MediaItem) -> Unit,
        toggleClick: (MediaItem, Boolean) -> Unit
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onItemClick(item) }
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
                Text(text = item.mediaMetadata.title.toString(), overflow = TextOverflow.Ellipsis, fontWeight = fontWeight)
                if (isPlaying) {
                    Icon(modifier = Modifier.padding(start = 4.dp), imageVector = Icons.Default.PlayCircle, contentDescription = "Is Playing")
                }
            }
            Checkbox(
                checked = isInPlaylist,
                onCheckedChange = { toggleClick(item, !isInPlaylist) }
            )
        }
    }

    companion object {
        private const val ASPECT_RATIO = 16 / 9f
    }
}
