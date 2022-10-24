/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.streams

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.data.ItemType
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Stream home view
 *
 * Display all the [DemoItem] in a List. Each item is clickable and mapped to [onItemClicked]
 *
 * @param demoListViewModel load demo item list
 * @param onItemClicked handler for the item click
 */
@Composable
fun StreamHome(demoListViewModel: DemoListViewModel, onItemClicked: (DemoItem) -> Unit) {
    val listItems = demoListViewModel.listDemoItem.collectAsState()
    ListStreamView(itemList = listItems.value, onItemClicked = onItemClicked)
}

@Composable
private fun ListStreamView(itemList: List<DemoItem>, onItemClicked: (DemoItem) -> Unit) {
    LazyColumn {
        items(itemList) { item ->
            DemoItemView(item = item, onItemClicked = onItemClicked)
            Divider()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListStreamPreview() {
    val context = LocalContext.current
    val listItems = listOf(
        DemoItem(ItemType.MEDIA, "id", "Title 1", "Description 1"),
        DemoItem(ItemType.MEDIA, "id", "Title 2", "Description 2"),
        DemoItem(ItemType.MEDIA, "id", "Title 3", "Description 3"),
    )
    PillarboxTheme {
        ListStreamView(itemList = listItems) {
            Toast.makeText(context, "${it.title} clicked", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun DemoItemView(item: DemoItem, onItemClicked: (DemoItem) -> Unit) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onItemClicked(item) }
            .wrapContentHeight()
            .fillMaxWidth()
            .defaultMinSize(minHeight = 40.dp)
    ) {
        Text(text = item.title, style = MaterialTheme.typography.body1)
        item.description?.let {
            Text(text = item.description, style = MaterialTheme.typography.caption)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoItemPreview() {
    val context = LocalContext.current
    val demoItem = DemoItem(title = "The title of the media", description = "Description of the media", id = "id", type = ItemType.MEDIA)
    PillarboxTheme {
        DemoItemView(item = demoItem) {
            Toast.makeText(context, "${it.title} clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
