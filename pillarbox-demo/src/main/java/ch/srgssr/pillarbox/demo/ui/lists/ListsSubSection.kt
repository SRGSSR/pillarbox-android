/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
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
import ch.srgssr.pillarbox.demo.ui.components.ContentView
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import kotlin.time.Duration.Companion.seconds

/**
 * Display a paged list of [Content].
 *
 * @param title The title of the list.
 * @param items The list of items to display.
 * @param modifier The [Modifier] to apply to the root of the list.
 * @param contentClick The action to perform when clicking on an item.
 */
@Composable
fun ListsSubSection(
    title: String,
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
                contentPadding = PaddingValues(
                    start = MaterialTheme.paddings.baseline,
                    end = MaterialTheme.paddings.baseline,
                    bottom = MaterialTheme.paddings.baseline
                ),
            ) {
                item(contentType = "title") {
                    DemoListHeaderView(
                        title = title,
                        modifier = Modifier.padding(start = MaterialTheme.paddings.baseline)
                    )
                }

                items(
                    count = items.itemCount,
                    key = items.itemKey(),
                    contentType = { "item" },
                ) { index ->
                    items[index]?.let { item ->
                        val shape = when {
                            items.itemCount == 1 -> MaterialTheme.shapes.medium

                            index == 0 -> RoundedCornerShape(
                                topStart = MaterialTheme.paddings.baseline,
                                topEnd = MaterialTheme.paddings.baseline,
                            )

                            index == items.itemCount - 1 -> RoundedCornerShape(
                                bottomStart = MaterialTheme.paddings.baseline,
                                bottomEnd = MaterialTheme.paddings.baseline,
                            )

                            else -> RectangleShape
                        }

                        ContentView(
                            content = item,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = shape
                                )
                                .clip(shape),
                            onClick = { contentClick(item) }
                        )

                        if (index < items.itemCount - 1) {
                            Divider()
                        }
                    }
                }

                if (items.loadState.append is LoadState.Loading) {
                    item(contentType = "LoadingView") {
                        LoadingView(
                            modifier = modifier
                                .fillMaxSize()
                                .padding(top = MaterialTheme.paddings.baseline)
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
private fun LoadingViewPreview() {
    val items = PagingData.empty<Content>(
        sourceLoadStates = LoadStates(
            refresh = LoadState.Loading,
            prepend = LoadState.Loading,
            append = LoadState.Loading
        )
    )

    PillarboxTheme {
        ListsSubSection(
            title = "Title loading",
            items = flowOf(items).collectAsLazyPagingItems(),
            contentClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyViewPreview() {
    val items = PagingData.empty<Content>(
        sourceLoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true)
        )
    )

    PillarboxTheme {
        ListsSubSection(
            title = "Title empty",
            items = flowOf(items).collectAsLazyPagingItems(),
            contentClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ListsSubSectionPreview() {
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
        ListsSubSection(
            title = "Title content",
            items = flowOf(items).collectAsLazyPagingItems(),
            contentClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorViewPreview() {
    val error = LoadState.Error(IllegalStateException("Unable to load content"))
    val items = PagingData.empty<Content>(
        sourceLoadStates = LoadStates(
            refresh = error,
            prepend = error,
            append = error
        )
    )

    PillarboxTheme {
        ListsSubSection(
            title = "Title error",
            items = flowOf(items).collectAsLazyPagingItems(),
            contentClick = {}
        )
    }
}
