/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MaximumLineLength", "MaxLineLength", "StringLiteralDuplication")

package ch.srgssr.pillarbox.demo.shared.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.demo.shared.source.BlockedTimeRangeAssetLoader
import java.io.Serializable

/**
 * Playlist
 *
 * @property title
 * @property items
 * @property languageTag
 */
@Suppress("UndocumentedPublicProperty")
data class Playlist(val title: String, val items: List<DemoItem>, val languageTag: String? = null) : Serializable {
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

    @Suppress("UndocumentedPublicClass")
    companion object {
        private const val serialVersionUID: Long = 1

        private val srgSsrStreamsUrls = Playlist(
            title = "SRG SSR streams (URLs)",
            items = listOf(
                DemoItem.URL(
                    title = "Sacha part à la rencontre d'univers atypiques",
                    uri = "https://rts-vod-amd.akamaized.net/ww/14970442/da2b38fb-ca9f-3c76-80c6-e6fa7f3c2699/master.m3u8",
                    description = "VOD - HLS",
                    imageUri = "https://www.rts.ch/2024/06/13/11/34/14970435.image/16x9",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Des violents orages ont touché Ajaccio, chef-lieu de la Corse, jeudi",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8",
                    description = "VOD - HLS (short)",
                    imageUri = "https://www.rts.ch/2022/08/18/12/38/13317144.image/16x9",
                    languageTag = "fr-CH",
                ),
                // urn:swi:video:48498670
                DemoItem.URL(
                    title = "Swiss wheelchair athlete wins top award",
                    uri = "https://cdn.prod.swi-services.ch/video-projects/94f5f5d1-5d53-4336-afda-9198462c45d9/localised-videos/ENG/renditions/ENG.mp4",
                    description = "VOD - MP4",
                    imageUri = "https://cdn.prod.swi-services.ch/video-delivery/images/94f5f5d1-5d53-4336-afda-9198462c45d9/_.1hAGinujJ.yERGrrGNzBGCNSxmhKZT/16x9",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Couleur 3 en vidéo (live)",
                    uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0",
                    description = "Video livestream - HLS",
                    imageUri = "https://img.rts.ch/audio/2010/image/924h3y-25865853.image?w=640&h=640",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Couleur 3 en vidéo (DVR)",
                    uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8",
                    description = "Video livestream with DVR - HLS",
                    imageUri = "https://il.srgssr.ch/images/?imageUrl=https%3A%2F%2Fwww.rts.ch%2F2020%2F05%2F18%2F14%2F20%2F11333286.image%2F16x9&format=jpg&width=960",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Tagesschau",
                    uri = "https://tagesschau.akamaized.net/hls/live/2020115/tagesschau/tagesschau_1/master.m3u8",
                    description = "Video livestream with DVR and timestamps - HLS",
                    imageUri = "https://images.tagesschau.de/image/89045d82-5cd5-46ad-8f91-73911add30ee/AAABh3YLLz0/AAABibBx2rU/20x9-1280/tagesschau-logo-100.jpg",
                    languageTag = "de-CH",
                ),
                DemoItem.URL(
                    title = "On en parle",
                    uri = "https://rts-aod-dd.akamaized.net/ww/13306839/63cc2653-8305-3894-a448-108810b553ef.mp3",
                    description = "AOD - MP3",
                    imageUri = "https://www.rts.ch/2023/09/28/17/49/11872957.image?w=624&h=351",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Couleur 3 (live)",
                    uri = "https://stream.srg-ssr.ch/m/couleur3/mp3_128",
                    description = "Audio livestream - MP3",
                    imageUri = "https://img.rts.ch/articles/2017/image/cxsqgp-25867841.image?w=640&h=640",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Couleur 3 (DVR)",
                    uri = "https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8",
                    description = "Audio livestream - HLS",
                    imageUri = "https://img.rts.ch/articles/2017/image/cxsqgp-25867841.image?w=640&h=640",
                    languageTag = "fr-CH",
                ),
            ),
            languageTag = "en-CH",
        )
        private val srgSsrStreamsUrns = Playlist(
            title = "SRG SSR streams (URNs)",
            items = listOf(
                DemoItem.URN(
                    title = "RTS 1",
                    urn = "urn:rts:video:3608506",
                    description = "DVR video livestream",
                    imageUri = "https://www.rts.ch/2023/09/06/14/43/14253742.image/16x9",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Couleur 3 (DVR)",
                    urn = "urn:rts:audio:3262363",
                    description = "DVR audio livestream",
                    imageUri = "https://www.rts.ch/2020/05/18/14/20/11333286.image/16x9",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Telegiornale flash",
                    urn = "urn:rsi:video:15916771",
                    description = "Superfluously token-protected video",
                    imageUri = "https://il.rsi.ch/rsi-api/resize/image/v2/WEBVISUAL/256699/",
                    languageTag = "it-CH",
                ),
                DemoItem.URN(
                    title = "SRF 1",
                    urn = "urn:srf:video:c4927fcf-e1a0-0001-7edd-1ef01d441651",
                    description = "Live video",
                    imageUri = "https://ws.srf.ch/asset/image/audio/d91bbe14-55dd-458c-bc88-963462972687/EPISODE_IMAGE",
                    languageTag = "de-CH",
                ),
                DemoItem.URN(
                    title = "Nachrichten von 08:00 Uhr - 08.03.2024",
                    urn = "urn:srf:audio:b9706015-632f-4e24-9128-5de074d98eda",
                    description = "On-demand audio stream",
                    languageTag = "de-CH",
                ),
                DemoItem.MultiAudioWithAccessibility,
                DemoItem.BlockedSegment,
                DemoItem.OverlapinglockedSegments
            ),
            languageTag = "en-CH",
        )
        val StoryUrns = Playlist(
            title = "Story urns",
            items = listOf(
                DemoItem.URN(
                    title = "Mario vs Sonic",
                    description = "Tataki 1",
                    urn = "urn:rts:video:13950405",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Pourquoi Beyoncé fait de la country",
                    description = "Tataki 2",
                    urn = "urn:rts:video:14815579",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "L'île North Sentinel",
                    description = "Tataki 3",
                    urn = "urn:rts:video:13795051",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Mourir pour ressembler à une idole",
                    description = "Tataki 4",
                    urn = "urn:rts:video:14020134",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Pourquoi les gens mangent des insectes ?",
                    description = "Tataki 5",
                    urn = "urn:rts:video:12631996",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Le concert de Beyoncé à Dubai",
                    description = "Tataki 6",
                    urn = "urn:rts:video:13752646",
                    languageTag = "fr-CH",
                )
            ),
            languageTag = "en-CH",
        )
        private val googleStreams = Playlist(
            title = "Google streams",
            items = listOf(
                DemoItem.URL(
                    title = "VoD - Dash (H264)",
                    uri = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "VoD - Dash Widewine cenc (H264)",
                    uri = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    licenseUri = "https://proxy.uat.widevine.com/proxy?video_id=2015_tears&provider=widevine_test",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "VoD - Dash (H265)",
                    uri = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "VoD - Dash widewine cenc (H265)",
                    uri = "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    licenseUri = "https://proxy.uat.widevine.com/proxy?video_id=2015_tears&provider=widevine_test",
                    languageTag = "en-CH",
                )
            ),
            languageTag = "en-CH",
        )
        private val appleStreams = Playlist(
            title = "Apple streams",
            items = listOf(
                DemoItem.URL(
                    title = "Apple Basic 4:3",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8",
                    description = "4x3 aspect ratio, H.264 @ 30Hz",
                    imageUri = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Apple Basic 16:9",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8",
                    description = "16x9 aspect ratio, H.264 @ 30Hz",
                    imageUri = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Apple Advanced 16:9 (TS)",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8",
                    description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Transport stream",
                    imageUri = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Apple Advanced 16:9 (fMP4)",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
                    description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Fragmented MP4",
                    imageUri = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Apple Advanced 16:9 (HEVC/H.264)",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_adv_example_hevc/master.m3u8",
                    description = "16x9 aspect ratio, H.264 and HEVC @ 30Hz and 60Hz",
                    imageUri = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Apple WWDC Keynote 2023",
                    uri = "https://events-delivery.apple.com/0105cftwpxxsfrpdwklppzjhjocakrsk/m3u8/vod_index-PQsoJoECcKHTYzphNkXohHsQWACugmET.m3u8",
                    imageUri = "https://www.apple.com/v/apple-events/home/ac/images/overview/recent-events/gallery/jun-2023__cjqmmqlyd21y_large_2x.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Apple Dolby Atmos",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/adv_dv_atmos/main.m3u8",
                    imageUri = "https://is1-ssl.mzstatic.com/image/thumb/-6farfCY0YClFd7-z_qZbA/1000x563.webp",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "The Morning Show - My Way: Season 1",
                    uri = "https://play-edge.itunes.apple.com/WebObjects/MZPlayLocal.woa/hls/subscription/playlist.m3u8?cc=CH&svcId=tvs.vds.4021&a=1522121579&isExternal=true&brandId=tvs.sbd.4000&id=518077009&l=en-GB&aec=UHD",
                    imageUri = "https://is1-ssl.mzstatic.com/image/thumb/cZUkXfqYmSy57DBI5TiTMg/1000x563.webp",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "The Morning Show - Change: Season 2",
                    uri = "https://play-edge.itunes.apple.com/WebObjects/MZPlayLocal.woa/hls/subscription/playlist.m3u8?cc=CH&svcId=tvs.vds.4021&a=1568297173&isExternal=true&brandId=tvs.sbd.4000&id=518034010&l=en-GB&aec=UHD",
                    imageUri = "https://is1-ssl.mzstatic.com/image/thumb/IxmmS1rQ7ouO-pKoJsVpGw/1000x563.webp",
                    languageTag = "en-CH",
                )
            ),
            languageTag = "en-CH",
        )
        private val thirdPartyStreams = Playlist(
            title = "Third-party streams",
            items = listOf(
                DemoItem.URL(
                    title = "Brain Farm Skate Phantom Flex",
                    uri = "https://sample.vodobox.net/skate_phantom_flex_4k/skate_phantom_flex_4k.m3u8",
                    description = "4K video",
                    imageUri = "https://i.ytimg.com/vi/d4_96ZWu3Vk/maxresdefault.jpg",
                    languageTag = "en-CH",
                )
            ),
            languageTag = "en-CH",
        )
        private val bitmovinStreams = Playlist(
            title = "Bitmovin streams streams",
            items = listOf(
                DemoItem.URL(
                    title = "Multiple subtitles and audio tracks",
                    uri = "https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
                    imageUri = "https://durian.blender.org/wp-content/uploads/2010/06/05.8b_comp_000272.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "4K, HEVC",
                    uri = "https://cdn.bitmovin.com/content/encoding_test_dash_hls/4k/hls/4k_profile/master.m3u8",
                    imageUri = "https://peach.blender.org/wp-content/uploads/bbb-splash.png",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "VoD, single audio track",
                    uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8",
                    imageUri = "https://img.redbull.com/images/c_crop,w_3840,h_1920,x_0,y_0,f_auto,q_auto/c_scale,w_1200/redbullcom/tv/FO-1MR39KNMH2111/fo-1mr39knmh2111-featuremedia",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "AES-128",
                    uri = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/m3u8s/11331.m3u8",
                    imageUri = "https://img.redbull.com/images/c_crop,w_3840,h_1920,x_0,y_0,f_auto,q_auto/c_scale,w_1200/redbullcom/tv/FO-1MR39KNMH2111/fo-1mr39knmh2111-featuremedia",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "AVC Progressive",
                    uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/MI201109210084_mpeg-4_hd_high_1080p25_10mbits.mp4",
                    imageUri = "https://img.redbull.com/images/c_crop,w_3840,h_1920,x_0,y_0,f_auto,q_auto/c_scale,w_1200/redbullcom/tv/FO-1MR39KNMH2111/fo-1mr39knmh2111-featuremedia",
                    languageTag = "en-CH",
                )
            ),
            languageTag = "en-CH",
        )
        private val unifiedStreaming = Playlist(
            title = "Unified Streaming - HLS",
            items = listOf(
                DemoItem.URL(
                    title = "Fragmented MP4",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Key Rotation",
                    uri = "https://demo.unified-streaming.com/k8s/keyrotation/stable/keyrotation/keyrotation.isml/.m3u8",
                    imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Alternate audio language",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Audio only",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8?filter=(type!=%22video%22)",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Trickplay",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.m3u8",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Limiting bandwidth use",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?max_bitrate=800000",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Dynamic Track Selection",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?filter=%28type%3D%3D%22audio%22%26%26systemBitrate%3C100000%29%7C%7C%28type%3D%3D%22video%22%26%26systemBitrate%3C1024000%29",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Pure live",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8",
                    imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Timeshift (5 minutes)",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?time_shift=300",
                    imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Live audio",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?filter=(type!=%22video%22)",
                    imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Pure live (scte35)",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/scte35.isml/.m3u8",
                    imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "fMP4, clear",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-fmp4.ism/.m3u8",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "fMP4, HEVC 4K",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hevc.ism/.m3u8",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                )
            ),
            languageTag = "en-CH",
        )
        private val unifiedStreamingDash = Playlist(
            title = "Unified Streaming - Dash",
            items = listOf(
                DemoItem.URL(
                    title = "MP4",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4/.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Fragmented MP4",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Trickplay",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Tiled thumbnails (live/timeline)",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-tiled-thumbnails-timeline.ism/.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Single - fragmented TTML",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-en.ism/.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Multiple - fragmented TTML",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-ttml.ism/.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Multiple - RFC 5646 language tags",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-rfc5646.ism/.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Accessibility - hard of hearing",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hoh-subs.ism/.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Pure live",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd",
                    imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Timeshift (5 minutes)",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd?time_shift=300",
                    imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "DVB DASH low latency",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live-low-latency.isml/.mpd",
                    imageUri = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Audio only",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd?filter=(type!=%22video%22)",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Alternate audio language",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Multiple audio codecs",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-codec.ism/.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Accessibility - audio description",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-desc-aud.ism/.mpd",
                    imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    languageTag = "en-CH",
                )
            ),
            languageTag = "en-CH",
        )
        private val aspectRatios = Playlist(
            title = "Aspect ratios",
            items = listOf(
                DemoItem.URN(
                    title = "Horizontal video",
                    urn = "urn:rts:video:14827306",
                    imageUri = "https://www.rts.ch/2024/04/10/19/23/14827621.image/16x9",
                    languageTag = "en-CH",
                ),
                DemoItem.URN(
                    title = "Square video",
                    urn = "urn:rts:video:8393241",
                    imageUri = "https://www.rts.ch/2017/02/16/07/08/8393235.image/16x9",
                    languageTag = "en-CH",
                ),
                DemoItem.URN(
                    title = "Vertical video",
                    urn = "urn:rts:video:13444390",
                    imageUri = "https://www.rts.ch/2022/10/06/17/32/13444380.image/4x5",
                    languageTag = "en-CH",
                )
            ),
            languageTag = "en-CH",
        )
        private val unbufferedStreams = Playlist(
            title = "Unbuffered streams",
            items = listOf(
                DemoItem.URL(
                    title = "Couleur 3 en direct",
                    uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0",
                    description = "Live video (unbuffered)",
                    imageUri = "https://www.rts.ch/2020/05/18/14/20/11333286.image/16x9",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Couleur 3 en direct",
                    uri = "https://stream.srg-ssr.ch/m/couleur3/mp3_128",
                    description = "Audio livestream (unbuffered)",
                    imageUri = "https://img.rts.ch/articles/2017/image/cxsqgp-25867841.image?w=320&h=320",
                    languageTag = "fr-CH",
                )
            ),
            languageTag = "en-CH",
        )
        private val cornerCases = Playlist(
            title = "Corner cases",
            items = listOf(
                DemoItem.URN(
                    title = "Expired URN",
                    urn = "urn:rts:video:13382911",
                    description = "Content that is not available anymore",
                    imageUri = "https://www.rts.ch/2022/09/20/09/57/13365589.image/16x9",
                    languageTag = "en-CH",
                ),
                DemoItem.URN(
                    title = "Unknown URN",
                    urn = "urn:srf:video:unknown",
                    description = "Content that does not exist",
                    languageTag = "en-CH",
                ),
                DemoItem.URL(
                    title = "Custom MediaSource",
                    uri = "https://custom-media.ch/fondue",
                    description = "Using a custom CustomMediaSource",
                    languageTag = "en-CH",
                ),
                BlockedTimeRangeAssetLoader.DemoItemBlockedTimeRangeAtStartAndEnd,
                BlockedTimeRangeAssetLoader.DemoItemBlockedTimeRangeOverlaps,
                BlockedTimeRangeAssetLoader.DemoItemBlockedTimeRangeIncluded,
            ),
            languageTag = "en-CH",
        )

        val examplesPlaylists = listOf(
            srgSsrStreamsUrls, srgSsrStreamsUrns, googleStreams, appleStreams, thirdPartyStreams, bitmovinStreams, unifiedStreaming,
            unifiedStreamingDash, aspectRatios, unbufferedStreams, cornerCases
        )

        val VideoUrls = Playlist(
            title = "Video urls",
            items = listOf(
                DemoItem.URL(
                    title = "Le R. - Légumes trop chers",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444390/f1b478f7-2ae9-3166-94b9-c5d5fe9610df/master.m3u8",
                    description = "Playlist item 1",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Le R. - Production de légumes bio",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444333/feb1d08d-e62c-31ff-bac9-64c0a7081612/master.m3u8",
                    description = "Playlist item 2",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Le R. - Endométriose",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444466/2787e520-412f-35fb-83d7-8dbb31b5c684/master.m3u8",
                    description = "Playlist item 3",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Le R. - Prix Nobel de littérature 2022",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444447/c1d17174-ad2f-31c2-a084-846a9247fd35/master.m3u8",
                    description = "Playlist item 4",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Le R. - Femme, vie, liberté",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444352/32145dc0-b5f8-3a14-ae11-5fc6e33aaaa4/master.m3u8",
                    description = "Playlist item 5",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Le R. - Attaque en Thaïlande",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444409/23f808a4-b14a-3d3e-b2ed-fa1279f6cf01/master.m3u8",
                    description = "Playlist item 6",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Le R. - Douches et vestiaires non genrés",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444371/3f26467f-cd97-35f4-916f-ba3927445920/master.m3u8",
                    description = "Playlist item 7",
                    languageTag = "fr-CH",
                ),
                DemoItem.URL(
                    title = "Le R. - Prends soin de toi, des autres et à demain",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13444428/857d97ef-0b8e-306e-bf79-3b13e8c901e4/master.m3u8",
                    description = "Playlist item 8",
                    languageTag = "fr-CH",
                )
            ),
            languageTag = "en-CH",
        )

        val VideoUrns = Playlist(
            title = "Video urns",
            items = listOf(
                DemoItem.URN(
                    title = "Le R. - Légumes trop chers",
                    urn = "urn:rts:video:13444390",
                    description = "Playlist item 1",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Le R. - Production de légumes bio",
                    urn = "urn:rts:video:13444333",
                    description = "Playlist item 2",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Le R. - Endométriose",
                    urn = "urn:rts:video:13444466",
                    description = "Playlist item 3",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Le R. - Prix Nobel de littérature 2022",
                    urn = "urn:rts:video:13444447",
                    description = "Playlist item 4",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Le R. - Femme, vie, liberté",
                    urn = "urn:rts:video:13444352",
                    description = "Playlist item 5",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Le R. - Attaque en Thailande",
                    urn = "urn:rts:video:13444409",
                    description = "Playlist item 6",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Le R. - Douches et vestinaires non genrés",
                    urn = "urn:rts:video:13444371",
                    description = "Playlist item 7",
                    languageTag = "fr-CH",
                ),
                DemoItem.URN(
                    title = "Le R. - Prend soin de toi des autres et à demain",
                    urn = "urn:rts:video:13444428",
                    description = "Playlist item 8",
                    languageTag = "fr-CH",
                )
            ),
            languageTag = "en-CH",
        )

        val MixedContent = Playlist(
            title = "Mixed Content",
            listOf(
                DemoItem.OnDemandHLS,
                DemoItem.OnDemandHorizontalVideo,
                DemoItem.Unknown,
                DemoItem.ShortOnDemandVideoHLS
            ),
            languageTag = "en-CH",
        )

        val MixedContentLiveDvrVod = Playlist(
            title = "Mixed live dvr and vod",
            listOf(
                DemoItem.OnDemandHLS,
                DemoItem.OnDemandHorizontalVideo,
                DemoItem.DvrVideo,
                DemoItem.ShortOnDemandVideoHLS
            ),
            languageTag = "en-CH",
        )

        val MixedContentLiveOnlyVod = Playlist(
            title = "Mixed live only and vod",
            listOf(
                DemoItem.OnDemandHLS,
                DemoItem.OnDemandHorizontalVideo,
                DemoItem.LiveVideo,
                DemoItem.ShortOnDemandVideoHLS,
            ),
            languageTag = "en-CH",
        )

        val EmptyPlaylist = Playlist(
            title = "Empty",
            items = emptyList(),
            languageTag = "en-CH",
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
            ),
            languageTag = "en-CH",
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
                DemoItem.Unknown,
            ),
            languageTag = "en-CH",
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
            ),
            languageTag = "en-CH",
        )

        val StreamGoogles = Playlist(
            title = "Google samples",
            items = listOf(
                DemoItem.GoogleDashH264,
                DemoItem.GoogleDashH264_CENC_Widewine,
                DemoItem.GoogleDashH265,
                DemoItem.GoogleDashH265_CENC_Widewine
            ),
            languageTag = "en-CH",
        )

        val BitmovinSamples = Playlist(
            title = "Bitmovin",
            items = listOf(
                DemoItem.BitmovinOnDemandProgressive,
                DemoItem.BitmovinOnDemandMultipleTracks,
                DemoItem.BitmovinOnDemand_4K_HEVC,
                DemoItem.BitmovinOnDemandAES128,
                DemoItem.BitmovinOnDemandSingleAudio,
            ),
            languageTag = "en-CH",
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
            ),
            languageTag = "en-CH",
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
            ),
            languageTag = "en-CH",
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
                BitmovinSamples.items,
            languageTag = "en-CH",
        )
    }
}
