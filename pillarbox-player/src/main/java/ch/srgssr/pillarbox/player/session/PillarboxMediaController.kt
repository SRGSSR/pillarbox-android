/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.media3.common.AudioAttributes
import androidx.media3.common.DeviceInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Rating
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.service.PillarboxMediaSessionService
import ch.srgssr.pillarbox.player.utils.DebugLogger
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.guava.await

/**
 * Pillarbox media controller
 *
 * @constructor Create empty Pillarbox media controller
 */
open class PillarboxMediaController internal constructor() : PillarboxPlayer, MediaController.Listener {

    class Builder(private val context: Context, private val clazz: Class<out PillarboxMediaSessionService>) {

        suspend fun build(): PillarboxMediaController {
            val pillarboxMediaController = PillarboxMediaController()
            val componentName = ComponentName(context, clazz)
            val sessionToken = SessionToken(context, componentName)
            val mediaController = MediaController.Builder(context, sessionToken)
                .setListener(pillarboxMediaController)
                .buildAsync()
                .await()

            pillarboxMediaController.setMediaController(mediaController)
            return pillarboxMediaController
        }
    }

    private lateinit var mediaController: MediaController
    private val listeners = HashSet<PillarboxPlayer.Listener>()
    val connectedToken: SessionToken?
        get() = mediaController.connectedToken

    val isConnected: Boolean
        get() = mediaController.isConnected

    val sessionActivity: PendingIntent?
        get() = mediaController.sessionActivity

    @get:UnstableApi
    val customLayout: ImmutableList<CommandButton>
        get() = mediaController.getCustomLayout()

    @get:UnstableApi
    val sessionExtras: Bundle
        get() = mediaController.getSessionExtras()

    val availableSessionCommands: SessionCommands
        get() = mediaController.getAvailableSessionCommands()

    override var smoothSeekingEnabled: Boolean = false
        set(value) {
            if (field != value) {
                if (value) {
                    sendCustomCommand(PillarboxSessionCommands.COMMAND_SEEK_ENABLED, Bundle.EMPTY)
                } else {
                    sendCustomCommand(PillarboxSessionCommands.COMMAND_SEEK_DISABLED, Bundle.EMPTY)
                }
                field = value
                val listeners = HashSet(listeners)
                for (listener in listeners) {
                    listener.onSmoothSeekingEnabledChanged(value)
                }
            }
        }

    override var trackingEnabled: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    internal fun setMediaController(mediaController: MediaController) {
        this.mediaController = mediaController

        // TODO: Fetch initial data
        // Called from wrong thread if we load not from application thread
        sendCustomCommand(PillarboxSessionCommands.COMMAND_SEEK_GET, Bundle.EMPTY).also {
            it.addListener({
                val result = it.get()
                DebugLogger.debug(TAG, "Fetch initial data ${result.extras}")
                if (result.resultCode == SessionResult.RESULT_SUCCESS) {
                    smoothSeekingEnabled = result.extras.getBoolean("smoothSeekingEnabled")
                }
            }, MoreExecutors.directExecutor())
        }
        Log.d(TAG, "fromSessionExtras = $sessionExtras")
    }

    /**
     * @see [MediaController.setRating]
     */
    fun setRating(mediaId: String, rating: Rating): ListenableFuture<SessionResult> {
        return mediaController.setRating(mediaId, rating)
    }

    /**
     * @See [MediaController.setRating]
     */
    fun setRating(rating: Rating): ListenableFuture<SessionResult> {
        return mediaController.setRating(rating)
    }

    /**
     * @See [MediaController.sendCustomCommand]
     */
    fun sendCustomCommand(command: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
        return mediaController.sendCustomCommand(command, args)
    }

    override fun onCustomCommand(controller: MediaController, command: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
        DebugLogger.debug(TAG, "onCustomCommand ${command.customAction} ${command.customExtras}")
        when (command.customAction) {
            PillarboxSessionCommands.SMOOTH_SEEKING_CHANGED -> {
                val smoothSeeking = command.customExtras.getBoolean("smoothSeekingEnabled")
                this.smoothSeekingEnabled = smoothSeeking
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        }
        return super.onCustomCommand(controller, command, args)
    }

    override fun onAvailableSessionCommandsChanged(controller: MediaController, commands: SessionCommands) {
        super.onAvailableSessionCommandsChanged(controller, commands)
    }

    override fun onDisconnected(controller: MediaController) {
        super.onDisconnected(controller)
    }

    override fun onExtrasChanged(controller: MediaController, extras: Bundle) {
        super.onExtrasChanged(controller, extras)
        Log.i(TAG, "onExtrasChanged $extras")
    }

    /**
     * @see [MediaController.isSessionCommandAvailable]
     */
    fun isSessionCommandAvailable(sessionCommandCode: Int): Boolean {
        return mediaController.isSessionCommandAvailable(sessionCommandCode)
    }

    /**
     * @see [MediaController.isSessionCommandAvailable]
     */
    fun isSessionCommandAvailable(sessionCommand: SessionCommand): Boolean {
        return mediaController.isSessionCommandAvailable(sessionCommand)
    }

    override fun getApplicationLooper(): Looper {
        return mediaController.applicationLooper
    }

    override fun addListener(listener: Player.Listener) {
        mediaController.addListener(listener)
        if (listener is PillarboxPlayer.Listener) {
            listeners.add(listener)
        }
    }

    override fun removeListener(listener: Player.Listener) {
        mediaController.removeListener(listener)
        if (listener is PillarboxPlayer.Listener) {
            listeners.remove(listener)
        }
    }

    override fun setMediaItems(mediaItems: List<MediaItem>) {
        mediaController.setMediaItems(mediaItems)
    }

    override fun setMediaItems(mediaItems: List<MediaItem>, resetPosition: Boolean) {
        mediaController.setMediaItems(mediaItems, resetPosition)
    }

    override fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int, startPositionMs: Long) {
        mediaController.setMediaItems(mediaItems, startIndex, startPositionMs)
    }

    override fun setMediaItem(mediaItem: MediaItem) {
        mediaController.setMediaItem(mediaItem)
    }

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
        mediaController.setMediaItem(mediaItem, startPositionMs)
    }

    override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) {
        mediaController.setMediaItem(mediaItem, resetPosition)
    }

    override fun addMediaItem(mediaItem: MediaItem) {
        mediaController.addMediaItem(mediaItem)
    }

    override fun addMediaItem(index: Int, mediaItem: MediaItem) {
        mediaController.addMediaItem(index, mediaItem)
    }

    override fun addMediaItems(mediaItems: List<MediaItem>) {
        mediaController.addMediaItems(mediaItems)
    }

    override fun addMediaItems(index: Int, mediaItems: List<MediaItem>) {
        mediaController.addMediaItems(index, mediaItems)
    }

    override fun moveMediaItem(currentIndex: Int, newIndex: Int) {
        mediaController.moveMediaItem(currentIndex, newIndex)
    }

    override fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        mediaController.moveMediaItems(fromIndex, toIndex, newIndex)
    }

    override fun replaceMediaItem(index: Int, mediaItem: MediaItem) {
        mediaController.replaceMediaItem(index, mediaItem)
    }

    override fun replaceMediaItems(fromIndex: Int, toIndex: Int, mediaItems: List<MediaItem>) {
        mediaController.replaceMediaItems(fromIndex, toIndex, mediaItems)
    }

    override fun removeMediaItem(index: Int) {
        mediaController.removeMediaItem(index)
    }

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) {
        mediaController.removeMediaItems(fromIndex, toIndex)
    }

    override fun clearMediaItems() {
        mediaController.clearMediaItems()
    }

    override fun isCommandAvailable(command: Int): Boolean {
        return mediaController.isCommandAvailable(command)
    }

    override fun canAdvertiseSession(): Boolean {
        return mediaController.canAdvertiseSession()
    }

    override fun getAvailableCommands(): Player.Commands {
        return mediaController.getAvailableCommands()
    }

    override fun prepare() {
        mediaController.prepare()
    }

    override fun getPlaybackState(): Int {
        return mediaController.getPlaybackState()
    }

    override fun getPlaybackSuppressionReason(): Int {
        return mediaController.getPlaybackSuppressionReason()
    }

    override fun isPlaying(): Boolean {
        return mediaController.isPlaying()
    }

    override fun getPlayerError(): PlaybackException? {
        return mediaController.getPlayerError()
    }

    override fun play() {
        mediaController.play()
    }

    override fun pause() {
        mediaController.pause()
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        mediaController.setPlayWhenReady(playWhenReady)
    }

    override fun getPlayWhenReady(): Boolean {
        return mediaController.getPlayWhenReady()
    }

    override fun setRepeatMode(repeatMode: Int) {
        mediaController.setRepeatMode(repeatMode)
    }

    override fun getRepeatMode(): Int {
        return mediaController.getRepeatMode()
    }

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        mediaController.setShuffleModeEnabled(shuffleModeEnabled)
    }

    override fun getShuffleModeEnabled(): Boolean {
        return mediaController.getShuffleModeEnabled()
    }

    override fun isLoading(): Boolean {
        return mediaController.isLoading()
    }

    override fun seekToDefaultPosition() {
        mediaController.seekToDefaultPosition()
    }

    override fun seekToDefaultPosition(mediaItemIndex: Int) {
        mediaController.seekToDefaultPosition(mediaItemIndex)
    }

    override fun seekTo(positionMs: Long) {
        mediaController.seekTo(positionMs)
    }

    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
        mediaController.seekTo(mediaItemIndex, positionMs)
    }

    override fun getSeekBackIncrement(): Long {
        return mediaController.getSeekBackIncrement()
    }

    override fun seekBack() {
        mediaController.seekBack()
    }

    override fun getSeekForwardIncrement(): Long {
        return mediaController.getSeekForwardIncrement()
    }

    override fun seekForward() {
        mediaController.seekForward()
    }

    @UnstableApi
    @Deprecated("")
    override fun hasPrevious(): Boolean {
        return mediaController.hasPrevious()
    }

    @UnstableApi
    @Deprecated("")
    override fun hasPreviousWindow(): Boolean {
        return mediaController.hasPreviousWindow()
    }

    override fun hasPreviousMediaItem(): Boolean {
        return mediaController.hasPreviousMediaItem()
    }

    @UnstableApi
    @Deprecated("")
    override fun previous() {
        mediaController.previous()
    }

    @UnstableApi
    @Deprecated("")
    override fun seekToPreviousWindow() {
        mediaController.seekToPreviousWindow()
    }

    override fun seekToPreviousMediaItem() {
        mediaController.seekToPreviousMediaItem()
    }

    override fun getMaxSeekToPreviousPosition(): Long {
        return mediaController.getMaxSeekToPreviousPosition()
    }

    override fun seekToPrevious() {
        mediaController.seekToPrevious()
    }

    @UnstableApi
    @Deprecated("")
    override fun hasNext(): Boolean {
        return mediaController.hasNext()
    }

    @UnstableApi
    @Deprecated("")
    override fun hasNextWindow(): Boolean {
        return mediaController.hasNextWindow()
    }

    override fun hasNextMediaItem(): Boolean {
        return mediaController.hasNextMediaItem()
    }

    @UnstableApi
    @Deprecated("")
    override fun next() {
        mediaController.next()
    }

    @UnstableApi
    @Deprecated("")
    override fun seekToNextWindow() {
        mediaController.seekToNextWindow()
    }

    override fun seekToNextMediaItem() {
        mediaController.seekToNextMediaItem()
    }

    override fun seekToNext() {
        mediaController.seekToNext()
    }

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        mediaController.setPlaybackParameters(playbackParameters)
    }

    override fun setPlaybackSpeed(speed: Float) {
        mediaController.setPlaybackSpeed(speed)
    }

    override fun getPlaybackParameters(): PlaybackParameters {
        return mediaController.getPlaybackParameters()
    }

    override fun stop() {
        mediaController.stop()
    }

    override fun release() {
        mediaController.release()
    }

    override fun getCurrentTracks(): Tracks {
        return mediaController.getCurrentTracks()
    }

    override fun getTrackSelectionParameters(): TrackSelectionParameters {
        return mediaController.getTrackSelectionParameters()
    }

    override fun setTrackSelectionParameters(parameters: TrackSelectionParameters) {
        mediaController.setTrackSelectionParameters(parameters)
    }

    override fun getMediaMetadata(): MediaMetadata {
        return mediaController.getMediaMetadata()
    }

    override fun getPlaylistMetadata(): MediaMetadata {
        return mediaController.getPlaylistMetadata()
    }

    override fun setPlaylistMetadata(mediaMetadata: MediaMetadata) {
        mediaController.setPlaylistMetadata(mediaMetadata)
    }

    @UnstableApi
    override fun getCurrentManifest(): Any? {
        return mediaController.currentManifest
    }

    override fun getCurrentTimeline(): Timeline {
        return mediaController.getCurrentTimeline()
    }

    override fun getCurrentPeriodIndex(): Int {
        return mediaController.getCurrentPeriodIndex()
    }

    @UnstableApi
    @Deprecated("")
    override fun getCurrentWindowIndex(): Int {
        return mediaController.currentWindowIndex
    }

    override fun getCurrentMediaItemIndex(): Int {
        return mediaController.getCurrentMediaItemIndex()
    }

    @UnstableApi
    @Deprecated("")
    override fun getNextWindowIndex(): Int {
        return mediaController.nextWindowIndex
    }

    override fun getNextMediaItemIndex(): Int {
        return mediaController.getNextMediaItemIndex()
    }

    @UnstableApi
    @Deprecated("")
    override fun getPreviousWindowIndex(): Int {
        return mediaController.previousWindowIndex
    }

    override fun getPreviousMediaItemIndex(): Int {
        return mediaController.getPreviousMediaItemIndex()
    }

    override fun getCurrentMediaItem(): MediaItem? {
        return mediaController.getCurrentMediaItem()
    }

    override fun getMediaItemCount(): Int {
        return mediaController.mediaItemCount
    }

    override fun getMediaItemAt(index: Int): MediaItem {
        return mediaController.getMediaItemAt(index)
    }

    override fun getDuration(): Long {
        return mediaController.getDuration()
    }

    override fun getCurrentPosition(): Long {
        return mediaController.getCurrentPosition()
    }

    override fun getBufferedPosition(): Long {
        return mediaController.getBufferedPosition()
    }

    @IntRange(from = 0L, to = 100L)
    override fun getBufferedPercentage(): Int {
        return mediaController.getBufferedPercentage()
    }

    override fun getTotalBufferedDuration(): Long {
        return mediaController.getTotalBufferedDuration()
    }

    @UnstableApi
    @Deprecated("")
    override fun isCurrentWindowDynamic(): Boolean {
        return mediaController.isCurrentWindowDynamic
    }

    override fun isCurrentMediaItemDynamic(): Boolean {
        return mediaController.isCurrentMediaItemDynamic()
    }

    @UnstableApi
    @Deprecated("")
    override fun isCurrentWindowLive(): Boolean {
        return mediaController.isCurrentWindowLive
    }

    override fun isCurrentMediaItemLive(): Boolean {
        return mediaController.isCurrentMediaItemLive()
    }

    override fun getCurrentLiveOffset(): Long {
        return mediaController.getCurrentLiveOffset()
    }

    @UnstableApi
    @Deprecated("")
    override fun isCurrentWindowSeekable(): Boolean {
        return mediaController.isCurrentWindowSeekable
    }

    override fun isCurrentMediaItemSeekable(): Boolean {
        return mediaController.isCurrentMediaItemSeekable()
    }

    override fun isPlayingAd(): Boolean {
        return mediaController.isPlayingAd()
    }

    override fun getCurrentAdGroupIndex(): Int {
        return mediaController.getCurrentAdGroupIndex()
    }

    override fun getCurrentAdIndexInAdGroup(): Int {
        return mediaController.getCurrentAdIndexInAdGroup()
    }

    override fun getContentDuration(): Long {
        return mediaController.getContentDuration()
    }

    override fun getContentPosition(): Long {
        return mediaController.getContentPosition()
    }

    override fun getContentBufferedPosition(): Long {
        return mediaController.getContentBufferedPosition()
    }

    override fun getAudioAttributes(): AudioAttributes {
        return mediaController.getAudioAttributes()
    }

    override fun setVolume(volume: Float) {
        mediaController.setVolume(volume)
    }

    @FloatRange(from = 0.0, to = 1.0)
    override fun getVolume(): Float {
        return mediaController.getVolume()
    }

    override fun clearVideoSurface() {
        mediaController.clearVideoSurface()
    }

    override fun clearVideoSurface(surface: Surface?) {
        mediaController.clearVideoSurface(surface)
    }

    override fun setVideoSurface(surface: Surface?) {
        mediaController.setVideoSurface(surface)
    }

    override fun setVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        mediaController.setVideoSurfaceHolder(surfaceHolder)
    }

    override fun clearVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        mediaController.clearVideoSurfaceHolder(surfaceHolder)
    }

    override fun setVideoSurfaceView(surfaceView: SurfaceView?) {
        mediaController.setVideoSurfaceView(surfaceView)
    }

    override fun clearVideoSurfaceView(surfaceView: SurfaceView?) {
        mediaController.clearVideoSurfaceView(surfaceView)
    }

    override fun setVideoTextureView(textureView: TextureView?) {
        mediaController.setVideoTextureView(textureView)
    }

    override fun clearVideoTextureView(textureView: TextureView?) {
        mediaController.clearVideoTextureView(textureView)
    }

    override fun getVideoSize(): VideoSize {
        return mediaController.getVideoSize()
    }

    @UnstableApi
    override fun getSurfaceSize(): Size {
        return mediaController.getSurfaceSize()
    }

    override fun getCurrentCues(): CueGroup {
        return mediaController.getCurrentCues()
    }

    override fun getDeviceInfo(): DeviceInfo {
        return mediaController.getDeviceInfo()
    }

    @IntRange(from = 0L)
    override fun getDeviceVolume(): Int {
        return mediaController.getDeviceVolume()
    }

    override fun isDeviceMuted(): Boolean {
        return mediaController.isDeviceMuted()
    }

    @Deprecated("")
    override fun setDeviceVolume(volume: Int) {
        mediaController.setDeviceVolume(volume)
    }

    override fun setDeviceVolume(volume: Int, flags: Int) {
        mediaController.setDeviceVolume(volume, flags)
    }

    @Deprecated("")
    override fun increaseDeviceVolume() {
        mediaController.increaseDeviceVolume()
    }

    override fun increaseDeviceVolume(flags: Int) {
        mediaController.increaseDeviceVolume(flags)
    }

    @Deprecated("")
    override fun decreaseDeviceVolume() {
        mediaController.decreaseDeviceVolume()
    }

    override fun decreaseDeviceVolume(flags: Int) {
        mediaController.decreaseDeviceVolume(flags)
    }

    @Deprecated("")
    override fun setDeviceMuted(muted: Boolean) {
        mediaController.setDeviceMuted(muted)
    }

    override fun setDeviceMuted(muted: Boolean, flags: Int) {
        mediaController.setDeviceMuted(muted, flags)
    }

    override fun setAudioAttributes(audioAttributes: AudioAttributes, handleAudioFocus: Boolean) {
        mediaController.setAudioAttributes(audioAttributes, handleAudioFocus)
    }

    companion object {
        private const val TAG = " PillarboxMediaController"
    }
}
