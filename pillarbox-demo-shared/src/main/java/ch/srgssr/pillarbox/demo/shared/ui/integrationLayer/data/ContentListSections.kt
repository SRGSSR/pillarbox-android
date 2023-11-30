/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data

import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.ContentList

/**
 * All the supported BUs.
 */
val bus = listOf(Bu.RSI, Bu.RTR, Bu.RTS, Bu.SRF, Bu.SWI)

/**
 * All the sections available in the "Lists" tab.
 */
val contentListSections = listOf(
    ContentListSection("TV Topics", bus.map { ContentList.TVTopics(it) }),
    ContentListSection("TV Shows", bus.map { ContentList.TVShows(it) }),
    ContentListSection("TV Latest Videos", bus.map { ContentList.TVLatestMedias(it) }),
    ContentListSection("TV Livestreams", (bus - Bu.SWI).map { ContentList.TVLivestreams(it) }),
    ContentListSection("TV Live Center", (bus - Bu.SWI).map { ContentList.TVLiveCenter(it) }),
    ContentListSection("TV Live Web", (bus - Bu.SWI).map { ContentList.TVLiveWeb(it) }),
    ContentListSection("Radio Livestreams", (bus - Bu.SWI).map { ContentList.RadioLiveStreams(it) }),
    ContentListSection("Radio Latest Audios", (bus - Bu.SWI).map { ContentList.RadioLatestMedias(it) }),
    ContentListSection("Radio Shows", (bus - Bu.SWI).map { ContentList.RadioShows(it) }),
)

/**
 * All the types of content list in the "Lists" tab.
 */
val contentListFactories = listOf(
    ContentList.TVTopics,
    ContentList.TVShows,
    ContentList.TVLatestMedias,
    ContentList.TVLivestreams,
    ContentList.TVLiveCenter,
    ContentList.TVLiveWeb,
    ContentList.RadioLiveStreams,
    ContentList.RadioLatestMedias,
    ContentList.RadioShows,
    ContentList.LatestMediaForShow,
    ContentList.LatestMediaForTopic,
    ContentList.RadioShowsForChannel,
    ContentList.RadioLatestMediasForChannel,
)
