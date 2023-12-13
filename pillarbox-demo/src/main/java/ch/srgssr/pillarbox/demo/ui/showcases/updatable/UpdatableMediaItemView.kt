/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.updatable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import ch.srgssr.pillarbox.core.business.DefaultPillarbox
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.player.currentMediaItemAsFlow
import ch.srgssr.pillarbox.ui.extension.currentMediaMetadataAsState
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.concurrent.timer
import kotlin.time.Duration.Companion.seconds

/**
 * Media item updater
 * Update periodically the title of the current MediaItem played.
 * We assume that there is only one item.
 */
@Stable
private class MediaItemUpdater(private val title: String) {
    private var counter: Int = 0

    /**
     * Start updating the title of the current media item from [player].
     *
     * @param player The player.
     */
    @Composable
    fun update(player: Player) {
        val currentMediaItem = player.currentMediaItemAsFlow().collectAsState(initial = player.currentMediaItem)
        DisposableEffect(Unit) {
            val timer = timer(name = "update-item", period = 3.seconds.inWholeMilliseconds) {
                // update media item only if url as loaded from urn
                currentMediaItem.value?.let {
                    if (it.localConfiguration != null) {
                        counter++
                        var newMediaItem = it.buildUpon()
                            .setMediaMetadata(
                                it.mediaMetadata.buildUpon()
                                    .setTitle("$title - $counter")
                                    .build()
                            )
                            .build()
                        if (counter == 5) {
                            newMediaItem = MediaItem.Builder()
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle("StreamUrl")
                                        .build()
                                )
                                .setUri(DemoItem.AppleBasic_16_9_TS_HLS.uri)
                                .build()
                        }
                        MainScope().launch(Dispatchers.Main) {
                            if (player.isCommandAvailable(Player.COMMAND_CHANGE_MEDIA_ITEMS)) {
                                player.replaceMediaItem(player.currentMediaItemIndex, newMediaItem)
                            }
                        }
                    }
                }
            }
            onDispose {
                timer.cancel()
            }
        }
    }
}

private val initialMediaItem = MediaItem.Builder()
    .setMediaId(DemoItem.OnDemandHorizontalVideo.uri)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(DemoItem.OnDemandHorizontalVideo.title)
            .build()
    )
    .build()

@Composable
internal fun UpdatableMediaItemView() {
    val context = LocalContext.current
    val mediaItemUpdater = remember {
        MediaItemUpdater(initialMediaItem.mediaMetadata.title.toString())
    }

    val player = remember {
        DefaultPillarbox(context = context).apply {
            setMediaItem(initialMediaItem)
        }
    }
    val mediaSession = remember(player) {
        MediaSession.Builder(context, player).build()
    }
    mediaItemUpdater.update(player)

    val currentItem = player.currentMediaMetadataAsState()
    PlayerSurface(player = player) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            Text(
                color = Color.Green,
                text = "${currentItem.title}"
            )
        }
    }

    DisposableEffect(player, context) {
        player.prepare()
        player.play()
        onDispose {
            mediaSession.release()
            player.stop()
            player.release()
        }
    }
}
