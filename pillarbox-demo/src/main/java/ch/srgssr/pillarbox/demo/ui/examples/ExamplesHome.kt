/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.examples

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.ui.examples.ExamplesViewModel
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.downloads.DownloadsViewModel
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Examples home page.
 *
 * Display all the [DemoItem] in a List.
 *
 * @param onDownloadStarted Called when the download of an item is requested.
 */
@Composable
fun ExamplesHome(onDownloadStarted: () -> Unit = {}) {
    val examplesViewModel: ExamplesViewModel = viewModel()
    val downloadsViewModel: DownloadsViewModel = viewModel()
    val context = LocalContext.current
    val playlists by examplesViewModel.contents.collectAsState()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    ListStreamView(
        playlists = playlists,
        onItemClicked = { SimplePlayerActivity.startActivity(context, it) },
        onDownloadClicked = { item ->
            // The download progress is displayed in a notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            downloadsViewModel.download(item)
            onDownloadStarted()
        },
    )
}

@Composable
private fun ListStreamView(
    playlists: List<Playlist>,
    onItemClicked: (item: DemoItem) -> Unit,
    onDownloadClicked: (item: DemoItem) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(MaterialTheme.paddings.baseline),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
    ) {
        item(contentType = "url_urn_input") {
            Card(modifier = Modifier.fillMaxWidth()) {
                InsertContentView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.paddings.small),
                    onPlayClick = onItemClicked
                )
            }
        }

        items(
            items = playlists,
            contentType = { "playlist" }
        ) { playlist ->
            DemoListHeaderView(
                title = playlist.title,
                modifier = Modifier.padding(start = MaterialTheme.paddings.baseline),
                languageTag = playlist.languageTag,
            )

            DemoListSectionView {
                playlist.items.forEachIndexed { index, item ->
                    val title = item.title ?: "No title"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DemoListItemView(
                            title = title,
                            modifier = Modifier.weight(1f),
                            subtitle = item.description,
                            languageTag = item.languageTag,
                            onClick = { onItemClicked(item) },
                        )

                        IconButton(onClick = { onDownloadClicked(item) }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = stringResource(R.string.download_item, title),
                            )
                        }
                    }

                    if (index < playlist.items.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ListStreamPreview() {
    val playlist = Playlist(
        "Playlist title 1",
        listOf(
            DemoItem.URL(title = "Title 1", uri = "Uri 1"),
            DemoItem.URL(title = "Title 2", uri = "Uri 2"),
            DemoItem.URL(title = "Title 3", uri = "Uri 3"),
        )
    )
    val playlists = listOf(playlist, playlist.copy(title = "Playlist title 2"))

    PillarboxTheme {
        ListStreamView(
            playlists = playlists,
            onItemClicked = {},
            onDownloadClicked = {},
        )
    }
}
