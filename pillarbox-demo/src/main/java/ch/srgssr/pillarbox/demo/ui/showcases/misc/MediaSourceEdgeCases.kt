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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.DefaultRendererCapabilitiesList
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxLoadControl
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.ui.widget.player.PlayerSurface
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

internal class EdgeCasesViewModel(application: Application) : AndroidViewModel(application) {
    val player: PillarboxExoPlayer
    val player2: PillarboxExoPlayer
    private val preloadManager: DefaultPreloadManager

    private class Listener(private val playerId: String, private val player: PillarboxExoPlayer) : AnalyticsListener {
        override fun onLoadCompleted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) {
            if (mediaLoadData.dataType > C.DATA_TYPE_CUSTOM_BASE) {
                Log.d(
                    "Coucou2",
                    "onLoadComplete($playerId) task = ${loadEventInfo.loadTaskId}  mediaType = ${mediaLoadData.dataType} ${
                        mediaLoadData.trackSelectionData
                    } ${eventTime.mediaPeriodId}"
                )
            }
            if (mediaLoadData.dataType == C.DATA_TYPE_MANIFEST) {
                Log.d("Coucou2", "onLoadCompleted($playerId) task = ${loadEventInfo.loadTaskId} manifest = ${loadEventInfo.uri}")
            }
        }
    }

    init {
        val playbackThread = HandlerThread("MediaSourceEdge:Playback", Process.THREAD_PRIORITY_AUDIO)
        playbackThread.start()
        val loadControl = PillarboxLoadControl()
        val renderFactory = DefaultRenderersFactory(application)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            .setEnableDecoderFallback(true)
        val playbackLooper = playbackThread.looper
        player = PillarboxExoPlayer(
            context = application,
            loadControl = loadControl,
            playbackLooper = playbackLooper
        )
        player2 = PillarboxExoPlayer(
            context = application,
            loadControl = loadControl,
            playbackLooper = playbackLooper
        )

        // I want to use the source factory to test this thing.
        val sourceFactory = PillarboxMediaSourceFactory(application)
        preloadManager = DefaultPreloadManager(
            DefaultPreloadControl(),
            sourceFactory,
            player.trackSelector!!,
            DefaultBandwidthMeter.getSingletonInstance(application),
            DefaultRendererCapabilitiesList.Factory(renderFactory),
            loadControl.allocator,
            player.playbackLooper,
        )
        preloadManager.invalidate()

        preloadManager.add(MEDIA_ITEM_1, 0)
        preloadManager.add(MEDIA_ITEM_2, 1)

        player.addAnalyticsListener(Listener("Player1", player))
        player2.addAnalyticsListener(Listener("Player2", player2))

        player.prepare()
        player.play()
        player2.prepare()
        player2.play()
        play(EdgeCasesViewModel.MEDIA_ITEM_1)
        preloadManager.setCurrentPlayingIndex(0)
    }

    fun play(mediaItem: MediaItem) {
        player.setMediaSource(preloadManager.getMediaSource(mediaItem)!!)
        player2.setMediaSource(preloadManager.getMediaSource(mediaItem)!!)
        player.play()
    }

    companion object {
        val MEDIA_ITEM_1 = DemoItem.AppleBasic_16_9_TS_HLS.toMediaItem()
        val MEDIA_ITEM_2 = DemoItem.GoogleDashH264.toMediaItem()
    }
}

@Composable
fun MediaSourceEdgeCases() {

    val viewModel: EdgeCasesViewModel = viewModel()

    LaunchedEffect(Unit) {
        delay(10.seconds)
        viewModel.play(EdgeCasesViewModel.MEDIA_ITEM_2)

        delay(5.seconds)
        viewModel.play(EdgeCasesViewModel.MEDIA_ITEM_1)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        PlayerSurface(modifier = Modifier.weight(1f), player = viewModel.player)
        PlayerSurface(modifier = Modifier.weight(1f), player = viewModel.player2)
    }
}

private class DefaultPreloadControl : TargetPreloadStatusControl<Int> {
    override fun getTargetPreloadStatus(rankingData: Int): TargetPreloadStatusControl.PreloadStatus? {
        return DefaultPreloadManager.Status(DefaultPreloadManager.Status.STAGE_LOADED_TO_POSITION_MS, 1000L)
    }
}
