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
import androidx.navigation.NavController
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.Playlist
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
            Playlist.VideoUrls,
            Playlist.VideoUrns,
            Playlist.MixedContent,
            Playlist.MixedContentLiveDvrVod,
            Playlist.MixedContentLiveOnlyVod,
            Playlist.EmptyPlaylist,
        )
    }
    val titleModifier = Modifier.padding(
        start = MaterialTheme.paddings.baseline,
        top = MaterialTheme.paddings.small
    )
    val itemModifier = Modifier.fillMaxWidth()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.paddings.baseline)
            .padding(bottom = MaterialTheme.paddings.baseline)
    ) {
        DemoListHeaderView(
            title = stringResource(R.string.layouts),
            modifier = Modifier.padding(start = MaterialTheme.paddings.baseline)
        )

        DemoListSectionView {
            DemoListItemView(
                title = stringResource(R.string.simple_player),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.SimplePlayer) }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.story),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.Story) }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.chapters),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.Chapters) }
            )
            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.thumbnails),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.ThumbnailShowcase) }
            )
        }

        DemoListHeaderView(
            title = stringResource(R.string.playlists),
            modifier = titleModifier
        )

        DemoListSectionView {
            playlists.forEach { item ->
                DemoListItemView(
                    title = item.title,
                    modifier = itemModifier,
                    subtitle = item.description,
                    onClick = { SimplePlayerActivity.startActivity(context, item) }
                )

                HorizontalDivider()
            }

            DemoListItemView(
                title = stringResource(R.string.showcase_playback_settings),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.ShowcasePlaybackSettings) },
            )
        }

        DemoListHeaderView(
            title = stringResource(R.string.integrations),
            modifier = titleModifier
        )

        DemoListSectionView {
            DemoListItemView(
                title = stringResource(R.string.exoplayer_view),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.ExoPlayerSample) }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.auto),
                modifier = itemModifier,
                onClick = {
                    val intent = Intent(context, MediaControllerActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }

        DemoListHeaderView(
            title = stringResource(R.string.misc),
            modifier = titleModifier
        )

        DemoListSectionView {
            DemoListItemView(
                title = stringResource(R.string.start_given_time_example),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.StartAtGivenTime) }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.adaptive),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.Adaptive) }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.player_swap),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.PlayerSwap) }
            )
            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.tracker_example),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.TrackingSample) }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.update_media_item_example),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.UpdatableSample) }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.smooth_seeking_example),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.SmoothSeeking) }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.video_360),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.Video360) }
            )

            HorizontalDivider()

            DemoListItemView(
                title = stringResource(R.string.showcase_countdown),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.CountdownShowcase) }
            )
        }
    }
}
