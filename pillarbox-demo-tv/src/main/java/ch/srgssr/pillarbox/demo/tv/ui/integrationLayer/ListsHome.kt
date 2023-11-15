/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.integrationLayer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.itemsIndexed
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import ch.srg.dataProvider.integrationlayer.data.ImageUrl
import ch.srg.dataProvider.integrationlayer.data.remote.Media
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.Show
import ch.srg.dataProvider.integrationlayer.data.remote.Topic
import ch.srg.dataProvider.integrationlayer.data.remote.Transmission
import ch.srg.dataProvider.integrationlayer.data.remote.Type
import ch.srg.dataProvider.integrationlayer.data.remote.Vendor
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentList
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentListViewModel
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ContentListSection
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListFactories
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListSections
import ch.srgssr.pillarbox.demo.tv.R
import ch.srgssr.pillarbox.demo.tv.player.PlayerActivity
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.flowOf
import java.text.DateFormat
import java.util.Date
import kotlin.time.Duration.Companion.seconds

/**
 * Screen of the "Lists" tab of the demo app on TV.
 *
 * @param sections The list of section to display.
 * @param modifier The [Modifier] to apply to this screen.
 *
 * @see ContentListSection
 */
@Composable
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
                        is ContentList.ContentListWithRadioChannel -> item.radioChannel.label
                        is ContentList.LatestMediaForShow -> item.show
                        is ContentList.LatestMediaForTopic -> item.topic
                    }
                },
                onItemClick = { _, contentList ->
                    navController.navigate(contentList.getDestinationRoute())
                }
            )
        }

        contentListFactories.forEach { contentListFactory ->
            composable(route = contentListFactory.route) {
                val context = LocalContext.current
                val contentList = contentListFactory.parse(it)
                val viewModel = viewModel<ContentListViewModel>(
                    factory = ContentListViewModel.Factory(
                        ilRepository = PlayerModule.createIlRepository(context),
                        contentList = contentListFactory.parse(it)
                    )
                )

                BackHandler {
                    navController.popBackStack()
                }

                ListsSection(
                    title = contentList.getDestinationTitle(),
                    items = viewModel.data.collectAsLazyPagingItems(),
                    scaleImageUrl = { imageUrl, containerWidth ->
                        viewModel.getScaledImageUrl(imageUrl, containerWidth)
                    },
                    onItemClick = { item ->
                        when (item) {
                            is Content.Media -> {
                                val demoItem = DemoItem(title = item.media.title, uri = item.media.urn)

                                PlayerActivity.startPlayer(context, demoItem)
                            }

                            is Content.Show -> {
                                val show = ContentList.LatestMediaForShow(
                                    urn = item.show.urn,
                                    show = item.show.title,
                                )

                                navController.navigate(show.getDestinationRoute())
                            }

                            is Content.Topic -> {
                                val topic = ContentList.LatestMediaForTopic(
                                    urn = item.topic.urn,
                                    topic = item.topic.title
                                )

                                navController.navigate(topic.getDestinationRoute())
                            }
                        }
                    }
                )
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

    val columnCount = 4
    val focusManager = LocalFocusManager.current
    val isOnFirstRow by remember {
        derivedStateOf { (focusedIndex / columnCount) <= 0 }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .onPreviewKeyEvent {
                if (it.key == Key.DirectionUp && it.type == KeyEventType.KeyDown && isOnFirstRow) {
                    focusedIndex = -1
                    focusManager.moveFocus(FocusDirection.Up)
                } else {
                    false
                }
            },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge
            )
        }

        TvLazyVerticalGrid(
            columns = TvGridCells.Fixed(columnCount),
            modifier = Modifier.focusRestorer(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(items = items) { index, item ->
                val focusRequester = remember { FocusRequester() }

                Card(
                    onClick = {
                        focusedIndex = index
                        onItemClick(index, item)
                    },
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = itemToString(item),
                            modifier = Modifier.padding(16.dp),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                                .copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun ListsSection(
    title: String,
    modifier: Modifier = Modifier,
    items: LazyPagingItems<Content>,
    scaleImageUrl: (imageUrl: String, containerWidth: Int) -> String,
    onItemClick: (item: Content) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge
        )

        when (val state = items.loadState.refresh) {
            is LoadState.Loading -> ListsSectionLoading(modifier = Modifier.fillMaxSize())
            is LoadState.NotLoading -> ListsSectionContent(
                items = items,
                modifier = Modifier.fillMaxSize(),
                scaleImageUrl = scaleImageUrl,
                onItemClick = onItemClick
            )

            is LoadState.Error -> ListsSectionError(
                throwable = state.error,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun ListsSectionLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.loading))
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
private fun ListsSectionContent(
    items: LazyPagingItems<Content>,
    modifier: Modifier = Modifier,
    scaleImageUrl: (imageUrl: String, containerWidth: Int) -> String,
    onItemClick: (item: Content) -> Unit
) {
    if (items.itemCount == 0) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(R.string.no_content))
        }
    } else {
        var focusedIndex by remember(items) { mutableIntStateOf(0) }

        val hasMedia = remember(items) { (0 until items.itemCount).mapNotNull { items.peek(it) }.any { it is Content.Media } }
        val columnCount = if (hasMedia) 3 else 4
        val focusManager = LocalFocusManager.current
        val isOnFirstRow by remember {
            derivedStateOf { (focusedIndex / columnCount) <= 0 }
        }
        val itemHeight = if (hasMedia) 160.dp else 104.dp

        TvLazyVerticalGrid(
            columns = TvGridCells.Fixed(columnCount),
            modifier = modifier
                .focusRestorer()
                .onPreviewKeyEvent {
                    if (it.key == Key.DirectionUp && it.type == KeyEventType.KeyDown && isOnFirstRow) {
                        focusedIndex = -1
                        focusManager.moveFocus(FocusDirection.Up)
                    } else {
                        false
                    }
                },
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                count = items.itemCount,
                key = items.itemKey()
            ) { index ->
                items[index]?.let { item ->
                    val focusRequester = remember { FocusRequester() }
                    var containerWidth by remember { mutableIntStateOf(0) }

                    Card(
                        onClick = {
                            focusedIndex = index
                            onItemClick(item)
                        },
                        modifier = Modifier
                            .height(itemHeight)
                            .focusRequester(focusRequester)
                            .onGloballyPositioned {
                                if (index == focusedIndex) {
                                    focusRequester.requestFocus()
                                }

                                containerWidth = it.size.width
                            }
                            .onFocusChanged {
                                if (it.hasFocus) {
                                    focusedIndex = index
                                }
                            }
                    ) {
                        when (item) {
                            is Content.Media -> MediaContent(
                                media = item.media,
                                imageUrl = scaleImageUrl(item.media.imageUrl.rawUrl, containerWidth),
                                imageTitle = item.media.imageTitle
                            )

                            is Content.Show -> ShowTopicContent(
                                title = item.show.title,
                                imageUrl = scaleImageUrl(item.show.imageUrl.rawUrl, containerWidth),
                                imageTitle = item.show.imageTitle
                            )

                            is Content.Topic -> ShowTopicContent(
                                title = item.topic.title,
                                imageUrl = item.topic.imageUrl?.rawUrl?.let {
                                    scaleImageUrl(it, containerWidth)
                                },
                                imageTitle = item.topic.imageTitle
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun MediaContent(
    media: Media,
    imageUrl: String,
    imageTitle: String?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = imageUrl,
            contentDescription = imageTitle,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                .padding(8.dp)
        ) {
            Text(
                text = media.title,
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium
                    .copy(
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(blurRadius = 3f)
                    )
            )

            val descriptionPrefix = when (media.mediaType) {
                MediaType.AUDIO -> "🎧"
                MediaType.VIDEO -> "🎬"
            }
            val showTitle = media.show?.title
            val dateString = DateFormat.getDateInstance().format(media.date)

            Text(
                text = "$descriptionPrefix ${showTitle ?: dateString}",
                modifier = Modifier.padding(top = 8.dp),
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall
            )

            if (!showTitle.isNullOrBlank()) {
                Text(
                    text = dateString,
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color.White,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun ShowTopicContent(
    title: String,
    imageUrl: String?,
    imageTitle: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = imageTitle,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                .padding(
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 4.dp
                ),
            color = Color.White,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium
                .copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(blurRadius = 3f)
                )
        )
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun ListsSectionError(
    throwable: Throwable,
    modifier: Modifier = Modifier
) {
    val message = throwable.localizedMessage ?: throwable.message ?: return

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Preview
@Composable
private fun ContentListsViewPreview() {
    PillarboxTheme {
        ListsHome(sections = contentListSections)
    }
}

@Preview
@Composable
private fun ListsSectionLoadingPreview() {
    PillarboxTheme {
        ListsSectionLoading()
    }
}

@Preview
@Composable
private fun ListsSectionContentPreview() {
    val data = listOf(
        Content.Media(
            Media(
                id = "id",
                vendor = Vendor.RTR,
                urn = "urn:media:id",
                title = "Media title",
                description = "Media description",
                date = Date(),
                duration = 30.seconds.inWholeMilliseconds,
                mediaType = MediaType.VIDEO,
                playableAbroad = true,
                type = Type.CLIP,
                imageUrl = ImageUrl("https://image2.png")
            )
        ),
        Content.Show(
            Show(
                id = "id",
                vendor = Vendor.RTR,
                urn = "urn:show:id",
                title = "Show Title",
                description = "Show description",
                transmission = Transmission.TV,
                imageUrl = ImageUrl("https://image1.png")
            )
        ),
        Content.Topic(
            Topic(
                id = "id",
                vendor = Vendor.RTR,
                urn = "urn:show:id",
                title = "Topic title",
                transmission = Transmission.TV,
                imageUrl = ImageUrl("https://imag2.png")
            )
        )
    )

    PillarboxTheme {
        ListsSectionContent(
            items = flowOf(PagingData.from(data)).collectAsLazyPagingItems(),
            scaleImageUrl = { imageUrl, _ -> imageUrl },
            onItemClick = {}
        )
    }
}

@Preview
@Composable
private fun ListsSectionErrorPreview() {
    PillarboxTheme {
        ListsSectionError(throwable = RuntimeException("Something bad happened!"))
    }
}
