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
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.ILRepository
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.RadioChannel
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

private val bus = listOf(Bu.RTS, Bu.SRF, Bu.RSI, Bu.RTR, Bu.SWI)

private data class SectionItem(val title: String, val listContent: List<ContentList>)

private val sections = listOf(
    SectionItem("TV Topcis", bus.map { ContentList.TvTopics(it) }),
    SectionItem("TV Shows", bus.map { ContentList.TvShows(it) }),
    SectionItem("TV Latest medias", bus.map { ContentList.TVLatestMedias(it) }),
    SectionItem("TV Livestreams", bus.map { ContentList.TVLivestreams(it) }),
    SectionItem("TV Live center", bus.map { ContentList.TVLiveCenter(it) }),
    SectionItem("TV Live web", bus.map { ContentList.TVLiveWeb(it) }),
    SectionItem("Radio livestream", bus.map { ContentList.RadioLiveStreams(it) }),
    SectionItem("Radio Latest medias", RadioChannel.values().map { ContentList.RadioLatestMedias(it) }),
    SectionItem("Radio Shows", RadioChannel.values().map { ContentList.RadioShows(it) }),
)

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
                // Open Media
            }
        }
    }

    composable(route = NavigationRoutes.contentLists) {
        ContentListsView { contentList ->
            navController.navigate(route = contentList.getDestinationRoute())
        }
    }

    composable(route = ContentList.TvTopics.route) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TvTopics.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.TvShows.route) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TvShows.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.TVLatestMedias.route) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TVLatestMedias.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.TVLivestreams.route) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TVLivestreams.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.TVLiveCenter.route) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TVLiveCenter.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.TVLiveWeb.route) { navBackStackEntry ->
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.TVLiveWeb.parse(navBackStackEntry),
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.RadioLatestMedias.route) {
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.RadioLatestMedias.parse(it)
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.RadioShows.route) {
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.RadioShows.parse(it)
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.RadioLiveStreams.route) {
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.RadioLiveStreams.parse(it)
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.LatestMediaForShow.route) {
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.LatestMediaForShow.parse(it)
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable(route = ContentList.LatestMediaForTopic.route) {
        val viewModel: ContentListViewModel = viewModel(
            factory = ContentListViewModel.Factory(
                ilRepository = ilRepository,
                contentList = ContentList.LatestMediaForTopic.parse(it)
            )
        )
        ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
    }

    composable("content/error") {
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
    Card(modifier = modifier, elevation = 3.dp) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(modifier = Modifier.padding(vertical = 6.dp), text = sectionItem.title.uppercase(), style = MaterialTheme.typography.body1)
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
