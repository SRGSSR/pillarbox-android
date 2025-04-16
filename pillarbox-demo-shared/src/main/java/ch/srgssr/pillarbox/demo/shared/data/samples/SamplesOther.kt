/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist

/**
 * Samples from an unknown source.
 */
@Suppress("StringLiteralDuplication", "MaximumLineLength", "MaxLineLength", "UndocumentedPublicProperty")
object SamplesOther {

    val OnDemandVideoUHD = DemoItem.URL(
        title = "Brain Farm Skate Phantom Flex",
        uri = "https://sample.vodobox.net/skate_phantom_flex_4k/skate_phantom_flex_4k.m3u8",
        description = "4K video",
        imageUri = "https://i.ytimg.com/vi/d4_96ZWu3Vk/maxresdefault.jpg",
        languageTag = "en-CH",
    )

    val LiveTimestampVideoHLS = DemoItem.URL(
        title = "Tagesschau",
        uri = "https://tagesschau.akamaized.net/hls/live/2020115/tagesschau/tagesschau_1/master.m3u8",
        description = "Video livestream with DVR and timestamps - HLS",
        imageUri = "https://images.tagesschau.de/image/89045d82-5cd5-46ad-8f91-73911add30ee/AAABh3YLLz0/AAABibBx2rU/20x9-1280/tagesschau-logo-100.jpg",
        languageTag = "de-CH",
    )

    val All = Playlist(title = "Other samples", languageTag = "en-CH", items = listOf(OnDemandVideoUHD, LiveTimestampVideoHLS))
}
