/*
 * Copyright (c) SRG SSR. All rights reserved.
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
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentList
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentListViewModel
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ContentListSection
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ILRepository
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListFactories
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListSections
import ch.srgssr.pillarbox.demo.ui.composable
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

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

    contentListFactories.forEach { contentListFactory ->
        composable(
            route = contentListFactory.route,
            pageView = DemoPageView(contentListFactory.trackerTitle, defaultListsLevels)
        ) { navBackStackEntry ->
            val viewModel = viewModel<ContentListViewModel>(
                factory = ContentListViewModel.Factory(
                    ilRepository = ilRepository,
                    contentList = contentListFactory.parse(navBackStackEntry),
                )
            )

            ContentListView(contentListViewModel = viewModel, contentClick = contentClick)
        }
    }

    composable("content/error", DemoPageView("error", defaultListsLevels)) {
        Text(text = "Cannot find content!")
    }
}

@Composable
private fun ContentListsView(onContentSelected: (ContentList) -> Unit) {
    LazyColumn {
        items(contentListSections) {
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
    PillarboxTheme {
        ContentListsView {
        }
    }
}

@Composable
private fun SectionItemView(
    sectionItem: ContentListSection,
    onContentSelected: (ContentList) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(modifier = Modifier.padding(vertical = 6.dp), text = sectionItem.title.uppercase(), style = MaterialTheme.typography.bodyLarge)
            for (content in sectionItem.contentList) {
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
