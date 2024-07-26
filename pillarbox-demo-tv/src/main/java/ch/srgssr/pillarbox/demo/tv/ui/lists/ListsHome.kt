/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Movie
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
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
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvGridItemSpan
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.itemsIndexed
import androidx.tv.foundation.lazy.grid.rememberTvLazyGridState
import androidx.tv.material3.Card
import androidx.tv.material3.CardColors
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
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
import ch.srg.dataProvider.integrationlayer.request.image.ImageWidth
import ch.srg.dataProvider.integrationlayer.request.image.decorated
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.shared.extension.onDpadEvent
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentList
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentListViewModel
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ContentListSection
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListFactories
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.contentListSections
import ch.srgssr.pillarbox.demo.tv.R
import ch.srgssr.pillarbox.demo.tv.ui.player.PlayerActivity
import ch.srgssr.pillarbox.demo.tv.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.tv.ui.theme.paddings
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.time.Duration.Companion.seconds
import ch.srgssr.pillarbox.demo.shared.R as sharedR

/**
 * Screen of the "Lists" tab in the TV demo app.
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
                focusFirstItem = false,
                itemToString = { it.title },
                navController = navController,
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

            ListsSection(
                title = section.title,
                items = section.contentList,
                focusFirstItem = true,
                itemToString = { item ->
                    item.destinationTitle
                },
                cardColors = CardDefaults.colors(
                    containerColor = Color(0xFFAF001E),
                    contentColor = Color.White,
                    focusedContentColor = Color.White,
                    pressedContentColor = Color.White
                ),
                navController = navController,
                onItemClick = { _, contentList ->
                    navController.navigate(contentList.destinationRoute)
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

                ListsSection(
                    modifier = Modifier.padding(horizontal = MaterialTheme.paddings.baseline),
                    title = contentList.destinationTitle,
                    items = viewModel.data.collectAsLazyPagingItems(),
                    focusFirstItem = true,
                    scaleImageUrl = { imageUrl, containerWidth ->
                        viewModel.getScaledImageUrl(imageUrl, containerWidth)
                    },
                    onItemClick = { item ->
                        when (item) {
                            is Content.Channel -> {
                                val nextContentList = when (contentList) {
                                    is ContentList.RadioShows -> ContentList.RadioShowsForChannel(
                                        bu = contentList.bu,
                                        channelId = item.id,
                                        channelTitle = item.title
                                    )

                                    is ContentList.RadioLatestMedias -> ContentList.RadioLatestMediasForChannel(
                                        bu = contentList.bu,
                                        channelId = item.id,
                                        channelTitle = item.title
                                    )

                                    else -> error("Unsupported content list")
                                }

                                navController.navigate(nextContentList.destinationRoute)
                            }

                            is Content.Media -> {
                                val demoItem = DemoItem(title = item.title, uri = item.urn)

                                PlayerActivity.startPlayer(context, demoItem)
                            }

                            is Content.Show -> {
                                val show = ContentList.LatestMediaForShow(
                                    urn = item.urn,
                                    show = item.title,
                                )

                                navController.navigate(show.destinationRoute)
                            }

                            is Content.Topic -> {
                                val topic = ContentList.LatestMediaForTopic(
                                    urn = item.urn,
                                    topic = item.title
                                )

                                navController.navigate(topic.destinationRoute)
                            }
                        }
                    },
                    emptyScreen = { emptyScreenModifier ->
                        Box(
                            modifier = emptyScreenModifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(R.string.no_content))
                        }
                    }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun <T> ListsSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    items: List<T>,
    focusFirstItem: Boolean,
    navController: NavHostController,
    itemToString: (item: T) -> String,
    cardColors: CardColors = CardDefaults.colors(),
    onItemClick: (index: Int, item: T) -> Unit
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
                        },
                    colors = cardColors
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = itemToString(item),
                            modifier = Modifier.padding(MaterialTheme.paddings.baseline),
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

/**
 * Display a list of [Content].
 *
 * @param T The specific type of [Content] in items.
 * @param modifier The [Modifier] to apply to the list.
 * @param title An optional title to display at the top of the list.
 * @param items The list of [Content] to display.
 * @param focusFirstItem `true` to automatically focus the first, `false` otherwise.
 * @param scaleImageUrl A callback used to get the URL of the scaled image, to match as much as possible the provided container width.
 * @param onItemClick The action to perform when clicking on one of the items.
 * @param emptyScreen The content to display when the list is empty.
 */
@Composable
fun <T : Content> ListsSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    items: LazyPagingItems<T>,
    focusFirstItem: Boolean,
    scaleImageUrl: (imageUrl: ImageUrl, containerWidth: Int) -> String,
    onItemClick: (item: T) -> Unit,
    emptyScreen: @Composable (modifier: Modifier) -> Unit
) {
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

        when (val state = items.loadState.refresh) {
            is LoadState.Loading -> ListsSectionLoading(modifier = Modifier.fillMaxSize())
            is LoadState.NotLoading -> ListsSectionContent(
                items = items,
                modifier = Modifier.fillMaxSize(),
                focusFirstItem = focusFirstItem,
                scaleImageUrl = scaleImageUrl,
                onItemClick = onItemClick,
                emptyScreen = emptyScreen
            )

            is LoadState.Error -> ListsSectionError(
                throwable = state.error,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ListsSectionLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.loading))
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun <T : Content> ListsSectionContent(
    items: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    focusFirstItem: Boolean,
    scaleImageUrl: (imageUrl: ImageUrl, containerWidth: Int) -> String,
    onItemClick: (item: T) -> Unit,
    emptyScreen: @Composable (modifier: Modifier) -> Unit
) {
    if (items.itemCount == 0) {
        emptyScreen(modifier)
    } else {
        var focusedIndex by rememberSaveable(items, focusFirstItem) {
            mutableIntStateOf(if (focusFirstItem) 0 else -1)
        }

        val hasMedia = remember(items) { (0 until items.itemCount).mapNotNull { items.peek(it) }.any { it is Content.Media } }
        val columnCount = if (hasMedia) 3 else 4
        val focusManager = LocalFocusManager.current
        val isOnFirstRow by remember {
            derivedStateOf { (focusedIndex / columnCount) <= 0 }
        }

        val coroutineScope = rememberCoroutineScope()
        val scrollState = rememberTvLazyGridState()

        TvLazyVerticalGrid(
            columns = TvGridCells.Fixed(columnCount),
            modifier = modifier
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
            items(
                count = items.itemCount,
                key = items.itemKey()
            ) { index ->
                items[index]?.let { item ->
                    val focusRequester = remember { FocusRequester() }
                    var containerWidth by remember { mutableIntStateOf(0) }

                    Card(
                        onClick = { onItemClick(item) },
                        modifier = Modifier
                            .aspectRatio(16f / 9)
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
                        ContentCard(
                            item = item,
                            scaleImageUrl = { imageUrl ->
                                scaleImageUrl(imageUrl, containerWidth)
                            }
                        )
                    }
                }
            }

            if (items.loadState.append is LoadState.Loading) {
                item(
                    contentType = "LoadingView",
                    span = { TvGridItemSpan(columnCount) }
                ) {
                    ListsSectionLoading(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(MaterialTheme.paddings.baseline)
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentCard(
    item: Content,
    scaleImageUrl: (imageUrl: ImageUrl) -> String,
) {
    when (item) {
        is Content.Channel -> CategoryContent(
            title = item.title,
            imageUrl = scaleImageUrl(item.imageUrl),
            imageTitle = item.imageTitle
        )

        is Content.Media -> MediaContent(
            media = item,
            imageUrl = scaleImageUrl(item.imageUrl),
            imageTitle = item.imageTitle
        )

        is Content.Show -> CategoryContent(
            title = item.title,
            imageUrl = scaleImageUrl(item.imageUrl),
            imageTitle = item.imageTitle
        )

        is Content.Topic -> CategoryContent(
            title = item.title,
            imageUrl = item.imageUrl?.let(scaleImageUrl),
            imageTitle = item.imageTitle
        )
    }
}

@Composable
private fun MediaContent(
    media: Content.Media,
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
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                .padding(MaterialTheme.paddings.small)
        ) {
            val mediaTypeIcon = when (media.mediaType) {
                MediaType.AUDIO -> Icons.Default.Headset
                MediaType.VIDEO -> Icons.Default.Movie
            }
            val duration = stringResource(sharedR.string.duration, media.duration)
            val description = "${media.date} - $duration"

            Icon(
                imageVector = mediaTypeIcon,
                contentDescription = null,
                tint = Color.White
            )

            Spacer(modifier = Modifier.weight(1f))

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

            Text(
                text = media.showTitle ?: description,
                modifier = Modifier.padding(top = MaterialTheme.paddings.small),
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall
            )

            if (media.showTitle != null) {
                Text(
                    text = description,
                    modifier = Modifier.padding(top = MaterialTheme.paddings.mini),
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
private fun CategoryContent(
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = MaterialTheme.paddings.small,
                        top = MaterialTheme.paddings.small,
                        end = MaterialTheme.paddings.small,
                        bottom = MaterialTheme.paddings.mini
                    ),
                color = if (imageUrl != null) Color.White else Color.Unspecified,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
                    .copy(
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(blurRadius = 3f)
                    )
            )
        }
    }
}

@Composable
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
private fun ListsHomePreview() {
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
            focusFirstItem = true,
            scaleImageUrl = { imageUrl, _ ->
                imageUrl.decorated(width = ImageWidth.W480)
            },
            onItemClick = {},
            emptyScreen = {}
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
