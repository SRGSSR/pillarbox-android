/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.DemoItem.URL
import ch.srgssr.pillarbox.demo.shared.data.Playlist

/**
 * Samples from BBC
 */
@Suppress("StringLiteralDuplication", "MaximumLineLength", "MaxLineLength", "UndocumentedPublicProperty")
object SamplesBBC {

    val HlsTestCardAVC = URL(
        title = "[HLS] Live HLS Audio and AVC video",
        description = "All representations in all languages",
        uri = "https://rdmedia.bbc.co.uk/testcard/simulcast/manifests/avc-full.m3u8",
    )

    val HlsVoD = URL(
        title = "[HLS] On-demand Testcard AVC Video",
        description = "Multiple Languages, AAC Stereo and Surround, Audio Description, AVC Video",
        uri = "https://rdmedia.bbc.co.uk/testcard/vod/manifests/avc-full.m3u8",
    )

    val DashLiveAVC = URL(
        title = "[DASH] Live Testcard AVC Video",
        uri = "https://rdmedia.bbc.co.uk/testcard/simulcast/manifests/avc-full.mpd"
    )

    val DashLiveHEVC = URL(
        title = "[DASH] Live Testcard HEVC Video",
        uri = "https://rdmedia.bbc.co.uk/testcard/simulcast/manifests/hevc-ctv.mpd"
    )

    val DashVoD = URL(
        title = "[DASH]  On-demand Testcard AVC Video",
        description = "Multiple Languages, AAC Stereo and Surround, Audio Description, AVC Video",
        uri = "https://rdmedia.bbc.co.uk/testcard/vod/manifests/avc-full.mpd",
    )

    val DashLiveLowLatencyAVC = URL(
        title = "[DASH] Low-Latency AVC Video",
        description = "4 Chunks per Segment, Multiple Languages",
        uri = "https://rdmedia.bbc.co.uk/testcard/lowlatency/manifests/ll-avc-full.mpd"
    )

    val DashLiveLowLatencyHEVC = URL(
        title = "[DASH] Low-Latency HEVC Video",
        description = "4 Chunks per Segment, Multiple Languages",
        uri = "https://rdmedia.bbc.co.uk/testcard/lowlatency/manifests/ll-hevc-ctv.mpd"
    )

    val DashAudioOnDemand = URL(
        title = "[DASH] Audio on-demand",
        description = "AAC-LC Surround (4 active channels) in English",
        uri = "https://rdmedia.bbc.co.uk/testcard/vod/manifests/radio-surround-en.mpd"
    )

    val DashAudioLiveAAC = URL(
        title = "[DASH] Live audio AAC-LC",
        description = "AAC-LC Stereo in English",
        uri = "https://rdmedia.bbc.co.uk/testcard/simulcast/manifests/radio-lc-en.mpd"
    )

    val DashAudioLiveHE_AAC = URL(
        title = "[DASH] Live audio HE-AAC",
        description = "HE-AAC Stereo in English",
        uri = "https://rdmedia.bbc.co.uk/testcard/simulcast/manifests/radio-he-en.mpd"
    )

    val All = Playlist(
        title = "BBC streams",
        items = listOf(
            HlsVoD,
            DashVoD,
            HlsTestCardAVC,
            DashLiveAVC,
            DashLiveHEVC,
            DashLiveLowLatencyAVC,
            DashLiveLowLatencyHEVC,
            DashAudioOnDemand,
            DashAudioLiveAAC,
            DashAudioLiveHE_AAC,
        )
    )
}
