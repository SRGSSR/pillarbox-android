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

/**
 * PillarboxMediaBrowser extends [PillarboxMediaController] but connect to a [PillarboxMediaLibrarySession] from a [MediaLibraryService].
 * @see MediaBrowser
 * @see MediaLibraryService
 */
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

    /**
     * Builder for [PillarboxMediaBrowser].
     *
     * @param context The context.
     * @param clazz The class of the [MediaLibraryService] that holds the [PillarboxMediaLibrarySession].
     */
    class Builder(private val context: Context, private val clazz: Class<out MediaLibraryService>) {
        private var listener: Listener = object : Listener {}

        /**
         * Set listener
         *
         * @param listener The listener
         * @return this builder for convenience.
         */
        fun setListener(listener: Listener): Builder {
            this.listener = listener
            return this
        }

        /**
         * Create a new [PillarboxMediaBrowser] and connect to a [PillarboxMediaBrowser].
         *
         * @return a [PillarboxMediaBrowser].
         */
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

    /**
     * A listener for events and incoming commands from [PillarboxMediaLibrarySession].
     */
    interface Listener : PillarboxMediaController.Listener {
        /**
         * Called when there's a change in the parent's children after you've subscribed to the parent with subscribe.
         *
         * @see MediaBrowser.Listener.onChildrenChanged
         */
        fun onChildrenChanged(
            browser: PillarboxMediaBrowser,
            parentId: String,
            itemCount: Int,
            params: LibraryParams?
        ) {
        }

        /**
         * Called when there's change in the search result requested by the previous search(String, MediaLibraryService.LibraryParams).
         *
         * @see MediaBrowser.Listener.onSearchResultChanged
         */
        fun onSearchResultChanged(
            browser: PillarboxMediaBrowser,
            query: String,
            itemCount: Int,
            params: LibraryParams?
        ) {
        }
    }

    internal fun setMediaBrowser(mediaBrowser: MediaBrowser) {
        setMediaController(mediaBrowser)
        this.mediaBrowser = mediaBrowser
    }

    /**
     * Get library root
     *
     * @param params The optional parameters for getting library root item.
     * @see MediaBrowser.getLibraryRoot
     */
    @JvmOverloads
    suspend fun getLibraryRoot(params: LibraryParams? = null) = mediaBrowser.getLibraryRoot(params).await()

    /**
     * Subscribes to a parent id for changes to its children.
     * When there's a change, [PillarboxMediaBrowser.Listener.onChildrenChanged] will be called with the MediaLibraryService.LibraryParams.
     * You may call [PillarboxMediaBrowser.getChildren] to get the children.
     *
     * @param parentId A non-empty parent id to subscribe to.
     * @param params Optional parameters.
     * @see MediaBrowser.subscribe
     */
    @JvmOverloads
    suspend fun subscribe(
        parentId: String,
        params: LibraryParams? = null
    ) = mediaBrowser.subscribe(parentId, params).await()

    /**
     * Unsubscribes from a parent id for changes to its children, which was previously subscribed by subscribe.
     *
     * @param parentId A non-empty parent id to unsubscribe from.
     * @see MediaBrowser.unsubscribe
     */
    suspend fun unsubscribe(parentId: String) = mediaBrowser.unsubscribe(parentId).await()

    /**
     * Get children for the parentId
     *
     * @param parentId A non-empty parent id for getting the children.
     * @param page A page number to get the paginated result starting from 0.
     * @param pageSize A page size to get the paginated result.
     * @param params Optional parameters.
     * @see MediaBrowser.getChildren
     */
    @JvmOverloads
    suspend fun getChildren(
        parentId: String,
        @IntRange(from = 0) page: Int,
        @IntRange(from = 1) pageSize: Int,
        params: LibraryParams? = null
    ) = mediaBrowser.getChildren(parentId, page, pageSize, params).await()

    /**
     * Get item
     *
     * @param mediaId A non-empty media id.
     * @see MediaBrowser.getItem
     */
    suspend fun getItem(mediaId: String) = mediaBrowser.getItem(mediaId).await()

    /**
     * Requests a search from the library service.
     *
     * @param query A non-empty search query.
     * @param params Optional parameters.
     * @see MediaBrowser.search
     */
    @JvmOverloads
    suspend fun search(query: String, params: LibraryParams? = null) = mediaBrowser.search(query, params).await()

    /**
     * Returns the search result from the library service.

     * @param query A non-empty search query that you've specified with [search].
     * @param page A page number to get the paginated result starting from 0
     * @param pageSize A page size to get the paginated result.
     * @param params Optional parameters.
     */
    @JvmOverloads
    suspend fun getSearchResult(
        query: String,
        @IntRange(from = 0) page: Int,
        @IntRange(from = 1) pageSize: Int,
        params: LibraryParams? = null
    ) = mediaBrowser.getSearchResult(query, page, pageSize, params).await()
}
