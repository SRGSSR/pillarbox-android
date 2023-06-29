/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.Content

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
    Box(modifier = modifier) {
        Text(text = content.topic.title)
    }
}

@Composable
private fun MediaView(content: Content.Media, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(text = content.media.title)
    }
}

@Composable
private fun ShowView(content: Content.Show, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(text = content.show.title)
    }
}
