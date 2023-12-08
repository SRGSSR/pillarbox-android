/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import kotlinx.coroutines.launch

/**
 * Examples home
 *
 * @param modifier
 * @param onItemSelected
 * @receiver
 */
@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun ExamplesHome(
    modifier: Modifier = Modifier,
    onItemSelected: (DemoItem) -> Unit = {},
) {
    val navController = rememberNavController()
    val playlists = Playlist.examplesPlaylists

    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.homeSamples,
        modifier = modifier
    ) {
        composable(NavigationRoutes.homeSamples) {
            ExamplesSection(
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
                        color = Color.White,
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
                title = playlist.title,
                items = playlist.items,
                focusFirstItem = true,
                navController = navController,
                onItemClick = { _, item ->
                    onItemSelected(item)
                }
            ) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MaterialTheme.paddings.baseline),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small)
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

@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
private fun <T> ExamplesSection(
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

    val columnCount = 4
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
                .onPreviewKeyEvent {
                    if (it.key == Key.DirectionUp && it.type == KeyEventType.KeyDown && isOnFirstRow) {
                        focusedIndex = -1
                        focusManager.moveFocus(FocusDirection.Up)
                    } else if (it.key == Key.Back && it.type == KeyEventType.KeyDown) {
                        if (!isOnFirstRow) {
                            focusedIndex = 0

                            coroutineScope.launch {
                                scrollState.animateScrollToItem(focusedIndex)
                            }

                            true
                        } else if (navController.previousBackStackEntry == null) {
                            focusedIndex = -1
                            focusManager.moveFocus(FocusDirection.Up)
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                },
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
                        .height(104.dp)
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
private fun ExamplesPreview() {
    PillarboxTheme {
        ExamplesHome()
    }
}
