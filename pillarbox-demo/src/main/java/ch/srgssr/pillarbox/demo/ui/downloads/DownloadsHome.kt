/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.downloads

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.exoplayer.offline.Download
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.ui.components.DemoListHeaderView
import ch.srgssr.pillarbox.demo.ui.components.DemoListSectionView
import ch.srgssr.pillarbox.demo.ui.player.SimplePlayerActivity
import ch.srgssr.pillarbox.demo.ui.theme.paddings
import ch.srgssr.pillarbox.download.title

/**
 * Downloads home page, listing all the downloads with their progress.
 *
 * @param modifier The [Modifier] to apply to the layout.
 * @param downloadsViewModel The [DownloadsViewModel].
 */
@Composable
fun DownloadsHome(
    modifier: Modifier = Modifier,
    downloadsViewModel: DownloadsViewModel = viewModel(),
) {
    val downloads by downloadsViewModel.downloads.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when {
        downloads == null -> Unit
        downloads.isNullOrEmpty() -> EmptyDownloads(modifier = modifier)
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(MaterialTheme.paddings.baseline),
        ) {
            item {
                DemoListHeaderView(
                    title = stringResource(R.string.downloads_stored_in, downloadsViewModel.downloadDirectory.absolutePath),
                    modifier = Modifier.padding(start = MaterialTheme.paddings.baseline),
                )
            }

            item {
                DemoListSectionView {
                    val items = downloads.orEmpty()
                    items.forEachIndexed { index, download ->
                        DownloadItemView(
                            download = download,
                            onClick = { SimplePlayerActivity.startActivity(context, download.toDemoItem()) },
                            onPause = { downloadsViewModel.pause(download) },
                            onResume = { downloadsViewModel.resume(download) },
                            onRestart = { downloadsViewModel.restart(download) },
                            onRemove = { downloadsViewModel.remove(download) },
                        )

                        if (index < items.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Suppress("LongParameterList")
private fun DownloadItemView(
    download: Download,
    onClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onRemove: () -> Unit,
) {
    val isCompleted = download.state == Download.STATE_COMPLETED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isCompleted, onClick = onClick)
            .padding(start = MaterialTheme.paddings.baseline, top = MaterialTheme.paddings.small, bottom = MaterialTheme.paddings.small),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.baseline),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {},
        ) {
            Text(
                text = download.title ?: download.request.id,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
            )

            Row(
                modifier = Modifier.padding(top = MaterialTheme.paddings.micro),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.mini),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                download.state.icon()?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = download.state.color(),
                    )
                }

                Text(
                    text = download.statusText(),
                    color = download.state.color(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (download.state.showsProgress()) {
                val progress = download.percentDownloaded.coerceAtLeast(0f) / 100f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.paddings.small),
                )
            }
        }

        DownloadActions(
            download = download,
            onPause = onPause,
            onResume = onResume,
            onRestart = onRestart,
            onRemove = onRemove,
        )
    }
}

@Composable
private fun DownloadActions(
    download: Download,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onRemove: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.download_actions),
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            val actionItem: @Composable (Int, ImageVector, () -> Unit) -> Unit = { label, icon, action ->
                DropdownMenuItem(
                    text = { Text(stringResource(label)) },
                    leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        action()
                    },
                )
            }

            when (download.state) {
                Download.STATE_QUEUED,
                Download.STATE_DOWNLOADING -> actionItem(R.string.download_pause, Icons.Default.Pause, onPause)

                Download.STATE_STOPPED -> actionItem(R.string.download_resume, Icons.Default.Download, onResume)

                else -> Unit
            }

            if (download.state != Download.STATE_REMOVING) {
                actionItem(R.string.download_restart, Icons.Default.Refresh, onRestart)

                val removeLabel = if (download.state == Download.STATE_COMPLETED) R.string.download_remove else R.string.download_cancel
                actionItem(removeLabel, Icons.Default.Delete, onRemove)
            }
        }
    }
}

@Composable
private fun EmptyDownloads(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.paddings.medium)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
        )

        Text(
            text = stringResource(R.string.downloads_empty),
            modifier = Modifier.padding(top = MaterialTheme.paddings.small),
        )

        Text(
            text = stringResource(R.string.downloads_empty_hint),
            modifier = Modifier.padding(top = MaterialTheme.paddings.mini),
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun Download.statusText(): String {
    val context = LocalContext.current
    val stateLabel = stringResource(state.labelResId())
    val size = contentLength.takeIf { it != C.LENGTH_UNSET.toLong() }?.let { Formatter.formatShortFileSize(context, it) }

    return when (state) {
        // The content length of adaptive streams (HLS, DASH) is unknown, but all the bytes are downloaded once completed
        Download.STATE_COMPLETED -> listOf(stateLabel, size ?: Formatter.formatShortFileSize(context, bytesDownloaded)).joinToString(" · ")
        Download.STATE_FAILED -> stateLabel
        else -> {
            val downloaded = Formatter.formatShortFileSize(context, bytesDownloaded)
            val percent = percentDownloaded.takeIf { it != C.PERCENTAGE_UNSET.toFloat() }?.let { "${it.toInt()}%" }
            val bytes = size?.let { "$downloaded / $it" } ?: downloaded

            listOfNotNull(stateLabel, percent, bytes).joinToString(" · ")
        }
    }
}

private fun @Download.State Int.labelResId(): Int {
    return when (this) {
        Download.STATE_QUEUED -> R.string.download_state_queued
        Download.STATE_DOWNLOADING -> R.string.download_state_downloading
        Download.STATE_STOPPED -> R.string.download_state_paused
        Download.STATE_COMPLETED -> R.string.download_state_completed
        Download.STATE_FAILED -> R.string.download_state_failed
        Download.STATE_REMOVING -> R.string.download_state_removing
        else -> R.string.download_state_restarting
    }
}

private fun @Download.State Int.icon(): ImageVector? {
    return when (this) {
        Download.STATE_COMPLETED -> Icons.Default.DownloadDone
        Download.STATE_FAILED -> Icons.Default.ErrorOutline
        Download.STATE_STOPPED -> Icons.Default.Pause
        else -> null
    }
}

@Composable
private fun @Download.State Int.color() = when (this) {
    Download.STATE_COMPLETED -> MaterialTheme.colorScheme.primary
    Download.STATE_FAILED -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.outline
}

private fun @Download.State Int.showsProgress(): Boolean {
    return this == Download.STATE_QUEUED ||
        this == Download.STATE_DOWNLOADING ||
        this == Download.STATE_STOPPED ||
        this == Download.STATE_RESTARTING
}
