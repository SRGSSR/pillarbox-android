/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.app.Application
import android.util.Log
import android.util.Rational
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.VideoSize
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.demo.service.PillarboxDownloadService
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.source.BlockedTimeRangeAssetLoader
import ch.srgssr.pillarbox.demo.shared.source.CustomAssetLoader
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.PreloadConfiguration
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.timeRange.Chapter
import ch.srgssr.pillarbox.player.asset.timeRange.Credit
import ch.srgssr.pillarbox.player.extension.setHandleAudioFocus
import ch.srgssr.pillarbox.player.extension.toRational
import ch.srgssr.pillarbox.player.network.PillarboxOkHttp
import ch.srgssr.pillarbox.player.utils.StringUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration.Companion.seconds

/**
 * Simple player view model than handle a PillarboxPlayer [player]
 */
class SimplePlayerViewModel(application: Application) : AndroidViewModel(application), PillarboxPlayer.Listener {
    /**
     * Player as PillarboxPlayer
     */
    val player = PillarboxExoPlayer(context = application) {
        val defaultDataSource = DefaultDataSource.Factory(application, OkHttpDataSource.Factory(PillarboxOkHttp()))
        val cachedDataSourceFactory = CacheDataSource.Factory()
            .setCache(PillarboxDownloadService.getCache(application))
            .setUpstreamDataSourceFactory(defaultDataSource)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        +DownloadLoader(
            DefaultMediaSourceFactory(application).setDataSourceFactory(cachedDataSourceFactory),
            PillarboxDownloadService.getDownloadManager(application)
        )

        srgAssetLoader(context = application) {
            dataSourceFactory(cachedDataSourceFactory)
        }
        +CustomAssetLoader(application)
        +BlockedTimeRangeAssetLoader(application)
        preloadConfiguration(PreloadConfiguration(10.seconds))
    }

    /**
     * Picture in picture enabled
     */
    val pictureInPictureEnabled = MutableStateFlow(false)

    /**
     * Picture in picture aspect ratio
     */
    var pictureInPictureRatio = MutableStateFlow(Rational(1, 1))

    init {
        player.addListener(this)
        /*
         * Seems to have no effect if not use with a foreground service to handle background playback.
         * Without service, playback may stop after ~ 1min with a socket time out.
         */
        player.setWakeMode(C.WAKE_MODE_NETWORK)

        /*
         * Will pause player when headphones are disconnected
         */
        player.setHandleAudioBecomingNoisy(true)

        /*
         * When handleAudioFocus = true, will pause media when interrupted.
         * Playback will resume depending on the "importance" of the interruption (call, playback)
         */
        player.setHandleAudioFocus(true)
    }

    /**
     * Add to [player] all [items] to the MediaItem list.
     * Will prepare and play the content.
     *
     * @param items to play
     */
    fun playUri(items: List<DemoItem>) {
        player.setMediaItems(items.map { it.toMediaItem() })
        player.prepare()
        player.play()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared => releasing the player")
        player.release()
        player.removeListener(this)
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        pictureInPictureRatio.value = videoSize.toRational()
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        Log.d(TAG, "onMediaMetadataChanged title = ${mediaMetadata.title}")
    }

    override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
        Log.d(TAG, "onPlaylistMetadataChanged title = ${mediaMetadata.title}")
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        val reasonString = StringUtil.timelineChangeReasonString(reason)
        Log.d(
            TAG,
            "onTimelineChanged $reasonString ${player.currentMediaItem?.mediaId}" +
                " ${player.currentMediaItem?.mediaMetadata?.title}" +
                " uri = ${player.currentMediaItem?.localConfiguration?.uri}" +
                " tag=${player.currentMediaItem?.localConfiguration?.tag}"
        )
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        val reasonString = when (reason) {
            Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY -> "PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY"
            Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS -> "PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS"
            Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE -> "PLAY_WHEN_READY_CHANGE_REASON_REMOTE"
            Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST -> "PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST"
            Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM -> "PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM"
            else -> "?"
        }
        Log.d(TAG, "onPlayWhenReadyChanged $reasonString ${player.currentMediaItem?.mediaMetadata?.title}")
    }

    override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
        val stateString = StringUtil.playerStateString(playbackState)
        Log.d(TAG, "onPlaybackStateChanged $stateString ${player.currentMediaItem?.mediaMetadata?.title}")
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.e(TAG, "onPlayerError", error)
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        Log.d(TAG, "onPlayerErrorChanged", error)
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        Log.d(TAG, "onPlaybackParametersChanged ${playbackParameters.speed}")
    }

    override fun onChapterChanged(chapter: Chapter?) {
        Log.i(TAG, "onChapterChanged $chapter")
    }

    override fun onCreditChanged(credit: Credit?) {
        Log.i(TAG, "onCreditChanged $credit")
    }

    private companion object {
        private const val TAG = "PillarboxDemo"
    }
}

private class DownloadLoader(
    mediaSourceFactory: MediaSource.Factory,
    private val downloadManager: DownloadManager
) : AssetLoader(mediaSourceFactory) {

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        val download = downloadManager.downloadIndex.getDownload(mediaItem.mediaId)
        return download != null
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        downloadManager.downloadIndex.getDownload(mediaItem.mediaId)?.let { download ->
            val mediaItemDownloaded = download.request.toMediaItem()
            Log.d("DOWNLOAD", "loadAsset for ${mediaItemDownloaded.localConfiguration?.uri} ${mediaItem.localConfiguration?.mimeType}")
            return Asset(
                mediaSource = mediaSourceFactory.createMediaSource(mediaItemDownloaded),
                mediaMetadata = mediaItem.mediaMetadata
            )
        } ?: throw IllegalStateException("No download associated with ${mediaItem.mediaId}")
    }
}
