/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.data.Playlist
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Media item library dialog
 *
 * @param mediaItemLibrary the multiple choice list
 * @param onItemSelected when dialog is validated
 * @param onDismissRequest when dialog is dismissed
 * @receiver
 * @receiver
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
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .padding(8.dp)
            ) {
                Text(
                    modifier = Modifier,
                    text = "Add to the playlist",
                    style = MaterialTheme.typography.headlineMedium
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(0.5f)
                ) {
                    items(mediaItemLibrary) {
                        val selected = selectedItems.contains(it)
                        SelectableDemoItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selected) {
                                        selectedItems.remove(it)
                                    } else {
                                        selectedItems.add(it)
                                    }
                                },
                            demoItem = it, selected = selectedItems.contains(it)
                        )
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = onDismissRequest) {
                        Text(text = "Cancel", overflow = TextOverflow.Ellipsis)
                    }
                    Button(onClick = { onItemSelected(selectedItems) }) {
                        Text(text = "Add", overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableDemoItem(modifier: Modifier, demoItem: DemoItem, selected: Boolean) {
    Row(modifier) {
        RadioButton(selected = selected, onClick = null)
        Text(text = demoItem.title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
private fun MediaItemLibraryPreview() {
    PillarboxTheme {
        MediaItemLibraryDialog(mediaItemLibrary = Playlist.All.items, onItemSelected = {}) {
        }
    }
}
