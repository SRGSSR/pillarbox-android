/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.drm.DrmSessionEventListener
import androidx.media3.exoplayer.source.BaseMediaSource
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MediaSource.MediaPeriodId
import androidx.media3.exoplayer.source.MediaSource.MediaSourceCaller
import androidx.media3.exoplayer.source.MediaSourceEventListener
import androidx.media3.exoplayer.upstream.Allocator
import androidx.media3.exoplayer.upstream.Loader
import androidx.media3.exoplayer.upstream.Loader.Loadable
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import kotlinx.coroutines.runBlocking
import java.io.IOException

object Tracker2 {

    interface Tracker {
        fun start(player: PillarboxExoPlayer, data: Any?)
        fun stop(player: PillarboxExoPlayer)
        fun allowDisable(): Boolean = true

        fun interface Factory {
            fun create(): Tracker
        }
    }

    class LogTracker(config: String) : Tracker {
        private val tag: String = config
        private var data: Any? = null

        override fun start(player: PillarboxExoPlayer, data: Any?) {
            this.data = data
            Log.d(tag, "Started with data = $data")
        }

        override fun stop(player: PillarboxExoPlayer) {
            Log.d(tag, "stop")
        }

        class Factory(val config: String) : Tracker.Factory {
            override fun create(): Tracker = LogTracker(config)
        }
    }

    class Asset2(
        val trackerFactoriesWithData: Map<Any, Pair<Tracker.Factory, Any?>> = mutableMapOf(
            "KEY_1" to (Tracker.Factory { LogTracker("Coucou3") } to "Data1"),
            KEY_3 to (Tracker.Factory { LogTracker("Coucou4") } to "Data2"),
        )
    ) {
        companion object {
            val KEY_3 = Any()
        }
    }
}

class PillarboxMediaSource2(
    private var mediaItem: MediaItem,
    private val assetLoader: AssetLoader,
) : BaseMediaSource() {
    private var mediaTransferListener: TransferListener? = null
    private val loader: Loader = Loader("AssetLoader")
    private var realMediaSource: MediaSource? = null
    private lateinit var eventHandler: Handler
    private var forwardingListener: ForwardingListener? = null
    private var mediaSourceCaller: MediaSourceCaller? = null

    class AssetLoadable(private val mediaItem: MediaItem, private val assetLoader: AssetLoader) : Loadable {
        var asset: Asset? = null

        override fun cancelLoad() {
            // Nothing
        }

        override fun load() {
            Log.d(TAG, "AssetLoadable ${Thread.currentThread()}")
            runBlocking {
                val assetBuilder = Asset.Builder()
                assetLoader.loadAsset(mediaItem, assetBuilder)
                asset = assetBuilder.build()
                asset?.mediaSource?.getOrThrow()
            }
        }
    }

    override fun getMediaItem(): MediaItem {
        return mediaItem
    }

    override fun maybeThrowSourceInfoRefreshError() {
    }

    override fun createPeriod(id: MediaPeriodId, allocator: Allocator, startPositionUs: Long): MediaPeriod {
        return realMediaSource!!.createPeriod(id, allocator, startPositionUs)
    }

    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        realMediaSource?.releasePeriod(mediaPeriod)
    }

    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        Log.i(TAG, "prepareSourceInternal")
        // 1) Load Asset
        this.mediaTransferListener = mediaTransferListener
        eventHandler = Util.createHandlerForCurrentLooper()
        loader.startLoading(
            AssetLoadable(mediaItem, assetLoader),
            object : Loader.Callback<AssetLoadable> {
                override fun onLoadCompleted(loadable: AssetLoadable, elapsedRealtimeMs: Long, loadDurationMs: Long) {
                    Log.d(TAG, "onLoadCompleted...")
                    realMediaSource = loadable.asset?.mediaSource?.getOrNull()

                    realMediaSource?.let { mediaSource ->
                        forwardingListener = ForwardingListener(
                            asset = loadable.asset!!,
                            mediaSourceEventDispatcher = createEventDispatcher(null),
                            drmEventDispatcher = createDrmEventDispatcher(null)
                        )
                        forwardingListener?.sendLoadComplete(elapsedRealtimeMs)
                        mediaSourceCaller = MediaSourceCaller { source, timeline -> refreshSourceInfo(timeline) }
                        mediaSource.addEventListener(
                            eventHandler, checkNotNull(forwardingListener)
                        )
                        mediaSource.addDrmEventListener(eventHandler, checkNotNull(forwardingListener))
                        mediaSource.prepareSource(checkNotNull(mediaSourceCaller), mediaTransferListener, playerId)
                        if (!isEnabled) {
                            mediaSource.disable(checkNotNull(mediaSourceCaller))
                        }
                    }
                }

                override fun onLoadCanceled(loadable: AssetLoadable, elapsedRealtimeMs: Long, loadDurationMs: Long, released: Boolean) {
                    Log.d(TAG, "onLoadCanceled $released")
                }

                override fun onLoadError(
                    loadable: AssetLoadable,
                    elapsedRealtimeMs: Long,
                    loadDurationMs: Long,
                    error: IOException,
                    errorCount: Int
                ): Loader.LoadErrorAction {
                    Log.e(TAG, "onLoadError", error)
                    return Loader.createRetryAction(true, 0L)
                }
            },
            3
        )
        // 2) Refresh Timeline
        // Create a Timeline with one Window with given MediaItem
        // refreshSourceInfo(Timeline...)
        // Load real MediaSource
    }

    override fun releaseSourceInternal() {
        Log.e(TAG, "releaseSourceInternal")
        realMediaSource?.let { mediaSource ->
            mediaSource.releaseSource(checkNotNull(mediaSourceCaller))
            mediaSource.removeEventListener(checkNotNull(forwardingListener))
            mediaSource.removeDrmEventListener(checkNotNull(forwardingListener))
        }
    }

    override fun enableInternal() {
        super.enableInternal()
        realMediaSource?.enable(checkNotNull(mediaSourceCaller))
    }

    override fun disableInternal() {
        super.disableInternal()
        realMediaSource?.disable(checkNotNull(mediaSourceCaller))
    }

    private class ForwardingListener(
        private val asset: Asset,
        private var mediaSourceEventDispatcher: MediaSourceEventListener.EventDispatcher,
        private var drmEventDispatcher: DrmSessionEventListener.EventDispatcher,
    ) : MediaSourceEventListener, DrmSessionEventListener {

        fun sendLoadComplete(elapsedRealTimeMs: Long) {
            val loadEventInfo = LoadEventInfo(LoadEventInfo.getNewId(), DataSpec(Uri.EMPTY), elapsedRealTimeMs)
            mediaSourceEventDispatcher.loadCompleted(loadEventInfo, C.DATA_TYPE_CUSTOM_BASE + 100)
        }

        private fun updateDispatchers(windowIndex: Int, mediaPeriodId: MediaPeriodId?) {
            if (mediaSourceEventDispatcher.windowIndex != windowIndex || mediaSourceEventDispatcher.mediaPeriodId != mediaPeriodId) {
                mediaSourceEventDispatcher = mediaSourceEventDispatcher.withParameters(windowIndex, mediaPeriodId)
            }
            if (drmEventDispatcher.windowIndex != windowIndex || mediaPeriodId != drmEventDispatcher.mediaPeriodId) {
                drmEventDispatcher = drmEventDispatcher.withParameters(windowIndex, mediaPeriodId)
            }
        }

        override fun onLoadCompleted(
            windowIndex: Int,
            mediaPeriodId: MediaPeriodId?,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) {
            updateDispatchers(windowIndex, mediaPeriodId)
            val forwardingMediaLoadData = MediaLoadData(
                mediaLoadData.dataType,
                mediaLoadData.trackType,
                mediaLoadData.trackFormat,
                mediaLoadData.trackSelectionReason,
                mediaLoadData.trackSelectionData ?: Tracker2.Asset2(),
                mediaLoadData.mediaStartTimeMs,
                mediaLoadData.mediaEndTimeMs
            )
            mediaSourceEventDispatcher.loadCompleted(loadEventInfo, forwardingMediaLoadData)
        }

        override fun onLoadError(
            windowIndex: Int,
            mediaPeriodId: MediaPeriodId?,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
            error: IOException,
            wasCanceled: Boolean
        ) {
            updateDispatchers(windowIndex, mediaPeriodId)
            mediaSourceEventDispatcher.loadError(loadEventInfo, mediaLoadData, error, wasCanceled)
        }

        override fun onLoadCanceled(
            windowIndex: Int,
            mediaPeriodId: MediaPeriodId?,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) {
            updateDispatchers(windowIndex, mediaPeriodId)
            mediaSourceEventDispatcher.loadCanceled(loadEventInfo, mediaLoadData)
        }

        override fun onLoadStarted(
            windowIndex: Int,
            mediaPeriodId: MediaPeriodId?,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) {
            updateDispatchers(windowIndex, mediaPeriodId)
            mediaSourceEventDispatcher.loadStarted(loadEventInfo, mediaLoadData)
        }

        override fun onDownstreamFormatChanged(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, mediaLoadData: MediaLoadData) {
            updateDispatchers(windowIndex, mediaPeriodId)
            mediaSourceEventDispatcher.downstreamFormatChanged(mediaLoadData)
        }

        override fun onUpstreamDiscarded(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId, mediaLoadData: MediaLoadData) {
            updateDispatchers(windowIndex, mediaPeriodId)
            mediaSourceEventDispatcher.upstreamDiscarded(mediaLoadData)
        }

        override fun onDrmKeysLoaded(windowIndex: Int, mediaPeriodId: MediaPeriodId?) {
            updateDispatchers(windowIndex, mediaPeriodId)
            drmEventDispatcher.drmKeysLoaded()
        }

        override fun onDrmKeysRemoved(windowIndex: Int, mediaPeriodId: MediaPeriodId?) {
            updateDispatchers(windowIndex, mediaPeriodId)
            drmEventDispatcher.drmKeysRemoved()
        }

        override fun onDrmKeysRestored(windowIndex: Int, mediaPeriodId: MediaPeriodId?) {
            updateDispatchers(windowIndex, mediaPeriodId)
            drmEventDispatcher.drmKeysRestored()
        }

        override fun onDrmSessionReleased(windowIndex: Int, mediaPeriodId: MediaPeriodId?) {
            updateDispatchers(windowIndex, mediaPeriodId)
            drmEventDispatcher.drmKeysRemoved()
        }

        override fun onDrmSessionAcquired(windowIndex: Int, mediaPeriodId: MediaPeriodId?, state: Int) {
            updateDispatchers(windowIndex, mediaPeriodId)
            drmEventDispatcher.drmSessionAcquired(state)
        }

        override fun onDrmSessionManagerError(windowIndex: Int, mediaPeriodId: MediaPeriodId?, error: Exception) {
            updateDispatchers(windowIndex, mediaPeriodId)
            drmEventDispatcher.drmSessionManagerError(error)
        }
    }

    private class PillarboxTimeline : Timeline() {
        override fun getWindowCount(): Int {
            return 1
        }

        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            TODO("Not yet implemented")
        }

        override fun getPeriodCount(): Int {
            return 1
        }

        override fun getPeriod(periodIndex: Int, period: Period, setIds: Boolean): Period {
            TODO("Not yet implemented")
        }

        override fun getIndexOfPeriod(uid: Any): Int {
            TODO("Not yet implemented")
        }

        override fun getUidOfPeriod(periodIndex: Int): Any {
            TODO("Not yet implemented")
        }
    }

    companion object {
        private const val TAG = "PB2"
    }
}
