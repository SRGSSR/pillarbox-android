/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist

/**
 * Samples from Bitmovin
 */
@Suppress("StringLiteralDuplication", "MaximumLineLength", "MaxLineLength", "UndocumentedPublicProperty")
object SamplesBitmovin {

    val MultipleSubtitlesAndAudioTracks = DemoItem.URL(
        title = "Multiple subtitles and audio tracks",
        uri = "https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
        imageUri = "https://durian.blender.org/wp-content/uploads/2010/06/05.8b_comp_000272.jpg",
        languageTag = "en-CH",
    )

    val HEVC_4K = DemoItem.URL(
        title = "4K, HEVC",
        uri = "https://cdn.bitmovin.com/content/encoding_test_dash_hls/4k/hls/4k_profile/master.m3u8",
        imageUri = "https://peach.blender.org/wp-content/uploads/bbb-splash.png",
        languageTag = "en-CH",
    )
    val SingleAudioTrack = DemoItem.URL(
        title = "VoD, single audio track",
        uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8",
        imageUri = "https://img.redbull.com/images/c_crop,w_3840,h_1920,x_0,y_0,f_auto,q_auto/c_scale,w_1200/redbullcom/tv/FO-1MR39KNMH2111/fo-1mr39knmh2111-featuremedia",
        languageTag = "en-CH",
    )
    val AES128 = DemoItem.URL(
        title = "AES-128",
        uri = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/m3u8s/11331.m3u8",
        imageUri = "https://img.redbull.com/images/c_crop,w_3840,h_1920,x_0,y_0,f_auto,q_auto/c_scale,w_1200/redbullcom/tv/FO-1MR39KNMH2111/fo-1mr39knmh2111-featuremedia",
        languageTag = "en-CH",
    )
    val AVCProgressive = DemoItem.URL(
        title = "AVC Progressive",
        uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/MI201109210084_mpeg-4_hd_high_1080p25_10mbits.mp4",
        imageUri = "https://img.redbull.com/images/c_crop,w_3840,h_1920,x_0,y_0,f_auto,q_auto/c_scale,w_1200/redbullcom/tv/FO-1MR39KNMH2111/fo-1mr39knmh2111-featuremedia",
        languageTag = "en-CH",
    )

    val All = Playlist(
        title = "Bitmovin streams streams",
        languageTag = "en-CH",
        items = listOf(
            MultipleSubtitlesAndAudioTracks,
            HEVC_4K,
            SingleAudioTrack,
            AES128,
            AVCProgressive,
        )
    )
}
