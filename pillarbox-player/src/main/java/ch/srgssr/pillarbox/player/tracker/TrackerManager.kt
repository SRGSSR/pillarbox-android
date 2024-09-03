/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.analytics.PillarboxAnalyticsListener
import ch.srgssr.pillarbox.player.analytics.PlaybackSessionManager
import ch.srgssr.pillarbox.player.analytics.extension.getUidOfPeriod
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource
import ch.srgssr.pillarbox.player.utils.DebugLogger
import java.io.IOException

class TrackerManager(private val pillarboxExoPlayer: PillarboxExoPlayer) : PlaybackSessionManager.Listener, PillarboxAnalyticsListener {
    private val window = Window()
    private val loadedAssets = mutableMapOf<Any, AssetHolder>()
    private var timeline = Timeline.EMPTY
    private var currentSession: PlaybackSessionManager.Session? = null

    internal data class AssetHolder(
        val session: PlaybackSessionManager.Session,
        val asset: Asset,
    ) {
        val trackers: List<MediaItemTracker> = asset.trackers
    }

    override fun onLoadCompleted(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        if (mediaLoadData.dataType == PillarboxMediaSource.DATA_TYPE_CUSTOM_ASSET || mediaLoadData.trackSelectionData is Asset) {
            val periodUid = eventTime.getUidOfPeriod(window)
            val asset = mediaLoadData.trackSelectionData as Asset
            val session = checkNotNull(pillarboxExoPlayer.sessionManager.getSessionFromPeriodUid(periodUid))

            loadedAssets[periodUid] = AssetHolder(session, asset).also { holder ->
                Log.i(TAG, "Asset loaded $$periodUid")
                holder.asset.trackers.forEach {
                    it.created(holder.session, pillarboxExoPlayer)
                    if (session == currentSession) {
                        it.start(holder.session, pillarboxExoPlayer)
                    }
                }
            }
        }
    }

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) {
        if (timeline.isEmpty) return
        val periodUid = eventTime.getUidOfPeriod(window)
        Log.e(TAG, "onLoadError ${pillarboxExoPlayer.sessionManager.getSessionFromPeriodUid(periodUid)}")
        if (mediaLoadData.dataType == PillarboxMediaSource.DATA_TYPE_CUSTOM_ASSET || mediaLoadData.trackSelectionData is Asset) {
            val asset = mediaLoadData.trackSelectionData as Asset
            val session = checkNotNull(pillarboxExoPlayer.sessionManager.getSessionFromPeriodUid(periodUid))

            loadedAssets[periodUid] = AssetHolder(session, asset).also { holder ->
                Log.i(TAG, "Asset loaded $$periodUid")
                holder.asset.trackers.forEach {
                    it.created(holder.session, pillarboxExoPlayer)
                    if (session == currentSession) {
                        it.start(holder.session, pillarboxExoPlayer)
                    }
                }
            }
        }
    }

    override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime, reason: Int) {
        this.timeline = eventTime.timeline
        if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
            DebugLogger.debug(TAG, "onTimelineChanged")
            val keys = loadedAssets.keys.toSet()
            for (periodUid in keys) {
                val windowIndex = timeline.getIndexOfPeriod(periodUid)
                if (periodUid != currentSession?.periodUid && windowIndex == C.INDEX_UNSET) {
                    loadedAssets.remove(periodUid)?.let { holder ->
                        // A session can be destroyed, ie no more in the session manager but still in loadedAssets!
                        // unloadAsset(holder.session, holder.asset) //FIXME needed?
                    }
                }
            }
        }
    }

    override fun onSessionCreated(session: PlaybackSessionManager.Session) {
        DebugLogger.debug(TAG, "onSessionCreated")
        loadedAssets[session.periodUid]?.let { holder ->
            Log.d(TAG, "Update session holder")
            loadedAssets[session.periodUid] = AssetHolder(session, holder.asset)
        }
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        if (state == Player.STATE_IDLE) {
            clearTrackers()
            loadedAssets.clear()
        }
    }

    private fun clearTrackers() {
        val keys = loadedAssets.keys.toSet()
        for (periodUid in keys) {
            val windowIndex = timeline.getIndexOfPeriod(periodUid)
            if (periodUid != currentSession?.periodUid && windowIndex == C.INDEX_UNSET) {
                loadedAssets.remove(periodUid)?.let { holder ->
                    // A session can be destroyed, ie no more in the session manager but still in loadedAssets!
                    unloadAsset(holder.session, holder.asset)
                }
            }
        }
    }

    override fun onSessionDestroyed(session: PlaybackSessionManager.Session) {
        val windowIndex = timeline.getIndexOfPeriod(session.periodUid)
        Log.d(TAG, "onSessionDestroyed $windowIndex")
        if (windowIndex == C.INDEX_UNSET) {
            loadedAssets.remove(session.periodUid)?.let { holder ->
                unloadAsset(session, holder.asset)
            }
        } else {
            loadedAssets[session.periodUid]?.let { holder ->
                unloadAsset(session, holder.asset)
            }
        }
    }

    override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
        Log.d(TAG, "onPlayerReleased clearing assets")
        loadedAssets.clear()
    }

    override fun onCurrentSessionChanged(oldSession: PlaybackSessionManager.SessionInfo?, newSession: PlaybackSessionManager.SessionInfo?) {
        DebugLogger.debug(TAG, "onCurrentSessionChanged $oldSession -> $newSession")
        oldSession?.let { session ->
            loadedAssets[session.session.periodUid]?.trackers?.forEach {
                it.stop(session, pillarboxExoPlayer)
            }
        }
        newSession?.let { session ->
            loadedAssets[session.session.periodUid]?.trackers?.forEach {
                it.start(session.session, pillarboxExoPlayer)
            }
        }

        currentSession = newSession?.session
    }

    private fun unloadAsset(session: PlaybackSessionManager.Session, asset: Asset) {
        Log.i(TAG, "Asset unload ${session.periodUid}")
        asset.trackers.forEach { tracker ->
            tracker.cleared(session, pillarboxExoPlayer)
        }
    }

    companion object {
        private const val TAG = "TrackerManager"
    }
}
