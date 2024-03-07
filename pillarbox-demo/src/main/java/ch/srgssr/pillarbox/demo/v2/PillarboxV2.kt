/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.v2

import android.content.ComponentName
import android.content.Context
import android.view.SurfaceView
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.CompositeMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.Allocator
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import ch.srgssr.pillarbox.demo.service.DemoMediaSessionService
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerData
import ch.srgssr.pillarbox.player.extension.setTrackerData
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

interface ItemTracker {
    fun start(player: PillarboxPlayerV2)
    fun stop(player: PillarboxPlayerV2)
}

interface PillarboxItemLoader {
    fun handle(pillarboxItem: PillarboxItem): Boolean
    suspend fun load(pillarboxItem: PillarboxItem): MediaItem
}

class CustomLoader : PillarboxItemLoader {
    override fun handle(pillarboxItem: PillarboxItem): Boolean {
        return pillarboxItem.mimeType == "yolo"
    }

    override suspend fun load(pillarboxItem: PillarboxItem): MediaItem {
        return MediaItem.Builder().setTrackerData(MediaItemTrackerData.EMPTY).build()
    }
}

// WE control the user!
data class PillarboxItem(
    val url: String? = null,
    val mimeType: String? = null,
    val id: String = MediaItem.DEFAULT_MEDIA_ID,
    val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
    val drmConfig: DrmConfiguration? = null
) {
    internal fun toMediaItem(): MediaItem {
        return MediaItem.fromUri(url!!)
    }

    companion object {
        internal fun fromMedia(mediaItem: MediaItem): PillarboxItem {
            return PillarboxItem(mediaItem.localConfiguration?.uri.toString())
        }
    }
}

class PillarboxV2MediaSource(
    private val pillarboxItem: PillarboxItem,
    private val mediaSource: MediaSource.Factory,
    private val loader: PillarboxItemLoader,
) : CompositeMediaSource<Unit>() {
    private var mediaItem = pillarboxItem.toMediaItem()

    override fun getMediaItem(): MediaItem {
        return mediaItem
    }

    override fun createPeriod(id: MediaSource.MediaPeriodId, allocator: Allocator, startPositionUs: Long): MediaPeriod {
        TODO("Not yet implemented")
    }

    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        TODO("Not yet implemented")
    }

    override fun onChildSourceInfoRefreshed(childSourceId: Unit?, mediaSource: MediaSource, newTimeline: Timeline) {
        TODO("Not yet implemented")
    }

    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        super.prepareSourceInternal(mediaTransferListener)
        runBlocking {
            val itemToLoad = loader.load(pillarboxItem)
            // mediaMetadata
            // trackers
            updateMediaItem(
                mediaItem.buildUpon().setMediaMetadata(itemToLoad.mediaMetadata)
                    .setTrackerData(itemToLoad.getMediaItemTrackerData())
                    .build()
            )
            mediaSource.createMediaSource(itemToLoad)
        }
    }
}

interface PillarboxPlayerV2 {
    /**
     * DON'T USE IT, internal usage!!!!!
     * Player
     */
    val player: Player
    fun add(item: PillarboxItem)

    fun remove(item: PillarboxItem)

    fun play() = player.play()

    fun pause() = player.pause()

    fun getCurrentItem(): PillarboxItem?

    fun getCurrentIndex(): Int = player.currentMediaItemIndex

    fun setSurfaceView(surfaceView: SurfaceView)

    fun getPlaybackState(): @Player.State Int

    fun getPlaybackStateAsFlow(): StateFlow<@Player.State Int>

    fun addListener(listener: Player.Listener)
}

object PillarboxMediaSessionBuilder {

    fun build(context: Context, player: PillarboxPlayerV2): MediaSession {
        Player.STATE_READY
        return MediaSession.Builder(context, player.player).build()
    }
}

class BackgroundService : MediaSessionService() {
    private lateinit var player: PillarboxPlayerV2
    override fun onCreate() {
        super.onCreate()
        player = PillarboxV2Impl(this)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return PillarboxMediaSessionBuilder.build(this, player)
    }
}

class PillarboxV2Impl(
    context: Context,
    loadControl: LoadControl = DefaultLoadControl(),
    loader: PillarboxItemLoader = CustomLoader(),

) : PillarboxPlayerV2 {
    private val sourceFactory = DefaultMediaSourceFactory(context)

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setLoadControl(loadControl)
        .build()
    override val player: ExoPlayer
        get() = exoPlayer

    override fun addListener(listener: Player.Listener) {
        exoPlayer.addListener(listener)
    }

    override fun add(item: PillarboxItem) {
        exoPlayer.addMediaItem(item.toMediaItem())
    }

    override fun remove(item: PillarboxItem) {
        exoPlayer.removeMediaItem(0)
    }

    override fun getCurrentItem(): PillarboxItem? {
        return exoPlayer.currentMediaItem?.let {
            PillarboxItem.fromMedia(it)
        }
    }

    override fun play() {
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun setSurfaceView(surfaceView: SurfaceView) {
        exoPlayer.setVideoSurfaceView(surfaceView)
    }

    override fun getPlaybackState(): Int {
        TODO("Not yet implemented")
    }

    override fun getPlaybackStateAsFlow(): StateFlow<Int> {
        TODO("Not yet implemented")
    }
}

class PillarboxMediaController(context: Context) : PillarboxPlayerV2 {
    private val sessionToken = SessionToken(context, ComponentName(context, DemoMediaSessionService::class.java))
    private val listenableFuture = MediaController.Builder(context, sessionToken)
        .buildAsync()
    private lateinit var controller: MediaController
    override val player: Player
        get() = controller

    override fun add(item: PillarboxItem) {
        controller.addMediaItem(item.toMediaItem())
    }

    override fun remove(item: PillarboxItem) {
        controller.removeMediaItem(0)
    }

    override fun getCurrentItem(): PillarboxItem? {
        return player.currentMediaItem?.let {
            PillarboxItem.fromMedia(it)
        }
    }

    override fun play() {
        controller.prepare()
        controller.play()
    }

    override fun pause() {
        controller.pause()
    }

    override fun setSurfaceView(surfaceView: SurfaceView) {
        controller.setVideoSurfaceView(surfaceView)
    }

    override fun getPlaybackState(): Int {
        TODO("Not yet implemented")
    }

    override fun getPlaybackStateAsFlow(): StateFlow<Int> {
        TODO("Not yet implemented")
    }

    override fun addListener(listener: Player.Listener) {
        TODO("Not yet implemented")
    }
}
