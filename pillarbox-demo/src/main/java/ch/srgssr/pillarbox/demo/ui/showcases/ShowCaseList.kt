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
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.player.mediacontroller.MediaControllerActivity

/**
 * Showcases home page.
 *
 * @param navController The [NavController] to navigate between screens.
 */
@Composable
fun ShowCaseList(navController: NavController) {
    val context = LocalContext.current
    val playlists = remember {
        listOf(
            Playlist.VideoUrls,
            Playlist.VideoUrns,
            Playlist.MixedContent,
            Playlist.MixedContentLiveDvrVod,
            Playlist.MixedContentLiveOnlyVod,
            Playlist("Empty", emptyList())
        )
    }
    val titleModifier = remember {
        Modifier.padding(
            start = 16.dp,
            top = 8.dp
        )
    }
    val itemModifier = remember {
        Modifier.fillMaxWidth()
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        DemoListHeaderView(
            title = stringResource(R.string.layouts),
            modifier = Modifier.padding(start = 16.dp)
        )

        DemoListSectionView {
            DemoListItemView(
                title = stringResource(R.string.simple_player),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.simplePlayer) }
            )

            Divider()

            DemoListItemView(
                title = stringResource(R.string.story),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.story) }
            )
        }

        DemoListHeaderView(
            title = stringResource(R.string.playlists),
            modifier = titleModifier
        )

        DemoListSectionView {
            playlists.forEachIndexed { index, item ->
                DemoListItemView(
                    title = item.title,
                    modifier = itemModifier,
                    onClick = { SimplePlayerActivity.startActivity(context, item) }
                )

                if (index < playlists.lastIndex) {
                    Divider()
                }
            }
        }

        DemoListHeaderView(
            title = stringResource(R.string.system_integration),
            modifier = titleModifier
        )

        DemoListSectionView {
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
            title = stringResource(R.string.embeddings),
            modifier = titleModifier
        )

        DemoListSectionView {
            DemoListItemView(
                title = stringResource(R.string.adaptive),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.adaptive) }
            )

            Divider()

            DemoListItemView(
                title = stringResource(R.string.player_swap),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.playerSwap) }
            )
        }

        DemoListHeaderView(
            title = stringResource(R.string.exoplayer),
            modifier = titleModifier
        )

        DemoListSectionView {
            DemoListItemView(
                title = stringResource(R.string.exoplayer_view),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.exoPlayerSample) }
            )
        }

        DemoListHeaderView(
            title = stringResource(R.string.tracking),
            modifier = titleModifier
        )

        DemoListSectionView(modifier = Modifier.padding(bottom = 16.dp)) {
            DemoListItemView(
                title = stringResource(R.string.tracker_example),
                modifier = itemModifier,
                onClick = { navController.navigate(NavigationRoutes.trackingSample) }
            )
        }
    }
}
