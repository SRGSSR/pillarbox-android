/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.srg.dataProvider.integrationlayer.data.ImageUrl
import ch.srg.dataProvider.integrationlayer.data.remote.Media
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.Show
import ch.srg.dataProvider.integrationlayer.data.remote.Topic
import ch.srg.dataProvider.integrationlayer.data.remote.Transmission
import ch.srg.dataProvider.integrationlayer.data.remote.Type
import ch.srg.dataProvider.integrationlayer.data.remote.Vendor
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

/**
 * Content view
 *
 * @param content
 * @param modifier
 */
@Composable
fun ContentView(content: Content, modifier: Modifier = Modifier) {
    when (content) {
        is Content.Topic -> TopicView(content = content, modifier = modifier)
        is Content.Show -> ShowView(content = content, modifier = modifier)
        is Content.Media -> MediaView(content = content, modifier = modifier)
    }
}

@Composable
private fun TopicView(content: Content.Topic, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Text(modifier = Modifier.padding(8.dp), text = content.topic.title)
    }
}

@Composable
private fun MediaView(content: Content.Media, modifier: Modifier = Modifier) {
    val simpleDateFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
    val dateString = simpleDateFormat.format(content.media.date)
    val subtitle = content.media.show?.let {
        "${it.title} - $dateString"
    } ?: dateString

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = content.media.title, style = MaterialTheme.typography.body1)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.subtitle1,
            )
        }
    }
}

@Composable
private fun ShowView(content: Content.Show, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Text(modifier = Modifier.padding(8.dp), text = content.show.title)
    }
}

@Preview
@Composable
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
    PillarboxTheme() {
        ShowView(modifier = Modifier.fillMaxWidth(), content = Content.Show(show))
    }
}

@Preview
@Composable
private fun TopicPreview() {
    val topic = Topic(
        id = "id",
        vendor = Vendor.RTR,
        urn = "urn:show:id",
        title = "Topic title",
        transmission = Transmission.TV,
        imageUrl = ImageUrl("https://imag2.png")
    )
    PillarboxTheme() {
        TopicView(modifier = Modifier.fillMaxWidth(), content = Content.Topic(topic))
    }
}

@Preview
@Composable
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
    PillarboxTheme() {
        MediaView(modifier = Modifier.fillMaxWidth(), content = Content.Media(media))
    }
}
