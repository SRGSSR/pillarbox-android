/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer.data

import androidx.paging.PagingData
import ch.srg.dataProvider.integrationlayer.data.remote.LiveCenterType
import ch.srg.dataProvider.integrationlayer.data.remote.Media
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.Show
import ch.srg.dataProvider.integrationlayer.data.remote.Topic
import ch.srg.dataProvider.integrationlayer.data.remote.Transmission
import ch.srg.dataProvider.integrationlayer.request.IlService
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srg.dataProvider.integrationlayer.request.parameters.IlMediaType
import ch.srg.dataProvider.integrationlayer.request.parameters.IlPaging
import ch.srg.dataProvider.integrationlayer.request.parameters.IlTransmission
import ch.srgssr.dataprovider.paging.DataProviderPaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
        return dataProviderPaging.getTrendingMedias(bu, IlMediaType(MediaType.VIDEO))
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
            pageSize = PageSize
        )
    }

    /**
     * Get t v shows
     *
     * @param bu
     */
    fun getTVShows(bu: Bu): Flow<PagingData<Show>> {
        return flow {
            val result = ilService.getTvAlphabeticalShows(bu = bu, pageSize = IlPaging.Size(PageSize))
            emit(PagingData.from(data = result.list))
        }
    }

    /**
     * Get radio shows
     *
     * @param radioChannel
     */
    fun getRadioShows(radioChannel: RadioChannel): Flow<PagingData<Show>> {
        return flow {
            val result = ilService.getRadioAlphabeticalRadioShowsByChannelId(
                bu = radioChannel.bu, channelId = radioChannel.channelId,
                pageSize =
                IlPaging
                    .Size(PageSize)
            )
            emit(PagingData.from(data = result.list))
        }
    }

    /**
     * Get tv topics
     *
     * @param bu
     */
    fun getTvTopics(bu: Bu): Flow<PagingData<Topic>> {
        return flow {
            val result = ilService.getTopics(bu = bu, transmissionType = IlTransmission(Transmission.TV))
            emit(PagingData.from(result.list))
        }
    }

    /**
     * Get latest media by show urn
     *
     * @param urn
     */
    fun getLatestMediaByShowUrn(urn: String): Flow<PagingData<Media>> {
        return dataProviderPaging.getLatestMediaByShowUrn(showUrn = urn, pageSize = PageSize)
    }

    /**
     * Get latest media by topic urn
     *
     * @param urn
     */
    fun getLatestMediaByTopicUrn(urn: String): Flow<PagingData<Media>> {
        return dataProviderPaging.getLatestMediaByTopicUrn(topicUrn = urn, pageSize = PageSize)
    }

    /**
     * Get tv live stream
     *
     * @param bu
     */
    fun getTvLiveStream(bu: Bu): Flow<PagingData<Media>> {
        return flow {
            val result = ilService.getTvLiveStreams(bu)
            emit(PagingData.from(result.list))
        }
    }

    /**
     * Get radio live stream
     *
     * @param bu
     */
    fun getRadioLiveStream(bu: Bu): Flow<PagingData<Media>> {
        return flow {
            val result = ilService.getRadioLiveStreams(bu)
            emit(PagingData.from(result.list))
        }
    }

    /**
     * Get tv live web
     *
     * @param bu
     */
    fun getTvLiveWeb(bu: Bu): Flow<PagingData<Media>> {
        return dataProviderPaging.getScheduledLiveStreamVideos(bu = bu, pageSize = PageSize)
    }

    /**
     * Get tv live center
     *
     * @param bu
     */
    fun getTvLiveCenter(bu: Bu): Flow<PagingData<Media>> {
        return dataProviderPaging.getLiveCenterVideos(bu = bu, pageSize = PageSize, type = LiveCenterType.SCHEDULED_LIVESTREAM)
    }

    companion object {
        private const val PageSize = 20
    }
}
