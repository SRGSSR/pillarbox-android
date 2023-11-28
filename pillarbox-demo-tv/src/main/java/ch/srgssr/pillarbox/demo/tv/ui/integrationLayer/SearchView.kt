/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.integrationLayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.FilterChip
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.SearchViewModel
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.bus
import ch.srgssr.pillarbox.demo.tv.player.PlayerActivity
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme

/**
 * Display the list of search results.
 *
 * @param searchViewModel The [SearchViewModel] used to perform the search.
 * @param modifier The [Modifier] to apply to the list.
 */
@Composable
fun SearchView(
    searchViewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val query by searchViewModel.query.collectAsState()
    val selectedBu by searchViewModel.bu.collectAsState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SearchRow(
            query = query,
            bus = bus,
            selectedBu = selectedBu,
            modifier = Modifier.fillMaxWidth(),
            onQueryChange = searchViewModel::setQuery,
            onBuChange = searchViewModel::selectBu
        )

        ListsSection(
            items = searchViewModel.result.collectAsLazyPagingItems(),
            focusFirstItem = false,
            scaleImageUrl = { imageUrl, containerWidth ->
                searchViewModel.getScaledImageUrl(imageUrl, containerWidth)
            },
            onItemClick = { item ->
                val demoItem = DemoItem(
                    title = item.title,
                    uri = item.urn,
                    description = item.description,
                    imageUrl = item.imageUrl
                )

                PlayerActivity.startPlayer(context, demoItem)
            },
            emptyScreen = { emptyScreenModifier ->
                if (searchViewModel.hasValidSearchQuery()) {
                    NoResults(modifier = emptyScreenModifier.fillMaxSize())
                } else {
                    NoContent(emptyScreenModifier.fillMaxSize())
                }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun SearchRow(
    query: String,
    bus: List<Bu>,
    selectedBu: Bu,
    modifier: Modifier = Modifier,
    onQueryChange: (query: String) -> Unit,
    onBuChange: (bu: Bu) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SearchInput(
            query = query,
            modifier = Modifier.fillMaxWidth(),
            onQueryChange = onQueryChange
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            bus.forEach { bu ->
                FilterChip(
                    selected = bu == selectedBu,
                    onClick = { onBuChange(bu) }
                ) {
                    Text(text = bu.name.uppercase())
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun SearchInput(
    query: String,
    modifier: Modifier = Modifier,
    onQueryChange: (query: String) -> Unit
) {
    val focusRequest = remember { FocusRequester() }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .focusRequester(focusRequest)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ),
        textStyle = MaterialTheme.typography.titleSmall
            .copy(color = MaterialTheme.colorScheme.onSurface),
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            imeAction = ImeAction.Search
        ),
        singleLine = true,
        cursorBrush = Brush.verticalGradient(
            colors = listOf(LocalContentColor.current, LocalContentColor.current)
        ),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.padding(16.dp)) {
                innerTextField()

                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        modifier = Modifier.graphicsLayer { alpha = 0.6f },
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        focusRequest.requestFocus()
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun NoResults(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.no_results))
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun NoContent(
    modifier: Modifier = Modifier
) {
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
@Preview(showBackground = true)
private fun SearchRowPreview() {
    PillarboxTheme {
        SearchRow(
            query = "Query",
            bus = bus,
            selectedBu = Bu.RTS,
            onQueryChange = {},
            onBuChange = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun SearchInputPreview() {
    PillarboxTheme {
        SearchInput(
            query = "Query",
            onQueryChange = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun NoResultsPreview() {
    PillarboxTheme {
        NoResults()
    }
}

@Composable
@Preview(showBackground = true)
private fun NoContentPreview() {
    PillarboxTheme {
        NoContent()
    }
}
