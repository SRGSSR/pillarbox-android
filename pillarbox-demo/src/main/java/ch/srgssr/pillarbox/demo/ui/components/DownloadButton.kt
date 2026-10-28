/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.exoplayer.offline.Download
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.service.DemoDownloadService

private const val Percent = 100f

/**
 * Button that starts, pauses, or resumes the download of [uri] with [DemoDownloadService], and shows its progress.
 * Nothing is displayed when downloads are not supported on this device.
 *
 * @param uri The URI of the media to download.
 * @param modifier The [Modifier] to apply to the button.
 */
@Composable
fun DownloadButton(
    uri: String,
    modifier: Modifier = Modifier,
) {
    if (!DemoDownloadService.isSupported) return

    val context = LocalContext.current
    val downloads by DemoDownloadService.downloads.collectAsState()
    val download = downloads[uri]
    val state = download?.state
    val isInProgress = isInProgress(state)
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

        IconButton(onClick = { DemoDownloadService.toggleDownload(context, uri) }) {
            Icon(
                imageVector = downloadIcon(state),
                contentDescription = stringResource(R.string.download),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun isInProgress(state: Int?): Boolean {
    return state == Download.STATE_QUEUED || state == Download.STATE_DOWNLOADING || state == Download.STATE_RESTARTING
}

private fun downloadIcon(state: Int?): ImageVector {
    return when {
        isInProgress(state) -> Icons.Default.Downloading
        state == Download.STATE_STOPPED -> Icons.Default.PauseCircle
        state == Download.STATE_COMPLETED -> Icons.Default.DownloadDone
        state == Download.STATE_FAILED -> Icons.Default.Download
        else -> Icons.Default.DownloadForOffline // Not downloaded, or being removed.
    }
}
