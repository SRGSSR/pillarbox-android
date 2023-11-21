/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ch.srg.dataProvider.integrationlayer.data.ImageUrl
import ch.srg.dataProvider.integrationlayer.data.remote.Media
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.Show
import ch.srg.dataProvider.integrationlayer.data.remote.Topic
import ch.srg.dataProvider.integrationlayer.data.remote.Transmission
import ch.srg.dataProvider.integrationlayer.data.remote.Type
import ch.srg.dataProvider.integrationlayer.data.remote.Vendor
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.ui.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import java.text.DateFormat
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
            title = content.topic.title,
            modifier = modifier.fillMaxWidth(),
            onClick = onClick
        )

        is Content.Show -> DemoListItemView(
            title = content.show.title,
            modifier = modifier.fillMaxWidth(),
            onClick = onClick
        )

        is Content.Media -> MediaView(
            content = content,
            modifier = modifier.fillMaxWidth(),
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
    val subtitleSuffix = when (content.media.mediaType) {
        MediaType.AUDIO -> "🎧"
        MediaType.VIDEO -> "🎬"
    }
    val showTitle = content.media.show?.title
    val dateString = DateFormat.getDateInstance().format(content.media.date)
    val subtitle = showTitle?.let { "$it - $dateString" } ?: dateString

    DemoListItemView(
        title = content.media.title,
        modifier = modifier,
        subtitle = "$subtitle $subtitleSuffix",
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
