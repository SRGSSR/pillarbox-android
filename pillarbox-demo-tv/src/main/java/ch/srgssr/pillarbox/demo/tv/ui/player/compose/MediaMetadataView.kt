/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
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
    Row(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                ),
            ),
        verticalAlignment = Alignment.Bottom,
    ) {
        AsyncImage(
            modifier = Modifier
                .padding(MaterialTheme.paddings.small)
                .clip(RoundedCornerShape(MaterialTheme.paddings.small))
                .width(200.dp)
                .aspectRatio(16 / 9f),
            contentScale = ContentScale.Fit,
            model = mediaMetadata.artworkUri,
            contentDescription = null,
        )

        Column(
            modifier = Modifier.padding(
                start = MaterialTheme.paddings.mini,
                top = MaterialTheme.paddings.small,
                end = 72.dp, // baseline + 56dp to not overlap with the settings button
                bottom = MaterialTheme.paddings.small,
            )
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
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
@Preview(device = Devices.TV_1080p)
@Suppress("MaximumLineLength", "MaxLineLength")
private fun MediaMetadataPreview() {
    PillarboxTheme {
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle("Title")
            .setDescription("Description")
            .setArtworkUri(Uri.parse("https://cdn.prod.swi-services.ch/video-delivery/images/14e4562f-725d-4e41-a200-7fcaa77df2fe/5rwf1Bq_m3GC5secOZcIcgbbrbZPf4nI/16x9)"))
            .build()

        MediaMetadataView(
            mediaMetadata = mediaMetadata,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        )
    }
}
