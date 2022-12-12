/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrations

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.ui.NavigationRoutes
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.player.mediacontroller.MediaControllerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Integrations home that displays integrations sample.
 * For exemple, playlists, story, ...
 *
 * @param navController
 * @param playlistsViewModel
 */
@Composable
fun IntegrationsHome(navController: NavController, playlistsViewModel: PlaylistsViewModel) {
    val context = LocalContext.current
    val listItems = playlistsViewModel.listPlaylist.collectAsState()
    Column {
        HeaderView(header = stringResource(id = R.string.playlists))
        Divider()
        for (playlist in listItems.value) {
            ItemView(title = playlist.title) {
                SimplePlayerActivity.startActivity(context, playlist)
            }
            Divider()
        }
        ItemView(title = stringResource(id = R.string.story)) {
            navController.navigate(NavigationRoutes.story)
        }
        ItemView(title = stringResource(id = R.string.media_controller)) {
            val intent = Intent(context, MediaControllerActivity::class.java)
            context.startActivity(intent)
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
        style = MaterialTheme.typography.body1
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
        style = MaterialTheme.typography.h4
    )
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
