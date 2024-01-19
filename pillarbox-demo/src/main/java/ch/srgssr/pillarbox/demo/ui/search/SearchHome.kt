/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.SearchViewModel
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.bus
import ch.srgssr.pillarbox.demo.ui.components.ContentView
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.demo.shared.R as sharedR

/**
 * Search home page.
 *
 * @param searchViewModel The [SearchViewModel] attached to this composable.
 * @param onSearchClicked The [Content.Media] clicked from this view.
 * @receiver
 */
@Composable
fun SearchHome(
    searchViewModel: SearchViewModel,
    onSearchClicked: (media: Content.Media) -> Unit
) {
    val lazyItems = searchViewModel.result.collectAsLazyPagingItems()
    val currentBu by searchViewModel.bu.collectAsState()
    val searchQuery by searchViewModel.query.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.paddings.baseline),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline)
    ) {
        SearchInput(
            query = searchQuery,
            bus = bus,
            selectedBu = currentBu,
            modifier = Modifier.fillMaxWidth(),
            onBuChange = searchViewModel::selectBu,
            onClearClick = searchViewModel::clear,
            onQueryChange = searchViewModel::setQuery
        )

        SearchResultList(
            searchViewModel = searchViewModel,
            items = lazyItems,
            contentClick = onSearchClicked
        )
    }
}

@Composable
private fun SearchResultList(
    searchViewModel: SearchViewModel,
    items: LazyPagingItems<Content.Media>,
    contentClick: (Content.Media) -> Unit,
    modifier: Modifier = Modifier
) {
    when (val loadState = items.loadState.refresh) {
        is LoadState.Loading -> LoadingView(modifier = modifier.fillMaxSize())

        is LoadState.NotLoading -> {
            if (items.itemCount == 0) {
                if (searchViewModel.hasValidSearchQuery()) {
                    NoResult(modifier = modifier.fillMaxSize())
                } else {
                    NoContent(modifier = modifier.fillMaxSize())
                }
            } else {
                LazyColumn(modifier = modifier) {
                    items(
                        count = items.itemCount,
                        key = items.itemKey()
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
                                    .padding(MaterialTheme.paddings.baseline)
                            )
                        }
                    }
                }
            }
        }

        is LoadState.Error -> ErrorView(
            error = loadState.error,
            modifier = modifier.fillMaxSize()
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchInput(
    query: String,
    bus: List<Bu>,
    selectedBu: Bu,
    modifier: Modifier = Modifier,
    onBuChange: (bu: Bu) -> Unit,
    onClearClick: () -> Unit,
    onQueryChange: (query: String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = {},
        active = false,
        onActiveChange = {},
        modifier = modifier.focusRequester(focusRequester),
        placeholder = { Text(text = stringResource(sharedR.string.search_placeholder)) },
        leadingIcon = {
            var showBuSelector by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .padding(end = MaterialTheme.paddings.small)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showBuSelector = true
                    }
                    .fillMaxHeight()
                    .padding(
                        start = MaterialTheme.paddings.baseline,
                        end = MaterialTheme.paddings.small
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconRotation by animateFloatAsState(
                    targetValue = if (showBuSelector) -180f else 0f,
                    label = "icon_rotation_animation"
                )

                Text(text = selectedBu.name.uppercase())

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(iconRotation)
                )
            }

            DropdownMenu(
                expanded = showBuSelector,
                onDismissRequest = { showBuSelector = false },
                offset = DpOffset(
                    x = 0.dp,
                    y = MaterialTheme.paddings.small
                )
            ) {
                bus.forEach { bu ->
                    DropdownMenuItem(
                        text = { Text(text = bu.name.uppercase()) },
                        onClick = {
                            onBuChange(bu)
                            showBuSelector = false
                        },
                        trailingIcon = if (selectedBu == bu) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotBlank(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear)
                    )
                }
            }
        },
        shape = MaterialTheme.shapes.large
    ) {}

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun NoContent(modifier: Modifier = Modifier) {
    StateMessage(modifier = modifier, message = stringResource(sharedR.string.empty_search_query), image = Icons.Default.Search)
}

@Composable
private fun NoResult(modifier: Modifier = Modifier) {
    StateMessage(modifier = modifier, message = stringResource(sharedR.string.no_results), image = Icons.Default.Block)
}

@Composable
private fun StateMessage(modifier: Modifier, message: String, image: ImageVector) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = image,
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = message,
            modifier = Modifier.padding(top = MaterialTheme.paddings.small)
        )
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(error: Throwable, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error.localizedMessage ?: error.message ?: "Error",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun SearchInputPreview() {
    PillarboxTheme {
        SearchInput(
            query = "Query",
            bus = bus,
            selectedBu = Bu.RTS,
            onBuChange = {},
            onClearClick = {},
            onQueryChange = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun NoContentPreview() {
    PillarboxTheme {
        NoContent()
    }
}

@Composable
@Preview(showBackground = true)
private fun NoResultPreview() {
    PillarboxTheme {
        NoResult()
    }
}

@Composable
@Preview(showBackground = true)
private fun LoadingViewPreview() {
    PillarboxTheme {
        LoadingView()
    }
}

@Composable
@Preview(showBackground = true)
private fun ErrorViewPreview() {
    PillarboxTheme {
        val error = IllegalStateException("Unable to load content")

        ErrorView(error)
    }
}
