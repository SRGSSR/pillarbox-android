/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.service

import androidx.media3.common.MediaItem
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * Default media session callback that allow to add [MediaItem] with an url or an mediaId to the MediaController.
 *
 * @see [MediaSession.Builder.setCallback]
 */
interface DefaultMediaSessionCallback : MediaSession.Callback {

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        for (mediaItem in mediaItems) {
            if (mediaItem.localConfiguration == null && mediaItem.mediaId.isBlank()) {
                return Futures.immediateFailedFuture(UnsupportedOperationException())
            }
        }
        return Futures.immediateFuture(mediaItems)
    }
}
