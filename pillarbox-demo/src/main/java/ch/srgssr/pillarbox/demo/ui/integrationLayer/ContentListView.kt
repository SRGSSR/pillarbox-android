/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ch.srg.dataProvider.integrationlayer.data.ImageUrl
import ch.srg.dataProvider.integrationlayer.data.remote.Media
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.Show
import ch.srg.dataProvider.integrationlayer.data.remote.Topic
import ch.srg.dataProvider.integrationlayer.data.remote.Transmission
import ch.srg.dataProvider.integrationlayer.data.remote.Type
import ch.srg.dataProvider.integrationlayer.data.remote.Vendor
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import kotlin.time.Duration.Companion.seconds

/**
 * Display a paged list of [Content].
 *
 * @param items The list of items to display.
 * @param modifier The [Modifier] to apply to the root of the list.
 * @param contentClick The action to perform when clicking on an item.
 */
@Composable
fun ContentListView(
    items: LazyPagingItems<Content>,
    modifier: Modifier = Modifier,
    contentClick: (Content) -> Unit
) {
    when (val loadState = items.loadState.refresh) {
        is LoadState.Loading -> LoadingView(modifier = modifier)

        is LoadState.NotLoading -> if (items.itemCount == 0) {
            EmptyView(modifier = modifier)
        } else {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(count = items.itemCount, key = items.itemKey()) { index ->
                    items[index]?.let { item ->
                        ContentView(
                            content = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { contentClick(item) }
                        )
                    }
                }
            }
        }

        is LoadState.Error -> ErrorView(
            error = loadState.error,
            modifier = modifier
        )
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyView(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = "No content")
    }
}

@Composable
private fun ErrorView(error: Throwable, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = error.localizedMessage ?: error.message ?: "Error",
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentListViewLoadingPreview() {
    val items = PagingData.empty<Content>(
        sourceLoadStates = LoadStates(
            refresh = LoadState.Loading,
            prepend = LoadState.Loading,
            append = LoadState.Loading
        )
    )

    PillarboxTheme {
        ContentListView(
            items = flowOf(items).collectAsLazyPagingItems(),
            contentClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentListViewEmptyPreview() {
    val items = PagingData.empty<Content>(
        sourceLoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true)
        )
    )

    PillarboxTheme {
        ContentListView(
            items = flowOf(items).collectAsLazyPagingItems(),
            contentClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentListViewPreview() {
    // TODO
    val items = PagingData.from(
        listOf(
            Content.Media(
                Media(
                    id = "id",
                    vendor = Vendor.RTR,
                    urn = "urn:media:id",
                    title = "Media title",
                    description = "Media description",
                    date = Date(),
                    duration = 30.seconds.inWholeMilliseconds,
                    mediaType = MediaType.VIDEO,
                    playableAbroad = true,
                    type = Type.CLIP,
                    imageUrl = ImageUrl("https://image2.png")
                )
            ),
            Content.Show(
                Show(
                    id = "id",
                    vendor = Vendor.RTR,
                    urn = "urn:show:id",
                    title = "Show Title",
                    description = "Show description",
                    transmission = Transmission.TV,
                    imageUrl = ImageUrl("https://image1.png")
                )
            ),
            Content.Topic(
                Topic(
                    id = "id",
                    vendor = Vendor.RTR,
                    urn = "urn:show:id",
                    title = "Topic title",
                    transmission = Transmission.TV,
                    imageUrl = ImageUrl("https://imag2.png")
                )
            )
        ),
        sourceLoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true)
        )
    )

    PillarboxTheme {
        ContentListView(
            items = flowOf(items).collectAsLazyPagingItems(),
            contentClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentListViewErrorPreview() {
    val error = LoadState.Error(IllegalStateException("Unable to load content"))
    val items = PagingData.empty<Content>(
        sourceLoadStates = LoadStates(
            refresh = error,
            prepend = error,
            append = error
        )
    )

    PillarboxTheme {
        ContentListView(
            items = flowOf(items).collectAsLazyPagingItems(),
            contentClick = {}
        )
    }
}
