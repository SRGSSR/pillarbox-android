/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaSession
import ch.srgssr.pillarbox.demo.shared.data.DemoBrowser
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.ui.showcases.integrations.MediaControllerActivity
import ch.srgssr.pillarbox.player.session.PillarboxMediaLibraryService
import ch.srgssr.pillarbox.player.session.PillarboxMediaLibrarySession
import ch.srgssr.pillarbox.player.session.PillarboxMediaSession
import ch.srgssr.pillarbox.player.utils.PendingIntentUtils
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.internal.toImmutableList

/**
 * The only way to handle an Android Auto application.
 *
 * Hints for testing: https://developer.android.com/training/cars/testing
 */
class DemoMediaLibraryService : PillarboxMediaLibraryService() {

    private lateinit var demoBrowser: DemoBrowser

    override fun onCreate() {
        super.onCreate()
        demoBrowser = DemoBrowser()
        val player = PlayerModule.provideDefaultPlayer(this)
        setPlayer(player = player, callback = DemoCallback(), sessionId = "AndroidAutoSession")
    }

    override fun sessionActivity(): PendingIntent {
        val intent = Intent(applicationContext, MediaControllerActivity::class.java)
        val flags = PendingIntentUtils.appendImmutableFlagIfNeeded(PendingIntent.FLAG_UPDATE_CURRENT)
        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            flags
        )
    }

    /**
     * Android Auto uses [DemoCallback] to create the navigation.
     */
    private inner class DemoCallback : PillarboxMediaLibrarySession.Callback {
        override fun onGetLibraryRoot(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootExtras = Bundle().apply {
                putBoolean(MEDIA_SEARCH_SUPPORTED, false)
                putBoolean(CONTENT_STYLE_SUPPORTED, true)
                putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
                putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
            }
            Log.d(TAG, "onGetLibraryRoot isSuggested = ${params?.isSuggested} isRecent = ${params?.isRecent}")
            val libraryParams = LibraryParams.Builder().setExtras(rootExtras).build()
            return Futures.immediateFuture(LibraryResult.ofItem(demoBrowser.rootMediaItem, libraryParams))
        }

        override fun onGetChildren(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return demoBrowser.getChildren(parentId)?.let {
                Futures.immediateFuture(LibraryResult.ofItemList(it.toImmutableList(), LibraryParams.Builder().build()))
            } ?: super.onGetChildren(session, browser, parentId, page, pageSize, params)
        }

        override fun onGetItem(
            session: PillarboxMediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val mediaItem = demoBrowser.getMediaItemFromId(mediaId) ?: MediaItem.EMPTY
            return Futures.immediateFuture(
                LibraryResult.ofItem(mediaItem, LibraryParams.Builder().build())
            )
        }

        override fun onAddMediaItems(
            mediaSession: PillarboxMediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            /*
             * MediaItem from Browser are directly the one we want to play.
             * For MediaItem with only id, like urn, it is fine. But one with uri is not, as the localConfiguration is null here.
             * We have to get the original mediaItem with the uri set.
             */
            return Futures.immediateFuture(mediaItems.map { demoBrowser.getMediaItemFromId(it.mediaId) ?: it }.toMutableList())
        }
    }

    private companion object {
        private const val TAG = "MediaLibraryService"
        private const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"
        private const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
        private const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
        private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
        private const val CONTENT_STYLE_LIST = 1
        private const val CONTENT_STYLE_GRID = 2
    }
}
