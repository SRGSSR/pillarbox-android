/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.ui.player

import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media3.common.C
import ch.srgssr.pillarbox.core.business.cast.SRGMediaItemConverter
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.RequestData
import com.google.android.gms.cast.TextTrackStyle
import com.google.android.gms.cast.tv.CastReceiverContext
import com.google.android.gms.cast.tv.media.EditAudioTracksData
import com.google.android.gms.cast.tv.media.EditTracksInfoData
import com.google.android.gms.cast.tv.media.FetchItemsRequestData
import com.google.android.gms.cast.tv.media.MediaCommandCallback
import com.google.android.gms.cast.tv.media.MediaLoadCommandCallback
import com.google.android.gms.cast.tv.media.MediaResumeSessionRequestData
import com.google.android.gms.cast.tv.media.QueueInsertRequestData
import com.google.android.gms.cast.tv.media.QueueRemoveRequestData
import com.google.android.gms.cast.tv.media.QueueReorderRequestData
import com.google.android.gms.cast.tv.media.QueueUpdateRequestData
import com.google.android.gms.cast.tv.media.SeekRequestData
import com.google.android.gms.cast.tv.media.SetPlaybackRateRequestData
import com.google.android.gms.cast.tv.media.StoreSessionRequestData
import com.google.android.gms.cast.tv.media.StoreSessionResponseData
import com.google.android.gms.cast.tv.media.UserActionRequestData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class PillarboxCastReceiver(private val mediaSession: PillarboxMediaSession) {
    private val castReceiverContext = CastReceiverContext.getInstance()
    private val mediaManager = CastReceiverContext.getInstance().mediaManager
    private val mediaCommandCallback = MediaCommandCallbackImpl()
    private val mediaLoadCommandCallback = MediaLoadCommandCallbackImpl()

    private val itemConvert = SRGMediaItemConverter()

    init {
        val token = MediaSessionCompat.Token.fromToken(mediaSession.mediaSession.platformToken)
        mediaManager.setSessionCompatToken(token)
        mediaManager.setMediaLoadCommandCallback(mediaLoadCommandCallback)
        mediaManager.setMediaCommandCallback(mediaCommandCallback)
    }

    fun release() {
        mediaManager.setMediaCommandCallback(null)
        mediaManager.setMediaLoadCommandCallback(null)
        mediaManager.mediaQueueManager.clear()
        mediaManager.broadcastMediaStatus()
        mediaManager.setSessionCompatToken(null)
    }

    fun onNewIntent(intent: Intent): Boolean = mediaManager.onNewIntent(intent)

    /**
     * The queue model in Cast is different from that in MediaSession. The Cast Connect library doesn't support reading a queue provided by MediaSession.
     */
    internal inner class MediaCommandCallbackImpl : MediaCommandCallback() {
        // This class has the default implementation to call methods of the MediaSession which MediaManager currently attaches to.
        override fun onQueueInsert(p0: String?, requestData: QueueInsertRequestData): Task<Void?> {
            Log.d(TAG, "onQueueInsert $p0")
            return super.onQueueInsert(p0, requestData)
        }

        override fun onQueueRemove(p0: String?, requestData: QueueRemoveRequestData): Task<Void?> {
            Log.d(TAG, "onQueueRemove")
            return super.onQueueRemove(p0, requestData)
        }

        override fun onQueueReorder(p0: String?, requestData: QueueReorderRequestData): Task<Void?> {
            Log.d(TAG, "onQueueReorder")
            return super.onQueueReorder(p0, requestData)
        }

        override fun onQueueUpdate(p0: String?, requestData: QueueUpdateRequestData): Task<Void?> {
            Log.d(
                TAG,
                "onQueueUpdate currentItemId = ${requestData.currentItemId} jump = ${requestData.jump} ${mediaManager.mediaQueueManager.queueItems?.size} ${requestData.shuffle}"
            )
            requestData.items?.map { Log.d(TAG, "  ${it.prettyString()}") } ?: Log.d(TAG, "No items")
            var newItemId = MediaQueueItem.INVALID_ITEM_ID
            if (requestData.jump != null) {
                newItemId = requestData.jump!!
            } else if (requestData.currentItemId != null) {
                newItemId = requestData.currentItemId!!
            }
            if (newItemId != MediaQueueItem.INVALID_ITEM_ID) {
                mediaManager.mediaQueueManager.currentItemId = newItemId
                // TODO check if newItemId really a list index?
                mediaSession.player.seekTo(newItemId - 1, C.TIME_UNSET)
                mediaManager.broadcastMediaStatus()
            }
            return super.onQueueUpdate(p0, requestData)
        }

        override fun onFetchItems(p0: String?, requestData: FetchItemsRequestData): Task<Void?> {
            Log.d(TAG, "onFetchItems")
            return super.onFetchItems(p0, requestData)
        }

        override fun onEditAudioTracks(
            p0: String?,
            requestData: EditAudioTracksData
        ): Task<Void?> {
            Log.d(TAG, "onEditAudioTracks: ")
            return super.onEditAudioTracks(p0, requestData)
        }

        override fun onEditTracksInfo(
            p0: String?,
            requestData: EditTracksInfoData
        ): Task<Void?> {
            Log.d(TAG, "onEditTracksInfo: ")
            return super.onEditTracksInfo(p0, requestData)
        }

        override fun onPause(
            p0: String?,
            requestData: RequestData
        ): Task<Void?> {
            Log.d(TAG, "onPause: ")
            return super.onPause(p0, requestData)
        }

        override fun onPlay(
            p0: String?,
            requestData: RequestData
        ): Task<Void?> {
            Log.d(TAG, "onPlay: ")
            return super.onPlay(p0, requestData)
        }

        override fun onPlayAgain(
            p0: String?,
            requestData: RequestData
        ): Task<Void?> {
            Log.d(TAG, "onPlayAgain: ")
            return super.onPlayAgain(p0, requestData)
        }

        override fun onSeek(
            p0: String?,
            requestData: SeekRequestData
        ): Task<Void?> {
            Log.d(TAG, "onSeek: ")
            return super.onSeek(p0, requestData)
        }

        override fun onSelectTracksByType(
            p0: String?,
            p1: Int,
            p2: List<MediaTrack?>
        ): Task<Void?> {
            Log.d(TAG, "onSelectTracksByType: ")
            return super.onSelectTracksByType(p0, p1, p2)
        }

        override fun onSetPlaybackRate(
            p0: String?,
            requestData: SetPlaybackRateRequestData
        ): Task<Void?> {
            Log.d(TAG, "onSetPlaybackRate: ")
            return super.onSetPlaybackRate(p0, requestData)
        }

        override fun onSetTextTrackStyle(
            p0: String?,
            p1: TextTrackStyle
        ): Task<Void?> {
            Log.d(TAG, "onSetTextTrackStyle: ")
            return super.onSetTextTrackStyle(p0, p1)
        }

        override fun onSkipAd(
            p0: String?,
            requestData: RequestData
        ): Task<Void?> {
            Log.d(TAG, "onSkipAd: ")
            return super.onSkipAd(p0, requestData)
        }

        override fun onStop(
            p0: String?,
            requestData: RequestData
        ): Task<Void?> {
            Log.d(TAG, "onStop: ")
            return super.onStop(p0, requestData)
        }

        override fun onStoreSession(
            p0: String?,
            requestData: StoreSessionRequestData
        ): Task<StoreSessionResponseData?> {
            Log.d(TAG, "onStoreSession: ")
            return super.onStoreSession(p0, requestData)
        }

        override fun onUserAction(
            p0: String?,
            userActionRequestData: UserActionRequestData
        ): Task<Void?> {
            Log.d(TAG, "onUserAction: ")
            return super.onUserAction(p0, userActionRequestData)
        }
    }

    internal inner class MediaLoadCommandCallbackImpl : MediaLoadCommandCallback() {
        override fun onLoad(
            senderId: String?,
            loadRequest: MediaLoadRequestData
        ): Task<MediaLoadRequestData?> {
            val mediaInfo = loadRequest.mediaInfo
            val queueData = loadRequest.queueData
            Log.d(TAG, "onLoad $senderId ${loadRequest.mediaInfo?.contentUrl}")
            queueData?.let {
                Log.d(TAG, "onLoad from QueueData")
                val mediaItems = it.items.orEmpty().mapNotNull(itemConvert::toMediaItem)
                val currentIndex = it.startIndex
                val position = it.startTime
                mediaManager.mediaQueueManager.clear()
                mediaSession.player.clearMediaItems()
                mediaSession.player.setMediaItems(mediaItems, currentIndex, position)
            } ?: {
                mediaInfo?.let {
                    Log.d(TAG, "onLoad from MediaInfo")
                    mediaSession.player.setMediaItem(itemConvert.toMediaItem(MediaQueueItem.Builder(it).build()))
                }
            }

            mediaSession.player.prepare()
            mediaSession.player.play()
            mediaManager.mediaStatusModifier.clear()
            mediaManager.setDataFromLoad(loadRequest)
            mediaManager.broadcastMediaStatus()
            return Tasks.forResult(loadRequest)
        }

        override fun onResumeSession(
            senderId: String?,
            loadRequestData: MediaResumeSessionRequestData
        ): Task<MediaLoadRequestData?> {
            Log.d(TAG, "onResumeSession: $senderId")
            return super.onResumeSession(senderId, loadRequestData)
        }
    }

    companion object {
        private const val TAG = "PillarboxCastReceiver"

        fun MediaQueueItem.prettyString(): String {
            return "[$itemId]: contentId = ${media?.contentId} contentUrl = ${media?.contentUrl} ${this.media?.metadata?.getString(
                com.google.android.gms.cast.MediaMetadata.KEY_TITLE
            )}"
        }
    }
}
