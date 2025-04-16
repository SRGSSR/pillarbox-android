/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist

/**
 * Samples from Google / Media3.
 */
@Suppress("StringLiteralDuplication", "MaximumLineLength", "MaxLineLength", "UndocumentedPublicProperty")
object SamplesGoogle {

    val DashH264 = DemoItem.URL(
        title = "VoD - Dash (H264)",
        uri = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )
    val DashH264Widevine = DemoItem.URL(
        title = "VoD - Dash Widevine cenc (H264)",
        uri = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        licenseUri = "https://proxy.uat.widevine.com/proxy?video_id=2015_tears&provider=widevine_test",
        languageTag = "en-CH",
    )
    val DashH265 = DemoItem.URL(
        title = "VoD - Dash (H265)",
        uri = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DashH265Widevine = DemoItem.URL(
        title = "VoD - Dash widevine cenc (H265)",
        uri = "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        licenseUri = "https://proxy.uat.widevine.com/proxy?video_id=2015_tears&provider=widevine_test",
        languageTag = "en-CH",
    )

    val All = Playlist(
        title = "Google streams",
        languageTag = "en-CH",
        items = listOf(
            DashH264,
            DashH264Widevine,
            DashH265,
            DashH265Widevine,
        )
    )
}
