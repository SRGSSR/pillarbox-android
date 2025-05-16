/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player.compose.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerScope
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings

@Composable
internal fun DrawerContent(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    listContent: LazyListScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .padding(horizontal = MaterialTheme.paddings.baseline)
            .padding(top = MaterialTheme.paddings.baseline),
    ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium) {
            title()
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = MaterialTheme.paddings.baseline),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
        ) {
            listContent()
        }
    }
}

@Composable
internal fun <T> NavigationDrawerScope.DrawerContent(
    title: @Composable () -> Unit,
    items: List<T>,
    isItemSelected: (index: Int, item: T) -> Boolean,
    modifier: Modifier = Modifier,
    onItemClick: (index: Int, item: T) -> Unit,
    leadingContent: @Composable (item: T) -> Unit,
    supportingContent: @Composable (item: T) -> Unit = {},
    content: @Composable (item: T) -> Unit,
) {
    DrawerContent(
        title = title,
        modifier = modifier,
        listContent = {
            itemsIndexed(items) { index, item ->
                NavigationDrawerItem(
                    selected = isItemSelected(index, item),
                    onClick = { onItemClick(index, item) },
                    leadingContent = { leadingContent(item) },
                    supportingContent = { supportingContent(item) },
                    content = { content(item) },
                )
            }
        },
    )
}
