/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Media item library dialog
 *
 * @param mediaItemLibrary the multiple choice list
 * @param onItemSelected when dialog is validated
 * @param onDismissRequest when dialog is dismissed
 */
@Composable
fun MediaItemLibraryDialog(
    mediaItemLibrary: List<DemoItem>,
    onItemSelected: (List<DemoItem>) -> Unit,
    onDismissRequest: () -> Unit
) {
    val selectedItems = remember {
        mutableStateListOf<DemoItem>()
    }
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(6.dp)
        ) {
            DialogContent(
                selectedItems = selectedItems,
                listDemoItem = mediaItemLibrary,
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .padding(MaterialTheme.paddings.baseline),
                onAddClick = {
                    onItemSelected(selectedItems)
                },
                onCancelClick = onDismissRequest,
                onItemToggleClick = { item: DemoItem, select: Boolean ->
                    if (select) {
                        selectedItems.add(item)
                    } else {
                        selectedItems.remove(item)
                    }
                }
            )
        }
    }
}

@Composable
private fun DialogContent(
    listDemoItem: List<DemoItem>,
    selectedItems: List<DemoItem>,
    onItemToggleClick: (DemoItem, Boolean) -> Unit,
    onCancelClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small)
    ) {
        Text(
            text = "Add to the playlist",
            style = MaterialTheme.typography.headlineMedium
        )
        Divider()
        LazyColumn(
            modifier = Modifier
                .weight(0.5f)
                .padding(horizontal = MaterialTheme.paddings.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline)
        ) {
            items(listDemoItem) {
                val selected = selectedItems.contains(it)
                SelectableDemoItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onItemToggleClick(it, !selected)
                        },
                    demoItem = it, selected = selectedItems.contains(it)
                )
            }
        }
        Divider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onCancelClick) {
                Text(text = "Cancel", overflow = TextOverflow.Ellipsis)
            }
            Button(onClick = onAddClick) {
                Text(text = "Add", overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SelectableDemoItem(modifier: Modifier, demoItem: DemoItem, selected: Boolean) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.mini),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = demoItem.title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
private fun MediaItemLibraryPreview() {
    val list = Playlist.All.items
    val selectedItems = listOf(list[0], list[3])
    PillarboxTheme {
        DialogContent(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .padding(MaterialTheme.paddings.baseline),
            listDemoItem = list,
            selectedItems = selectedItems,
            onItemToggleClick = { _, _ -> },
            onCancelClick = { },
            onAddClick = { }
        )
    }
}
