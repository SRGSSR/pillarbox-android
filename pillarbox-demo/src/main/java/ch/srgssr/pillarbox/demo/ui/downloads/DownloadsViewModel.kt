/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.downloads

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.Download
import ch.srgssr.pillarbox.demo.R
import ch.srgssr.pillarbox.demo.download.DemoDownloads
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.download.DownloadNotSupportedException
import ch.srgssr.pillarbox.download.title
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

/**
 * [AndroidViewModel] handling the downloads of the demo.
 *
 * @param application The [Application].
 */
class DownloadsViewModel(application: Application) : AndroidViewModel(application) {
    private val downloadManager = DemoDownloads.getDownloadManager(application)

    /**
     * All the downloads, most recent first.
     */
    val downloads: StateFlow<List<Download>?> = downloadManager.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /**
     * The directory in which the downloads are stored.
     */
    val downloadDirectory: File = downloadManager.downloadDirectory

    /**
     * Downloads [item].
     *
     * @param item The [DemoItem] to download.
     */
    fun download(item: DemoItem) {
        val application = getApplication<Application>()

        viewModelScope.launch {
            try {
                downloadManager.download(item.toMediaItem())
                showToast(application.getString(R.string.download_added, item.title.orEmpty()))
            } catch (e: CancellationException) {
                throw e
            } catch (e: DownloadNotSupportedException) {
                showToast(application.getString(R.string.download_not_supported, e.message))
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.e(TAG, "Unable to download ${item.uri}", e)
                showToast(application.getString(R.string.download_failed))
            }
        }
    }

    /**
     * Pauses [download].
     */
    fun pause(download: Download) {
        downloadManager.pause(download.request.id)
    }

    /**
     * Resumes [download].
     */
    fun resume(download: Download) {
        downloadManager.resume(download.request.id)
    }

    /**
     * Restarts [download] from scratch.
     */
    fun restart(download: Download) {
        viewModelScope.launch {
            try {
                downloadManager.restart(download.request.id)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.e(TAG, "Unable to restart ${download.request.id}", e)
                showToast(getApplication<Application>().getString(R.string.download_failed))
            }
        }
    }

    /**
     * Cancels [download] if it is in progress, and removes it.
     */
    fun remove(download: Download) {
        downloadManager.remove(download.request.id)
    }

    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val TAG = "DownloadsViewModel"
    }
}

/**
 * Converts this [Download] into a [DemoItem] that can be played. When it is played by a player using the [DemoDownloads] manager,
 * the downloaded content is played.
 */
fun Download.toDemoItem(): DemoItem {
    val id = request.id
    // The download id is the URN of SRG SSR content, or the URL of the stream otherwise
    return if (id.startsWith("urn:")) {
        DemoItem.URN(urn = id, title = title)
    } else {
        DemoItem.URL(uri = id, title = title)
    }
}
