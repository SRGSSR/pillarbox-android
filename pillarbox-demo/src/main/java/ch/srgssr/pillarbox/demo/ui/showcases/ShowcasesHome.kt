/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavController
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesApple
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.showcases.integrations.MediaControllerActivity
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Showcases home page.
 *
 * @param navController The [NavController] to navigate between screens.
 */
@Composable
fun ShowcasesHome(navController: NavController) {
    val context = LocalContext.current
    val playlists = remember {
        listOf(
            SamplesSRG.StoryVideoUrls,
            SamplesSRG.StoryVideoUrns,
            Playlist(
                title = "Mixed content",
                items = listOf(
                    SamplesApple.Basic_16_9,
                    SamplesSRG.OnDemandHorizontalVideo,
                    SamplesSRG.Unknown,
                    SamplesSRG.Tataki_1,
                )
            ),
            Playlist(
                title = "Mixed content with live dvr",
                items = listOf(
                    SamplesApple.Basic_16_9,
                    SamplesSRG.DvrVideo,
                    SamplesSRG.OnDemandHorizontalVideo,
                )
            ),
            Playlist(
                title = "Mixed content with live only",
                items = listOf(
                    SamplesApple.Basic_16_9,
                    SamplesSRG.LiveVideo,
                    SamplesSRG.OnDemandHorizontalVideo,
                )
            ),
            Playlist(title = "Empty", items = emptyList())
        )
    }
    val titleModifier = Modifier.padding(
        start = MaterialTheme.paddings.baseline,
        top = MaterialTheme.paddings.small
    )
    val sectionModifier = { size: Int ->
        Modifier.semantics {
            collectionInfo = CollectionInfo(rowCount = size, columnCount = 1)
        }
    }
    val itemModifier = { index: Int ->
        Modifier
            .fillMaxWidth()
            .semantics {
                collectionItemInfo = CollectionItemInfo(
                    rowIndex = index,
                    rowSpan = 1,
                    columnIndex = 1,
                    columnSpan = 1,
                )
            }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.paddings.baseline)
            .padding(bottom = MaterialTheme.paddings.baseline)
    ) {
        val layoutDestinations = listOf(
            stringResource(R.string.simple_player) to NavigationRoutes.SimplePlayer,
            stringResource(R.string.story) to NavigationRoutes.Story,
            stringResource(R.string.chapters) to NavigationRoutes.Chapters,
            stringResource(R.string.thumbnail) to NavigationRoutes.ThumbnailShowcase,
        )
        val miscDestinations = listOf(
            stringResource(R.string.start_given_time_example) to NavigationRoutes.StartAtGivenTime,
            stringResource(R.string.showcase_time_based_content) to NavigationRoutes.TimeBasedContent,
            stringResource(R.string.adaptive) to NavigationRoutes.Adaptive,
            stringResource(R.string.player_swap) to NavigationRoutes.PlayerSwap,
            stringResource(R.string.tracker_example) to NavigationRoutes.TrackingSample,
            stringResource(R.string.update_media_item_example) to NavigationRoutes.UpdatableSample,
            stringResource(R.string.smooth_seeking_example) to NavigationRoutes.SmoothSeeking,
            stringResource(R.string.video_360) to NavigationRoutes.Video360,
            stringResource(R.string.showcase_countdown) to NavigationRoutes.CountdownShowcase,
        )

        DemoListHeaderView(
            title = stringResource(R.string.layouts),
            modifier = Modifier.padding(start = MaterialTheme.paddings.baseline)
        )

        DemoListSectionView(
            modifier = sectionModifier(layoutDestinations.size),
        ) {
            layoutDestinations.forEachIndexed { index, (label, destination) ->
                DemoListItemView(
                    title = label,
                    modifier = itemModifier(index),
                    onClick = { navController.navigate(destination) }
                )

                if (index < layoutDestinations.lastIndex) {
                    HorizontalDivider()
                }
            }
        }

        DemoListHeaderView(
            title = stringResource(R.string.playlists),
            modifier = titleModifier
        )

        DemoListSectionView(
            modifier = sectionModifier(playlists.size + 1),
        ) {
            playlists.forEachIndexed { index, item ->
                DemoListItemView(
                    title = item.title,
                    modifier = itemModifier(index),
                    languageTag = item.languageTag,
                    onClick = { SimplePlayerActivity.startActivity(context, item) }
                )

                HorizontalDivider()
            }

            DemoListItemView(
                title = stringResource(R.string.showcase_playback_settings),
                modifier = itemModifier(playlists.size),
                onClick = { navController.navigate(NavigationRoutes.ShowcasePlaybackSettings) },
            )
        }

        DemoListHeaderView(
            title = stringResource(R.string.integrations),
            modifier = titleModifier
        )

        DemoListSectionView(
            modifier = sectionModifier(2),
        ) {
            DemoListItemView(
                title = stringResource(R.string.exoplayer_view),
                modifier = itemModifier(0),
                onClick = { navController.navigate(NavigationRoutes.ExoPlayerSample) }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.media3_compose),
                modifier = itemModifier(1),
                onClick = { navController.navigate(NavigationRoutes.Media3ComposeSample) }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.auto),
                modifier = itemModifier(2),
                onClick = {
                    val intent = Intent(context, MediaControllerActivity::class.java)
                    context.startActivity(intent)
                }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.showcase_cast),
                modifier = itemModifier(2),
                onClick = {
                    navController.navigate(CastShowCaseNavigationRoute)
                }
            )
        }

        DemoListHeaderView(
            title = stringResource(R.string.misc),
            modifier = titleModifier
        )

        DemoListSectionView(
            modifier = sectionModifier(miscDestinations.size),
        ) {
            miscDestinations.forEachIndexed { index, (label, destination) ->
                DemoListItemView(
                    title = label,
                    modifier = itemModifier(index),
                    onClick = { navController.navigate(destination) }
                )

                if (index < miscDestinations.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}
