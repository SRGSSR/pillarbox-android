/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.DemoItem.URL
import ch.srgssr.pillarbox.demo.shared.data.Playlist

/**
 * Samples from DASH-IF
 * https://reference.dashif.org/dash.js/nightly/samples/dash-if-reference-player/
 */
@Suppress("StringLiteralDuplication", "MaximumLineLength", "MaxLineLength", "UndocumentedPublicProperty")
object SamplesDASHIF {

    val AkamaiLiveLowLatencySingleRate = URL(
        title = "[Akamai] Akamai Low Latency Stream (Single Rate)",
        uri = "https://akamaibroadcasteruseast.akamaized.net/cmaf/live/657078/akasource/out.mpd"
    )

    val AkamaiLiveLowLatencyMultiRate = URL(
        title = "[Akamai] Akamai Low Latency Stream (Multi Rate)",
        uri = "https://cmafref.akamaized.net/cmaf/live-ull/2006350/akambr/out.mpd"
    )

    val LiveLowLatencySingleRate = URL(
        title = "[DASH-IF] Low Latency (Single-Rate) (livesim-chunked)",
        uri = "https://livesim2.dashif.org/livesim2/chunkdur_1/ato_7/testpic4_8s/Manifest300.mpd"
    )

    val LiveLowLatencyMultiRate = URL(
        title = "[DASH-IF] Low Latency (Multi-Rate) (livesim-chunked)",
        uri = "https://livesim2.dashif.org/livesim2/chunkdur_1/ato_7/testpic4_8s/Manifest.mpd"
    )

    val BroadPeakLiveLowLatencySegmentTemplate = URL(
        title = "[Broadpeak] Live low latency (SegmentTemplate, CMSD)",
        uri = "https://explo.broadpeak.tv:8343/bpk-tv/spring/lowlat/index.mpd"
    )

    val BroadPeakLiveLowLatencySegmentTimeline = URL(
        title = "[Broadpeak] Live low latency (SegmentTimeline, CMSD)",
        uri = "https://explo.broadpeak.tv:8343/bpk-tv/spring/lowlat/index_timeline.mpd"
    )

    val Live10SecTimeOffset = URL(
        title = "[DASH-IF] 10 seconds availabilityTimeOffset (livesim)",
        uri = " https://livesim2.dashif.org/livesim2/ato_10/testpic_2s/Manifest.mpd"
    )

    val MultiDrmMultiPeriod = URL(
        title = "DASH IF - Multi DRM multi period",
        uri = "https://d24rwxnt7vw9qb.cloudfront.net/out/v1/d0409ade052145c5a639d8db3c5ce4b4/index.mpd",
        licenseUri = "https://lic.staging.drmtoday.com/license-proxy-widevine/cenc/?specConform=true",
        licenseRequestHeaders = mutableMapOf(
            "x-dt-custom-data" to "ewogICAgInVzZXJJZCI6ICJhd3MtZWxlbWVudGFsOjpzcGVrZS10ZXN0aW5nIiwKICAgICJzZXNzaW9uSWQiOiAidGVzdHNlc3Npb25tdWx0aWtleSIsCiAgICAibWVyY2hhbnQiOiAiYXdzLWVsZW1lbnRhbCIKfQ"
        ),
    )

    val ClearMultiPeriodStatic = URL(
        title = "DASH IF - Mutliperiod clear",
        description = "Axinom 1080p, static mpd",
        uri = "https://media.axprod.net/TestVectors/v7-Clear/Manifest_MultiPeriod_1080p.mpd"
    )

    val ClearMultiPeriodLive = URL(
        title = "DASH IF - Mutliperiod dynamic",
        description = "Dash-If Multiperiod template every 1min",
        uri = "https://livesim2.dashif.org/livesim2/periods_60/continuous_1/testpic_2s/Manifest.mpd"
    )

    val MultiPeriodVodExample = URL(
        title = "DASH IF - Multiperiod VoD Bunny",
        description = "Dash-If - Multiperiod VoD example",
        uri = "https://dash.akamaized.net/dash264/TestCases/5a/nomor/1.mpd"
    )

    val MultiPeriodDifferentContentVodExample = URL(
        title = "DASH IF - Multiperiod Complex",
        uri = "https://media.axprod.net/TestVectors/v8-MultiContent/Encrypted/Manifest.mpd"
    )

    val All = Playlist(
        title = "DASH IF - streams",
        items = listOf(
            MultiPeriodVodExample,
            MultiPeriodDifferentContentVodExample,
            ClearMultiPeriodStatic,
            ClearMultiPeriodLive,
            MultiDrmMultiPeriod,
            Live10SecTimeOffset,
            AkamaiLiveLowLatencySingleRate,
            AkamaiLiveLowLatencyMultiRate,
            LiveLowLatencySingleRate,
            LiveLowLatencyMultiRate,
            BroadPeakLiveLowLatencySegmentTemplate,
            BroadPeakLiveLowLatencySegmentTimeline,
        )
    )
}
