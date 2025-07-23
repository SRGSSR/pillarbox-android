/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler

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
    Row(
        modifier = modifier
            .padding(MaterialTheme.paddings.baseline)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.large,
            )
            .padding(MaterialTheme.paddings.small)
            .animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            if (mediaMetadata.artworkUri != null) {
                AsyncImage(
                    model = mediaMetadata.artworkUri,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .width(150.dp),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small)) {
                mediaMetadata.title?.let { title ->
                    Text(
                        text = title.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                mediaMetadata.description?.let { description ->
                    Text(
                        text = description.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalCoilApi::class)
private fun MediaMetadataPreview() {
    PillarboxTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(
                color = Color.Red.toArgb(),
                width = 800,
                height = 600,
            )
        }
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle("Title")
            .setDescription("Description")
            .setArtworkUri("https://image.url/".toUri())
            .build()

        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            MediaMetadataView(
                mediaMetadata = mediaMetadata,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
