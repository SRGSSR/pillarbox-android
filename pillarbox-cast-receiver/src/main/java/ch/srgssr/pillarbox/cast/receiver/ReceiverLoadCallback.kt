/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast.receiver

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata as Media3Metadata
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.tv.media.MediaLoadCommandCallback
import com.google.android.gms.cast.tv.media.MediaManager
import com.google.android.gms.cast.tv.media.MediaResumeSessionRequestData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class ReceiverLoadCallback(val player: PillarboxExoPlayer, val mediaManager: MediaManager): MediaLoadCommandCallback() {
    override fun onLoad(senderId: String?, request: MediaLoadRequestData): Task<MediaLoadRequestData?> {
        try {
            // Initializes MediaStatusModifier and MediaQueueManager overrides using the information from a load request.
            mediaManager.setDataFromLoad(request)

            val mediaItems: List<MediaItem> = request.queueData?.items?.map { item ->
                val metadata = Media3Metadata
                    .Builder()
                    .setTitle(item.media?.metadata?.getString(com.google.android.gms.cast.MediaMetadata.KEY_TITLE))
                    .setArtworkUri(item.media?.metadata?.images?.get(0)?.url)
                    .build()
                val mediaItem = MediaItem
                    .Builder()
                    .setUri(item.media?.contentUrl)
                    .setMediaMetadata(metadata)
                    .setMediaId(item.itemId.toString()) // The iOS sender rely on MediaId so it's very important to set this value. Do not use mediaManager.mediaQueueManager.autoGenerateItemId().toString() but as mediaManager.setDataFromLoad(request) the mediaManager have already itemIds.
                    .build()
                Log.d("ReceiverCallback", "mediaItemId ${mediaItem.mediaId}")
                mediaItem
            } ?: emptyList()

            player.setMediaItems(mediaItems, request.queueData?.startIndex ?: 0, request.currentTime)
            player.prepare()
            player.play()

            mediaManager.mediaStatusModifier.mediaInfoModifier?.setDataFromMediaInfo(mediaManager.mediaQueueManager.mediaQueueData?.items?.first { it.itemId == mediaManager.mediaQueueManager.currentItemId }?.media)
            mediaManager.broadcastMediaStatus()

            Log.d("ReceiverCallback", "MediaLoadRequestData: items = ${mediaItems.map { it.mediaId }}")
        }
        catch (exception: Exception) {
            Log.d("ReceiverCallback", exception.toString())
        }

        return Tasks.forResult(request)
    }

    override fun onResumeSession(senderId: String?, request: MediaResumeSessionRequestData): Task<MediaLoadRequestData?> {
        Log.d("ReceiverCallback", "MediaResumeSessionRequestData")
        return super.onResumeSession(senderId, request)
    }
}
