/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.app.PendingIntent
import androidx.annotation.IntRange
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.session.PillarboxMediaLibrarySession.Builder
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * An extended [PillarboxMediaSession] for the [PillarboxMediaLibraryService].
 * Build an instance with [Builder] and return it from [PillarboxMediaLibraryService.onGetPillarboxSession]
 * or [PillarboxMediaLibraryService.onGetSession] with  [PillarboxMediaLibrarySession.mediaSession].
 *
 * @see MediaLibrarySession
 * @see PillarboxMediaLibraryService
 * @see PillarboxMediaBrowser
 */
open class PillarboxMediaLibrarySession internal constructor() :
    PillarboxMediaSession() {

    /**
     * An extended [PillarboxMediaSession.Callback] for the [PillarboxMediaLibrarySession].
     * <p>When you return [LibraryResult] with [MediaItem] media items, each item must
     * have valid [MediaItem.mediaId ]and specify [MediaMetadata.isBrowsable] and [MediaMetadata.isPlayable] in its [MediaItem.mediaMetadata].
     * @see MediaLibrarySession.Callback
     */
    interface Callback : PillarboxMediaSession.Callback {
        /**
         * Called when a [PillarboxMediaBrowser]  requests the root [MediaItem].
         * @see MediaLibrarySession.Callback.onGetLibraryRoot
         */
        fun onGetLibraryRoot(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: MediaLibraryService.LibraryParams?,
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_NOT_SUPPORTED))
        }

        /**
         * Called when a [PillarboxMediaBrowser] requests the child media items of the given parent id.
         * @see MediaLibrarySession.Callback.onGetChildren
         */
        fun onGetChildren(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            @IntRange(from = 0) page: Int,
            @IntRange(from = 1) pageSize: Int,
            params: MediaLibraryService.LibraryParams?,
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_NOT_SUPPORTED))
        }

        /**
         * Called when a [PillarboxMediaBrowser] requests a [MediaItem] from mediaId.
         * @see MediaLibrarySession.Callback.onGetItem
         */
        fun onGetItem(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_NOT_SUPPORTED))
        }

        /**
         * Called when a [androidx.media3.session.MediaBrowser] requests a search.
         * @see MediaLibrarySession.Callback.onSearch
         */
        fun onSearch(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_NOT_SUPPORTED))
        }

        /**
         * Called when a [PillarboxMediaBrowser] requests the child media items of the given parent id.
         * @see MediaLibrarySession.Callback.onGetSearchResult
         */
        fun onGetSearchResult(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_NOT_SUPPORTED))
        }
    }

    /**
     * A builder for [PillarboxMediaLibrarySession].
     *
     * Any incoming requests from the [PillarboxMediaBrowser] will be handled on the application
     * thread of the underlying [PillarboxPlayer].
     *
     * @param service The [MediaLibraryService] that instantiates the [PillarboxMediaLibrarySession].
     * @param player The underlying player to perform playback and handle transport controls.
     * @param callback The [Callback] to handle requests from [PillarboxMediaBrowser].
     */
    class Builder(
        private val service: MediaLibraryService,
        private val player: PillarboxPlayer,
        private val callback: Callback,
    ) {
        private var pendingIntent: PendingIntent? = PendingIntentUtils.getDefaultPendingIntent(service)
        private var id: String? = null

        /**
         * Set session activity
         * @see MediaLibrarySession.Builder.setSessionActivity
         * @param pendingIntent The [PendingIntent].
         * @return the builder for convenience.
         */
        fun setSessionActivity(pendingIntent: PendingIntent): Builder {
            this.pendingIntent = pendingIntent
            return this
        }

        /**
         * Set id
         * @see MediaLibrarySession.Builder.setId
         * @param id The ID. Must be unique among all sessions per package.
         * @return the builder for convenience.
         */
        fun setId(id: String): Builder {
            this.id = id
            return this
        }

        /**
         * Build
         *
         * @return a new [PillarboxMediaLibrarySession]
         */
        fun build(): PillarboxMediaLibrarySession {
            val pillarboxMediaSession = PillarboxMediaLibrarySession()
            val media3Callback = MediaLibraryCallbackImpl(callback, pillarboxMediaSession)
            val mediaSessionBuilder = MediaLibrarySession.Builder(service, player, media3Callback)
            val mediaSession = mediaSessionBuilder.apply {
                id?.let { setId(it) }
                pendingIntent?.let { setSessionActivity(it) }
            }.build()
            pillarboxMediaSession.setMediaSession(mediaSession)
            return pillarboxMediaSession
        }
    }

    override val mediaSession: MediaLibrarySession
        get() = super.mediaSession as MediaLibrarySession

    internal class MediaLibraryCallbackImpl(callback: Callback, mediaSession: PillarboxMediaLibrarySession) :
        MediaSessionCallbackImpl(callback, mediaSession), MediaLibrarySession.Callback {
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return (callback as Callback).onGetLibraryRoot(this.mediaSession as PillarboxMediaLibrarySession, browser, params)
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return (callback as Callback).onGetChildren(this.mediaSession as PillarboxMediaLibrarySession, browser, parentId, page, pageSize, params)
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return (callback as Callback).onGetItem(this.mediaSession as PillarboxMediaLibrarySession, browser, mediaId)
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            return (callback as Callback).onSearch(this.mediaSession as PillarboxMediaLibrarySession, browser, query, params)
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return (callback as Callback).onGetSearchResult(this.mediaSession as PillarboxMediaLibrarySession, browser, query, page, pageSize, params)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            return (callback as Callback).onAddMediaItems(this.mediaSession as PillarboxMediaLibrarySession, controller, mediaItems)
        }
    }
}
