/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.downloads

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.srgssr.pillarbox.demo.shared.data.DemoItem

/**
 * [DownloadButton] downloading [item] with the [DownloadsViewModel], and showing the state of its download.
 *
 * @param item The [DemoItem] to download.
 * @param title The title of the item, used for accessibility.
 * @param modifier The [Modifier] to apply to the layout.
 * @param downloadsViewModel The [DownloadsViewModel] handling the download.
 */
@Composable
fun DemoItemDownloadButton(
    item: DemoItem,
    title: String,
    modifier: Modifier = Modifier,
    downloadsViewModel: DownloadsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val downloadId = remember(item) { downloadsViewModel.getDownloadId(item) }
    val downloadsById by downloadsViewModel.downloadsById.collectAsStateWithLifecycle()
    val preparingIds by downloadsViewModel.preparingIds.collectAsStateWithLifecycle()
    val download = downloadsById[downloadId]
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    DownloadButton(
        title = title,
        download = download,
        isPreparing = downloadId in preparingIds,
        onDownload = {
            // The download progress is also displayed in a notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            downloadsViewModel.download(item)
        },
        onPause = { download?.let(downloadsViewModel::pause) },
        onResume = { download?.let(downloadsViewModel::resume) },
        onRestart = { download?.let(downloadsViewModel::restart) },
        modifier = modifier,
    )
}
