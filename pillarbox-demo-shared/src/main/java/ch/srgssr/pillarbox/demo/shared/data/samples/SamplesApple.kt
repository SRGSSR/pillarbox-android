/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist

/**
 * Samples from Apple.
 */
@Suppress("StringLiteralDuplication", "MaximumLineLength", "MaxLineLength", "UndocumentedPublicProperty")
object SamplesApple {

    val Basic_4_3 = DemoItem.URL(
        title = "Apple Basic 4:3",
        uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8",
        description = "4x3 aspect ratio, H.264 @ 30Hz",
        imageUri = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200",
        languageTag = "en-CH",
    )

    val Basic_16_9 = DemoItem.URL(
        title = "Apple Basic 16:9",
        uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8",
        description = "16x9 aspect ratio, H.264 @ 30Hz",
        imageUri = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200",
        languageTag = "en-CH",
    )

    val Advanced_16_9_TS = DemoItem.URL(
        title = "Apple Advanced 16:9 (TS)",
        uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8",
        description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Transport stream",
        imageUri = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200",
        languageTag = "en-CH",
    )

    val Advanced_16_9_fMP4 = DemoItem.URL(
        title = "Apple Advanced 16:9 (fMP4)",
        uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
        description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Fragmented MP4",
        imageUri = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200",
        languageTag = "en-CH",
    )

    val Advanced_16_9_HEVC_h264 = DemoItem.URL(
        title = "Apple Advanced 16:9 (HEVC/H.264)",
        uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_adv_example_hevc/master.m3u8",
        description = "16x9 aspect ratio, H.264 and HEVC @ 30Hz and 60Hz",
        imageUri = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200",
        languageTag = "en-CH",
    )

    val Atmos = DemoItem.URL(
        title = "Apple Dolby Atmos",
        uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/adv_dv_atmos/main.m3u8",
        imageUri = "https://is1-ssl.mzstatic.com/image/thumb/-6farfCY0YClFd7-z_qZbA/1000x563.webp",
        languageTag = "en-CH",
    )

    val WWDC_2023 = DemoItem.URL(
        title = "Apple WWDC Keynote 2023",
        uri = "https://events-delivery.apple.com/0105cftwpxxsfrpdwklppzjhjocakrsk/m3u8/vod_index-PQsoJoECcKHTYzphNkXohHsQWACugmET.m3u8",
        imageUri = "https://www.apple.com/v/apple-events/home/ac/images/overview/recent-events/gallery/jun-2023__cjqmmqlyd21y_large_2x.jpg",
        languageTag = "en-CH",
    )

    val MorningShowSeason1 = DemoItem.URL(
        title = "The Morning Show - My Way: Season 1",
        uri = "https://play-edge.itunes.apple.com/WebObjects/MZPlayLocal.woa/hls/subscription/playlist.m3u8?cc=CH&svcId=tvs.vds.4021&a=1522121579&isExternal=true&brandId=tvs.sbd.4000&id=518077009&l=en-GB&aec=UHD",
        imageUri = "https://is1-ssl.mzstatic.com/image/thumb/cZUkXfqYmSy57DBI5TiTMg/1000x563.webp",
        languageTag = "en-CH",
    )

    val MorningShowSeason2 = DemoItem.URL(
        title = "The Morning Show - Change: Season 2",
        uri = "https://play-edge.itunes.apple.com/WebObjects/MZPlayLocal.woa/hls/subscription/playlist.m3u8?cc=CH&svcId=tvs.vds.4021&a=1568297173&isExternal=true&brandId=tvs.sbd.4000&id=518034010&l=en-GB&aec=UHD",
        imageUri = "https://is1-ssl.mzstatic.com/image/thumb/IxmmS1rQ7ouO-pKoJsVpGw/1000x563.webp",
        languageTag = "en-CH",
    )

    val All = Playlist(
        title = "Apple streams",
        languageTag = "en-CH",
        items = listOf(
            Basic_4_3,
            Basic_16_9,
            Advanced_16_9_TS,
            Advanced_16_9_fMP4,
            Advanced_16_9_HEVC_h264,
            Atmos,
            WWDC_2023,
            MorningShowSeason1,
            MorningShowSeason2
        )
    )
}
