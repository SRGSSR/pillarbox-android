/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.item

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
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

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
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
            )
            subtitle?.let {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp),
                    text = subtitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Preview
@Composable
private fun DemoItemPreview() {
    MaterialTheme {
        Column(modifier = Modifier.width(400.dp)) {
            DemoItemView(title = "Title 1", subtitle = "Subtitle 1", onClick = { })
            DemoItemView(title = "Title 2", subtitle = null, onClick = {})
            DemoItemView(title = "Title 3", subtitle = "Subtitle 3", onClick = { })
        }
    }
}
