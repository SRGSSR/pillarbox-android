/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.controls

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import coil.compose.AsyncImage

/**
 * Media metadata view
 *
 * @param mediaMetadata The [MediaMetadata] to display.
 * @param modifier The Modifier.
 */
@Composable
fun MediaMetadataView(
    mediaMetadata: MediaMetadata,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        AsyncImage(
            modifier = Modifier
                .width(120.dp)
                .wrapContentHeight(),
            contentScale = ContentScale.Fit,
            model = mediaMetadata.artworkUri,
            contentDescription = null,
        )
        Column(modifier = Modifier.padding(horizontal = MaterialTheme.paddings.baseline)) {
            Text(
                text = mediaMetadata.title?.toString() ?: "No title",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            mediaMetadata.description?.let {
                Text(
                    text = mediaMetadata.description.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun MediaMetadataPreview() {
    val mediaMetadata = MediaMetadata.Builder().setTitle("Title").setDescription("Description").build()
    MediaMetadataView(mediaMetadata = mediaMetadata, modifier = Modifier.fillMaxSize())
}
