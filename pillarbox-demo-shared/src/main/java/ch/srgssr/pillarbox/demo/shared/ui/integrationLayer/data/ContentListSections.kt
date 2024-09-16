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
private val busWithoutSWI = listOf(Bu.RSI, Bu.RTR, Bu.RTS, Bu.SRF)

/**
 * All the sections available in the "Lists" tab.
 */
val contentListSections = listOf(
    ContentListSection("TV Topics", bus.map { ContentList.TVTopics(it) }),
    ContentListSection("TV Shows", bus.map { ContentList.TVShows(it) }),
    ContentListSection("TV Latest Videos", bus.map { ContentList.TVLatestMedias(it) }),
    ContentListSection("TV Livestreams", busWithoutSWI.map { ContentList.TVLivestreams(it) }),
    ContentListSection("TV Live Center", busWithoutSWI.map { ContentList.TVLiveCenter(it) }),
    ContentListSection("TV Live Web", busWithoutSWI.map { ContentList.TVLiveWeb(it) }),
    ContentListSection("Radio Livestreams", busWithoutSWI.map { ContentList.RadioLiveStreams(it) }),
    ContentListSection("Radio Latest Audios", busWithoutSWI.map { ContentList.RadioLatestMedias(it) }),
    ContentListSection("Radio Shows", busWithoutSWI.map { ContentList.RadioShows(it) }),
)
