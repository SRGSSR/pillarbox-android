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
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.os.BundleCompat
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
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.ActionableTimeInterval
import ch.srgssr.pillarbox.player.asset.BlockedInterval
import ch.srgssr.pillarbox.player.asset.Chapter
import ch.srgssr.pillarbox.player.utils.DebugLogger
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.guava.await

/**
 * Pillarbox media controller implements [PillarboxPlayer] and wrap a [MediaController].
 * @see MediaController
 */
open class PillarboxMediaController internal constructor() : PillarboxPlayer {

    /**
     * Builder for [PillarboxMediaController].
     *
     * @param context The context.
     * @param clazz The class of the [MediaSessionService] that holds the [PillarboxMediaSession].
     */
    class Builder(private val context: Context, private val clazz: Class<out MediaSessionService>) {

        private var listener: Listener = object : Listener {}

        /**
         * Set listener
         *
         * @param listener The [Listener].
         * @return [Builder] for convenience.
         */
        fun setListener(listener: Listener): Builder {
            this.listener = listener
            return this
        }

        /**
         * Create a new [PillarboxMediaController] and connect to a [PillarboxMediaSession].
         *
         * @return a [PillarboxMediaController].
         */
        suspend fun build(): PillarboxMediaController {
            val pillarboxMediaController = PillarboxMediaController()
            val listener = MediaControllerListenerImpl(listener, pillarboxMediaController)
            val componentName = ComponentName(context, clazz)
            val sessionToken = SessionToken(context, componentName)
            val mediaController = MediaController.Builder(context, sessionToken)
                .setListener(listener)
                .buildAsync()
                .await()

            pillarboxMediaController.setMediaController(mediaController)
            return pillarboxMediaController
        }
    }

    /**
     * A listener for events and incoming commands from [PillarboxMediaSession].
     */
    interface Listener {

        /**
         * Called when the session sends a custom command.
         *
         * @see MediaController.Listener.onCustomCommand
         */
        fun onCustomCommand(
            controller: PillarboxMediaController,
            command: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
        }

        /**
         * Called when the available session commands are changed by session.
         * @see MediaController.Listener.onAvailableSessionCommandsChanged
         */
        fun onAvailableSessionCommandsChanged(controller: PillarboxMediaController, commands: SessionCommands) {}

        /**
         * Called when the controller is disconnected from the session.
         * The controller becomes unavailable afterwards and this listener won't be called anymore.
         *
         * @see MediaController.Listener.onDisconnected
         */
        fun onDisconnected(controller: PillarboxMediaController) {}

        /**
         * Called when the session extras are set on the session side.
         * @see MediaController.Listener.onExtrasChanged
         */
        fun onExtrasChanged(controller: PillarboxMediaController, extras: Bundle) {}
    }

    /**
     * Forward [MediaController.Listener] to the listener and apply it to the mediaController.
     */
    internal open class MediaControllerListenerImpl(
        val listener: Listener,
        val mediaController: PillarboxMediaController
    ) : MediaController.Listener {
        override fun onCustomCommand(
            controller: MediaController,
            command: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            DebugLogger.debug(TAG, "onCustomCommand ${command.customAction} ${command.customExtras}")
            mediaController.onSessionCommand(command, args)
            return listener.onCustomCommand(mediaController, command, args)
        }

        override fun onAvailableSessionCommandsChanged(controller: MediaController, commands: SessionCommands) {
            listener.onAvailableSessionCommandsChanged(mediaController, commands)
        }

        override fun onDisconnected(controller: MediaController) {
            listener.onDisconnected(mediaController)
        }

        override fun onExtrasChanged(controller: MediaController, extras: Bundle) {
            mediaController.onSessionExtrasChanged(extras)
            listener.onExtrasChanged(mediaController, extras)
        }
    }

    private lateinit var mediaController: MediaController
    private lateinit var playerSessionState: PlayerSessionState

    private val listeners = mutableSetOf<PillarboxPlayer.Listener>()

    /**
     * The [SessionToken] of the connected session, or `null` if it is not connected.
     * @see MediaController.getConnectedToken
     */
    val connectedToken: SessionToken?
        get() = mediaController.connectedToken

    /**
     * Is connected
     * @see MediaController.isConnected
     */
    val isConnected: Boolean
        get() = mediaController.isConnected

    /**
     * Session activity
     * @see MediaController.getSessionActivity
     */
    val sessionActivity: PendingIntent?
        get() = mediaController.sessionActivity

    /**
     * Custom layout
     * @see MediaController.getCustomLayout
     */
    @get:UnstableApi
    val customLayout: ImmutableList<CommandButton>
        get() = mediaController.getCustomLayout()

    /**
     * Session extras
     * @see MediaController.getSessionActivity
     */
    @get:UnstableApi
    val sessionExtras: Bundle
        get() = mediaController.getSessionExtras()

    /**
     * Available session commands
     * @see MediaController.getAvailableSessionCommands
     */
    val availableSessionCommands: SessionCommands
        get() = mediaController.getAvailableSessionCommands()

    override var smoothSeekingEnabled: Boolean
        set(value) {
            sendCustomCommand(PillarboxSessionCommands.setSmoothSeekingEnabled(value))
        }
        get() {
            return playerSessionState.smoothSeekingEnabled
        }

    override var trackingEnabled: Boolean
        set(value) {
            sendCustomCommand(PillarboxSessionCommands.setTrackerEnabled(value))
        }
        get() {
            return playerSessionState.trackingEnabled
        }

    internal fun setMediaController(mediaController: MediaController) {
        this.mediaController = mediaController
        playerSessionState = PlayerSessionState(sessionExtras)
        DebugLogger.debug(TAG, "setMediaController $mediaController state = $playerSessionState")
    }

    private fun onSessionExtrasChanged(extras: Bundle) {
        val oldValue = playerSessionState
        val newValue = PlayerSessionState(extras)
        DebugLogger.debug(TAG, "onSessionExtrasChanged($oldValue -> $newValue)")
        playerSessionState = newValue
        if (oldValue.smoothSeekingEnabled != newValue.smoothSeekingEnabled) {
            for (listener in listeners) {
                listener.onSmoothSeekingEnabledChanged(newValue.smoothSeekingEnabled)
            }
        }
    }

    private fun onSessionCommand(command: SessionCommand, args: Bundle) {
        DebugLogger.debug(TAG, "onSessionCommand $command $args")
        when (command.customAction) {
            PillarboxSessionCommands.CHAPTER_CHANGED -> {
                val chapter: Chapter? = BundleCompat.getParcelable(args, PillarboxSessionCommands.ARG_CHAPTER_CHANGED, Chapter::class.java)
                listeners.forEach {
                    it.onCurrentChapterChanged(chapter)
                }
            }

            PillarboxSessionCommands.BLOCKED_INTERVAL_CHANGED -> {
                val blockedInterval = BundleCompat.getParcelable(args, PillarboxSessionCommands.ARG_BLOCKED_INTERVAL, BlockedInterval::class.java)
                blockedInterval?.let {
                    listeners.forEach { listener ->
                        listener.onBlockIntervalReached(blockedInterval)
                    }
                }
            }

            PillarboxSessionCommands.TIME_INTERVAL_CHANGED -> {
                val timeInterval = BundleCompat.getParcelable(args, PillarboxSessionCommands.ARG_TIME_INTERVAL, ActionableTimeInterval::class.java)
                timeInterval?.let {
                    listeners.forEach { listener ->
                        listener.onTimeIntervalChanged(timeInterval)
                    }
                }
            }
        }
    }

    /**
     * @see [MediaController.setRating]
     */
    fun setRating(mediaId: String, rating: Rating): ListenableFuture<SessionResult> {
        return mediaController.setRating(mediaId, rating)
    }

    /**
     * @see [MediaController.setRating]
     */
    fun setRating(rating: Rating): ListenableFuture<SessionResult> {
        return mediaController.setRating(rating)
    }

    /**
     * @see [MediaController.sendCustomCommand]
     */
    @JvmOverloads
    fun sendCustomCommand(command: SessionCommand, args: Bundle = Bundle.EMPTY): ListenableFuture<SessionResult> {
        val result = mediaController.sendCustomCommand(command, args)
        result.addListener(
            {
                val resultSession = result.get()
                if (resultSession.resultCode != SessionResult.RESULT_SUCCESS) {
                    DebugLogger.warning(TAG, "SessionResult ${command.customAction} code ${resultSession.resultCode}")
                }
            }, MoreExecutors.directExecutor()
        )
        return result
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
    @Deprecated("Use #hasPreviousMediaItem() instead.", ReplaceWith("hasPreviousMediaItem()"))
    override fun hasPrevious(): Boolean {
        @Suppress("DEPRECATION")
        return mediaController.hasPrevious()
    }

    @UnstableApi
    @Deprecated("Use #hasPreviousMediaItem() instead.", ReplaceWith("hasPreviousMediaItem()"))
    override fun hasPreviousWindow(): Boolean {
        @Suppress("DEPRECATION")
        return mediaController.hasPreviousWindow()
    }

    override fun hasPreviousMediaItem(): Boolean {
        return mediaController.hasPreviousMediaItem()
    }

    @UnstableApi
    @Deprecated("Use #seekToPreviousMediaItem() instead.", ReplaceWith("seekToPreviousMediaItem()"))
    override fun previous() {
        @Suppress("DEPRECATION")
        mediaController.previous()
    }

    @UnstableApi
    @Deprecated("Use #seekToPreviousMediaItem() instead.", ReplaceWith("seekToPreviousMediaItem()"))
    override fun seekToPreviousWindow() {
        @Suppress("DEPRECATION")
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
    @Deprecated("Use #hasNextMediaItem() instead.", ReplaceWith("hasNextMediaItem()"))
    override fun hasNext(): Boolean {
        @Suppress("DEPRECATION")
        return mediaController.hasNext()
    }

    @UnstableApi
    @Deprecated("Use #hasNextMediaItem() instead.", ReplaceWith("hasNextMediaItem()"))
    override fun hasNextWindow(): Boolean {
        @Suppress("DEPRECATION")
        return mediaController.hasNextWindow()
    }

    override fun hasNextMediaItem(): Boolean {
        return mediaController.hasNextMediaItem()
    }

    @UnstableApi
    @Deprecated("Use #seekToNextMediaItem() instead.", ReplaceWith("seekToNextMediaItem()"))
    override fun next() {
        @Suppress("DEPRECATION")
        mediaController.next()
    }

    @UnstableApi
    @Deprecated("Use #seekToNextMediaItem() instead.", ReplaceWith("seekToNextMediaItem()"))
    override fun seekToNextWindow() {
        @Suppress("DEPRECATION")
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
    @Deprecated("Use getCurrentMediaItemIndex() instead.", ReplaceWith("getCurrentMediaItemIndex()"))
    override fun getCurrentWindowIndex(): Int {
        @Suppress("DEPRECATION")
        return mediaController.currentWindowIndex
    }

    override fun getCurrentMediaItemIndex(): Int {
        return mediaController.getCurrentMediaItemIndex()
    }

    @UnstableApi
    @Deprecated("Use getNextMediaItemIndex() instead.", ReplaceWith("getNextMediaItemIndex()"))
    override fun getNextWindowIndex(): Int {
        @Suppress("DEPRECATION")
        return mediaController.nextWindowIndex
    }

    override fun getNextMediaItemIndex(): Int {
        return mediaController.getNextMediaItemIndex()
    }

    @UnstableApi
    @Deprecated("Use getPreviousMediaItemIndex() instead.", ReplaceWith("getPreviousMediaItemIndex()"))
    override fun getPreviousWindowIndex(): Int {
        @Suppress("DEPRECATION")
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
    @Deprecated("Use isCurrentMediaItemDynamic() instead.", ReplaceWith("isCurrentMediaItemDynamic()"))
    override fun isCurrentWindowDynamic(): Boolean {
        @Suppress("DEPRECATION")
        return mediaController.isCurrentWindowDynamic
    }

    override fun isCurrentMediaItemDynamic(): Boolean {
        return mediaController.isCurrentMediaItemDynamic()
    }

    @UnstableApi
    @Deprecated("Use isCurrentMediaItemLive() instead.", ReplaceWith("isCurrentMediaItemLive()"))
    override fun isCurrentWindowLive(): Boolean {
        @Suppress("DEPRECATION")
        return mediaController.isCurrentWindowLive
    }

    override fun isCurrentMediaItemLive(): Boolean {
        return mediaController.isCurrentMediaItemLive()
    }

    override fun getCurrentLiveOffset(): Long {
        return mediaController.getCurrentLiveOffset()
    }

    @UnstableApi
    @Deprecated("Use isCurrentMediaItemSeekable() instead.", ReplaceWith("isCurrentMediaItemSeekable()"))
    override fun isCurrentWindowSeekable(): Boolean {
        @Suppress("DEPRECATION")
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

    @Deprecated("Use setDeviceVolume(Int, Int) instead.", ReplaceWith("setDeviceVolume(volume, 0)"))
    override fun setDeviceVolume(volume: Int) {
        @Suppress("DEPRECATION")
        mediaController.setDeviceVolume(volume)
    }

    override fun setDeviceVolume(volume: Int, flags: Int) {
        mediaController.setDeviceVolume(volume, flags)
    }

    @Deprecated("Use increaseDeviceVolume(Int) instead.", ReplaceWith("increaseDeviceVolume(0)"))
    override fun increaseDeviceVolume() {
        @Suppress("DEPRECATION")
        mediaController.increaseDeviceVolume()
    }

    override fun increaseDeviceVolume(flags: Int) {
        mediaController.increaseDeviceVolume(flags)
    }

    @Deprecated("Use decreaseDeviceVolume(Int) instead.", ReplaceWith("decreaseDeviceVolume(0)"))
    override fun decreaseDeviceVolume() {
        @Suppress("DEPRECATION")
        mediaController.decreaseDeviceVolume()
    }

    override fun decreaseDeviceVolume(flags: Int) {
        mediaController.decreaseDeviceVolume(flags)
    }

    @Deprecated("Use setDeviceMuted(Boolean, Int) instead.", ReplaceWith("setDeviceMuted(muted, 0)"))
    override fun setDeviceMuted(muted: Boolean) {
        @Suppress("DEPRECATION")
        mediaController.setDeviceMuted(muted)
    }

    override fun setDeviceMuted(muted: Boolean, flags: Int) {
        mediaController.setDeviceMuted(muted, flags)
    }

    override fun setAudioAttributes(audioAttributes: AudioAttributes, handleAudioFocus: Boolean) {
        mediaController.setAudioAttributes(audioAttributes, handleAudioFocus)
    }

    companion object {
        private const val TAG = "PillarboxMediaController"
    }
}
