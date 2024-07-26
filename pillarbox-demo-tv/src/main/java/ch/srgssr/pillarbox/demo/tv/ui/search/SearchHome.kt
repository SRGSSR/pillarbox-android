/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.search

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import ch.srg.dataProvider.integrationlayer.request.image.ImageWidth
import ch.srg.dataProvider.integrationlayer.request.image.decorated
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.shared.R
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.extension.onDpadEvent
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.SearchViewModel
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.bus
import ch.srgssr.pillarbox.demo.tv.ui.lists.ListsSection
import ch.srgssr.pillarbox.demo.tv.ui.player.PlayerActivity
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings

/**
 * Display the list of search results.
 *
 * @param searchViewModel The [SearchViewModel] used to perform the search.
 * @param modifier The [Modifier] to apply to the list.
 */
@Composable
fun SearchHome(
    searchViewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val query by searchViewModel.query.collectAsState()
    val selectedBu by searchViewModel.bu.collectAsState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline)
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
                    imageUrl = item.imageUrl.decorated(width = ImageWidth.W480)
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
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline)
    ) {
        SearchInput(
            query = query,
            modifier = Modifier
                .fillMaxWidth()
                .onDpadEvent(
                    onBack = {
                        focusManager.moveFocus(FocusDirection.Up)
                        true
                    }
                ),
            onQueryChange = onQueryChange
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline)
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
private fun SearchInput(
    query: String,
    modifier: Modifier = Modifier,
    onQueryChange: (query: String) -> Unit
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.background(
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
            Box(modifier = Modifier.padding(MaterialTheme.paddings.baseline)) {
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
}

@Composable
private fun NoResults(
    modifier: Modifier = Modifier
) {
    StateMessage(
        modifier = modifier,
        message = stringResource(R.string.no_results),
        image = Icons.Default.Block
    )
}

@Composable
private fun NoContent(
    modifier: Modifier = Modifier
) {
    StateMessage(
        modifier = modifier,
        message = stringResource(R.string.empty_search_query),
        image = Icons.Default.Search
    )
}

@Composable
private fun StateMessage(
    modifier: Modifier,
    message: String,
    image: ImageVector
) {
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
