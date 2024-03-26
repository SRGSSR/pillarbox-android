/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.content.ComponentName
import android.content.Context
import androidx.annotation.IntRange
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.SessionToken
import ch.srgssr.pillarbox.player.service.PillarboxMediaSessionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.withContext

class PillarboxMediaBrowser private constructor() : PillarboxMediaController(), MediaBrowser.Listener {
    private lateinit var mediaBrowser: MediaBrowser

    class Builder(private val context: Context, private val clazz: Class<out PillarboxMediaSessionService>) {

        suspend fun build(): PillarboxMediaController {
            return withContext(Dispatchers.IO) {
                val pillarboxMediaController = PillarboxMediaBrowser()
                val componentName = ComponentName(context, clazz)
                val sessionToken = SessionToken(context, componentName)
                val mediaBrowser = MediaBrowser.Builder(context, sessionToken)
                    .setListener(pillarboxMediaController)
                    .buildAsync()
                    .await()
                pillarboxMediaController.setMediaBrowser(mediaBrowser)
                pillarboxMediaController
            }
        }
    }

    override fun onChildrenChanged(browser: MediaBrowser, parentId: String, itemCount: Int, params: LibraryParams?) {
        super.onChildrenChanged(browser, parentId, itemCount, params)
        TODO("Implement maybe a custom listener")
    }

    override fun onSearchResultChanged(browser: MediaBrowser, query: String, itemCount: Int, params: LibraryParams?) {
        super.onSearchResultChanged(browser, query, itemCount, params)
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
