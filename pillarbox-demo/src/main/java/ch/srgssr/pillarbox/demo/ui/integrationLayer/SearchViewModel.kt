/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertHeaderItem
import androidx.paging.map
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.ILRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Search view model to search media for the chosen bu
 *
 * @property ilRepository
 * @constructor Create empty Search view model
 */
class SearchViewModel(private val ilRepository: ILRepository) : ViewModel() {
    private val _bu = MutableStateFlow(Bu.RTS)

    /**
     * Currently selected [Bu].
     */
    val bu: StateFlow<Bu> = _bu

    private val _query = MutableStateFlow("")

    /**
     * Current search query string.
     */
    val query: StateFlow<String> = _query

    private val config = combine(bu, query) { bu, query -> Config(bu, query) }

    /**
     * Result of the search trigger by [bu] and [query]
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val result: Flow<PagingData<SearchContent>> = config.flatMapLatest { config ->
        if (config.query.length >= 3) {
            ilRepository.search(config.bu, config.query).map { mediaPagingData ->
                val pagingData: PagingData<SearchContent> = mediaPagingData.map { item -> SearchContent.MediaResult(Content.Media(item)) }
                pagingData.insertHeaderItem(item = SearchContent.BuSelector)
            }.cachedIn(viewModelScope)
        } else {
            val loadState = LoadState.NotLoading(true)
            flowOf(
                PagingData.empty(
                    sourceLoadStates = LoadStates(
                        refresh = loadState,
                        prepend = loadState,
                        append = loadState
                    )
                )
            )
        }
    }

    /**
     * Clear search query parameter.
     */
    fun clear() {
        _query.value = ""
    }

    /**
     * Set the search query.
     *
     * @param query The search query
     */
    fun setQuery(query: String) {
        _query.value = query
    }

    /**
     * Select bu.
     *
     * @param bu The [Bu] to select.
     */
    fun selectBu(bu: Bu) {
        _bu.value = bu
    }

    internal data class Config(val bu: Bu, val query: String)

    @Suppress("UndocumentedPublicClass")
    class Factory(
        private var ilRepository: ILRepository,
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(ilRepository) as T
        }
    }
}

/**
 * Search content
 */
sealed interface SearchContent {
    /**
     * Bu selector
     */
    object BuSelector : SearchContent

    /**
     * Search Media result data
     *
     * @property media
     */
    data class MediaResult(val media: Content.Media) : SearchContent
}
