/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.streams

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ch.srgssr.pillarbox.demo.data.DemoItem
import ch.srgssr.pillarbox.demo.data.ItemType
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

@Composable
fun ListStreamView(list: List<DemoItem>) {
    for (item in list) {
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PillarboxTheme {
        ListStreamView(list = listOf(DemoItem(ItemType.MEDIA, "id", "Title")))
    }
}

@Composable
fun DemoItemView(item: DemoItem) {
}
