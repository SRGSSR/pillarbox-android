/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ch.srg.dataProvider.integrationlayer.data.remote.LiveCenterType
import ch.srg.dataProvider.integrationlayer.data.remote.Media
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.SearchParams
import ch.srg.dataProvider.integrationlayer.data.remote.Show
import ch.srg.dataProvider.integrationlayer.data.remote.Topic
import ch.srg.dataProvider.integrationlayer.data.remote.Transmission
import ch.srg.dataProvider.integrationlayer.request.IlService
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srg.dataProvider.integrationlayer.request.parameters.IlMediaType
import ch.srg.dataProvider.integrationlayer.request.parameters.IlPaging.Unlimited.toIlPaging
import ch.srg.dataProvider.integrationlayer.request.parameters.IlTransmission
import ch.srgssr.dataprovider.paging.DataProviderPaging
import ch.srgssr.dataprovider.paging.datasource.NextUrlPagingSource
import kotlinx.coroutines.flow.Flow

/**
 * IntegrationLayer repository
 */
class ILRepository(
    private val dataProviderPaging: DataProviderPaging,
    private val ilService: IlService
) {

    /**
     * Get tv latest media
     *
     * @param bu
     */
    fun getTvLatestMedia(bu: Bu): Flow<PagingData<Media>> {
        return dataProviderPaging.getTvLatestEpisodes(bu, pageSize = PAGE_SIZE)
    }

    /**
     * Get radio latest media
     *
     * @param radioChannel
     */
    fun getRadioLatestMedia(radioChannel: RadioChannel): Flow<PagingData<Media>> {
        return dataProviderPaging.getLatestMediaByChannelId(
            bu = radioChannel.bu,
            channelId = radioChannel.channelId,
            type = IlMediaType(MediaType.AUDIO),
            pageSize = PAGE_SIZE
        )
    }

    /**
     * Get t v shows
     *
     * @param bu
     */
    fun getTVShows(bu: Bu): Flow<PagingData<Show>> {
        return dataProviderPaging.getTvAlphabeticalShows(bu = bu, pageSize = PAGE_SIZE)
    }

    /**
     * Get radio shows
     *
     * @param radioChannel
     */
    fun getRadioShows(radioChannel: RadioChannel): Flow<PagingData<Show>> {
        return dataProviderPaging.getRadioAlphabeticalShowsByChannelId(
            bu = radioChannel.bu,
            radioChannelId = radioChannel.channelId,
            pageSize = PAGE_SIZE
        )
    }

    /**
     * Get tv topics
     *
     * @param bu
     */
    fun getTvTopics(bu: Bu): Flow<PagingData<Topic>> {
        return Pager(config = PagingConfig(pageSize = PAGE_SIZE), pagingSourceFactory = {
            NextUrlPagingSource(
                initialCall = { ilService.getTopics(bu = bu, transmissionType = IlTransmission(Transmission.TV)) },
                nextCall = { null }
            )
        }).flow
    }

    /**
     * Get latest media by show urn
     *
     * @param urn
     */
    fun getLatestMediaByShowUrn(urn: String): Flow<PagingData<Media>> {
        return dataProviderPaging.getLatestMediaByShowUrn(showUrn = urn, pageSize = PAGE_SIZE)
    }

    /**
     * Get latest media by show urn
     *
     * @param urn
     * @param pageSize
     * @return
     */
    suspend fun getLatestMediaByShowUrn(urn: String, pageSize: Int): Result<List<Media>> {
        return runCatching { ilService.getLatestMediaByShowUrn(urn, pageSize.toIlPaging()).list }
    }

    /**
     * Get latest media by topic urn
     *
     * @param urn
     */
    fun getLatestMediaByTopicUrn(urn: String): Flow<PagingData<Media>> {
        return dataProviderPaging.getLatestMediaByTopicUrn(topicUrn = urn, pageSize = PAGE_SIZE)
    }

    /**
     * Get tv live stream
     *
     * @param bu
     */
    fun getTvLiveStream(bu: Bu): Flow<PagingData<Media>> {
        return Pager(config = PagingConfig(pageSize = PAGE_SIZE), pagingSourceFactory = {
            NextUrlPagingSource(
                initialCall = { ilService.getTvLiveStreams(bu) },
                nextCall = { ilService.getMediaListNextUrl(it) }
            )
        }).flow
    }

    /**
     * Get radio live stream
     *
     * @param bu
     */
    fun getRadioLiveStream(bu: Bu): Flow<PagingData<Media>> {
        return Pager(config = PagingConfig(pageSize = PAGE_SIZE), pagingSourceFactory = {
            NextUrlPagingSource(
                initialCall = { ilService.getRadioLiveStreams(bu) },
                nextCall = { ilService.getMediaListNextUrl(it) }
            )
        }).flow
    }

    /**
     * Get tv live web
     *
     * @param bu
     */
    fun getTvLiveWeb(bu: Bu): Flow<PagingData<Media>> {
        return dataProviderPaging.getScheduledLiveStreamVideos(bu = bu, pageSize = PAGE_SIZE)
    }

    /**
     * Get tv live center
     *
     * @param bu
     */
    fun getTvLiveCenter(bu: Bu): Flow<PagingData<Media>> {
        return dataProviderPaging.getLiveCenterVideos(bu = bu, pageSize = PAGE_SIZE, type = LiveCenterType.SCHEDULED_LIVESTREAM)
    }

    /**
     * Get tv live center
     *
     * @param bu
     * @param pageSize
     * @return
     */
    suspend fun getTvLiveCenter(bu: Bu, pageSize: Int): Result<List<Media>> {
        return runCatching { ilService.getLiveCenterVideos(bu = bu, pageSize = pageSize.toIlPaging()).list }
    }

    /**
     * Search
     *
     * @param bu
     * @param query
     */
    fun search(bu: Bu, query: String): Flow<PagingData<Media>> {
        return dataProviderPaging.searchMedia(
            bu = bu, searchTerm = query,
            queryParameters = SearchParams.MediaParams(includeAggregations = false),
            pageSize = PAGE_SIZE
        )
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
