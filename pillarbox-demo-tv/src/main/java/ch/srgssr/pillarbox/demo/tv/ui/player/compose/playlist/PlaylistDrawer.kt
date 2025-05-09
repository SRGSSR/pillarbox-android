/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.media3.common.Player
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.tv.R
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import ch.srgssr.pillarbox.ui.extension.currentMediaItemIndexAsState
import ch.srgssr.pillarbox.ui.extension.getCurrentMediaItemsAsState
import coil3.compose.AsyncImage

/**
 * Drawer used to display a player's playlist.
 *
 * @param player The currently active player.
 * @param modifier The [Modifier] to apply to this layout.
 */
@Composable
fun NavigationDrawerScope.PlaylistDrawer(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val mediaItems by player.getCurrentMediaItemsAsState()
    val currentMediaItemIndex by player.currentMediaItemIndexAsState()

    Column(
        modifier = modifier
            .padding(horizontal = MaterialTheme.paddings.baseline)
            .padding(top = MaterialTheme.paddings.baseline),
    ) {
        Text(
            text = stringResource(R.string.playlist),
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn(
            contentPadding = PaddingValues(vertical = MaterialTheme.paddings.baseline),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline)
        ) {
            itemsIndexed(mediaItems) { index, mediaItem ->
                NavigationDrawerItem(
                    selected = index == currentMediaItemIndex,
                    onClick = {
                        player.seekToDefaultPosition(index)
                        player.play()
                    },
                    leadingContent = {
                        AsyncImage(
                            model = mediaItem.mediaMetadata.artworkUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
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
                    content = {
                        Text(
                            text = mediaItem.mediaMetadata.title.toString(),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    }
                )
            }
        }
    }
}
