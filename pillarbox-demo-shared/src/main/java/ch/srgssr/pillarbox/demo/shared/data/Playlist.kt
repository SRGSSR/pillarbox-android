/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MaximumLineLength", "MaxLineLength")

package ch.srgssr.pillarbox.demo.shared.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.io.Serializable

/**
 * Playlist
 *
 * @property title
 * @property items
 * @property description optional
 */
@Suppress("UndocumentedPublicProperty")
data class Playlist(val title: String, val items: List<DemoItem>, val description: String? = null) : Serializable {
    /**
     * To media item
     *
     * @return not playable MediaItem
     */
    fun toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(title)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setIsBrowsable(true)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
    }

    companion object {
        private const val serialVersionUID: Long = 1

        private val srgSsrStreamsUrls = Playlist(
            title = "SRG SSR streams (URLs)",
            items = listOf(
                DemoItem(
                    title = "Switzerland says sorry! The fondue invasion",
                    uri = "https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8",
                    description = "VOD - HLS"
                ),
                DemoItem(
                    title = "Des violents orages ont touché Ajaccio, chef-lieu de la Corse, jeudi",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8",
                    description = "VOD - HLS (short)"
                ),
                DemoItem(
                    title = "The dig",
                    uri = "https://media.swissinfo.ch/media/video/dddaff93-c2cd-4b6e-bdad-55f75a519480/rendition/154a844b-de1d-4854-93c1-5c61cd07e98c.mp4",
                    description = "VOD - MP4"
                ),
                DemoItem(
                    title = "Couleur 3 en vidéo (live)",
                    uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0",
                    description = "Video livestream - HLS"
                ),
                DemoItem(
                    title = "Couleur 3 en vidéo (DVR)",
                    uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8",
                    description = "Video livestream with DVR - HLS"
                ),
                DemoItem(
                    title = "Tageschau",
                    uri = "https://tagesschau.akamaized.net/hls/live/2020115/tagesschau/tagesschau_1/master.m3u8",
                    description = "Video livestream with DVR and timestamps - HLS"
                ),
                DemoItem(
                    title = "On en parle",
                    uri = "https://rts-aod-dd.akamaized.net/ww/13306839/63cc2653-8305-3894-a448-108810b553ef.mp3",
                    description = "AOD - MP3"
                ),
                DemoItem(
                    title = "Couleur 3 (live)",
                    uri = "https://stream.srg-ssr.ch/m/couleur3/mp3_128",
                    description = "Audio livestream - MP3"
                ),
                DemoItem(
                    title = "Couleur 3 (DVR)",
                    uri = "https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8",
                    description = "Audio livestream - HLS"
                )
            )
        )
        private val srgSsrStreamsUrns = Playlist(
            title = "SRG SSR streams (URNs)",
            items = listOf(
                DemoItem(
                    title = "RSI 1",
                    uri = "urn:rsi:video:livestream_La1",
                    description = "Live video"
                ),
                DemoItem(
                    title = "RTS 1",
                    uri = "urn:rts:video:3608506",
                    description = "DVR video livestream"
                ),
                DemoItem(
                    title = "Couleur 3 (DVR)",
                    uri = "urn:rts:audio:3262363",
                    description = "DVR audio livestream"
                ),
                DemoItem(
                    title = "Telegiornale flash",
                    uri = "urn:rsi:video:15916771",
                    description = "Superfluously token-protected video"
                ),
                DemoItem(
                    title = "SRF 1",
                    uri = "urn:srf:video:c4927fcf-e1a0-0001-7edd-1ef01d441651",
                    description = "Live video"
                ),
                DemoItem(
                    title = "Il lavoro di TerraProject per una fotografia documentaria",
                    uri = "urn:rsi:audio:8833144",
                    description = "On-demand audio stream"
                )
            )
        )
        private val googleStreams = Playlist(
            title = "Google streams",
            items = listOf(
                DemoItem(
                    title = "VoD - Dash (H264)",
                    uri = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"
                ),
                DemoItem(
                    title = "VoD - Dash Widewine cenc (H264)",
                    uri = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd",
                    licenseUrl = "https://proxy.uat.widevine.com/proxy?video_id=2015_tears&provider=widevine_test"
                ),
                DemoItem(
                    title = "VoD - Dash (H265)",
                    uri = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd"
                ),
                DemoItem(
                    title = "VoD - Dash widewine cenc (H265)",
                    uri = "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears.mpd",
                    licenseUrl = "https://proxy.uat.widevine.com/proxy?video_id=2015_tears&provider=widevine_test"
                )
            )
        )
        private val appleStreams = Playlist(
            title = "Apple streams",
            items = listOf(
                DemoItem(
                    title = "Apple Basic 4:3",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8",
                    description = "4x3 aspect ratio, H.264 @ 30Hz"
                ),
                DemoItem(
                    title = "Apple Basic 16:9",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8",
                    description = "16x9 aspect ratio, H.264 @ 30Hz"
                ),
                DemoItem(
                    title = "Apple Advanced 16:9 (TS)",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8",
                    description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Transport stream"
                ),
                DemoItem(
                    title = "Apple Advanced 16:9 (fMP4)",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
                    description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Fragmented MP4"
                ),
                DemoItem(
                    title = "Apple Advanced 16:9 (HEVC/H.264)",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_adv_example_hevc/master.m3u8",
                    description = "16x9 aspect ratio, H.264 and HEVC @ 30Hz and 60Hz"
                ),
                DemoItem(
                    title = "Apple WWDC Keynote 2023",
                    uri = "https://events-delivery.apple.com/0105cftwpxxsfrpdwklppzjhjocakrsk/m3u8/vod_index-PQsoJoECcKHTYzphNkXohHsQWACugmET.m3u8"
                ),
                DemoItem(
                    title = "Apple Dolby Atmos",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/adv_dv_atmos/main.m3u8"
                ),
                DemoItem(
                    title = "The Morning Show - My Way: Season 1",
                    uri = "https://play-edge.itunes.apple.com/WebObjects/MZPlayLocal.woa/hls/subscription/playlist.m3u8?cc=CH&svcId=tvs.vds.4021&a=1522121579&isExternal=true&brandId=tvs.sbd.4000&id=518077009&l=en-GB&aec=UHD"
                ),
                DemoItem(
                    title = "The Morning Show - Change: Season 2",
                    uri = "https://play-edge.itunes.apple.com/WebObjects/MZPlayLocal.woa/hls/subscription/playlist.m3u8?cc=CH&svcId=tvs.vds.4021&a=1568297173&isExternal=true&brandId=tvs.sbd.4000&id=518034010&l=en-GB&aec=UHD"
                )
            )
        )
        private val thirdPartyStreams = Playlist(
            title = "Third-party streams",
            items = listOf(
                DemoItem(
                    title = "Brain Farm Skate Phantom Flex",
                    uri = "https://sample.vodobox.net/skate_phantom_flex_4k/skate_phantom_flex_4k.m3u8",
                    description = "4K video"
                )
            )
        )
        private val bitmovinStreams = Playlist(
            title = "Bitmovin streams streams",
            items = listOf(
                DemoItem(
                    title = "Multiple subtitles and audio tracks",
                    uri = "https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
                ),
                DemoItem(
                    title = "4K, HEVC",
                    uri = "https://cdn.bitmovin.com/content/encoding_test_dash_hls/4k/hls/4k_profile/master.m3u8"
                ),
                DemoItem(
                    title = "VoD, single audio track",
                    uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"
                ),
                DemoItem(
                    title = "AES-128",
                    uri = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/m3u8s/11331.m3u8"
                ),
                DemoItem(
                    title = "AVC Progressive",
                    uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/MI201109210084_mpeg-4_hd_high_1080p25_10mbits.mp4"
                )
            )
        )
        private val unifiedStreaming = Playlist(
            title = "Unified Streaming - HLS",
            items = listOf(
                DemoItem(
                    title = "Fragmented MP4",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
                ),
                DemoItem(
                    title = "Key Rotation",
                    uri = "https://demo.unified-streaming.com/k8s/keyrotation/stable/keyrotation/keyrotation.isml/.m3u8"
                ),
                DemoItem(
                    title = "Alternate audio language",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8"
                ),
                DemoItem(
                    title = "Audio only",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8?filter=(type!=%22video%22)"
                ),
                DemoItem(
                    title = "Trickplay",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.m3u8"
                ),
                DemoItem(
                    title = "Limiting bandwidth use",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?max_bitrate=800000"
                ),
                DemoItem(
                    title = "Dynamic Track Selection",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?filter=%28type%3D%3D%22audio%22%26%26systemBitrate%3C100000%29%7C%7C%28type%3D%3D%22video%22%26%26systemBitrate%3C1024000%29"
                ),
                DemoItem(
                    title = "Pure live",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8"
                ),
                DemoItem(
                    title = "Timeshift (5 minutes)",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?time_shift=300"
                ),
                DemoItem(
                    title = "Live audio",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?filter=(type!=%22video%22)"
                ),
                DemoItem(
                    title = "Pure live (scte35)",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/scte35.isml/.m3u8"
                ),
                DemoItem(
                    title = "fMP4, clear",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-fmp4.ism/.m3u8"
                ),
                DemoItem(
                    title = "fMP4, HEVC 4K",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hevc.ism/.m3u8"
                )
            )
        )
        private val unifiedStreamingDash = Playlist(
            title = "Unified Streaming - Dash",
            items = listOf(
                DemoItem(
                    title = "MP4",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4/.mpd"
                ),
                DemoItem(
                    title = "Fragmented MP4",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.mpd"
                ),
                DemoItem(
                    title = "Trickplay",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.mpd"
                ),
                DemoItem(
                    title = "Tiled thumbnails (live/timeline)",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-tiled-thumbnails-timeline.ism/.mpd"
                ),
                DemoItem(
                    title = "Single - fragmented TTML",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-en.ism/.mpd"
                ),
                DemoItem(
                    title = "Multiple - fragmented TTML",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-ttml.ism/.mpd"
                ),
                DemoItem(
                    title = "Multiple - RFC 5646 language tags",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-rfc5646.ism/.mpd"
                ),
                DemoItem(
                    title = "Accessibility - hard of hearing",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hoh-subs.ism/.mpd"
                ),
                DemoItem(
                    title = "Pure live",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd"
                ),
                DemoItem(
                    title = "Timeshift (5 minutes)",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd?time_shift=300"
                ),
                DemoItem(
                    title = "DVB DASH low latency",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live-low-latency.isml/.mpd"
                ),
                DemoItem(
                    title = "Audio only",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd?filter=(type!=%22video%22)"
                ),
                DemoItem(
                    title = "Alternate audio language",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd"
                ),
                DemoItem(
                    title = "Multiple audio codecs",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-codec.ism/.mpd"
                ),
                DemoItem(
                    title = "Accessibility - audio description",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-desc-aud.ism/.mpd"
                )
            )
        )
        private val aspectRatios = Playlist(
            title = "Aspect ratios",
            items = listOf(
                DemoItem(
                    title = "Horizontal video",
                    uri = "urn:rts:video:6820736"
                ),
                DemoItem(
                    title = "Square video",
                    uri = "urn:rts:video:8393241"
                ),
                DemoItem(
                    title = "Vertical video",
                    uri = "urn:rts:video:13444390"
                )
            )
        )
        private val unbufferedStreams = Playlist(
            title = "Unbuffered streams",
            items = listOf(
                DemoItem(
                    title = "Couleur 3 en direct",
                    uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0",
                    description = "Live video (unbuffered)"
                ),
                DemoItem(
                    title = "Couleur 3 en direct",
                    uri = "http://stream.srg-ssr.ch/m/couleur3/mp3_128",
                    description = "Audio livestream (unbuffered)"
                )
            )
        )
        private val cornerCases = Playlist(
            title = "Corner cases",
            items = listOf(
                DemoItem(
                    title = "Expired URN",
                    uri = "urn:rts:video:13382911",
                    description = "Content that is not available anymore"
                ),
                DemoItem(
                    title = "Unknown URN",
                    uri = "urn:srf:video:unknown",
                    description = "Content that does not exist"
                )
            )
        )

        val examplesPlaylists = listOf(
            srgSsrStreamsUrls, srgSsrStreamsUrns, googleStreams, appleStreams, thirdPartyStreams, bitmovinStreams, unifiedStreaming,
            unifiedStreamingDash, aspectRatios, unbufferedStreams, cornerCases
        )

        val VideoUrls = Playlist(
            title = "Video urls",
            items = listOf(
                DemoItem(
                    title = "Le R. - Légumes trop chers",
                    description = "Playlist item 1",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444390/f1b478f7-2ae9-3166-94b9-c5d5fe9610df/master.m3u8"
                ),
                DemoItem(
                    title = "Le R. - Production de légumes bio",
                    description = "Playlist item 2",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444333/feb1d08d-e62c-31ff-bac9-64c0a7081612/master.m3u8"
                ),
                DemoItem(
                    title = "Le R. - Endométriose",
                    description = "Playlist item 3",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444466/2787e520-412f-35fb-83d7-8dbb31b5c684/master.m3u8"
                ),
                DemoItem(
                    title = "Le R. - Prix Nobel de littérature 2022",
                    description = "Playlist item 4",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444447/c1d17174-ad2f-31c2-a084-846a9247fd35/master.m3u8"
                ),
                DemoItem(
                    title = "Le R. - Femme, vie, liberté",
                    description = "Playlist item 5",

                    uri = "https://rts-vod-amd.akamaized.net/ww/13444352/32145dc0-b5f8-3a14-ae11-5fc6e33aaaa4/master.m3u8"
                ),
                DemoItem(
                    title = "Le R. - Attaque en Thaïlande",
                    description = "Playlist item 6",

                    uri = "https://rts-vod-amd.akamaized.net/ww/13444409/23f808a4-b14a-3d3e-b2ed-fa1279f6cf01/master.m3u8"
                ),
                DemoItem(
                    title = "Le R. - Douches et vestiaires non genrés",
                    description = "Playlist item 7",

                    uri = "https://rts-vod-amd.akamaized.net/ww/13444371/3f26467f-cd97-35f4-916f-ba3927445920/master.m3u8"
                ),
                DemoItem(
                    title = "Le R. - Prends soin de toi, des autres et à demain",
                    description = "Playlist item 8",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444428/857d97ef-0b8e-306e-bf79-3b13e8c901e4/master.m3u8"
                )
            )
        )

        val VideoUrns = Playlist(
            title = "Video urns",
            items = listOf(
                DemoItem(
                    title = "Le R. - Légumes trop chers",
                    description = "Playlist item 1",
                    uri = "urn:rts:video:13444390"
                ),
                DemoItem(
                    title = "Le R. - Production de légumes bio",
                    description = "Playlist item 2",
                    uri = "urn:rts:video:13444333"
                ),
                DemoItem(
                    title = "Le R. - Endométriose",
                    description = "Playlist item 3",
                    uri = "urn:rts:video:13444466"
                ),
                DemoItem(
                    title = "Le R. - Prix Nobel de littérature 2022",
                    description = "Playlist item 4",
                    uri = "urn:rts:video:13444447"
                ),
                DemoItem(
                    title = "Le R. - Femme, vie, liberté",
                    description = "Playlist item 5",
                    uri = "urn:rts:video:13444352"
                ),
                DemoItem(
                    title = "Le R. - Attaque en Thailande",
                    description = "Playlist item 6",
                    uri = "urn:rts:video:13444409"
                ),
                DemoItem(
                    title = "Le R. - Douches et vestinaires non genrés",
                    description = "Playlist item 7",
                    uri = "urn:rts:video:13444371"
                ),
                DemoItem(
                    title = "Le R. - Prend soin de toi des autres et à demain",
                    description = "Playlist item 8",
                    uri = "urn:rts:video:13444428"
                )
            )
        )

        val MixedContent = Playlist(
            title = "Mixed Content",
            listOf(
                DemoItem.OnDemandHLS,
                DemoItem.OnDemandHorizontalVideo,
                DemoItem.Unknown,
                DemoItem.ShortOnDemandVideoHLS
            )
        )

        val MixedContentLiveDvrVod = Playlist(
            title = "Mixed live dvr and vod",
            listOf(
                DemoItem.OnDemandHLS,
                DemoItem.OnDemandHorizontalVideo,
                DemoItem.DvrVideo,
                DemoItem.ShortOnDemandVideoHLS
            )
        )

        val MixedContentLiveOnlyVod = Playlist(
            title = "Mixed live only and vod",
            listOf(
                DemoItem.OnDemandHLS,
                DemoItem.OnDemandHorizontalVideo,
                DemoItem.LiveVideo,
                DemoItem.ShortOnDemandVideoHLS,
            )
        )

        val StreamUrls = Playlist(
            title = "Media with urls",
            items = listOf(
                DemoItem.OnDemandHLS,
                DemoItem.ShortOnDemandVideoHLS,
                DemoItem.OnDemandVideoMP4,
                DemoItem.OnDemandVideoUHD,
                DemoItem.LiveVideoHLS,
                DemoItem.DvrVideoHLS,
                DemoItem.LiveTimestampVideoHLS,
                DemoItem.OnDemandAudioMP3,
                DemoItem.LiveAudioMP3,
                DemoItem.DvrAudioHLS
            )
        )

        val StreamUrns = Playlist(
            title = "Media with urns",
            items = listOf(
                DemoItem.OnDemandHorizontalVideo,
                DemoItem.OnDemandSquareVideo,
                DemoItem.OnDemandVerticalVideo,
                DemoItem.SuperfluouslyTokenProtectedVideo,
                DemoItem.LiveVideo,
                DemoItem.DvrVideo,
                DemoItem.DvrAudio,
                DemoItem.OnDemandAudio,
                DemoItem.Expired,
                DemoItem.Unknown
            )
        )

        val StreamApples = Playlist(
            title = "Apple samples",
            items = listOf(
                DemoItem.AppleBasic_16_9_TS_HLS,
                DemoItem.AppleBasic_4_3_HLS,
                DemoItem.AppleAdvanced_16_9_TS_HLS,
                DemoItem.AppleAdvanced_16_9_fMP4_HLS,
                DemoItem.AppleAdvanced_16_9_HEVC_h264_HLS,
                DemoItem.AppleAtmos,
                DemoItem.AppleWWDC_2023,
                DemoItem.AppleTvSample,
            )
        )

        val StreamGoogles = Playlist(
            title = "Google samples",
            items = listOf(
                DemoItem.GoogleDashH264,
                DemoItem.GoogleDashH264_CENC_Widewine,
                DemoItem.GoogleDashH265,
                DemoItem.GoogleDashH265_CENC_Widewine
            )
        )

        val BitmovinSamples = Playlist(
            title = "Bitmovin",
            items = listOf(
                DemoItem.BitmovinOnDemandProgressive,
                DemoItem.BitmovinOnDemandMultipleTracks,
                DemoItem.BitmovinOnDemand_4K_HEVC,
                DemoItem.BitmovinOnDemandAES128,
                DemoItem.BitmovinOnDemandSingleAudio,
            )
        )

        val UnifiedStreamingHls = Playlist(
            title = "Unified streaming - HLS",
            items = listOf(
                DemoItem.UnifiedStreamingOnDemandAudioOnly,
                DemoItem.UnifiedStreamingOnDemandAlternateAudio,
                DemoItem.UnifiedStreamingLiveAudio,
                DemoItem.UnifiedStreamingOnDemand_fMP4_HEVC_4K,
                DemoItem.UnifiedStreamingOnDemand_fMP4,
                DemoItem.UnifiedStreamingOnDemand_fMP4_Clear,
                DemoItem.UnifiedStreamingTimeshift,
                DemoItem.UnifiedStreamingPureLive,
                DemoItem.UnifiedStreamingPureLiveScte35,
                DemoItem.UnifiedStreamingOnDemandTrickplay,
                DemoItem.UnifiedStreamingOnDemandLimitedBandwidth,
                DemoItem.UnifiedStreamingOnDemandDynamicTrackSelection,
            )
        )

        val UnifiedStreamingDash = Playlist(
            title = "Unified streaming - Dash",
            items = listOf(
                DemoItem.UnifiedStreamingOnDemand_Dash_MP4,
                DemoItem.UnifiedStreamingOnDemand_Dash_FragmentedMP4,
                DemoItem.UnifiedStreamingOnDemand_Dash_TrickPlay,
                DemoItem.UnifiedStreamingOnDemand_Dash_TiledThumbnails,
                DemoItem.UnifiedStreamingOnDemand_Dash_Single_TTML,
                DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_TTML,
                DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_RFC_tags,
                DemoItem.UnifiedStreamingOnDemand_Dash_Accessibility,
                DemoItem.UnifiedStreamingOnDemand_Dash_PureLive,
                DemoItem.UnifiedStreamingOnDemand_Dash_Timeshift,
                DemoItem.UnifiedStreamingOnDemand_Dash_DVB_LowLatency,
                DemoItem.UnifiedStreamingOnDemand_Dash_AudioOnly,
                DemoItem.UnifiedStreamingOnDemand_Dash_AlternateAudioLanguage,
                DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_Audio_Codec,
                DemoItem.UnifiedStreamingOnDemand_Dash_AccessibilityAudio,
            )
        )

        val All = Playlist(
            title = "Standard items",
            items = StreamUrls.items +
                StreamUrns.items +
                VideoUrns.items +
                StreamGoogles.items +
                StreamApples.items +
                UnifiedStreamingHls.items +
                UnifiedStreamingDash.items +
                BitmovinSamples.items
        )
    }
}
