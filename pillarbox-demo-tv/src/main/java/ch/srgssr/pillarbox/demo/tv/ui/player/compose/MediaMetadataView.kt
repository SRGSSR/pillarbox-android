/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaMetadata
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import coil.compose.AsyncImage

/**
 * Media metadata view
 *
 * @param mediaMetadata The [MediaMetadata] to display.
 * @param modifier The Modifier.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaMetadataView(
    mediaMetadata: MediaMetadata,
    modifier: Modifier = Modifier,
) {
    Row(modifier.background(color = Color.Black)) {
        AsyncImage(
            modifier = Modifier
                .width(200.dp)
                .aspectRatio(16 / 9f),
            contentScale = ContentScale.Fit,
            model = mediaMetadata.artworkUri,
            contentDescription = null,
        )
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.padding(MaterialTheme.paddings.mini)
        ) {
            Text(
                text = mediaMetadata.title?.toString() ?: "No title",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            mediaMetadata.description?.let {
                Text(
                    text = mediaMetadata.description.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun MediaMetadataPreview() {
    val mediaMetadata = MediaMetadata.Builder().setTitle("Title").setDescription("Description").build()
    MediaMetadataView(mediaMetadata = mediaMetadata, modifier = Modifier.fillMaxSize())
}
