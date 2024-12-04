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
@Suppress("StringLiteralDuplication")
val contentListSections = listOf(
    ContentListSection("TV Topics", bus.map { ContentList.TVTopics(it) }, languageTag = "en-CH"),
    ContentListSection("TV Shows", bus.map { ContentList.TVShows(it) }, languageTag = "en-CH"),
    ContentListSection("TV Latest Videos", bus.map { ContentList.TVLatestMedias(it) }, languageTag = "en-CH"),
    ContentListSection("TV Livestreams", busWithoutSWI.map { ContentList.TVLivestreams(it) }, languageTag = "en-CH"),
    ContentListSection("TV Live Center", busWithoutSWI.map { ContentList.TVLiveCenter(it) }, languageTag = "en-CH"),
    ContentListSection("TV Live Web", busWithoutSWI.map { ContentList.TVLiveWeb(it) }, languageTag = "en-CH"),
    ContentListSection("Radio Livestreams", busWithoutSWI.map { ContentList.RadioLiveStreams(it) }, languageTag = "en-CH"),
    ContentListSection("Radio Latest Audios", busWithoutSWI.map { ContentList.RadioLatestMedias(it) }, languageTag = "en-CH"),
    ContentListSection("Radio Shows", busWithoutSWI.map { ContentList.RadioShows(it) }, languageTag = "en-CH"),
)
