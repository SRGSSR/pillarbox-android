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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu.Companion.RSI
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu.Companion.RTR
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu.Companion.RTS
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu.Companion.SRF
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu.Companion.SWI
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
    val focusRequester = remember { FocusRequester() }
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
            focusRequester = focusRequester,
            modifier = Modifier.fillMaxWidth(),
            onBuChange = searchViewModel::selectBu,
            onClearClick = searchViewModel::clear,
            onQueryChange = searchViewModel::setQuery
        )

        SearchResultList(
            searchViewModel = searchViewModel,
            items = lazyItems,
            focusRequester = focusRequester,
            currentBu = currentBu,
            contentClick = onSearchClicked
        )
    }
}

@Composable
private fun SearchResultList(
    searchViewModel: SearchViewModel,
    items: LazyPagingItems<Content.Media>,
    focusRequester: FocusRequester,
    currentBu: Bu,
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
                    val softwareKeyboardController = LocalSoftwareKeyboardController.current

                    NoContent(
                        modifier = modifier
                            .fillMaxSize()
                            .semantics {
                                onClick {
                                    focusRequester.requestFocus()
                                    softwareKeyboardController?.show()
                                    true
                                }
                            },
                    )
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
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shape = shape
                                    )
                                    .clip(shape),
                                languageTag = currentBu.languageTag,
                                onClick = { contentClick(item) }
                            )

                            if (index < items.itemCount - 1) {
                                HorizontalDivider()
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
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onBuChange: (bu: Bu) -> Unit,
    onClearClick: () -> Unit,
    onQueryChange: (query: String) -> Unit
) {
    SearchBar(
        inputField = {
            val softwareKeyboardController = LocalSoftwareKeyboardController.current

            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        onClick {
                            focusRequester.requestFocus()
                            softwareKeyboardController?.show()
                            true
                        }
                    },
                placeholder = { Text(text = stringResource(sharedR.string.search_placeholder)) },
                leadingIcon = {
                    var showBuSelector by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .padding(end = MaterialTheme.paddings.small)
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClickLabel = stringResource(sharedR.string.change_bu),
                            ) {
                                showBuSelector = true
                            }
                            .padding(
                                start = MaterialTheme.paddings.baseline,
                                end = MaterialTheme.paddings.small
                            ),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconScaleY by animateFloatAsState(
                            targetValue = if (showBuSelector) -1f else 1f,
                            label = "icon_scale_animation",
                        )

                        BuLabel(selectedBu)

                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.scale(scaleX = 1f, scaleY = iconScaleY),
                        )
                    }

                    DropdownMenu(
                        expanded = showBuSelector,
                        onDismissRequest = { showBuSelector = false },
                        modifier = Modifier.semantics {
                            collectionInfo = CollectionInfo(rowCount = bus.size, columnCount = 1)
                        },
                        offset = DpOffset(
                            x = 0.dp,
                            y = MaterialTheme.paddings.small
                        )
                    ) {
                        bus.forEachIndexed { index, bu ->
                            val isSelected = selectedBu == bu

                            DropdownMenuItem(
                                text = { BuLabel(bu) },
                                onClick = {
                                    onBuChange(bu)
                                    showBuSelector = false
                                },
                                modifier = Modifier.semantics {
                                    selected = isSelected
                                    collectionItemInfo = CollectionItemInfo(
                                        rowIndex = index,
                                        rowSpan = 1,
                                        columnIndex = 1,
                                        columnSpan = 1,
                                    )
                                },
                                trailingIcon = if (isSelected) {
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
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier.focusRequester(focusRequester),
        shape = MaterialTheme.shapes.large,
        windowInsets = WindowInsets(0.dp),
    ) {}

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

private val Bu.languageTag: String?
    get() {
        return when (this) {
            SRF -> "de-CH"
            RTS -> "fr-CH"
            RTR -> "rm-CH"
            SWI -> "de-CH"
            RSI -> "it-CH"
            else -> null
        }
    }

@Composable
private fun BuLabel(
    bu: Bu,
    modifier: Modifier = Modifier,
) {
    val localeList = bu.languageTag?.let { LocaleList(Locale(it)) }

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(localeList = localeList)) {
                append(bu.name.uppercase())
            }
        },
        modifier = modifier,
    )
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
        modifier = modifier.semantics(mergeDescendants = true) {},
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
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
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
            selectedBu = RTS,
            focusRequester = remember { FocusRequester() },
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
