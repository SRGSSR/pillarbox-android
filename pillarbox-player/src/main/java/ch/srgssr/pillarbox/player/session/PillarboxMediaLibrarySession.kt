/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.app.PendingIntent
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

open class PillarboxMediaLibrarySession internal constructor(callback: Callback) :
    PillarboxMediaSession(callback),
    MediaLibrarySession.Callback {

    interface Callback : PillarboxMediaSession.Callback {
        fun onGetLibraryRoot(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_NOT_SUPPORTED))
        }

        fun onGetChildren(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_NOT_SUPPORTED))
        }

        fun onGetItem(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_NOT_SUPPORTED))
        }

        fun onSearch(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_NOT_SUPPORTED))
        }

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

    class Builder(private val context: Context, private val player: PillarboxPlayer, private val callback: Callback) {
        private var pendingIntent: PendingIntent? = PendingIntentUtils.getDefaultPendingIntent(context)
        private var id: String? = null

        fun setSessionActivity(pendingIntent: PendingIntent): Builder {
            this.pendingIntent = pendingIntent
            return this
        }

        fun setId(id: String): Builder {
            this.id = id
            return this
        }

        fun build(): PillarboxMediaLibrarySession {
            val pillarboxMediaSession = PillarboxMediaLibrarySession(callback)
            val mediaSessionBuilder = MediaLibrarySession.Builder(context, player, pillarboxMediaSession)
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

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return (callback as Callback).onGetLibraryRoot(this, browser, params)
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return (callback as Callback).onGetChildren(this, browser, parentId, page, pageSize, params)
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return (callback as Callback).onGetItem(this, browser, mediaId)
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<Void>> {
        return (callback as Callback).onSearch(this, browser, query, params)
    }

    override fun onGetSearchResult(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return (callback as Callback).onGetSearchResult(this, browser, query, page, pageSize, params)
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        return (callback as Callback).onAddMediaItems(this, controller, mediaItems)
    }
}
