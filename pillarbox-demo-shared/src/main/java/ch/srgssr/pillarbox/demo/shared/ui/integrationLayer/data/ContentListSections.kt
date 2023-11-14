/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data

import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentList

private val bus = listOf(Bu.RTS, Bu.SRF, Bu.RSI, Bu.RTR, Bu.SWI)

/**
 * All the sections available in the "Lists" tab.
 */
val contentListSections = listOf(
    ContentListSection("TV Topics", bus.map { ContentList.TvTopics(it) }),
    ContentListSection("TV Shows", bus.map { ContentList.TvShows(it) }),
    ContentListSection("TV Latest medias", bus.map { ContentList.TVLatestMedias(it) }),
    ContentListSection("TV Livestreams", bus.map { ContentList.TVLivestreams(it) }),
    ContentListSection("TV Live center", bus.map { ContentList.TVLiveCenter(it) }),
    ContentListSection("TV Live web", bus.map { ContentList.TVLiveWeb(it) }),
    ContentListSection("Radio livestream", bus.map { ContentList.RadioLiveStreams(it) }),
    ContentListSection("Radio Latest medias", RadioChannel.entries.map { ContentList.RadioLatestMedias(it) }),
    ContentListSection("Radio Shows", RadioChannel.entries.map { ContentList.RadioShows(it) }),
)

/**
 * All the types of content list in the "Lists" tab.
 */
val contentListFactories = listOf(
    ContentList.TvTopics,
    ContentList.TvShows,
    ContentList.TVLatestMedias,
    ContentList.TVLivestreams,
    ContentList.TVLiveCenter,
    ContentList.TVLiveWeb,
    ContentList.RadioLiveStreams,
    ContentList.RadioLatestMedias,
    ContentList.RadioShows,
    ContentList.LatestMediaForShow,
    ContentList.LatestMediaForTopic
)
