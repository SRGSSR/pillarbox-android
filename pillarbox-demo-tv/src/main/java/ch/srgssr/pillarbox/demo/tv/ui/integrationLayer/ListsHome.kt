/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.integrationLayer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.itemsIndexed
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentList
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ContentListSection
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListFactories
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListSections
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme

/**
 * Screen of the "Lists" tab of the demo app on TV.
 *
 * @param sections The list of section to display.
 * @param modifier The [Modifier] to apply to this screen.
 *
 * @see ContentListSection
 */
@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun ListsHome(
    sections: List<ContentListSection>,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.contentLists,
        modifier = modifier.fillMaxSize()
    ) {
        composable(NavigationRoutes.contentLists) {
            ListsSection(
                items = sections,
                itemToString = { it.title },
                onItemClick = { index, _ ->
                    navController.navigate("${NavigationRoutes.contentList}/$index")
                }
            )
        }

        composable(
            route = "${NavigationRoutes.contentList}/{index}",
            arguments = listOf(
                navArgument("index") { type = NavType.IntType }
            )
        ) {
            val sectionIndex = it.arguments?.getInt("index") ?: 0
            val section = sections[sectionIndex]

            BackHandler {
                navController.popBackStack()
            }

            ListsSection(
                title = section.title,
                items = section.contentList,
                itemToString = { item ->
                    when (item) {
                        is ContentList.ContentListWithBu -> item.bu.name.uppercase()
                        is ContentList.RadioLatestMedias -> item.radioChannel.label
                        is ContentList.RadioShows -> item.radioChannel.label
                        else -> ""
                    }
                },
                onItemClick = { _, contentList ->
                    navController.navigate(contentList.getDestinationRoute())
                }
            )
        }

        contentListFactories.forEach { contentListFactory ->
            composable(route = contentListFactory.route) {
                val contentList = contentListFactory.parse(it)

                BackHandler {
                    navController.popBackStack()
                }

                // TODO Integrate content (https://github.com/SRGSSR/pillarbox-android/issues/298)
                Text(text = contentList.toString())
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
private fun <T> ListsSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    items: List<T>,
    itemToString: (item: T) -> String,
    onItemClick: (index: Int, item: T) -> Unit
) {
    var focusedIndex by remember(items) { mutableIntStateOf(0) }

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge
            )
        }

        TvLazyVerticalGrid(
            columns = TvGridCells.Fixed(4),
            modifier = Modifier.focusRestorer(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(items = items) { index, item ->
                val focusRequester = remember { FocusRequester() }

                Card(
                    onClick = { focusedIndex = index; onItemClick(index, item) },
                    modifier = Modifier
                        .height(96.dp)
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = itemToString(item),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ContentListsViewPreview() {
    PillarboxTheme {
        ListsHome(sections = contentListSections)
    }
}
