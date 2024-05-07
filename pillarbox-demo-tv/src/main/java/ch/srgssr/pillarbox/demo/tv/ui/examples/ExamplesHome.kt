/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.itemsIndexed
import androidx.tv.foundation.lazy.grid.rememberTvLazyGridState
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.shared.ui.examples.ExamplesViewModel
import ch.srgssr.pillarbox.demo.tv.extension.onDpadEvent
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

/**
 * Examples home
 *
 * @param modifier
 * @param onItemSelected
 * @receiver
 */
@Composable
fun ExamplesHome(
    modifier: Modifier = Modifier,
    onItemSelected: (DemoItem) -> Unit = {},
) {
    val examplesViewModel: ExamplesViewModel = viewModel()
    val navController = rememberNavController()
    val playlists by examplesViewModel.contents.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.homeSamples,
        modifier = modifier
    ) {
        composable(NavigationRoutes.homeSamples) {
            ExamplesSection(
                columnCount = 4,
                items = playlists,
                focusFirstItem = false,
                navController = navController,
                onItemClick = { index, _ ->
                    navController.navigate("${NavigationRoutes.homeSample}/$index")
                }
            ) { item ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title,
                        modifier = Modifier.padding(MaterialTheme.paddings.baseline),
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                            .copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        composable(
            route = "${NavigationRoutes.homeSample}/{index}",
            arguments = listOf(
                navArgument("index") { type = NavType.IntType }
            )
        ) {
            val playlistIndex = it.arguments?.getInt("index") ?: 0
            val playlist = playlists[playlistIndex]

            ExamplesSection(
                columnCount = 3,
                title = playlist.title,
                items = playlist.items,
                focusFirstItem = true,
                navController = navController,
                onItemClick = { _, item ->
                    onItemSelected(item)
                }
            ) { item ->
                Box {
                    if (item.imageUrl != null) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                            .padding(MaterialTheme.paddings.small),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodyMedium
                                .copy(fontWeight = FontWeight.Bold)
                        )

                        item.description?.let { description ->
                            Text(
                                text = description,
                                modifier = Modifier.padding(top = MaterialTheme.paddings.small),
                                color = Color.White,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun <T> ExamplesSection(
    columnCount: Int,
    modifier: Modifier = Modifier,
    title: String? = null,
    items: List<T>,
    focusFirstItem: Boolean,
    navController: NavHostController,
    onItemClick: (index: Int, item: T) -> Unit,
    content: @Composable (item: T) -> Unit
) {
    var focusedIndex by rememberSaveable(items, focusFirstItem) {
        mutableIntStateOf(if (focusFirstItem) 0 else -1)
    }

    val focusManager = LocalFocusManager.current
    val isOnFirstRow by remember {
        derivedStateOf { (focusedIndex / columnCount) <= 0 }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge
            )
        }

        val coroutineScope = rememberCoroutineScope()
        val scrollState = rememberTvLazyGridState()

        TvLazyVerticalGrid(
            columns = TvGridCells.Fixed(columnCount),
            modifier = Modifier
                .focusRestorer()
                .onDpadEvent(
                    onUp = {
                        if (isOnFirstRow) {
                            focusedIndex = -1
                            focusManager.moveFocus(FocusDirection.Up)
                        } else {
                            false
                        }
                    },
                    onBack = {
                        if (!isOnFirstRow) {
                            focusedIndex = 0

                            coroutineScope.launch {
                                scrollState.animateScrollToItem(focusedIndex)
                            }

                            true
                        } else if (navController.previousBackStackEntry == null) {
                            focusedIndex = -1
                            focusManager.moveFocus(FocusDirection.Up)
                            true
                        } else {
                            false
                        }
                    }
                ),
            state = scrollState,
            contentPadding = PaddingValues(vertical = MaterialTheme.paddings.baseline),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline)
        ) {
            itemsIndexed(items = items) { index, item ->
                val focusRequester = remember { FocusRequester() }

                Card(
                    onClick = { onItemClick(index, item) },
                    modifier = Modifier
                        .aspectRatio(16f / 9)
                        .focusRequester(focusRequester)
                        .onGloballyPositioned {
                            if (index == focusedIndex) {
                                focusRequester.requestFocus()
                            }
                        }
                        .onFocusChanged {
                            if (it.hasFocus) {
                                focusedIndex = index
                            }
                        }
                ) {
                    content(item)
                }
            }
        }
    }
}

@Preview
@Composable
private fun ExamplesHomePreview() {
    PillarboxTheme {
        ExamplesHome()
    }
}
