/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.paging.compose.collectAsLazyPagingItems
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentList
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentListViewModel
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ILRepository
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListFactories
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListSections
import ch.srgssr.pillarbox.demo.ui.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.composable
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

private val defaultListsLevels = listOf("app", "pillarbox", "lists")

/**
 * Build Navigation for integration layer list view
 */
fun NavGraphBuilder.listNavGraph(navController: NavController, ilRepository: ILRepository) {
    val contentClick = { content: Content ->
        when (content) {
            is Content.Show -> {
                val contentList = ContentList.LatestMediaForShow(
                    urn = content.urn,
                    show = content.title
                )

                navController.navigate(contentList.destinationRoute)
            }

            is Content.Topic -> {
                val contentList = ContentList.LatestMediaForTopic(
                    urn = content.topic.urn,
                    topic = content.topic.title
                )

                navController.navigate(contentList.destinationRoute)
            }

            is Content.Media -> {
                val item = DemoItem(title = content.title, uri = content.urn)
                SimplePlayerActivity.startActivity(navController.context, item)
            }
        }
    }

    composable(route = NavigationRoutes.contentLists, DemoPageView("home", defaultListsLevels)) {
        ContentListsView { contentList ->
            navController.navigate(route = contentList.destinationRoute)
        }
    }

    contentListFactories.forEach { contentListFactory ->
        composable(
            route = contentListFactory.route,
            pageView = DemoPageView(contentListFactory.trackerTitle, defaultListsLevels)
        ) { navBackStackEntry ->
            val contentList = contentListFactory.parse(navBackStackEntry)
            val viewModel = viewModel<ContentListViewModel>(
                factory = ContentListViewModel.Factory(
                    ilRepository = ilRepository,
                    contentList = contentList,
                )
            )

            ContentListView(
                title = contentList.destinationTitle,
                items = viewModel.data.collectAsLazyPagingItems(),
                modifier = Modifier.fillMaxWidth(),
                contentClick = contentClick
            )
        }
    }

    composable("content/error", DemoPageView("error", defaultListsLevels)) {
        Text(text = "Cannot find content!")
    }
}

@Composable
private fun ContentListsView(onContentSelected: (ContentList) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = MaterialTheme.paddings.baseline,
            end = MaterialTheme.paddings.baseline,
            bottom = MaterialTheme.paddings.baseline
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small)
    ) {
        items(contentListSections) { section ->
            DemoListHeaderView(
                title = section.title,
                modifier = Modifier.padding(start = MaterialTheme.paddings.baseline)
            )

            DemoListSectionView {
                section.contentList.forEachIndexed { index, item ->
                    DemoListItemView(
                        title = item.destinationTitle,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onContentSelected(item) }
                    )

                    if (index < section.contentList.lastIndex) {
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ContentListPreview() {
    PillarboxTheme {
        ContentListsView {
        }
    }
}
