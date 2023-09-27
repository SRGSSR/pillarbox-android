/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.player.mediacontroller.MediaControllerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

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
    Column(modifier = Modifier.verticalScroll(enabled = true, state = scrollState)) {
        HeaderView(header = stringResource(id = R.string.layouts))
        ItemView(title = stringResource(id = R.string.simple_player)) {
            navController.navigate(NavigationRoutes.simplePlayer)
        }
        Divider()
        ItemView(title = stringResource(id = R.string.story)) {
            navController.navigate(NavigationRoutes.story)
        }
        Divider()
        HeaderView(header = stringResource(id = R.string.playlists))
        for (playlist in listItems) {
            ItemView(title = playlist.title) {
                SimplePlayerActivity.startActivity(context, playlist)
            }
            Divider()
        }
        HeaderView(header = stringResource(id = R.string.system_integration))
        ItemView(title = stringResource(id = R.string.auto)) {
            val intent = Intent(context, MediaControllerActivity::class.java)
            context.startActivity(intent)
        }

        HeaderView(header = stringResource(id = R.string.embeddings))
        ItemView(title = stringResource(id = R.string.adaptive)) {
            navController.navigate(NavigationRoutes.adaptive)
        }
        Divider()
        ItemView(title = stringResource(id = R.string.player_swap)) {
            navController.navigate(NavigationRoutes.playerSwap)
        }
        HeaderView(header = stringResource(id = R.string.exoplayer))
        ItemView(title = stringResource(id = R.string.exoplayer_view)) {
            navController.navigate(NavigationRoutes.exoPlayerSample)
        }
        HeaderView(header = stringResource(id = R.string.tracking))
        ItemView(title = stringResource(id = R.string.tracker_example)) {
            navController.navigate(NavigationRoutes.trackingSample)
        }
    }
}

@Composable
private fun ItemView(title: String, onClick: () -> Unit) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        text = title,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun HeaderView(header: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        text = header,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineMedium
    )
    Divider()
}

@Preview(showBackground = true)
@Composable
private fun PreviewItems() {
    PillarboxTheme {
        Column() {
            HeaderView(header = "Header 1")
            ItemView("Item 1") {}
            Divider()
            ItemView("Item 2") {}
            Divider()
            HeaderView(header = "header 2")
            ItemView("Item 3") {}
        }
    }
}
