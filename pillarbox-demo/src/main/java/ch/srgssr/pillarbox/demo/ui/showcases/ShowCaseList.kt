/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.ui.DemoItemView
import ch.srgssr.pillarbox.demo.ui.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.player.mediacontroller.MediaControllerActivity

/**
 * Showcases home page
 *
 * @param navController The NavController to navigate into within MainNavigation.
 */
@Composable
fun ShowCaseList(navController: NavController) {
    val context = LocalContext.current
    val listItems = remember {
        listOf(
            Playlist.VideoUrls,
            Playlist.VideoUrns,
            Playlist.MixedContent,
            Playlist.MixedContentLiveDvrVod,
            Playlist.MixedContentLiveOnlyVod,
            Playlist("Empty", emptyList())
        )
    }
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(state = scrollState)
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val itemModifier = Modifier.fillMaxWidth()
        DemoListHeaderView(modifier = itemModifier, title = stringResource(id = R.string.layouts))
        DemoItemView(modifier = itemModifier, title = stringResource(id = R.string.simple_player)) {
            navController.navigate(NavigationRoutes.simplePlayer)
        }
        DemoItemView(modifier = itemModifier, title = stringResource(id = R.string.story)) {
            navController.navigate(NavigationRoutes.story)
        }

        DemoListHeaderView(modifier = itemModifier, title = stringResource(id = R.string.playlists))
        for (playlist in listItems) {
            DemoItemView(modifier = itemModifier, title = playlist.title) {
                SimplePlayerActivity.startActivity(context, playlist)
            }
        }
        DemoListHeaderView(modifier = itemModifier, title = stringResource(id = R.string.system_integration))
        DemoItemView(modifier = itemModifier, title = stringResource(id = R.string.auto)) {
            val intent = Intent(context, MediaControllerActivity::class.java)
            context.startActivity(intent)
        }

        DemoListHeaderView(modifier = itemModifier, title = stringResource(id = R.string.embeddings))
        DemoItemView(modifier = itemModifier, title = stringResource(id = R.string.adaptive)) {
            navController.navigate(NavigationRoutes.adaptive)
        }
        DemoItemView(modifier = itemModifier, title = stringResource(id = R.string.player_swap)) {
            navController.navigate(NavigationRoutes.playerSwap)
        }

        DemoListHeaderView(modifier = itemModifier, title = stringResource(id = R.string.exoplayer))
        DemoItemView(modifier = itemModifier, title = stringResource(id = R.string.exoplayer_view)) {
            navController.navigate(NavigationRoutes.exoPlayerSample)
        }

        DemoListHeaderView(modifier = itemModifier, title = stringResource(id = R.string.tracking))
        DemoItemView(modifier = itemModifier, title = stringResource(id = R.string.tracker_example)) {
            navController.navigate(NavigationRoutes.trackingSample)
        }
    }
}
