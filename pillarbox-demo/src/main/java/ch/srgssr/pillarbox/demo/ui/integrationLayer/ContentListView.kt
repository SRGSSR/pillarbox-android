/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.Content

/**
 * Display content list from [ContentListViewModel]
 *
 * @param contentListViewModel
 * @param modifier
 * @param contentClick
 * @receiver
 */
@Composable
fun ContentListView(
    contentListViewModel: ContentListViewModel,
    modifier: Modifier = Modifier,
    contentClick: (Content) -> Unit = {}
) {
    val lazyPagingItems: LazyPagingItems<Content> = contentListViewModel.data.collectAsLazyPagingItems()
    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
        items(count = lazyPagingItems.itemCount, key = lazyPagingItems.itemKey()) { index ->
            val item = lazyPagingItems[index]
            item?.let {
                ContentView(
                    content = it,
                    Modifier
                        .padding(bottom = 2.dp)
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clickable { contentClick(it) }
                )
            }
        }
        if (lazyPagingItems.loadState.refresh is LoadState.Error) {
            item {
                ErrorView(error = (lazyPagingItems.loadState.refresh as LoadState.Error).error)
            }
        }
        if (lazyPagingItems.loadState.refresh is LoadState.Loading) {
            item {
                LoadingView(
                    modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                )
            }
        }
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(error: Throwable, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = error.localizedMessage ?: error.message ?: "Error", style = MaterialTheme.typography.h2, color = MaterialTheme.colors.error)
    }
}
