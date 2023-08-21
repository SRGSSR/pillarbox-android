/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.Content

private val bus = listOf(Bu.RTS, Bu.SRF, Bu.RSI, Bu.RTR, Bu.SWI)

/**
 * Search view
 *
 * @param searchViewModel The [SearchViewModel] attached to this composable.
 * @param onSearchClicked The [Content.Media] clicked from this view.
 * @receiver
 */
@Composable
fun SearchView(searchViewModel: SearchViewModel, onSearchClicked: (Content.Media) -> Unit) {
    val lazyItems = searchViewModel.result.collectAsLazyPagingItems()
    val currentBu = searchViewModel.bu.collectAsState()
    val searchQuery = searchViewModel.query.collectAsState()
    var queryState by remember(searchQuery.value) {
        mutableStateOf(searchQuery.value)
    }
    LaunchedEffect(queryState) {
        searchViewModel.query.value = queryState
    }
    val focusRequester = remember { FocusRequester() }
    Column {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            trailingIcon = {
                IconButton(onClick = searchViewModel::clear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search"
                    )
                }
            },
            singleLine = true,
            maxLines = 1,
            placeholder = { Text(text = "Search") },
            value = queryState,
            onValueChange = { queryState = it }
        )
        if (lazyItems.itemCount == 0 && lazyItems.loadState.refresh is LoadState.NotLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No content")
            }
        }
        SearchResultList(
            lazyPagingItems = lazyItems,
            contentClick = onSearchClicked,
            currentBu = currentBu.value,
            buClicked = searchViewModel::selectBu
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun SearchResultList(
    lazyPagingItems: LazyPagingItems<SearchContent>,
    contentClick: (Content.Media) -> Unit,
    currentBu: Bu,
    buClicked: (Bu) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(count = lazyPagingItems.itemCount, key = lazyPagingItems.itemKey()) { index ->
            val item = lazyPagingItems[index]
            item?.let { searchContent ->
                when (searchContent) {
                    is SearchContent.MediaResult -> {
                        ContentView(
                            content = searchContent.media,
                            Modifier
                                .padding(bottom = 2.dp)
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .clickable { contentClick(searchContent.media) }
                        )
                    }

                    is SearchContent.BuSelector -> {
                        BuSelector(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            listBu = bus, selectedBu = currentBu, onBuSelected = buClicked
                        )
                    }
                }
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
private fun BuSelector(listBu: List<Bu>, selectedBu: Bu, onBuSelected: (Bu) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (bu in listBu) {
            Button(onClick = { onBuSelected(bu) }, enabled = bu != selectedBu) {
                Text(text = bu.name.uppercase())
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
        Text(
            text = error.localizedMessage ?: error.message ?: "Error",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}
