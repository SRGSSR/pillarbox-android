/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.lists

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
import ch.srgssr.pillarbox.demo.composable
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentList
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentListViewModel
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ILRepository
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListFactories
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListSections
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import java.net.URL

private val defaultListsLevels = listOf("app", "pillarbox", "lists")

/**
 * Build Navigation for integration layer list view
 */
fun NavGraphBuilder.listsNavGraph(navController: NavController, ilRepository: ILRepository, ilHost: URL) {
    val contentClick = { contentList: ContentList, content: Content ->
        when (content) {
            is Content.Show -> {
                val nextContentList = ContentList.LatestMediaForShow(
                    urn = content.urn,
                    show = content.title
                )

                navController.navigate(nextContentList.destinationRoute)
            }

            is Content.Topic -> {
                val nextContentList = ContentList.LatestMediaForTopic(
                    urn = content.urn,
                    topic = content.title
                )

                navController.navigate(nextContentList.destinationRoute)
            }

            is Content.Media -> {
                val item = DemoItem(title = content.title, uri = content.urn)
                SimplePlayerActivity.startActivity(navController.context, item, ilHost)
            }

            is Content.Channel -> {
                val nextContentList = when (contentList) {
                    is ContentList.RadioShows -> ContentList.RadioShowsForChannel(
                        bu = contentList.bu,
                        channelId = content.id,
                        channelTitle = content.title
                    )

                    is ContentList.RadioLatestMedias -> ContentList.RadioLatestMediasForChannel(
                        bu = contentList.bu,
                        channelId = content.id,
                        channelTitle = content.title
                    )

                    else -> error("Unsupported content list")
                }

                navController.navigate(nextContentList.destinationRoute)
            }
        }
    }

    composable(route = NavigationRoutes.contentLists, DemoPageView("home", defaultListsLevels)) {
        ListsHome { contentList ->
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

            ListsSubSection(
                title = contentList.destinationTitle,
                items = viewModel.data.collectAsLazyPagingItems(),
                modifier = Modifier.fillMaxWidth(),
                contentClick = { contentClick(contentList, it) }
            )
        }
    }

    composable("content/error", DemoPageView("error", defaultListsLevels)) {
        Text(text = "Cannot find content!")
    }
}

@Composable
private fun ListsHome(onContentSelected: (ContentList) -> Unit) {
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
        ListsHome {
        }
    }
}
