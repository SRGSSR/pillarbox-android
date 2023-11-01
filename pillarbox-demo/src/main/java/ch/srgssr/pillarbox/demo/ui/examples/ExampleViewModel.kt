/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.examples

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.ILRepository
import ch.srgssr.pillarbox.demo.ui.integrationLayer.di.IntegrationLayerModule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * Example view model
 *
 * @param application Android Application to create [ILRepository]
 */
class ExampleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ILRepository = IntegrationLayerModule.createIlRepository(application)

    /**
     * Contents to display
     */
    val contents: StateFlow<List<Playlist>> = flow {
        val listDrmContent = repository.getLatestMediaByShowUrn(SHOW_URN, 2).getOrDefault(emptyList())
        val listTokenProtectedContent = repository.getTvLiveCenter(Bu.RTS, 2).getOrDefault(emptyList())
        val playlist = Playlist(
            title = PROTECTED_CONTENT_TITLE,
            items = (listDrmContent + listTokenProtectedContent).map { DemoItem(title = it.title, description = it.lead, uri = it.urn) }
        )
        emit(LIST_STATIC_PLAYLIST + playlist)
    }.stateIn(viewModelScope, started = SharingStarted.Eagerly, LIST_STATIC_PLAYLIST)

    companion object {
        private const val SHOW_URN = "urn:rts:show:tv:532539"
        private const val PROTECTED_CONTENT_TITLE = "Protected contents"

        private val LIST_STATIC_PLAYLIST = listOf(
            Playlist.StreamUrls,
            Playlist.StreamUrns,
            Playlist.PlaySuisseStreams,
            Playlist.StreamApples,
            Playlist.StreamGoogles,
            Playlist.BitmovinSamples,
            Playlist.UnifiedStreaming,
            Playlist.UnifiedStreamingDash,
        )
    }
}
