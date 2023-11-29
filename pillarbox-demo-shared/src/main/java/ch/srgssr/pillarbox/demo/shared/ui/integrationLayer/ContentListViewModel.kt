/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer

import androidx.annotation.Px
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ch.srg.dataProvider.integrationlayer.data.ImageUrl
import ch.srg.dataProvider.integrationlayer.request.image.ImageWidth
import ch.srg.dataProvider.integrationlayer.request.image.decorated
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ILRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * [ViewModel] used to display the various sections in "Lists".
 *
 * @param ilRepository The repository used to load the data from the integration layer.
 * @param contentList The type of list to display.

 * @constructor Create a new [ContentListViewModel].
 */
class ContentListViewModel(
    private val ilRepository: ILRepository,
    private val contentList: ContentList,
) : ViewModel() {

    /**
     * Data flow of PagingData<Content>
     */
    val data = load().cachedIn(viewModelScope)

    private fun load(): Flow<PagingData<Content>> {
        return when (contentList) {
            is ContentList.TVLatestMedias -> ilRepository.getTvLatestMedia(bu = contentList.bu)
                .mapPaging { Content.Media(it) }

            is ContentList.RadioLatestMedias -> ilRepository.getRadioLatestMedia(contentList.radioChannel)
                .mapPaging { Content.Media(it) }

            is ContentList.TVShows -> ilRepository.getTVShows(contentList.bu)
                .mapPaging { Content.Show(it) }

            is ContentList.TVTopics -> ilRepository.getTvTopics(contentList.bu)
                .mapPaging { Content.Topic(it) }

            is ContentList.LatestMediaForShow -> ilRepository.getLatestMediaByShowUrn(contentList.urn)
                .mapPaging { Content.Media(it) }

            is ContentList.LatestMediaForTopic -> ilRepository.getLatestMediaByTopicUrn(contentList.urn)
                .mapPaging { Content.Media(it) }

            is ContentList.RadioShows -> ilRepository.getRadioShows(contentList.radioChannel)
                .mapPaging { Content.Show(it) }

            is ContentList.TVLiveWeb -> ilRepository.getTvLiveWeb(bu = contentList.bu)
                .mapPaging { Content.Media(it) }

            is ContentList.TVLiveCenter -> ilRepository.getTvLiveCenter(bu = contentList.bu)
                .mapPaging { Content.Media(it) }

            is ContentList.TVLivestreams -> ilRepository.getTvLiveStream(bu = contentList.bu)
                .mapPaging { Content.Media(it) }

            is ContentList.RadioLiveStreams -> ilRepository.getRadioLiveStream(bu = contentList.bu)
                .mapPaging { Content.Media(it) }

            else -> {
                require(false) { "Can't find data for $contentList" }
                flowOf(PagingData.empty())
            }
        }
    }

    /**
     * Get the URL of the scaled image, in the specified format, to match as much as possible the container width.
     *
     * @param imageUrl The original image URL.
     * @param containerWidth The width, in pixels, of the image container.
     *
     * @return xx
     */
    fun getScaledImageUrl(
        imageUrl: ImageUrl,
        @Px containerWidth: Int,
    ): String {
        val width = ImageWidth.getFromPixels(containerWidth)

        return imageUrl.decorated(width = width)
    }

    private fun <T : Any, R : Any> Flow<PagingData<T>>.mapPaging(transform: (T) -> R): Flow<PagingData<R>> {
        return map { pagingData ->
            pagingData.map(transform)
        }
    }

    @Suppress("UndocumentedPublicClass")
    class Factory(
        private var ilRepository: ILRepository,
        private val contentList: ContentList,
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContentListViewModel(ilRepository, contentList) as T
        }
    }
}
