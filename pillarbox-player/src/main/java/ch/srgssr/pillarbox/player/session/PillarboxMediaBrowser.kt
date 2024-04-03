/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.content.ComponentName
import android.content.Context
import androidx.annotation.IntRange
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.SessionToken
import kotlinx.coroutines.guava.await

class PillarboxMediaBrowser private constructor() : PillarboxMediaController() {
    private lateinit var mediaBrowser: MediaBrowser

    private class MediaBrowserListenerImpl(
        listener: Listener,
        mediaBrowser: PillarboxMediaBrowser
    ) : MediaControllerListenerImpl(listener, mediaBrowser), MediaBrowser.Listener {

        override fun onChildrenChanged(browser: MediaBrowser, parentId: String, itemCount: Int, params: LibraryParams?) {
            (listener as Listener).onChildrenChanged(mediaController as PillarboxMediaBrowser, parentId, itemCount, params)
        }

        override fun onSearchResultChanged(browser: MediaBrowser, query: String, itemCount: Int, params: LibraryParams?) {
            (listener as Listener).onSearchResultChanged(mediaController as PillarboxMediaBrowser, query, itemCount, params)
        }
    }

    class Builder(private val context: Context, private val clazz: Class<out MediaLibraryService>) {
        private var listener: Listener = object : Listener {}

        fun setListener(listener: Listener): Builder {
            this.listener = listener
            return this
        }

        suspend fun build(): PillarboxMediaBrowser {
            val pillarboxMediaController = PillarboxMediaBrowser()
            val listener = MediaBrowserListenerImpl(listener, pillarboxMediaController)
            val componentName = ComponentName(context, clazz)
            val sessionToken = SessionToken(context, componentName)
            val mediaBrowser = MediaBrowser.Builder(context, sessionToken)
                .setListener(listener)
                .buildAsync()
                .await()
            pillarboxMediaController.setMediaBrowser(mediaBrowser)
            return pillarboxMediaController
        }
    }

    interface Listener : PillarboxMediaController.Listener {
        fun onChildrenChanged(
            browser: PillarboxMediaBrowser,
            parentId: String,
            itemCount: Int,
            params: LibraryParams?
        ) {}

        fun onSearchResultChanged(browser: PillarboxMediaBrowser, query: String, itemCount: Int, params: LibraryParams?) {}
    }

    internal fun setMediaBrowser(mediaBrowser: MediaBrowser) {
        setMediaController(mediaBrowser)
        this.mediaBrowser = mediaBrowser
    }

    fun getLibraryRoot(params: LibraryParams? = null) = mediaBrowser.getLibraryRoot(params)

    fun subscribe(parentId: String, params: LibraryParams? = null) = mediaBrowser.subscribe(parentId, params)

    fun unsubscribe(parentId: String) = mediaBrowser.unsubscribe(parentId)

    fun getChildren(
        parentId: String,
        @IntRange(from = 0) page: Int,
        @IntRange(from = 1) pageSize: Int,
        params: LibraryParams? = null
    ) = mediaBrowser.getChildren(parentId, page, pageSize, params)

    fun getItem(mediaId: String) = mediaBrowser.getItem(mediaId)

    fun search(query: String, params: LibraryParams? = null) = mediaBrowser.search(query, params)

    fun getSearchResult(
        query: String,
        @IntRange(from = 0) page: Int,
        @IntRange(from = 1) pageSize: Int,
        params: LibraryParams? = null
    ) = mediaBrowser.getSearchResult(query, page, pageSize, params)
}
