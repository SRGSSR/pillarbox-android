/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MaximumLineLength", "MaxLineLength")

package ch.srgssr.pillarbox.demo.shared.data

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.MediaMetadata
import java.io.Serializable

/**
 * Demo item
 *
 * @property title
 * @property uri
 * @property description
 * @property imageUrl
 * @property licenseUrl
 */
@Suppress("UndocumentedPublicProperty")
data class DemoItem(
    val title: String,
    val uri: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val licenseUrl: String? = null,
) : Serializable {
    /**
     * Convert to a [MediaItem]
     * When [uri] is a Urn, set [MediaItem.Builder.setUri] to null,
     * Urn ItemSource need to have a urn defined in [MediaItem.mediaId] not its uri.
     */
    fun toMediaItem(): MediaItem {
        val uri: String? = if (this.uri.startsWith("urn:")) null else this.uri
        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(this.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setDescription(description)
                    .setArtworkUri(imageUrl?.let { Uri.parse(it) })
                    .build()
            )
            .setDrmConfiguration(
                licenseUrl?.let {
                    DrmConfiguration.Builder(C.WIDEVINE_UUID)
                        .setLicenseUri(licenseUrl)
                        .build()
                }
            )
            .build()
    }

    companion object {
        private const val serialVersionUID: Long = 1

        val OnDemandHLS = DemoItem(
            title = "VOD - HLS",
            description = "Switzerland says sorry! The fondue invasion",
            uri = "https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8"
        )

        val ShortOnDemandVideoHLS = DemoItem(
            title = "VOD - HLS (short)",
            description = "Des violents orages ont touché Ajaccio, chef-lieu de la Corse, jeudi",
            uri = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8"
        )

        val OnDemandVideoMP4 = DemoItem(
            title = "VOD - MP4",
            description = "The dig",
            uri = "https://media.swissinfo.ch/media/video/dddaff93-c2cd-4b6e-bdad-55f75a519480/rendition/154a844b-de1d-4854-93c1-5c61cd07e98c.mp4"
        )

        val OnDemandVideoUHD = DemoItem(
            title = "Brain Farm Skate Phantom Flex",
            description = "4K video",
            uri = "https://sample.vodobox.net/skate_phantom_flex_4k/skate_phantom_flex_4k.m3u8"
        )

        val LiveVideoHLS = DemoItem(
            title = "Video livestream - HLS",
            description = "Couleur 3 en vidéo (live)",
            uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0"
        )

        val DvrVideoHLS = DemoItem(
            title = "Video livestream with DVR - HLS",
            description = "Couleur 3 en vidéo (DVR)",
            uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8"
        )

        val LiveTimestampVideoHLS = DemoItem(
            title = "Video livestream with DVR and timestamps - HLS",
            description = "Tageschau",
            uri = "https://tagesschau.akamaized.net/hls/live/2020115/tagesschau/tagesschau_1/master.m3u8"
        )

        val OnDemandAudioMP3 = DemoItem(
            title = "AOD - MP3",
            description = "On en parle",
            uri = "https://rts-aod-dd.akamaized.net/ww/13306839/63cc2653-8305-3894-a448-108810b553ef.mp3"
        )

        val LiveAudioMP3 = DemoItem(
            title = "Audio livestream - MP3",
            description = "Couleur 3 (live)",
            uri = "https://stream.srg-ssr.ch/m/couleur3/mp3_128"
        )

        val DvrAudioHLS = DemoItem(
            title = "Audio livestream - HLS",
            description = "Couleur 3 (DVR)",
            uri = "https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8"
        )

        val AppleBasic_4_3_HLS = DemoItem(
            title = "Apple Basic 4:3",
            description = "4x3 aspect ratio, H.264 @ 30Hz",

            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8"
        )

        val AppleBasic_16_9_TS_HLS = DemoItem(
            title = "Apple Basic 16:9",
            description = "16x9 aspect ratio, H.264 @ 30Hz",

            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"
        )

        val AppleAdvanced_16_9_TS_HLS = DemoItem(
            title = "Apple Advanced 16:9 (TS)",
            description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Transport stream",

            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8"
        )

        val AppleAdvanced_16_9_fMP4_HLS = DemoItem(
            title = "Apple Advanced 16:9 (fMP4)",
            description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Fragmented MP4",

            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8"
        )

        val AppleAdvanced_16_9_HEVC_h264_HLS = DemoItem(
            title = "Apple Advanced 16:9 (HEVC/H.264)",
            description = "16x9 aspect ratio, H.264 and HEVC @ 30Hz and 60Hz",
            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_adv_example_hevc/master.m3u8"
        )

        val AppleAtmos = DemoItem(
            title = "Apple Atmos",
            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/adv_dv_atmos/main.m3u8"
        )

        val AppleWWDC_2023 = DemoItem(
            title = "Apple WWDC Keynote 2023",
            uri = "https://events-delivery.apple.com/0105cftwpxxsfrpdwklppzjhjocakrsk/m3u8/vod_index-PQsoJoECcKHTYzphNkXohHsQWACugmET.m3u8"
        )

        val AppleTvSample = DemoItem(
            title = "Apple tv trailer",
            description = "Lot of audios and subtitles choices",
            uri = "https://play-edge.itunes.apple.com/WebObjects/MZPlayLocal.woa/hls/subscription/playlist.m3u8?cc=CH&svcId=tvs.vds.4021&a=1522121579&isExternal=true&brandId=tvs.sbd.4000&id=518077009&l=en-GB&aec=UHD\n"
        )

        val GoogleDashH264 = DemoItem(
            title = "VoD - Dash (H264)",
            uri = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"
        )

        val GoogleDashH264_CENC_Widewine = DemoItem(
            title = "VoD - Dash Widewine cenc (H264)",
            uri = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd",
            licenseUrl = "https://proxy.uat.widevine.com/proxy?video_id=2015_tears&provider=widevine_test"
        )

        val GoogleDashH265 = DemoItem(
            title = "VoD - Dash (H265)",
            uri = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd"
        )

        val GoogleDashH265_CENC_Widewine = DemoItem(
            title = "VoD - Dash widewine cenc (H265)",
            uri = "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears.mpd",
            licenseUrl = "https://proxy.uat.widevine.com/proxy?video_id=2015_tears&provider=widevine_test"
        )

        val OnDemandHorizontalVideo = DemoItem(
            title = "Horizontal video",
            uri = "urn:rts:video:6820736"
        )

        val OnDemandSquareVideo = DemoItem(
            title = "Square video",
            uri = "urn:rts:video:8393241"
        )

        val OnDemandVerticalVideo = DemoItem(
            title = "Vertical video",
            uri = "urn:rts:video:13444390"
        )

        val TokenProtectedVideo = DemoItem(
            title = "Token-protected video",
            description = "Ski alpin, Slalom Messieurs",
            uri = "urn:swisstxt:video:rts:c56ea781-99ad-40c3-8d9b-444cc5ac3aea"
        )

        val SuperfluouslyTokenProtectedVideo = DemoItem(
            title = "Superfluously token-protected video",
            description = "Telegiornale flash",
            uri = "urn:rsi:video:15916771"
        )

        val DrmProtectedVideo = DemoItem(
            title = "DRM-protected video",
            description = "Top Models 8870",
            uri = "urn:rts:video:13639837"
        )

        val LiveVideo = DemoItem(
            title = "Live video",
            description = "SRF 1",
            uri = "urn:srf:video:c4927fcf-e1a0-0001-7edd-1ef01d441651"
        )

        val DvrVideo = DemoItem(
            title = "DVR video livestream",
            description = "RTS 1",
            uri = "urn:rts:video:3608506"
        )

        val DvrAudio = DemoItem(
            title = "DVR audio livestream",
            description = "Couleur 3 (DVR)",
            uri = "urn:rts:audio:3262363"
        )

        val OnDemandAudio = DemoItem(
            title = "On-demand audio stream",
            description = "Il lavoro di TerraProject per una fotografia documentaria",
            uri = "urn:rsi:audio:8833144"
        )

        val Expired = DemoItem(
            title = "Expired URN",
            description = "Content that is not available anymore",
            uri = "urn:rts:video:13382911"
        )

        val Unknown = DemoItem(
            title = "Unknown URN",
            description = "Content that does not exist",
            uri = "urn:srf:video:unknown"
        )

        val BitmovinOnDemandMultipleTracks = DemoItem(
            title = "Multiple subtitles and audio tracks",
            description = "On some devices codec may crash",
            uri = "https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
        )

        val BitmovinOnDemand_4K_HEVC = DemoItem(
            title = "4K, HEVC",
            uri = "https://cdn.bitmovin.com/content/encoding_test_dash_hls/4k/hls/4k_profile/master.m3u8"
        )

        val BitmovinOnDemandSingleAudio = DemoItem(
            title = "VoD, single audio track",
            uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"
        )

        val BitmovinOnDemandAES128 = DemoItem(
            title = "AES-128",
            uri = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/m3u8s/11331.m3u8"
        )

        val BitmovinOnDemandProgressive = DemoItem(
            title = "AVC Progressive",
            uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/MI201109210084_mpeg-4_hd_high_1080p25_10mbits.mp4"
        )

        val UnifiedStreamingOnDemand_fMP4 = DemoItem(
            title = "HLS - Fragmented MP4",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
        )

        val UnifiedStreamingOnDemandAlternateAudio = DemoItem(
            title = "HLS - Alternate audio language",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8"
        )

        val UnifiedStreamingOnDemandAudioOnly = DemoItem(
            title = "HLS - Audio only",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8?filter=(type!=%22video%22)"
        )

        val UnifiedStreamingOnDemandTrickplay = DemoItem(
            title = "HLS - Trickplay",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.m3u8"
        )

        val UnifiedStreamingOnDemandLimitedBandwidth = DemoItem(
            title = "Limiting bandwidth use",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?max_bitrate=800000"
        )

        val UnifiedStreamingOnDemandDynamicTrackSelection = DemoItem(
            title = "Dynamic Track Selection",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?filter=%28type%3D%3D%22audio%22%26%26systemBitrate%3C100000%29%7C%7C%28type%3D%3D%22video%22%26%26systemBitrate%3C1024000%29"
        )

        val UnifiedStreamingPureLive = DemoItem(
            title = "Pure live",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8"
        )

        val UnifiedStreamingTimeshift = DemoItem(
            title = "Timeshift (5 minutes)",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?time_shift=300"
        )

        val UnifiedStreamingLiveAudio = DemoItem(
            title = "Live audio",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?filter=(type!=%22video%22)"
        )

        val UnifiedStreamingPureLiveScte35 = DemoItem(
            title = "Pure live (scte35)",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/scte35.isml/.m3u8"
        )

        val UnifiedStreamingOnDemand_fMP4_Clear = DemoItem(
            title = "fMP4, clear",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-fmp4.ism/.m3u8"
        )

        val UnifiedStreamingOnDemand_fMP4_HEVC_4K = DemoItem(
            title = "fMP4, HEVC 4K",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hevc.ism/.m3u8"
        )

        val UnifiedStreamingOnDemand_Dash_MP4 = DemoItem(
            title = "Dash - MP4",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4/.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_FragmentedMP4 = DemoItem(
            title = "Dash - Fragmented MP4",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_TrickPlay = DemoItem(
            title = "Dash - TrickPlay",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_TiledThumbnails = DemoItem(
            title = "Dash - Tiled thumbnails (live/timeline)",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-tiled-thumbnails-timeline.ism/.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_Accessibility = DemoItem(
            title = "Dash - Accessibility - hard of hearing",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hoh-subs.ism/.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_Single_TTML = DemoItem(
            title = "Dash - Single - fragmented TTML",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-en.ism/.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_Multiple_RFC_tags = DemoItem(
            title = "Dash - Multiple - RFC 5646 language tags",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-rfc5646.ism/.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_Multiple_TTML = DemoItem(
            title = "Dash - Multiple - fragmented TTML",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-ttml.ism/.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_AudioOnly = DemoItem(
            title = "Dash - Audio only",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd?filter=(type!=%22video%22)"
        )

        val UnifiedStreamingOnDemand_Dash_Multiple_Audio_Codec = DemoItem(
            title = "Dash - Multiple audio codecs",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-codec.ism/.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_AlternateAudioLanguage = DemoItem(
            title = "Dash - Alternate audio language",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_AccessibilityAudio = DemoItem(
            title = "Dash - Accessibility - audio description",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-desc-aud.ism/.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_PureLive = DemoItem(
            title = "Dash - Pure live",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd"
        )

        val UnifiedStreamingOnDemand_Dash_Timeshift = DemoItem(
            title = "Dash - Timeshift (5 minutes)",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd?time_shift=300"
        )

        val UnifiedStreamingOnDemand_Dash_DVB_LowLatency = DemoItem(
            title = "Dash - DVB DASH low latency",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live-low-latency.isml/.mpd"
        )

        val PlaySuisseTest1 = DemoItem(
            title = "Test1",
            description = "Forced subtitles",
            uri = "https://prd.vod-srgssr.ch/origin/1053457/fr/master.m3u8?complexSubs=true"
        )
    }
}
