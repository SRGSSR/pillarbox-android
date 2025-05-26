/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.ui.player.DemoPlayerView
import ch.srgssr.pillarbox.demo.ui.theme.paddings

/**
 * Showcase allowing the user to decide if the current media item should pause when it ends.
 *
 * @param playlist The [Playlist] to play.
 * @param modifier The [Modifier] to apply to the layout.
 */
@Composable
fun CustomPlaybackSettingsShowcase(
    playlist: Playlist,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val player = remember(playlist) {
        PlayerModule.provideDefaultPlayer(context).apply {
            setMediaItems(playlist.items.map { it.toMediaItem() })
            prepare()
            play()
        }
    }

    var pauseAtEndOfItem by remember { mutableStateOf(player.pauseAtEndOfMediaItems) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(pauseAtEndOfItem) {
                    pauseAtEndOfItem = !pauseAtEndOfItem
                    player.pauseAtEndOfMediaItems = pauseAtEndOfItem
                }
                .padding(
                    horizontal = MaterialTheme.paddings.baseline,
                    vertical = MaterialTheme.paddings.small,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(R.string.pause_end_media_items))

            Switch(
                checked = pauseAtEndOfItem,
                onCheckedChange = null,
            )
        }

        DemoPlayerView(
            player = player,
            displayPlaylist = true,
        )

        LifecycleResumeEffect(player) {
            player.play()
            onPauseOrDispose {
                player.pause()
            }
        }
        DisposableEffect(player) {
            onDispose {
                player.release()
            }
        }
    }
}
