/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * A dialog allowing the user to add items to the current playlist.
 *
 * @param items The items to display.
 * @param modifier The [Modifier] to apply to the root of the layout.
 * @param onAddClick The action to perform when the "Add" button is clicked.
 * @param onDismissRequest The action to perform when the dialog is dimissed/"Cancel" is clicked.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MediaItemLibraryDialog(
    items: List<DemoItem>,
    modifier: Modifier = Modifier,
    onAddClick: (items: List<DemoItem>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val selectedItems = remember {
        mutableStateListOf<DemoItem>()
    }

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column {
                Text(
                    text = "Add to the playlist",
                    modifier = Modifier.padding(MaterialTheme.paddings.baseline),
                    color = AlertDialogDefaults.titleContentColor,
                    style = MaterialTheme.typography.headlineSmall,
                )

                ItemList(
                    items = items,
                    selectedItems = selectedItems,
                    modifier = Modifier.weight(1f),
                    onItemClick = { item, checked ->
                        if (checked) {
                            selectedItems.add(item)
                        } else {
                            selectedItems.remove(item)
                        }
                    }
                )

                ButtonsRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.paddings.small),
                    onAddClick = {
                        onAddClick(selectedItems)
                        onDismissRequest()
                    },
                    onCancelClick = onDismissRequest,
                )
            }
        }
    }
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
                derivedStateOf {
                    item in selectedItems
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item, !checked) }
                    .padding(
                        horizontal = MaterialTheme.paddings.baseline,
                        vertical = MaterialTheme.paddings.small,
                    ),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = null,
                )

                Text(
                    text = item.title,
                    color = AlertDialogDefaults.textContentColor,
                )
            }
        }
    }
}

@Composable
private fun ButtonsRow(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(onClick = onCancelClick) {
            Text("Cancel")
        }

        TextButton(onClick = onAddClick) {
            Text(text = "Add")
        }
    }
}

@Preview
@Composable
private fun MediaItemLibraryDialogPreview() {
    val items = Playlist.All.items

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
    val items = Playlist.All.items.take(10)

    PillarboxTheme {
        ItemList(
            items = items,
            selectedItems = listOf(items[0], items[3], items[4], items[8]),
            onItemClick = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun ButtonsRowPrewiew() {
    PillarboxTheme {
        ButtonsRow(
            modifier = Modifier.fillMaxWidth(),
            onAddClick = {},
            onCancelClick = {},
        )
    }
}
