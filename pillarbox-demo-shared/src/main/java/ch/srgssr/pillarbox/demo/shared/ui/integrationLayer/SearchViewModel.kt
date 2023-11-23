/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer

import androidx.annotation.Px
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ch.srg.dataProvider.integrationlayer.data.IlImage
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.core.business.images.ImageScalingService
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ILRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlin.time.Duration.Companion.milliseconds

/**
 * [ViewModel] used to search for content in a specific BU.
 *
 * @param ilRepository The repository used to load the data from the integration layer.
 * @param imageScalingService The service to scale the image to display.
 *
 * @constructor Create a new [SearchViewModel].
 */
class SearchViewModel(
    private val ilRepository: ILRepository,
    private val imageScalingService: ImageScalingService
) : ViewModel() {
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

    @OptIn(FlowPreview::class)
    private val config = combine(bu, query) { bu, query -> Config(bu, query) }.debounce(100.milliseconds)

    /**
     * Result of the search trigger by [bu] and [query]
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val result: Flow<PagingData<Content.Media>> = config.transformLatest { (bu, query) ->
        emit(
            PagingData.empty(
                sourceLoadStates = LoadStates(
                    refresh = LoadState.Loading,
                    prepend = LoadState.Loading,
                    append = LoadState.Loading
                )
            )
        )

        if (hasValidSearchQuery(query)) {
            emitAll(
                ilRepository.search(bu, query).map { mediaPagingData ->
                    mediaPagingData.map { item -> Content.Media(item) }
                }.cachedIn(viewModelScope)
            )
        } else {
            val loadState = LoadState.NotLoading(true)
            emit(
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

    /**
     * Checks if the provided [query] is valid.
     *
     * @return `true` if [query] has at least [VALID_SEARCH_QUERY_THRESHOLD] characters, `false` otherwise
     */
    fun hasValidSearchQuery(query: String = this.query.value): Boolean {
        return query.length >= VALID_SEARCH_QUERY_THRESHOLD
    }

    /**
     * Get the URL of the scaled image, in the specified format, to match as much as possible the container width.
     *
     * @param imageUrl The original image URL.
     * @param containerWidth The width, in pixels, of the image container.
     * @param format The desired format of the transformed image.
     *
     * @return xx
     */
    fun getScaledImageUrl(
        imageUrl: String,
        @Px containerWidth: Int,
        format: ImageScalingService.ImageFormat = ImageScalingService.ImageFormat.WEBP
    ): String {
        val size = IlImage.Size.getClosest(containerWidth)
        val width = enumValueOf<ImageScalingService.ImageWidth>(size.name)

        return imageScalingService.getScaledImageUrl(
            imageUrl = imageUrl,
            width = width,
            format = format
        )
    }

    internal data class Config(val bu: Bu, val query: String)

    @Suppress("UndocumentedPublicClass")
    class Factory(
        private var ilRepository: ILRepository,
        private val imageScalingService: ImageScalingService = PlayerModule.provideImageScalingService()
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(ilRepository, imageScalingService) as T
        }
    }

    private companion object {
        private const val VALID_SEARCH_QUERY_THRESHOLD = 3
    }
}
