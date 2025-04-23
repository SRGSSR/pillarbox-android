/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist

/**
 * Samples from UnifiedStreaming.
 */
@Suppress("StringLiteralDuplication", "MaximumLineLength", "MaxLineLength", "UndocumentedPublicProperty")
object SamplesUnifiedStreaming {

    val DASH_MP4 = DemoItem.URL(
        title = "DASH - MP4",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4/.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_Fragmented_MP4 = DemoItem.URL(
        title = "DASH - Fragmented MP4",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_TrickPlay = DemoItem.URL(
        title = "DASH - Trickplay",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_Tiled_Thumbnails = DemoItem.URL(
        title = "DASH - Tiled thumbnails (live/timeline)",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-tiled-thumbnails-timeline.ism/.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_Accessibility = DemoItem.URL(
        title = "DASH - Accessibility - hard of hearing",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hoh-subs.ism/.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_Single_TTML = DemoItem.URL(
        title = "DASH - Single - fragmented TTML",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-en.ism/.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_Multiple_RFC_tags = DemoItem.URL(
        title = "DASH - Multiple - RFC 5646 language tags",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-rfc5646.ism/.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_Multiple_TTML = DemoItem.URL(
        title = "DASH - Multiple - fragmented TTML",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-ttml.ism/.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_AudioOnly = DemoItem.URL(
        title = "DASH - Audio only",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd?filter=(type!=%22video%22)",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_MultipleAudioCodec = DemoItem.URL(
        title = "DASH - Multiple audio codecs",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-codec.ism/.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_AlternateAudioLanguage = DemoItem.URL(
        title = "DASH - Alternate audio language",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_AccessibilityAudio = DemoItem.URL(
        title = "DASH - Accessibility - audio description",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-desc-aud.ism/.mpd",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH_PureLive = DemoItem.URL(
        title = "DASH - Pure live",
        uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd",
        imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
        languageTag = "en-CH",
    )

    val DASH_Timeshift = DemoItem.URL(
        title = "DASH - Timeshift (5 minutes)",
        uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd?time_shift=300",
        imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
        languageTag = "en-CH",
    )

    val DASH_DVB_LowLatency = DemoItem.URL(
        title = "DASH - DVB DASH low latency",
        uri = "https://demo.unified-streaming.com/k8s/live/stable/live-low-latency.isml/.mpd",
        imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
        languageTag = "en-CH",
    )

    val HLS_FragmentMp4 = DemoItem.URL(
        title = "HLS - Fragmented MP4",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val HLS_KeyRotation = DemoItem.URL(
        title = "HLS - Key Rotation",
        uri = "https://demo.unified-streaming.com/k8s/keyrotation/stable/keyrotation/keyrotation.isml/.m3u8",
        imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
        languageTag = "en-CH",
    )

    val HLS_AlternateAudioLanguage = DemoItem.URL(
        title = "HLS - Alternate audio language",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val HLS_AudioOnly = DemoItem.URL(
        title = "HLS - Audio only",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8?filter=(type!=%22video%22)",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val HLS_TrickPlay = DemoItem.URL(
        title = "HLS - Trickplay",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.m3u8",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val HLS_LimitingBandwidthUse = DemoItem.URL(
        title = "HLS - Limiting bandwidth use",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?max_bitrate=800000",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val HLS_DynamicTrackSelection = DemoItem.URL(
        title = "HLS - Dynamic Track Selection",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?filter=%28type%3D%3D%22audio%22%26%26systemBitrate%3C100000%29%7C%7C%28type%3D%3D%22video%22%26%26systemBitrate%3C1024000%29",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val HLS_PureLive = DemoItem.URL(
        title = "HLS - Pure live",
        uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8",
        imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
        languageTag = "en-CH",
    )

    val HLS_Timeshift = DemoItem.URL(
        title = "HLS - Timeshift (5 minutes)",
        uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?time_shift=300",
        imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
        languageTag = "en-CH",
    )

    val HLS_LiveAudio = DemoItem.URL(
        title = "HLS - Live audio",
        uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?filter=(type!=%22video%22)",
        imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
        languageTag = "en-CH",
    )

    val HLS_PureLive_scte35 = DemoItem.URL(
        title = "HLS -Pure live (scte35)",
        uri = "https://demo.unified-streaming.com/k8s/live/stable/scte35.isml/.m3u8",
        imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
        languageTag = "en-CH",
    )

    val HLS_fMP4_clear = DemoItem.URL(
        title = "HLS - fMP4, clear",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-fmp4.ism/.m3u8",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val HLS_fMP4_4k = DemoItem.URL(
        title = "HLS - fMP4, HEVC 4K",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hevc.ism/.m3u8",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val DASH = Playlist(
        "UnifiedStreaming DASH",
        languageTag = "en-CH",
        items = listOf(
            DASH_MP4,
            DASH_Fragmented_MP4,
            DASH_TrickPlay,
            DASH_Tiled_Thumbnails,
            DASH_Accessibility,
            DASH_Single_TTML,
            DASH_Multiple_RFC_tags,
            DASH_Multiple_TTML,
            DASH_AudioOnly,
            DASH_MultipleAudioCodec,
            DASH_AlternateAudioLanguage,
            DASH_AccessibilityAudio,
            DASH_PureLive,
            DASH_Timeshift,
            DASH_DVB_LowLatency,
        ).map { it.copy(title = it.title?.removePrefix("DASH - ")) }
    )

    val HLS = Playlist(
        title = "UnifiedStreaming HLS",
        languageTag = "en-CH",
        items = listOf(
            HLS_FragmentMp4,
            HLS_KeyRotation,
            HLS_AlternateAudioLanguage,
            HLS_AudioOnly,
            HLS_TrickPlay,
            HLS_LimitingBandwidthUse,
            HLS_DynamicTrackSelection,
            HLS_PureLive,
            HLS_Timeshift,
            HLS_LiveAudio,
            HLS_PureLive_scte35,
            HLS_fMP4_clear,
            HLS_fMP4_4k,
        ).map { it.copy(title = it.title?.removePrefix("HLS - ")) }
    )

    val All = Playlist(
        title = "UnifiedStreaming streams",
        items = listOf(
            HLS_FragmentMp4,
            HLS_KeyRotation,
            HLS_AlternateAudioLanguage,
            HLS_AudioOnly,
            HLS_TrickPlay,
            HLS_LimitingBandwidthUse,
            HLS_DynamicTrackSelection,
            HLS_PureLive,
            HLS_Timeshift,
            HLS_LiveAudio,
            HLS_PureLive_scte35,
            HLS_fMP4_clear,
            HLS_fMP4_4k,
            DASH_MP4,
            DASH_Fragmented_MP4,
            DASH_TrickPlay,
            DASH_Tiled_Thumbnails,
            DASH_Accessibility,
            DASH_Single_TTML,
            DASH_Multiple_RFC_tags,
            DASH_Multiple_TTML,
            DASH_AudioOnly,
            DASH_MultipleAudioCodec,
            DASH_AlternateAudioLanguage,
            DASH_AccessibilityAudio,
            DASH_PureLive,
            DASH_Timeshift,
            DASH_DVB_LowLatency,
        ),
        languageTag = "en-CH"
    )
}
