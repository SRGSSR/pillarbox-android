/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesAll
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * A dialog allowing the user to add items to the current playlist.
 *
 * @param items The items to display.
 * @param modifier The [Modifier] to apply to the root of the layout.
 * @param onAddClick The action to perform when the "Add" button is clicked.
 * @param onDismissRequest The action to perform when the dialog is dismissed/"Cancel" is clicked.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MediaItemLibraryDialog(
    items: List<DemoItem>,
    modifier: Modifier = Modifier,
    onAddClick: (items: List<DemoItem>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val selectedItems = remember { mutableStateListOf<DemoItem>() }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onAddClick(selectedItems)
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(android.R.string.ok))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = { selectedItems.addAll(items) }) {
                Text(text = stringResource(android.R.string.selectAll))
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.add_to_playlist))

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                    )
                }
            }
        },
        text = {
            ItemList(
                items = items,
                selectedItems = selectedItems,
                onItemClick = { item, checked ->
                    if (checked) {
                        selectedItems.add(item)
                    } else {
                        selectedItems.remove(item)
                    }
                },
            )
        },
    )
}

@Composable
private fun ItemList(
    items: List<DemoItem>,
    selectedItems: List<DemoItem>,
    modifier: Modifier = Modifier,
    onItemClick: (item: DemoItem, checked: Boolean) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        items(items) { item ->
            val checked by remember(item) {
                derivedStateOf { item in selectedItems }
            }

            ListItem(
                headlineContent = {
                    Text(
                        text = item.title ?: "No title",
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                    )
                },
                modifier = Modifier.clickable {
                    onItemClick(item, !checked)
                },
                leadingContent = {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = null,
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = AlertDialogDefaults.containerColor,
                    headlineColor = AlertDialogDefaults.textContentColor,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun MediaItemLibraryDialogPreview() {
    val items = SamplesAll.playlist.items

    PillarboxTheme {
        MediaItemLibraryDialog(
            items = items,
            onAddClick = {},
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ItemListPreview() {
    val items = SamplesAll.playlist.items.take(10)
    val selectedItems = remember { mutableStateListOf(items[0], items[3], items[4], items[8]) }

    PillarboxTheme {
        ItemList(
            items = items,
            selectedItems = selectedItems,
            onItemClick = { item, checked ->
                if (checked) {
                    selectedItems.add(item)
                } else {
                    selectedItems.remove(item)
                }
            },
        )
    }
}
