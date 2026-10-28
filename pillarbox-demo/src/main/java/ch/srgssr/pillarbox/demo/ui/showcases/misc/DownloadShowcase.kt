/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ch.srgssr.pillarbox.demo.service.DemoDownloadService
import ch.srgssr.pillarbox.demo.service.DownloadAssetLoader
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.components.DownloadButton
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerView

private val DownloadItems = listOf(
    SamplesSRG.OnDemandVideoMP4,
    SamplesSRG.OnDemandAudioMP3,
    SamplesSRG.Tataki_URL_1,
    SamplesSRG.Tataki_URL_2,
    SamplesSRG.Tataki_URL_3,
)

/**
 * A showcase demonstrating how to download content and play it from the download cache.
 */
@Composable
fun DownloadShowcase() {
    val context = LocalContext.current
    val player = remember {
        PlayerModule.provideDefaultPlayer(context, listOf(DownloadAssetLoader(context)))
    }
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(Unit) {
        DemoDownloadService.start(context)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ExoPlayerView(
            player = player,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(MaterialTheme.paddings.baseline),
        ) {
            item {
                DemoListSectionView {
                    DownloadItems.forEachIndexed { index, item ->
                        DemoListItemView(
                            title = item.title ?: "No title",
                            modifier = Modifier.fillMaxWidth(),
                            subtitle = item.description,
                            languageTag = item.languageTag,
                            trailingContent = { DownloadButton(uri = item.uri) },
                            onClick = {
                                player.setMediaItem(item.toMediaItem())
                                player.prepare()
                                player.play()
                            },
                        )

                        if (index < DownloadItems.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
