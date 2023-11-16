/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content

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
    val currentBu by searchViewModel.bu.collectAsState()
    val searchQuery by searchViewModel.query.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val hasNoContent = lazyItems.itemCount == 0 &&
        lazyItems.loadState.refresh is LoadState.NotLoading &&
        !searchViewModel.hasValidSearchQuery()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var showBuSelector by remember { mutableStateOf(true) }

        AnimatedVisibility(visible = showBuSelector) {
            BuSelector(
                listBu = bus,
                selectedBu = currentBu,
                onBuSelected = searchViewModel::selectBu
            )
        }

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .focusRequester(focusRequester),
            trailingIcon = {
                IconButton(onClick = searchViewModel::clear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search"
                    )
                }
            },
            prefix = if (showBuSelector) {
                null
            } else {
                {
                    Text(text = "[${currentBu.name.uppercase()}] ")
                }
            },
            singleLine = true,
            maxLines = 1,
            placeholder = { Text(text = "Search") },
            value = searchQuery,
            onValueChange = searchViewModel::setQuery
        )
        if (hasNoContent) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No content")
            }
        }
        SearchResultList(
            lazyPagingItems = lazyItems,
            contentClick = onSearchClicked,
            onScroll = { showBuSelector = it },
            searchViewModel = searchViewModel,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun SearchResultList(
    searchViewModel: SearchViewModel,
    lazyPagingItems: LazyPagingItems<Content.Media>,
    contentClick: (Content.Media) -> Unit,
    onScroll: (scrollUp: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasNoResult = lazyPagingItems.itemCount == 0 &&
        lazyPagingItems.loadState.refresh is LoadState.NotLoading &&
        searchViewModel.hasValidSearchQuery()
    val scrollState = rememberLazyListState()
    val isScrollingUp = scrollState.isScrollingUp()

    LaunchedEffect(isScrollingUp) {
        onScroll(isScrollingUp)
    }

    LazyColumn(
        modifier = modifier,
        state = scrollState
    ) {
        items(count = lazyPagingItems.itemCount, key = lazyPagingItems.itemKey()) { index ->
            val item = lazyPagingItems[index]
            item?.let { mediaResult ->
                ContentView(
                    content = mediaResult,
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clickable { contentClick(mediaResult) }
                )
            }
        }
        if (hasNoResult) {
            item {
                NoResult(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
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

// Snippet from a Google Codelab:
// https://github.com/android/codelab-android-compose/blob/main/AnimationCodelab/finished/src/main/java/com/example/android/codelab/animation/ui/home/Home.kt#L339
@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }

    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BuSelector(listBu: List<Bu>, selectedBu: Bu, onBuSelected: (Bu) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(listBu) { bu ->
            FilterChip(
                selected = bu == selectedBu,
                onClick = { onBuSelected(bu) },
                label = { Text(text = bu.name.uppercase()) },
            )
        }
    }
}

@Composable
private fun NoResult(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No result")
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
