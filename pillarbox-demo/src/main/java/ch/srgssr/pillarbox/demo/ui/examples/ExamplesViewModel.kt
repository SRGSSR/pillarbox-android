/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.examples

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ILRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * Examples view model
 *
 * @param application Android Application to create [ILRepository]
 */
class ExamplesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ILRepository = PlayerModule.createIlRepository(application)

    /**
     * Contents to display
     */
    val contents: StateFlow<List<Playlist>> = flow {
        val listDrmContent = repository.getLatestMediaByShowUrn(SHOW_URN, PROTECTED_CONTENT_PAGE_SIZE).getOrDefault(emptyList())
            .map { item ->
                val showTitle = item.show?.title.orEmpty()

                DemoItem(
                    title = if (showTitle.isNotBlank()) {
                        "$showTitle (${item.title})"
                    } else {
                        item.title
                    },
                    description = "DRM-protected video",
                    uri = item.urn
                )
            }
        val listTokenProtectedContent = repository.getTvLiveCenter(Bu.RTS, PROTECTED_CONTENT_PAGE_SIZE).getOrDefault(emptyList())
            .map { item ->
                DemoItem(
                    title = item.title,
                    description = "Token-protected video",
                    uri = item.urn
                )
            }
        val allProtectedContent = listDrmContent + listTokenProtectedContent

        if (allProtectedContent.isEmpty()) {
            emit(Playlist.examplesPlaylists)
        } else {
            val protectedPlaylist = Playlist(
                title = "Protected streams (URNs)",
                items = allProtectedContent
            )
            val updatedPlaylists = Playlist.examplesPlaylists.toMutableList()
                .apply {
                    add(PROTECTED_STREAMS_PLAYLIST_INDEX, protectedPlaylist)
                }

            emit(updatedPlaylists)
        }
    }.stateIn(viewModelScope, started = SharingStarted.Eagerly, Playlist.examplesPlaylists)

    private companion object {
        private const val PROTECTED_CONTENT_PAGE_SIZE = 2
        private const val PROTECTED_STREAMS_PLAYLIST_INDEX = 2
        private const val SHOW_URN = "urn:rts:show:tv:532539"
    }
}
