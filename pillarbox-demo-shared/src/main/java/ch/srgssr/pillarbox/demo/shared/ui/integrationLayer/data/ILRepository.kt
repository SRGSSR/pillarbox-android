/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("TooManyFunctions")

package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ch.srg.dataProvider.integrationlayer.data.remote.Channel
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
     * Get TV latest media
     *
     * @param bu
     */
    fun getTvLatestMedia(bu: Bu): Flow<PagingData<Media>> {
        return dataProviderPaging.getTvLatestEpisodes(bu, pageSize = PAGE_SIZE)
    }

    /**
     * Get radio latest media
     *
     * @param bu
     * @param channelId
     */
    fun getRadioLatestMedia(bu: Bu, channelId: String): Flow<PagingData<Media>> {
        return dataProviderPaging.getLatestMediaByChannelId(
            bu = bu,
            channelId = channelId,
            type = IlMediaType(MediaType.AUDIO),
            pageSize = PAGE_SIZE
        )
    }

    /**
     * Get TV shows
     *
     * @param bu
     */
    fun getTVShows(bu: Bu): Flow<PagingData<Show>> {
        return dataProviderPaging.getTvAlphabeticalShows(bu = bu, pageSize = PAGE_SIZE)
    }

    /**
     * Get radio shows
     *
     * @param bu
     * @param radioChannelId
     */
    fun getRadioShows(bu: Bu, radioChannelId: String): Flow<PagingData<Show>> {
        return dataProviderPaging.getRadioAlphabeticalShowsByChannelId(
            bu = bu,
            radioChannelId = radioChannelId,
            pageSize = PAGE_SIZE
        )
    }

    /**
     * Get TV topics
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
     * Get the latest media by show urn
     *
     * @param urn
     */
    fun getLatestMediaByShowUrn(urn: String): Flow<PagingData<Media>> {
        return dataProviderPaging.getLatestMediaByShowUrn(showUrn = urn, pageSize = PAGE_SIZE)
    }

    /**
     * Get the latest media by show urn
     *
     * @param urn
     * @param pageSize
     * @return
     */
    suspend fun getLatestMediaByShowUrn(urn: String, pageSize: Int): Result<List<Media>> {
        return runCatching { ilService.getLatestMediaByShowUrn(urn, pageSize.toIlPaging()).list }
    }

    /**
     * Get the latest media by topic urn
     *
     * @param urn
     */
    fun getLatestMediaByTopicUrn(urn: String): Flow<PagingData<Media>> {
        return dataProviderPaging.getLatestMediaByTopicUrn(topicUrn = urn, pageSize = PAGE_SIZE)
    }

    /**
     * Get TV live stream
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
     * Get TV live web
     *
     * @param bu
     */
    fun getTvLiveWeb(bu: Bu): Flow<PagingData<Media>> {
        return dataProviderPaging.getScheduledLiveStreamVideos(bu = bu, pageSize = PAGE_SIZE)
    }

    /**
     * Get TV live center
     *
     * @param bu
     */
    fun getTvLiveCenter(bu: Bu): Flow<PagingData<Media>> {
        return dataProviderPaging.getLiveCenterVideos(bu = bu, pageSize = PAGE_SIZE, type = LiveCenterType.SCHEDULED_LIVESTREAM)
    }

    /**
     * Get TV live center
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
            bu = bu,
            searchTerm = query,
            queryParameters = SearchParams.MediaParams(includeAggregations = false),
            pageSize = PAGE_SIZE
        )
    }

    /**
     * Get the list of radio channels for the provided [bu].
     *
     * @param bu The BU for which we want to get the radio channels.
     */
    fun getRadioChannels(bu: Bu): Flow<PagingData<Channel>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                NextUrlPagingSource(
                    initialCall = {
                        ilService.getChannelList(
                            bu = bu,
                            transmissionType = IlTransmission(Transmission.RADIO)
                        )
                    },
                    nextCall = { null }
                )
            }
        ).flow
    }

    private companion object {
        private const val PAGE_SIZE = 20
    }
}
