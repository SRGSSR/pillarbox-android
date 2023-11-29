/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.srg.dataProvider.integrationlayer.data.ImageUrl
import ch.srg.dataProvider.integrationlayer.data.remote.Media
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.Show
import ch.srg.dataProvider.integrationlayer.data.remote.Topic
import ch.srg.dataProvider.integrationlayer.data.remote.Transmission
import ch.srg.dataProvider.integrationlayer.data.remote.Type
import ch.srg.dataProvider.integrationlayer.data.remote.Vendor
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.ui.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import java.util.Date
import kotlin.time.Duration.Companion.seconds

/**
 * Content view.
 *
 * @param content The content to display.
 * @param modifier The [Modifier] to apply to the component.
 * @param onClick The action to perform when clicking the component.
 */
@Composable
fun ContentView(
    content: Content,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    when (content) {
        is Content.Topic -> DemoListItemView(
            title = content.title,
            modifier = modifier.fillMaxWidth(),
            onClick = onClick
        )

        is Content.Show -> DemoListItemView(
            title = content.title,
            modifier = modifier.fillMaxWidth(),
            onClick = onClick
        )

        is Content.Media -> MediaView(
            content = content,
            modifier = modifier.fillMaxWidth(),
            onClick = onClick
        )

        is Content.Channel -> DemoListItemView(
            title = content.title,
            modifier = modifier.fillMaxWidth(),
            subtitle = content.description,
            onClick = onClick
        )
    }
}

@Composable
private fun MediaView(
    content: Content.Media,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val mediaTypeIcon = when (content.mediaType) {
        MediaType.AUDIO -> "🎧"
        MediaType.VIDEO -> "🎬"
    }
    val subtitlePrefix = if (content.showTitle != null) {
        "${content.showTitle} - "
    } else {
        ""
    }
    val duration = stringResource(R.string.duration, content.duration)

    DemoListItemView(
        title = content.title,
        modifier = modifier,
        subtitle = "$mediaTypeIcon $subtitlePrefix ${content.date} - $duration",
        onClick = onClick
    )
}

@Composable
@Preview(showBackground = true)
private fun ShowPreview() {
    val show = Show(
        id = "id",
        vendor = Vendor.RTR,
        urn = "urn:show:id",
        title = "Show Title",
        description = "Show description",
        transmission = Transmission.TV,
        imageUrl = ImageUrl("https://image1.png")
    )

    PillarboxTheme {
        ContentView(
            content = Content.Show(show),
            onClick = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun TopicPreview() {
    val topic = Topic(
        id = "id",
        vendor = Vendor.RTR,
        urn = "urn:show:id",
        title = "Topic title",
        transmission = Transmission.TV,
        imageUrl = ImageUrl("https://image2.png")
    )

    PillarboxTheme {
        ContentView(
            content = Content.Topic(topic),
            onClick = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun MediaPreview() {
    val media = Media(
        id = "id",
        vendor = Vendor.RTR,
        urn = "urn:media:id",
        title = "Media title",
        description = "Media description",
        date = Date(),
        duration = 30.seconds.inWholeMilliseconds,
        mediaType = MediaType.VIDEO,
        playableAbroad = true,
        type = Type.CLIP,
        imageUrl = ImageUrl("https://image2.png")
    )

    PillarboxTheme {
        ContentView(
            content = Content.Media(media),
            onClick = {}
        )
    }
}
