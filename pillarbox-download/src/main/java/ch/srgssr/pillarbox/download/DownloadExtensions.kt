/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.download

import androidx.media3.exoplayer.offline.Download

/**
 * The title of the downloaded media, as stored by [PillarboxDownloadManager.download], or `null` if unknown.
 */
val Download.title: String?
    get() = request.data.takeIf { it.isNotEmpty() }?.decodeToString()

/**
 * Whether the download has been paused with [PillarboxDownloadManager.pause].
 */
val Download.isPaused: Boolean
    get() = state == Download.STATE_STOPPED && stopReason == PillarboxDownloadManager.STOP_REASON_PAUSED
