/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.composable
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.ILRepository
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.RadioChannel
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

private val bus = listOf(Bu.RTS, Bu.SRF, Bu.RSI, Bu.RTR, Bu.SWI)

private data class SectionItem(val title: String, val listContent: List<ContentList>)

private val sections = listOf(
    SectionItem("TV Topics", bus.map { ContentList.TvTopics(it) }),
    SectionItem("TV Shows", bus.map { ContentList.TvShows(it) }),
    SectionItem("TV Latest medias", bus.map { ContentList.TVLatestMedias(it) }),
    SectionItem("TV Livestreams", bus.map { ContentList.TVLivestreams(it) }),
    SectionItem("TV Live center", bus.map { ContentList.TVLiveCenter(it) }),
    SectionItem("TV Live web", bus.map { ContentList.TVLiveWeb(it) }),
    SectionItem("Radio livestream", bus.map { ContentList.RadioLiveStreams(it) }),
    SectionItem("Radio Latest medias", RadioChannel.entries.map { ContentList.RadioLatestMedias(it) }),
    SectionItem("Radio Shows", RadioChannel.entries.map { ContentList.RadioShows(it) }),
)

private val defaultListsLevels = listOf("app", "pillarbox", "lists")

/**
 * Build Navigation for integration layer list view
 */
fun NavGraphBuilder.listNavGraph(navController: NavController, ilRepository: ILRepository) {
    val contentClick = { content: Content ->
        when (content) {
            is Content.Show -> {
                val contentList = ContentList.LatestMediaForShow(content.show.urn)
                navController.navigate(contentList.getDestinationRoute())
            }

            is Content.Topic -> {
                val contentList = ContentList.LatestMediaForTopic(content.topic.urn)
                navController.navigate(contentList.getDestinationRoute())
            }

            is Content.Media -> {
                val item = DemoItem(title = content.media.title, uri = content.media.urn)
                SimplePlayerActivity.startActivity(navController.context, item)
            }
        }
    }

    composable(route = NavigationRoutes.contentLists, DemoPageView("home", defaultListsLevels)) {
        ContentListsView { contentList ->
            navController.navigate(route = contentList.getDestinationRoute())
        }
    }

    composable(route = ContentList.TvTopics.route, DemoPageView("tv topics", defaultListsLevels)) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TvTopics.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.TvShows.route, DemoPageView("tv shows", defaultListsLevels)) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TvShows.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.TVLatestMedias.route, DemoPageView("tv latest medias", defaultListsLevels)) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TVLatestMedias.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.TVLivestreams.route, DemoPageView("tv livestreams", defaultListsLevels)) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TVLivestreams.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.TVLiveCenter.route, DemoPageView("tv live center", defaultListsLevels)) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TVLiveCenter.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.TVLiveWeb.route, DemoPageView(" tv live web", defaultListsLevels)) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TVLiveWeb.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.RadioLatestMedias.route, DemoPageView("Radio latest medias", defaultListsLevels)) {
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.RadioLatestMedias.parse(it)
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.RadioShows.route, DemoPageView("Radio shows", defaultListsLevels)) {
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.RadioShows.parse(it)
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.RadioLiveStreams.route, DemoPageView("Radio livestreams", defaultListsLevels)) {
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.RadioLiveStreams.parse(it)
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.LatestMediaForShow.route, DemoPageView("Latest media for show", defaultListsLevels)) {
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.LatestMediaForShow.parse(it)
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.LatestMediaForTopic.route, DemoPageView("Latest media for topic", defaultListsLevels)) {
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.LatestMediaForTopic.parse(it)
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable("content/error", DemoPageView("error", defaultListsLevels)) {
        Text(text = "Cannot find content!")
    }
}

@Composable
private fun ContentListsView(onContentSelected: (ContentList) -> Unit) {
    LazyColumn {
        items(sections) {
            SectionItemView(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                sectionItem = it, onContentSelected = onContentSelected
            )
        }
    }
}

@Preview
@Composable
private fun ContentListPreview() {
    PillarboxTheme() {
        ContentListsView() {
        }
    }
}

@Composable
private fun SectionItemView(
    sectionItem: SectionItem,
    onContentSelected: (ContentList) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(modifier = Modifier.padding(vertical = 6.dp), text = sectionItem.title.uppercase(), style = MaterialTheme.typography.bodyLarge)
            for (content in sectionItem.listContent) {
                val label = when (content) {
                    is ContentList.ContentListWithBu -> content.bu.name
                    is ContentList.RadioLatestMedias -> content.radioChannel.label
                    is ContentList.RadioShows -> content.radioChannel.label
                    else -> ""
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = { onContentSelected(content) }
                ) {
                    Text(text = label.uppercase())
                }
            }
        }
    }
}
