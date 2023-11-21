/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

private val bus = listOf(Bu.RSI, Bu.RTR, Bu.RTS, Bu.SRF, Bu.SWI)

/**
 * Search view
 *
 * @param searchViewModel The [SearchViewModel] attached to this composable.
 * @param onSearchClicked The [Content.Media] clicked from this view.
 * @receiver
 */
@Composable
fun SearchView(
    searchViewModel: SearchViewModel,
    onSearchClicked: (media: Content.Media) -> Unit
) {
    val lazyItems = searchViewModel.result.collectAsLazyPagingItems()
    val currentBu by searchViewModel.bu.collectAsState()
    val searchQuery by searchViewModel.query.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    NoContent(modifier = Modifier.fillMaxSize())
                }
            } else {
                LazyColumn(modifier = modifier) {
                    items(
                        count = items.itemCount,
                        key = items.itemKey()
                    ) { index ->
                        items[index]?.let { item ->
                            val shape = when (index) {
                                0 -> RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                )

                                items.itemCount - 1 -> RoundedCornerShape(
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp,
                                )

                                else -> RectangleShape
                            }

                            ContentView(
                                content = item,
                                modifier = Modifier.background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = shape
                                ),
                                onClick = { contentClick(item) }
                            )

                            if (index < items.itemCount - 1) {
                                Divider()
                            }
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

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.focusRequester(focusRequester),
        placeholder = { Text(text = stringResource(R.string.search_placeholder)) },
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
        prefix = {
            var showBuSelector by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showBuSelector = true
                    }
                    .padding(8.dp)
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
                onDismissRequest = { showBuSelector = false }
            ) {
                bus.forEach { bu ->
                    DropdownMenuItem(
                        text = { Text(text = bu.name.uppercase()) },
                        onClick = {
                            onBuChange(bu)
                            showBuSelector = false
                        }
                    )
                }
            }
        },
        singleLine = true,
        maxLines = 1
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun NoContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = stringResource(R.string.empty_search_query),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun NoResult(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.no_results))
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
private fun SearchInputWithPrefixPreview() {
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
