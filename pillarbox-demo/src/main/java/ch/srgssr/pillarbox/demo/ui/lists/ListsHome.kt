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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import androidx.paging.compose.collectAsLazyPagingItems
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlLocation
import ch.srgssr.pillarbox.demo.DemoPageView
import ch.srgssr.pillarbox.demo.composable
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentList
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentListViewModel
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ILRepository
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListSections
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

private val defaultListsLevels = listOf("app", "pillarbox", "lists")

/**
 * Build Navigation for integration layer list view
 */
fun NavGraphBuilder.listsNavGraph(
    navController: NavController,
    ilRepository: ILRepository,
    ilHost: IlHost,
    forceSAM: Boolean,
    ilLocation: IlLocation?,
) {
    val contentClick = { contentList: ContentList, content: Content ->
        when (content) {
            is Content.Show -> {
                val nextContentList = ContentList.LatestMediaForShow(
                    urn = content.urn,
                    show = content.title,
                    languageTag = contentList.languageTag,
                )

                navController.navigate(nextContentList)
            }

            is Content.Topic -> {
                val nextContentList = ContentList.LatestMediaForTopic(
                    urn = content.urn,
                    topic = content.title,
                    languageTag = contentList.languageTag,
                )

                navController.navigate(nextContentList)
            }

            is Content.Media -> {
                val item = DemoItem.URN(
                    title = content.title,
                    urn = content.urn,
                    host = ilHost,
                    forceSAM = forceSAM,
                    ilLocation = ilLocation,
                    languageTag = contentList.languageTag,
                )

                SimplePlayerActivity.startActivity(navController.context, item)
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

                navController.navigate(nextContentList)
            }
        }
    }

    composable<NavigationRoutes.ContentLists>(DemoPageView("home", defaultListsLevels)) {
        ListsHome { contentList ->
            navController.navigate(contentList)
        }
    }

    addContentListRoute<ContentList.TVTopics>(trackerTitle = "tv-topics", ilRepository, contentClick)

    addContentListRoute<ContentList.TVShows>(trackerTitle = "tv-shows", ilRepository, contentClick)

    addContentListRoute<ContentList.TVLatestMedias>(trackerTitle = "tv-latest-videos", ilRepository, contentClick)

    addContentListRoute<ContentList.TVLivestreams>(trackerTitle = "tv-livestreams", ilRepository, contentClick)

    addContentListRoute<ContentList.TVLiveCenter>(trackerTitle = "live-center", ilRepository, contentClick)

    addContentListRoute<ContentList.TVLiveWeb>(trackerTitle = "live-web", ilRepository, contentClick)

    addContentListRoute<ContentList.RadioLiveStreams>(trackerTitle = "radio-livestreams", ilRepository, contentClick)

    addContentListRoute<ContentList.RadioLatestMedias>(trackerTitle = "latest-audios", ilRepository, contentClick)

    addContentListRoute<ContentList.RadioShows>(trackerTitle = "shows", ilRepository, contentClick)

    addContentListRoute<ContentList.LatestMediaForShow>(trackerTitle = "latest-media-for-show", ilRepository, contentClick)

    addContentListRoute<ContentList.LatestMediaForTopic>(trackerTitle = "latest-media-for-topic", ilRepository, contentClick)

    addContentListRoute<ContentList.RadioShowsForChannel>(trackerTitle = "shows-for-channel", ilRepository, contentClick)

    addContentListRoute<ContentList.RadioLatestMediasForChannel>(trackerTitle = "latest-audios-for-channel", ilRepository, contentClick)
}

private inline fun <reified T : ContentList> NavGraphBuilder.addContentListRoute(
    trackerTitle: String,
    ilRepository: ILRepository,
    crossinline onClick: (contentList: T, content: Content) -> Unit,
) {
    composable<T>(pageView = DemoPageView(trackerTitle, defaultListsLevels)) { navBackStackEntry ->
        val contentList = navBackStackEntry.toRoute<T>()
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
            languageTag = contentList.languageTag,
            contentClick = { onClick(contentList, it) }
        )
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
                modifier = Modifier.padding(start = MaterialTheme.paddings.baseline),
                languageTag = section.languageTag,
            )

            DemoListSectionView(
                modifier = Modifier.semantics {
                    collectionInfo = CollectionInfo(rowCount = section.contentList.size, columnCount = 1)
                },
            ) {
                section.contentList.forEachIndexed { index, item ->
                    DemoListItemView(
                        title = item.destinationTitle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                collectionItemInfo = CollectionItemInfo(
                                    rowIndex = index,
                                    rowSpan = 1,
                                    columnIndex = 1,
                                    columnSpan = 1,
                                )
                            },
                        languageTag = item.languageTag,
                        onClick = { onContentSelected(item) }
                    )

                    if (index < section.contentList.lastIndex) {
                        HorizontalDivider()
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
