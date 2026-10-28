/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.service.DemoDownloadService
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.demo.ui.components.DemoListItemView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerView

private const val StopReasonPaused = 1
private const val Percent = 100f

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
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(DemoDownloadService.getCacheDataSourceFactory(context))
            )
            .build()
    }
    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    val downloadEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    // Start the service so its DownloadManager loads downloads saved in earlier sessions.
    LaunchedEffect(Unit) {
        if (downloadEnabled) {
            DownloadService.start(context, DemoDownloadService::class.java)
        }
    }

    // Downloads use the URI as ID, see DownloadRequest below.
    val downloads by DemoDownloadService.downloads.collectAsState()

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
                        val download = downloads[item.uri]
                        DownloadItemRow(
                            item = item,
                            download = download,
                            downloadEnabled = downloadEnabled,
                            onClick = {
                                // The player's DefaultMediaSourceFactory already reads from the
                                // download cache and infers the format (HLS, MP4, MP3...) from the URI.
                                player.setMediaItem(MediaItem.fromUri(item.uri))
                                player.prepare()
                                player.play()
                            },
                            onDownloadClick = {
                                when (download?.state) {
                                    // A null download is not downloaded yet.
                                    null, Download.STATE_FAILED, Download.STATE_REMOVING -> {
                                        // Using the URI as ID means downloading the same item twice
                                        // updates the existing download instead of adding a duplicate.
                                        val downloadRequest = DownloadRequest.Builder(item.uri, item.uri.toUri()).build()
                                        DownloadService.sendAddDownload(
                                            context,
                                            DemoDownloadService::class.java,
                                            downloadRequest,
                                            false,
                                        )
                                    }

                                    // A non-zero stop reason pauses this download only.
                                    Download.STATE_QUEUED,
                                    Download.STATE_DOWNLOADING,
                                    Download.STATE_RESTARTING -> DownloadService.sendSetStopReason(
                                        context,
                                        DemoDownloadService::class.java,
                                        item.uri,
                                        StopReasonPaused,
                                        false,
                                    )

                                    Download.STATE_STOPPED -> DownloadService.sendSetStopReason(
                                        context,
                                        DemoDownloadService::class.java,
                                        item.uri,
                                        Download.STOP_REASON_NONE,
                                        false,
                                    )

                                    else -> Unit // Download.STATE_COMPLETED
                                }
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

@Composable
private fun DownloadItemRow(
    item: DemoItem.URL,
    download: Download?,
    downloadEnabled: Boolean,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DemoListItemView(
            title = item.title ?: "No title",
            modifier = Modifier.weight(1f),
            subtitle = item.description,
            languageTag = item.languageTag,
            onClick = onClick,
        )

        if (downloadEnabled) {
            DownloadButton(
                download = download,
                onClick = onDownloadClick,
            )
        }
    }
}

@Composable
private fun DownloadButton(
    download: Download?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = download?.state
    val isInProgress = state == Download.STATE_QUEUED || state == Download.STATE_DOWNLOADING || state == Download.STATE_RESTARTING
    // Progress between 0 and 1, or null when it is not known yet.
    val progress = download?.percentDownloaded
        ?.takeIf { it != C.PERCENTAGE_UNSET.toFloat() }
        ?.let { it / Percent }

    // The progress ring is drawn behind the icon button, at the same size.
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when {
            // Progress is unknown at the very start of a download.
            isInProgress && progress == null -> {
                CircularProgressIndicator(modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
            }

            isInProgress || state == Download.STATE_STOPPED -> {
                CircularProgressIndicator(
                    progress = { progress ?: 0f },
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = 3.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        IconButton(onClick = onClick) {
            val icon = when {
                isInProgress -> Icons.Default.Downloading
                state == Download.STATE_STOPPED -> Icons.Default.PauseCircle
                state == Download.STATE_COMPLETED -> Icons.Default.DownloadDone
                state == Download.STATE_FAILED -> Icons.Default.Download
                else -> Icons.Default.DownloadForOffline // Not downloaded, or being removed.
            }

            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.download),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
