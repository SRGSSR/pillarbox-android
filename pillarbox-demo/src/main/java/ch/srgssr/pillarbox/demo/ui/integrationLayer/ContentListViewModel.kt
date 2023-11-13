/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.Content
import ch.srgssr.pillarbox.demo.ui.integrationLayer.data.ILRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Content list view model
 *
 * @property ilRepository
 * @property contentList
 * @constructor Create empty Content list view model
 */
class ContentListViewModel(
    var ilRepository: ILRepository,
    private val contentList: ContentList
) : ViewModel() {

    /**
     * Data flow of PagingData<Content>
     */
    val data = load().cachedIn(viewModelScope)

    private fun load(): Flow<PagingData<Content>> {
        return when (contentList) {
            is ContentList.TVLatestMedias -> {
                ilRepository.getTvLatestMedia(bu = contentList.bu).map { pagingData ->
                    pagingData.map { media -> Content.Media(media) }
                }
            }

            is ContentList.RadioLatestMedias -> {
                ilRepository.getRadioLatestMedia(contentList.radioChannel).map { pagingData ->
                    pagingData.map { media -> Content.Media(media) }
                }
            }

            is ContentList.TvShows -> {
                ilRepository.getTVShows(contentList.bu).map { pagingData ->
                    pagingData.map { show -> Content.Show(show) }
                }
            }

            is ContentList.TvTopics -> {
                ilRepository.getTvTopics(contentList.bu).map { pagingData ->
                    pagingData.map { topic -> Content.Topic(topic) }
                }
            }

            is ContentList.LatestMediaForShow -> {
                ilRepository.getLatestMediaByShowUrn(contentList.urn).map { pagingData ->
                    pagingData.map { media -> Content.Media(media) }
                }
            }

            is ContentList.LatestMediaForTopic -> {
                ilRepository.getLatestMediaByTopicUrn(contentList.urn).map { pagingData ->
                    pagingData.map { media -> Content.Media(media) }
                }
            }

            is ContentList.RadioShows -> {
                ilRepository.getRadioShows(contentList.radioChannel).map { pagingData ->
                    pagingData.map { show -> Content.Show(show) }
                }
            }

            is ContentList.TVLiveWeb -> {
                ilRepository.getTvLiveWeb(bu = contentList.bu).map { pagingData ->
                    pagingData.map { media -> Content.Media(media) }
                }
            }

            is ContentList.TVLiveCenter -> {
                ilRepository.getTvLiveCenter(bu = contentList.bu).map { pagingData ->
                    pagingData.map { media -> Content.Media(media) }
                }
            }

            is ContentList.TVLivestreams -> {
                ilRepository.getTvLiveStream(bu = contentList.bu).map { pagingData ->
                    pagingData.map { media -> Content.Media(media) }
                }
            }

            is ContentList.RadioLiveStreams -> {
                ilRepository.getRadioLiveStream(bu = contentList.bu).map { pagingData ->
                    pagingData.map { media -> Content.Media(media) }
                }
            }

            else -> {
                require(false) { "Can't find data for $contentList" }
                flowOf(PagingData.empty())
            }
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
