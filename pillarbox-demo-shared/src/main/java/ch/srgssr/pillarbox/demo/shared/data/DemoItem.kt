/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data

import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlLocation
import java.io.Serializable

/**
 * Generic media item that can represent either a content playable by URL or by URN.
 *
 * @property uri The URI of the media.
 * @property title The title of the media
 * @property description The optional description of the media.
 * @property imageUri The optional image URI of the media.
 * @property languageTag The IETF BCP47 language tag of the title and description.
 */
sealed class DemoItem(
    open val uri: String,
    open val title: String?,
    open val description: String?,
    open val imageUri: String?,
    open val languageTag: String? = null,
) : Serializable {
    /**
     * Represents a media item playable by URL.
     *
     * @property uri The URI of the media.
     * @property title The title of the media
     * @property description The optional description of the media.
     * @property imageUri The optional image URI of the media.
     * @property languageTag The IETF BCP47 language tag of the title and description.
     * @property licenseUri The optional license URI of the media.
     */
    data class URL(
        override val uri: String,
        override val title: String? = null,
        override val description: String? = null,
        override val imageUri: String? = null,
        override val languageTag: String? = null,
        val licenseUri: String? = null,
    ) : DemoItem(uri, title, description, imageUri, languageTag) {
        override fun toMediaItem(): MediaItem {
            return MediaItem.Builder()
                .setUri(uri)
                .setMediaId(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setDescription(description)
                        .setArtworkUri(imageUri?.toUri())
                        .build()
                )
                .setDrmConfiguration(
                    licenseUri?.let {
                        DrmConfiguration.Builder(C.WIDEVINE_UUID)
                            .setLicenseUri(licenseUri)
                            .setMultiSession(true)
                            .build()
                    }
                )
                .build()
        }
    }

    /**
     * Represents a media item playable by URN.
     *
     * @property urn The URN of the media.
     * @property title The title of the media
     * @property description The optional description of the media.
     * @property imageUri The optional image URI of the media.
     * @property languageTag The IETF BCP47 language tag of the title and description.
     * @property host The host from which to load the media.
     * @property forceSAM Whether to use SAM instead of the IL.
     * @property ilLocation The optional location from which to load the media.
     */
    data class URN(
        val urn: String,
        override val title: String? = null,
        override val description: String? = null,
        override val imageUri: String? = null,
        override val languageTag: String? = null,
        val host: IlHost = IlHost.PROD,
        val forceSAM: Boolean = false,
        val ilLocation: IlLocation? = null,
    ) : DemoItem(urn, title, description, imageUri, languageTag) {
        override fun toMediaItem(): MediaItem {
            return SRGMediaItem(urn) {
                host(host)
                forceSAM(forceSAM)
                ilLocation(ilLocation)
                mediaMetadata {
                    setTitle(title)
                    setDescription(description)
                    setArtworkUri(imageUri?.toUri())
                }
            }
        }
    }

    /**
     * Converts this [DemoItem] into a [MediaItem].
     */
    abstract fun toMediaItem(): MediaItem

    @Suppress("StringLiteralDuplication", "MaximumLineLength", "MaxLineLength", "UndocumentedPublicProperty")
    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID: Long = 1

        val OnDemandHLS = URL(
            title = "VOD - HLS",
            uri = "https://rts-vod-amd.akamaized.net/ww/14970442/da2b38fb-ca9f-3c76-80c6-e6fa7f3c2699/master.m3u8",
            description = "Sacha part à la rencontre d'univers atypiques",
            languageTag = "fr-CH",
        )

        val ShortOnDemandVideoHLS = URL(
            title = "VOD - HLS (short)",
            uri = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8",
            description = "Des violents orages ont touché Ajaccio, chef-lieu de la Corse, jeudi",
            languageTag = "fr-CH",
        )

        val OnDemandVideoMP4 = URL(
            title = "VOD - MP4",
            uri = "https://cdn.prod.swi-services.ch/video-projects/94f5f5d1-5d53-4336-afda-9198462c45d9/localised-videos/ENG/renditions/ENG.mp4",
            description = "Swiss wheelchair athlete wins top award",
            languageTag = "en-CH",
        )

        val OnDemandVideoUHD = URL(
            title = "Brain Farm Skate Phantom Flex",
            uri = "https://sample.vodobox.net/skate_phantom_flex_4k/skate_phantom_flex_4k.m3u8",
            description = "4K video",
            languageTag = "en-CH",
        )

        val LiveVideoHLS = URL(
            title = "Video livestream - HLS",
            uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0",
            description = "Couleur 3 en vidéo (live)",
            languageTag = "fr-CH",
        )

        val DvrVideoHLS = URL(
            title = "Video livestream with DVR - HLS",
            uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8",
            description = "Couleur 3 en vidéo (DVR)",
            languageTag = "fr-CH",
        )

        val LiveTimestampVideoHLS = URL(
            title = "Video livestream with DVR and timestamps - HLS",
            uri = "https://tagesschau.akamaized.net/hls/live/2020115/tagesschau/tagesschau_1/master.m3u8",
            description = "Tageschau",
            languageTag = "de-CH",
        )

        val OnDemandAudioMP3 = URL(
            title = "AOD - MP3",
            uri = "https://rts-aod-dd.akamaized.net/ww/13306839/63cc2653-8305-3894-a448-108810b553ef.mp3",
            description = "On en parle",
            languageTag = "fr-CH",
        )

        val LiveAudioMP3 = URL(
            title = "Audio livestream - MP3",
            uri = "https://stream.srg-ssr.ch/m/couleur3/mp3_128",
            description = "Couleur 3 (live)",
            languageTag = "fr-CH",
        )

        val DvrAudioHLS = URL(
            title = "Audio livestream - HLS",
            uri = "https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8",
            description = "Couleur 3 (DVR)",
            languageTag = "fr-CH",
        )

        val AppleBasic_4_3_HLS = URL(
            title = "Apple Basic 4:3",
            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8",
            description = "4x3 aspect ratio, H.264 @ 30Hz",
            languageTag = "en-CH",
        )

        val AppleBasic_16_9_TS_HLS = URL(
            title = "Apple Basic 16:9",
            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8",
            description = "16x9 aspect ratio, H.264 @ 30Hz",
            languageTag = "en-CH",
        )

        val AppleAdvanced_16_9_TS_HLS = URL(
            title = "Apple Advanced 16:9 (TS)",
            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8",
            description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Transport stream",
            languageTag = "en-CH",
        )

        val AppleAdvanced_16_9_fMP4_HLS = URL(
            title = "Apple Advanced 16:9 (fMP4)",
            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
            description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Fragmented MP4",
            languageTag = "en-CH",
        )

        val AppleAdvanced_16_9_HEVC_h264_HLS = URL(
            title = "Apple Advanced 16:9 (HEVC/H.264)",
            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_adv_example_hevc/master.m3u8",
            description = "16x9 aspect ratio, H.264 and HEVC @ 30Hz and 60Hz",
            languageTag = "en-CH",
        )

        val AppleAtmos = URL(
            title = "Apple Atmos",
            uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/adv_dv_atmos/main.m3u8",
            languageTag = "en-CH",
        )

        val AppleWWDC_2023 = URL(
            title = "Apple WWDC Keynote 2023",
            uri = "https://events-delivery.apple.com/0105cftwpxxsfrpdwklppzjhjocakrsk/m3u8/vod_index-PQsoJoECcKHTYzphNkXohHsQWACugmET.m3u8",
            languageTag = "en-CH",
        )

        val AppleTvSample = URL(
            title = "Apple TV trailer",
            uri = "https://play-edge.itunes.apple.com/WebObjects/MZPlayLocal.woa/hls/subscription/playlist.m3u8?cc=CH&svcId=tvs.vds.4021&a=1522121579&isExternal=true&brandId=tvs.sbd.4000&id=518077009&l=en-GB&aec=UHD",
            description = "Lot of audios and subtitles choices",
            languageTag = "en-CH",
        )

        val GoogleDashH264 = URL(
            title = "VoD - Dash (H264)",
            uri = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd",
            languageTag = "en-CH",
        )

        val GoogleDashH264_CENC_Widewine = URL(
            title = "VoD - Dash Widewine cenc (H264)",
            uri = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd",
            licenseUri = "https://proxy.uat.widevine.com/proxy?video_id=2015_tears&provider=widevine_test",
            languageTag = "en-CH",
        )

        val GoogleDashH265 = URL(
            title = "VoD - Dash (H265)",
            uri = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd",
            languageTag = "en-CH",
        )

        val GoogleDashH265_CENC_Widewine = URL(
            title = "VoD - Dash widewine cenc (H265)",
            uri = "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears.mpd",
            licenseUri = "https://proxy.uat.widevine.com/proxy?video_id=2015_tears&provider=widevine_test",
            languageTag = "en-CH",
        )

        val OnDemandHorizontalVideo = URN(
            title = "Horizontal video",
            urn = "urn:rts:video:14827306",
            languageTag = "fr-CH",
        )

        val OnDemandSquareVideo = URN(
            title = "Square video",
            urn = "urn:rts:video:8393241",
            languageTag = "en-CH",
        )

        val OnDemandVerticalVideo = URN(
            title = "Vertical video",
            urn = "urn:rts:video:13444390",
            languageTag = "en-CH",
        )

        val SuperfluouslyTokenProtectedVideo = URN(
            title = "Superfluously token-protected video",
            urn = "urn:rsi:video:2660088",
            description = "Telegiornale flash",
            languageTag = "it-CH",
        )

        val LiveVideo = URN(
            title = "Live video",
            urn = "urn:srf:video:c4927fcf-e1a0-0001-7edd-1ef01d441651",
            description = "SRF 1",
            languageTag = "de-CH",
        )

        val DvrVideo = URN(
            title = "DVR video livestream",
            urn = "urn:rts:video:3608506",
            description = "RTS 1",
            languageTag = "fr-CH",
        )

        val DvrAudio = URN(
            title = "DVR audio livestream",
            urn = "urn:rts:audio:3262363",
            description = "Couleur 3 (DVR)",
            languageTag = "fr-CH",
        )

        val OnDemandAudio = URN(
            title = "On-demand audio stream",
            urn = "urn:srf:audio:b9706015-632f-4e24-9128-5de074d98eda",
            description = "Nachrichten von 08:00 Uhr - 08.03.2024",
            languageTag = "de-CH",
        )

        val Expired = URN(
            title = "Expired URN",
            urn = "urn:rts:video:13382911",
            description = "Content that is not available anymore",
            languageTag = "en-CH",
        )

        val Unknown = URN(
            title = "Unknown URN",
            urn = "urn:srf:video:unknown",
            description = "Content that does not exist",
            languageTag = "en-CH",
        )

        val BitmovinOnDemandMultipleTracks = URL(
            title = "Multiple subtitles and audio tracks",
            uri = "https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            description = "On some devices codec may crash",
            languageTag = "en-CH",
        )

        val BitmovinOnDemand_4K_HEVC = URL(
            title = "4K, HEVC",
            uri = "https://cdn.bitmovin.com/content/encoding_test_dash_hls/4k/hls/4k_profile/master.m3u8",
            languageTag = "en-CH",
        )

        val BitmovinOnDemandSingleAudio = URL(
            title = "VoD, single audio track",
            uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8",
            languageTag = "en-CH",
        )

        val BitmovinOnDemandAES128 = URL(
            title = "AES-128",
            uri = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/m3u8s/11331.m3u8",
            languageTag = "en-CH",
        )

        val BitmovinOnDemandProgressive = URL(
            title = "AVC Progressive",
            uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/MI201109210084_mpeg-4_hd_high_1080p25_10mbits.mp4",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_fMP4 = URL(
            title = "HLS - Fragmented MP4",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemandAlternateAudio = URL(
            title = "HLS - Alternate audio language",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemandAudioOnly = URL(
            title = "HLS - Audio only",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8?filter=(type!=%22video%22)",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemandTrickplay = URL(
            title = "HLS - Trickplay",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.m3u8",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemandLimitedBandwidth = URL(
            title = "Limiting bandwidth use",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?max_bitrate=800000",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemandDynamicTrackSelection = URL(
            title = "Dynamic Track Selection",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?filter=%28type%3D%3D%22audio%22%26%26systemBitrate%3C100000%29%7C%7C%28type%3D%3D%22video%22%26%26systemBitrate%3C1024000%29",
            languageTag = "en-CH",
        )

        val UnifiedStreamingPureLive = URL(
            title = "Pure live",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8",
            languageTag = "en-CH",
        )

        val UnifiedStreamingTimeshift = URL(
            title = "Timeshift (5 minutes)",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?time_shift=300",
            languageTag = "en-CH",
        )

        val UnifiedStreamingLiveAudio = URL(
            title = "Live audio",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?filter=(type!=%22video%22)",
            languageTag = "en-CH",
        )

        val UnifiedStreamingPureLiveScte35 = URL(
            title = "Pure live (scte35)",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/scte35.isml/.m3u8",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_fMP4_Clear = URL(
            title = "fMP4, clear",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-fmp4.ism/.m3u8",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_fMP4_HEVC_4K = URL(
            title = "fMP4, HEVC 4K",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hevc.ism/.m3u8",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_MP4 = URL(
            title = "Dash - MP4",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4/.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_FragmentedMP4 = URL(
            title = "Dash - Fragmented MP4",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_TrickPlay = URL(
            title = "Dash - TrickPlay",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_TiledThumbnails = URL(
            title = "Dash - Tiled thumbnails (live/timeline)",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-tiled-thumbnails-timeline.ism/.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_Accessibility = URL(
            title = "Dash - Accessibility - hard of hearing",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hoh-subs.ism/.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_Single_TTML = URL(
            title = "Dash - Single - fragmented TTML",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-en.ism/.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_Multiple_RFC_tags = URL(
            title = "Dash - Multiple - RFC 5646 language tags",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-rfc5646.ism/.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_Multiple_TTML = URL(
            title = "Dash - Multiple - fragmented TTML",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-ttml.ism/.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_AudioOnly = URL(
            title = "Dash - Audio only",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd?filter=(type!=%22video%22)",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_Multiple_Audio_Codec = URL(
            title = "Dash - Multiple audio codecs",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-codec.ism/.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_AlternateAudioLanguage = URL(
            title = "Dash - Alternate audio language",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_AccessibilityAudio = URL(
            title = "Dash - Accessibility - audio description",
            uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-desc-aud.ism/.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_PureLive = URL(
            title = "Dash - Pure live",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_Timeshift = URL(
            title = "Dash - Timeshift (5 minutes)",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd?time_shift=300",
            languageTag = "en-CH",
        )

        val UnifiedStreamingOnDemand_Dash_DVB_LowLatency = URL(
            title = "Dash - DVB DASH low latency",
            uri = "https://demo.unified-streaming.com/k8s/live/stable/live-low-latency.isml/.mpd",
            languageTag = "en-CH",
        )

        val BlockedSegment = URN(
            title = "Blocked segment at 29:26",
            urn = "urn:srf:video:40ca0277-0e53-4312-83e2-4710354ff53e",
            imageUri = "https://ws.srf.ch/asset/image/audio/f1a1ab5d-c009-4ba1-aae0-a2be5b89edd9/EPISODE_IMAGE/1465482801.png",
            languageTag = "en-CH",
        )

        val OverlapinglockedSegments = URN(
            title = "Overlapping segments",
            urn = "urn:srf:video:d57f5c1c-080f-49a2-864e-4a1a83e41ae1",
            imageUri = "https://ws.srf.ch/asset/image/audio/75c3d4a4-4357-4703-b407-2d076aa15fd7/EPISODE_IMAGE/1384985072.png",
            languageTag = "en-CH",
        )

        val MultiAudioWithAccessibility = URN(
            title = "Multi audio with AD track",
            description = "Bonjour la Suisse (5/5) - Que du bonheur?",
            urn = "urn:rts:video:8806923",
            imageUri = "https://www.rts.ch/2017/07/28/21/11/8806915.image/16x9",
            languageTag = "en-CH",
        )
    }
}
