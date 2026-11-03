/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.downloads

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.exoplayer.offline.Download
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.ui.theme.PillarboxTheme

/**
 * Download button showing the state of a download, like in the Play Store:
 * - not downloaded: a download icon, tap to download.
 * - preparing, queued, restarting or removing: a spinning ring.
 * - downloading: a ring filling up with the progress around a pause icon, tap to pause.
 * - paused: the ring stopped at the current progress around a download icon, tap to resume.
 * - downloaded or failed: a done or error icon, tap to show the available actions.
 *
 * A long press shows the available actions (pause, resume, restart, cancel or remove) in every state with an existing download.
 *
 * @param title The title of the item, used for accessibility.
 * @param download The [Download] of the item, or `null` if it hasn't been downloaded.
 * @param isPreparing Whether the download of the item is being prepared.
 * @param onDownload Called to download the item.
 * @param onPause Called to pause [download].
 * @param onResume Called to resume [download].
 * @param onRestart Called to restart [download].
 * @param onRemove Called to cancel or remove [download].
 * @param modifier The [Modifier] to apply to the layout.
 */
@Composable
@Suppress("LongParameterList", "CyclomaticComplexMethod")
fun DownloadButton(
    title: String,
    download: Download?,
    isPreparing: Boolean,
    onDownload: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    val buttonState = DownloadButtonState.from(download, isPreparing)
    val progress = download?.percentDownloaded?.takeIf { it != C.PERCENTAGE_UNSET.toFloat() }?.let { it / 100f }
    val animatedProgress by animateFloatAsState(targetValue = progress ?: 0f, label = "download_progress")

    val onClick: (() -> Unit)? = when (buttonState) {
        DownloadButtonState.Idle -> onDownload
        DownloadButtonState.Pending, DownloadButtonState.Downloading -> onPause
        DownloadButtonState.Paused -> onResume
        DownloadButtonState.Completed, DownloadButtonState.Failed -> ({ showMenu = true })
        DownloadButtonState.Preparing, DownloadButtonState.Removing -> null
    }
    val onLongClick: (() -> Unit)? = if (download != null && buttonState != DownloadButtonState.Removing) {
        { showMenu = true }
    } else {
        null
    }
    val clickLabel = when (buttonState) {
        DownloadButtonState.Idle -> stringResource(R.string.download_item, title)
        DownloadButtonState.Preparing -> stringResource(R.string.download_preparing_item, title)
        DownloadButtonState.Pending, DownloadButtonState.Downloading -> stringResource(R.string.download_pause_item, title)
        DownloadButtonState.Paused -> stringResource(R.string.download_resume_item, title)
        DownloadButtonState.Completed, DownloadButtonState.Failed, DownloadButtonState.Removing -> {
            stringResource(R.string.download_actions_item, title)
        }
    }
    val stateText = download?.statusText() ?: if (isPreparing) stringResource(R.string.download_state_preparing) else null

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(ButtonSize)
                .clip(CircleShape)
                .combinedClickable(
                    interactionSource = null,
                    indication = ripple(bounded = false, radius = ButtonSize / 2),
                    enabled = onClick != null || onLongClick != null,
                    role = Role.Button,
                    onLongClickLabel = stringResource(R.string.download_actions_item, title).takeIf { onLongClick != null },
                    onLongClick = onLongClick,
                    onClick = onClick ?: {},
                )
                .semantics {
                    contentDescription = clickLabel
                    stateText?.let { stateDescription = it }
                },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = buttonState,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith fadeOut() },
                // The states showing the ring share the same content, so that the ring isn't recreated when switching between them
                contentKey = { state -> state.takeUnless { it.showsRing } },
                label = "download_button",
            ) { state ->
                when (state) {
                    DownloadButtonState.Idle -> DownloadIcon(Icons.Default.Download, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    DownloadButtonState.Completed -> DownloadIcon(Icons.Default.DownloadDone, tint = MaterialTheme.colorScheme.primary)
                    DownloadButtonState.Failed -> DownloadIcon(Icons.Default.ErrorOutline, tint = MaterialTheme.colorScheme.error)
                    DownloadButtonState.Preparing,
                    DownloadButtonState.Pending,
                    DownloadButtonState.Removing,
                    DownloadButtonState.Downloading,
                    DownloadButtonState.Paused -> ProgressRing(
                        state = state,
                        progress = if (state == DownloadButtonState.Downloading || state == DownloadButtonState.Paused) {
                            progress?.let { { animatedProgress } }
                        } else {
                            null
                        },
                    )
                }
            }
        }

        if (download != null) {
            DownloadActionsMenu(
                download = download,
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                onPause = onPause,
                onResume = onResume,
                onRestart = onRestart,
                onRemove = onRemove,
            )
        }
    }
}

/**
 * Menu with the actions available for [download].
 *
 * @param download The [Download].
 * @param expanded Whether the menu is shown.
 * @param onDismissRequest Called when the menu should be dismissed, including after an action is selected.
 * @param onPause Called to pause [download].
 * @param onResume Called to resume [download].
 * @param onRestart Called to restart [download].
 * @param onRemove Called to cancel or remove [download].
 */
@Composable
@Suppress("LongParameterList")
internal fun DownloadActionsMenu(
    download: Download,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onRemove: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        val actionItem: @Composable (Int, ImageVector, () -> Unit) -> Unit = { label, icon, action ->
            DropdownMenuItem(
                text = { Text(stringResource(label)) },
                leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
                onClick = {
                    onDismissRequest()
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

@Composable
private fun DownloadIcon(
    imageVector: ImageVector,
    tint: Color,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier.size(IconSize),
        tint = tint,
    )
}

/**
 * Ring showing the download progress, or spinning if [progress] is `null`, with a pause or resume icon in its center when applicable.
 */
@Composable
private fun ProgressRing(
    state: DownloadButtonState,
    progress: (() -> Float)?,
) {
    val isPaused = state == DownloadButtonState.Paused
    val indicatorColor = if (isPaused) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outlineVariant

    Box(contentAlignment = Alignment.Center) {
        if (progress != null) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(RingSize),
                color = indicatorColor,
                strokeWidth = RingStrokeWidth,
                trackColor = trackColor,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(RingSize),
                color = indicatorColor,
                strokeWidth = RingStrokeWidth,
                trackColor = trackColor,
                strokeCap = StrokeCap.Round,
            )
        }

        val icon = when (state) {
            DownloadButtonState.Pending, DownloadButtonState.Downloading -> Icons.Default.Pause
            DownloadButtonState.Paused -> Icons.Default.Download
            else -> null
        }
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(InnerIconSize),
                tint = if (isPaused) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private enum class DownloadButtonState(val showsRing: Boolean) {
    Idle(showsRing = false),
    Preparing(showsRing = true),
    Pending(showsRing = true),
    Removing(showsRing = true),
    Downloading(showsRing = true),
    Paused(showsRing = true),
    Completed(showsRing = false),
    Failed(showsRing = false);

    companion object {
        fun from(download: Download?, isPreparing: Boolean): DownloadButtonState {
            return when (download?.state) {
                null -> if (isPreparing) Preparing else Idle
                Download.STATE_QUEUED -> if (download.percentDownloaded > 0f) Downloading else Pending
                Download.STATE_DOWNLOADING -> Downloading
                Download.STATE_STOPPED -> Paused
                Download.STATE_COMPLETED -> Completed
                Download.STATE_FAILED -> Failed
                Download.STATE_REMOVING -> Removing
                // Restarting
                else -> Pending
            }
        }
    }
}

private val ButtonSize = 48.dp
private val RingSize = 32.dp
private val RingStrokeWidth = 3.dp
private val IconSize = 24.dp
private val InnerIconSize = 16.dp

@Composable
@Preview(showBackground = true)
private fun DownloadButtonPreview() {
    PillarboxTheme {
        Row {
            DownloadButton(
                title = "Title",
                download = null,
                isPreparing = false,
                onDownload = {},
                onPause = {},
                onResume = {},
                onRestart = {},
                onRemove = {},
            )

            DownloadButton(
                title = "Title",
                download = null,
                isPreparing = true,
                onDownload = {},
                onPause = {},
                onResume = {},
                onRestart = {},
                onRemove = {},
            )
        }
    }
}
