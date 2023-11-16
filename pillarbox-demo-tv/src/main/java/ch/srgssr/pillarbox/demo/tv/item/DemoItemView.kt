/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardScale
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme

/**
 * Demo item view
 *
 * @param title
 * @param onClick
 * @param modifier
 * @param subtitle
 * @receiver
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DemoItemView(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        scale = CardScale.None
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                modifier = Modifier,
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
            )
            subtitle?.let {
                Text(
                    modifier = Modifier,
                    text = subtitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Preview
@Composable
private fun DemoItemPreview() {
    PillarboxTheme {
        Column(
            modifier = Modifier
                .width(400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val itemModifier = Modifier
                .fillMaxWidth()
            DemoItemView(modifier = itemModifier, title = "Title 1", subtitle = "Subtitle 1", onClick = { })
            DemoItemView(modifier = itemModifier, title = "Title 2", subtitle = null, onClick = {})
            DemoItemView(modifier = itemModifier, title = "Title 3", subtitle = "Subtitle 3", onClick = { })
        }
    }
}
