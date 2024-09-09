/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Timeline
import androidx.media3.common.Timeline.Window
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.ui.player.PlayerView
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxLoadControl
import ch.srgssr.pillarbox.player.analytics.extension.getUidOfPeriod
import ch.srgssr.pillarbox.player.asset.UrlAssetLoader
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource2
import ch.srgssr.pillarbox.player.source.Tracker2
import ch.srgssr.pillarbox.player.utils.StringUtil
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    val loadControl = PillarboxLoadControl()
    val playbackThread = HandlerThread("MediaSourceEdge:Playback", Process.THREAD_PRIORITY_AUDIO).apply { start() }
    val playbackLooper = playbackThread.looper
    val player = PillarboxExoPlayer(context = application, loadControl = loadControl, playbackLooper = playbackLooper)
    val player2 = PillarboxExoPlayer(context = application, loadControl = loadControl, playbackLooper = playbackLooper)
    val mediaSource: PillarboxMediaSource2

    class TrackerManager(private val player: PillarboxExoPlayer) : AnalyticsListener {
        private val window = Window()

        //
        private val trackers = mutableMapOf<Any, Map<Any, List<Tracker2.Tracker>>>() // PuidTrackerKeyListTrackers

        override fun onLoadCompleted(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
            val asset2 = mediaLoadData.trackSelectionData as Tracker2.Asset2?
            asset2?.let {
                val periodUid = eventTime.getUidOfPeriod(window)
                trackers.getOrPut(periodUid) {
                    val mapTrackers = mutableMapOf<Any, List<Tracker2.Tracker>>()
                    for (entry in it.trackerFactoriesWithData) {
                        val tracker = entry.value.first.create()
                        val data = entry.value.second
                        tracker.start(player, data)
                        mapTrackers[entry.key]
                    }
                    mapTrackers
                }
            }
        }
    }

    init {
        val mediaItem = DemoItem.OnDemandVideoMP4.toMediaItem()
        val srgAssetLoader = UrlAssetLoader(DefaultMediaSourceFactory(application))
        mediaSource = PillarboxMediaSource2(mediaItem, srgAssetLoader)
        player.addAnalyticsListener(TrackerManager(player))
        player2.addAnalyticsListener(TrackerManager(player2))
        player.addAnalyticsListener(object : AnalyticsListener {
            override fun onLoadCompleted(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                Log.d(
                    "Coucou",
                    "onLoadCompleted(${mediaLoadData.dataType}) puid = ${eventTime.getUidOfPeriod(Timeline.Window())} ${mediaLoadData.trackSelectionData}"
                )
            }

            override fun onTracksChanged(eventTime: AnalyticsListener.EventTime, tracks: Tracks) {
                Log.d("Coucou", "onTracksChanged")
            }

            override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime, reason: Int) {
                Log.d("Coucou", "onTimelineChanged ${StringUtil.timelineChangeReasonString(reason)}")
            }
        })
        player2.addAnalyticsListener(object : AnalyticsListener {
            override fun onLoadCompleted(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                Log.i(
                    "Coucou",
                    "onLoadCompleted(${mediaLoadData.dataType}) puid = ${eventTime.getUidOfPeriod(Timeline.Window())} ${
                        loadEventInfo.uri
                    } ${mediaLoadData.trackSelectionData}"
                )
            }

            override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime, reason: Int) {
                Log.i("Coucou", "onTimelineChanged ${StringUtil.timelineChangeReasonString(reason)}")
            }
        })
        player.setMediaSource(mediaSource)

        player.prepare()
        player2.prepare()
        player.play()
        player2.play()
    }

    override fun onCleared() {
        player.release()
        player2.release()
        playbackThread.quit()
    }
}

@Composable
fun NewMediaSourceShowCase() {
    val viewModel: PlayerViewModel = viewModel()
    LaunchedEffect(Unit) {
        delay(5.seconds)
        // Doesn't receive the load complete from MediaSource.prepare. Normal it is already prepared :P
        viewModel.player2.setMediaSource(viewModel.mediaSource)

        delay(5.seconds)
        viewModel.player.clearMediaItems()
        viewModel.player2.clearMediaItems()

        delay(2.seconds)
        viewModel.player.setMediaSource(viewModel.mediaSource)
        viewModel.player2.setMediaSource(viewModel.mediaSource)
    }
    Column {
        PlayerView(player = viewModel.player, modifier = Modifier.weight(1f))
        PlayerView(player = viewModel.player2, modifier = Modifier.weight(1f))
    }
}
