/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.demo.cast.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.cast.ui.theme.paddings
import coil3.compose.AsyncImage

@Composable
internal fun PlaylistView(items: List<MediaItem>, currentMediaItemIndex: Int, modifier: Modifier = Modifier, onItemClicked: (Int) -> Unit = {}) {
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.mini)) {
        itemsIndexed(items.filter { it.localConfiguration != null }) { index, item ->
            MediaItemView(
                item.mediaMetadata,
                selected = currentMediaItemIndex == index,
                modifier
                    .fillMaxWidth()
                    .clickable { onItemClicked(index) }
            )
        }
    }
}

@Composable
private fun MediaItemView(metadata: MediaMetadata, selected: Boolean, modifier: Modifier = Modifier) {
    val itemModifier = if (selected) {
        modifier.background(MaterialTheme.colorScheme.inverseSurface.copy(0.25f))
    } else {
        modifier
    }
    Row(
        itemModifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = metadata.artworkUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp),
            placeholder = painterResource(androidx.media3.cast.R.drawable.cast_album_art_placeholder),
            error = painterResource(androidx.media3.cast.R.drawable.cast_album_art_placeholder)
        )
        val title = metadata.title ?: "No title"
        val subtitle = metadata.subtitle ?: "No subtitle"
        Column(modifier = Modifier) {
            Text(text = title.toString(), style = if (selected) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall)
            Text(text = subtitle.toString(), style = if (selected) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview
@Composable
private fun MediaItemPreview() {
    val mediaMetadata = MediaMetadata.Builder()
        .setTitle("Title")
        .setSubtitle("Subtitle")
        .setArtworkUri("https://img.rts.ch/audio/2010/image/924h3y-25865853.image?w=640&h=640".toUri())
        .build()
    MediaItemView(metadata = mediaMetadata, selected = false, modifier = Modifier.fillMaxWidth())
}

@Preview
@Composable
private fun PlaylistPreview() {
    val mediaItems = listOf(
        MediaItem.Builder()
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Title")
                    .setSubtitle("Subtitle")
                    .setArtworkUri("https://img.rts.ch/audio/2010/image/924h3y-25865853.image?w=640&h=640".toUri())
                    .build()
            )
            .build(),
        MediaItem.Builder()
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Title 2")
                    .setSubtitle("Subtitle 2")
                    .build()
            )
            .build(),
    )
    PillarboxTheme {
        PlaylistView(items = mediaItems, currentMediaItemIndex = 0, modifier = Modifier.fillMaxWidth())
    }
}
