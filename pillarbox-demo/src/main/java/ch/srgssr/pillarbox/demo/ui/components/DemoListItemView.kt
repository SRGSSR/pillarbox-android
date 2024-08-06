/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Demo item view.
 *
 * @param title The title of the item.
 * @param modifier The [Modifier] to apply to the root of the item.
 * @param subtitle The optional subtitle of the item.
 * @param onClick The action to perform when an item is clicked.
 */
@Composable
fun DemoListItemView(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .minimumInteractiveComponentSize()
            .padding(
                horizontal = MaterialTheme.paddings.baseline,
                vertical = MaterialTheme.paddings.small
            )
    ) {
        Text(
            text = title,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                modifier = Modifier.padding(top = MaterialTheme.paddings.micro),
                color = MaterialTheme.colorScheme.outline,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Demo item view.
 *
 * @param leadingText The leading text of the item.
 * @param trailingText The trailing text of the item.
 * @param modifier The [Modifier] to apply to the root of the item.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun DemoListItemView(
    leadingText: String,
    trailingText: String,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = modifier
            .combinedClickable(
                onLongClick = { clipboardManager.setText(AnnotatedString(trailingText)) },
                onClick = {},
            )
            .padding(
                horizontal = MaterialTheme.paddings.baseline,
                vertical = MaterialTheme.paddings.small
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = leadingText,
            modifier = Modifier
                .padding(end = MaterialTheme.paddings.baseline)
                .align(Alignment.Top),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = trailingText,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun DemoItemTitleSubtitlePreview() {
    PillarboxTheme {
        Column {
            val itemModifier = Modifier.fillMaxWidth()

            DemoListItemView(
                modifier = itemModifier,
                title = "Title 1",
                subtitle = "Description 1",
                onClick = {},
            )

            DemoListItemView(
                modifier = itemModifier,
                title = "Title 2",
                onClick = {},
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun DemoItemLeadingTrailingPreview() {
    PillarboxTheme {
        DemoListItemView(
            leadingText = "Title 1",
            trailingText = "Description 1",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
