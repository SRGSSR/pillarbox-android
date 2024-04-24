/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MaximumLineLength", "MaxLineLength", "StringLiteralDuplication")

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
                    description = "VOD - HLS",
                    imageUrl = "https://cdn.prod.swi-services.ch/video-delivery/images/14e4562f-725d-4e41-a200-7fcaa77df2fe/5rwf1Bq_m3GC5secOZcIcgbbrbZPf4nI/16x9"
                ),
                DemoItem(
                    title = "Des violents orages ont touché Ajaccio, chef-lieu de la Corse, jeudi",
                    uri = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8",
                    description = "VOD - HLS (short)",
                    imageUrl = "https://www.rts.ch/2022/08/18/12/38/13317144.image/16x9"
                ),
                DemoItem(
                    title = "Swiss wheelchair athlete wins top award",
                    uri = "https://cdn.prod.swi-services.ch/video-projects/94f5f5d1-5d53-4336-afda-9198462c45d9/localised-videos/ENG/renditions/ENG.mp4",
                    description = "VOD - MP4 (urn:swi:video:48498670)",
                    imageUrl = "https://cdn.prod.swi-services.ch/video-delivery/images/94f5f5d1-5d53-4336-afda-9198462c45d9/_.1hAGinujJ.yERGrrGNzBGCNSxmhKZT/16x9"
                ),
                DemoItem(
                    title = "Couleur 3 en vidéo (live)",
                    uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0",
                    description = "Video livestream - HLS",
                    imageUrl = "https://img.rts.ch/audio/2010/image/924h3y-25865853.image?w=640&h=640"
                ),
                DemoItem(
                    title = "Couleur 3 en vidéo (DVR)",
                    uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8",
                    description = "Video livestream with DVR - HLS",
                    imageUrl = "https://il.srgssr.ch/images/?imageUrl=https%3A%2F%2Fwww.rts.ch%2F2020%2F05%2F18%2F14%2F20%2F11333286.image%2F16x9&format=jpg&width=960"
                ),
                DemoItem(
                    title = "Tagesschau",
                    uri = "https://tagesschau.akamaized.net/hls/live/2020115/tagesschau/tagesschau_1/master.m3u8",
                    description = "Video livestream with DVR and timestamps - HLS",
                    imageUrl = "https://images.tagesschau.de/image/89045d82-5cd5-46ad-8f91-73911add30ee/AAABh3YLLz0/AAABibBx2rU/20x9-1280/tagesschau-logo-100.jpg"
                ),
                DemoItem(
                    title = "On en parle",
                    uri = "https://rts-aod-dd.akamaized.net/ww/13306839/63cc2653-8305-3894-a448-108810b553ef.mp3",
                    description = "AOD - MP3",
                    imageUrl = "https://www.rts.ch/2023/09/28/17/49/11872957.image?w=624&h=351"
                ),
                DemoItem(
                    title = "Couleur 3 (live)",
                    uri = "https://stream.srg-ssr.ch/m/couleur3/mp3_128",
                    description = "Audio livestream - MP3",
                    imageUrl = "https://img.rts.ch/articles/2017/image/cxsqgp-25867841.image?w=640&h=640"
                ),
                DemoItem(
                    title = "Couleur 3 (DVR)",
                    uri = "https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8",
                    description = "Audio livestream - HLS",
                    imageUrl = "https://img.rts.ch/articles/2017/image/cxsqgp-25867841.image?w=640&h=640"
                )
            )
        )
        private val srgSsrStreamsUrns = Playlist(
            title = "SRG SSR streams (URNs)",
            items = listOf(
                DemoItem(
                    title = "RTS 1",
                    uri = "urn:rts:video:3608506",
                    description = "DVR video livestream",
                    imageUrl = "https://www.rts.ch/2023/09/06/14/43/14253742.image/16x9"
                ),
                DemoItem(
                    title = "Couleur 3 (DVR)",
                    uri = "urn:rts:audio:3262363",
                    description = "DVR audio livestream",
                    imageUrl = "https://www.rts.ch/2020/05/18/14/20/11333286.image/16x9"
                ),
                DemoItem(
                    title = "Telegiornale flash",
                    uri = "urn:rsi:video:15916771",
                    description = "Superfluously token-protected video",
                    imageUrl = "https://il.rsi.ch/rsi-api/resize/image/v2/WEBVISUAL/256699/"
                ),
                DemoItem(
                    title = "SRF 1",
                    uri = "urn:srf:video:c4927fcf-e1a0-0001-7edd-1ef01d441651",
                    description = "Live video",
                    imageUrl = "https://ws.srf.ch/asset/image/audio/d91bbe14-55dd-458c-bc88-963462972687/EPISODE_IMAGE"
                ),
                DemoItem(
                    title = "Nachrichten von 08:00 Uhr - 08.03.2024",
                    uri = "urn:srf:audio:b9706015-632f-4e24-9128-5de074d98eda",
                    description = "On-demand audio stream"
                ),
                DemoItem.BlockedSegment,
                DemoItem.OverlapinglockedSegments
            )
        )
        private val googleStreams = Playlist(
            title = "Google streams",
            items = listOf(
                DemoItem(
                    title = "VoD - Dash (H264)",
                    uri = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "VoD - Dash Widewine cenc (H264)",
                    uri = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
                    licenseUrl = "https://proxy.uat.widevine.com/proxy?video_id=2015_tears&provider=widevine_test"
                ),
                DemoItem(
                    title = "VoD - Dash (H265)",
                    uri = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "VoD - Dash widewine cenc (H265)",
                    uri = "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
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
                    description = "4x3 aspect ratio, H.264 @ 30Hz",
                    imageUrl = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200"
                ),
                DemoItem(
                    title = "Apple Basic 16:9",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8",
                    description = "16x9 aspect ratio, H.264 @ 30Hz",
                    imageUrl = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200"
                ),
                DemoItem(
                    title = "Apple Advanced 16:9 (TS)",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8",
                    description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Transport stream",
                    imageUrl = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200"
                ),
                DemoItem(
                    title = "Apple Advanced 16:9 (fMP4)",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
                    description = "16x9 aspect ratio, H.264 @ 30Hz and 60Hz, Fragmented MP4",
                    imageUrl = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200"
                ),
                DemoItem(
                    title = "Apple Advanced 16:9 (HEVC/H.264)",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_adv_example_hevc/master.m3u8",
                    description = "16x9 aspect ratio, H.264 and HEVC @ 30Hz and 60Hz",
                    imageUrl = "https://www.apple.com/newsroom/images/default/apple-logo-og.jpg?202312141200"
                ),
                DemoItem(
                    title = "Apple WWDC Keynote 2023",
                    uri = "https://events-delivery.apple.com/0105cftwpxxsfrpdwklppzjhjocakrsk/m3u8/vod_index-PQsoJoECcKHTYzphNkXohHsQWACugmET.m3u8",
                    imageUrl = "https://www.apple.com/v/apple-events/home/ac/images/overview/recent-events/gallery/jun-2023__cjqmmqlyd21y_large_2x.jpg"
                ),
                DemoItem(
                    title = "Apple Dolby Atmos",
                    uri = "https://devstreaming-cdn.apple.com/videos/streaming/examples/adv_dv_atmos/main.m3u8",
                    imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/-6farfCY0YClFd7-z_qZbA/1000x563.webp"
                ),
                DemoItem(
                    title = "The Morning Show - My Way: Season 1",
                    uri = "https://play-edge.itunes.apple.com/WebObjects/MZPlayLocal.woa/hls/subscription/playlist.m3u8?cc=CH&svcId=tvs.vds.4021&a=1522121579&isExternal=true&brandId=tvs.sbd.4000&id=518077009&l=en-GB&aec=UHD",
                    imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/cZUkXfqYmSy57DBI5TiTMg/1000x563.webp"
                ),
                DemoItem(
                    title = "The Morning Show - Change: Season 2",
                    uri = "https://play-edge.itunes.apple.com/WebObjects/MZPlayLocal.woa/hls/subscription/playlist.m3u8?cc=CH&svcId=tvs.vds.4021&a=1568297173&isExternal=true&brandId=tvs.sbd.4000&id=518034010&l=en-GB&aec=UHD",
                    imageUrl = "https://is1-ssl.mzstatic.com/image/thumb/IxmmS1rQ7ouO-pKoJsVpGw/1000x563.webp"
                )
            )
        )
        private val thirdPartyStreams = Playlist(
            title = "Third-party streams",
            items = listOf(
                DemoItem(
                    title = "Brain Farm Skate Phantom Flex",
                    uri = "https://sample.vodobox.net/skate_phantom_flex_4k/skate_phantom_flex_4k.m3u8",
                    description = "4K video",
                    imageUrl = "https://i.ytimg.com/vi/d4_96ZWu3Vk/maxresdefault.jpg"
                )
            )
        )
        private val bitmovinStreams = Playlist(
            title = "Bitmovin streams streams",
            items = listOf(
                DemoItem(
                    title = "Multiple subtitles and audio tracks",
                    uri = "https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
                    imageUrl = "https://durian.blender.org/wp-content/uploads/2010/06/05.8b_comp_000272.jpg"
                ),
                DemoItem(
                    title = "4K, HEVC",
                    uri = "https://cdn.bitmovin.com/content/encoding_test_dash_hls/4k/hls/4k_profile/master.m3u8",
                    imageUrl = "https://peach.blender.org/wp-content/uploads/bbb-splash.png"
                ),
                DemoItem(
                    title = "VoD, single audio track",
                    uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8",
                    imageUrl = "https://img.redbull.com/images/c_crop,w_3840,h_1920,x_0,y_0,f_auto,q_auto/c_scale,w_1200/redbullcom/tv/FO-1MR39KNMH2111/fo-1mr39knmh2111-featuremedia"
                ),
                DemoItem(
                    title = "AES-128",
                    uri = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/m3u8s/11331.m3u8",
                    imageUrl = "https://img.redbull.com/images/c_crop,w_3840,h_1920,x_0,y_0,f_auto,q_auto/c_scale,w_1200/redbullcom/tv/FO-1MR39KNMH2111/fo-1mr39knmh2111-featuremedia"
                ),
                DemoItem(
                    title = "AVC Progressive",
                    uri = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/MI201109210084_mpeg-4_hd_high_1080p25_10mbits.mp4",
                    imageUrl = "https://img.redbull.com/images/c_crop,w_3840,h_1920,x_0,y_0,f_auto,q_auto/c_scale,w_1200/redbullcom/tv/FO-1MR39KNMH2111/fo-1mr39knmh2111-featuremedia"
                )
            )
        )
        private val unifiedStreaming = Playlist(
            title = "Unified Streaming - HLS",
            items = listOf(
                DemoItem(
                    title = "Fragmented MP4",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Key Rotation",
                    uri = "https://demo.unified-streaming.com/k8s/keyrotation/stable/keyrotation/keyrotation.isml/.m3u8",
                    imageUrl = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png"
                ),
                DemoItem(
                    title = "Alternate audio language",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Audio only",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.m3u8?filter=(type!=%22video%22)",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Trickplay",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.m3u8",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Limiting bandwidth use",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?max_bitrate=800000",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Dynamic Track Selection",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8?filter=%28type%3D%3D%22audio%22%26%26systemBitrate%3C100000%29%7C%7C%28type%3D%3D%22video%22%26%26systemBitrate%3C1024000%29",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Pure live",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8",
                    imageUrl = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png"
                ),
                DemoItem(
                    title = "Timeshift (5 minutes)",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?time_shift=300",
                    imageUrl = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png"
                ),
                DemoItem(
                    title = "Live audio",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.m3u8?filter=(type!=%22video%22)",
                    imageUrl = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png"
                ),
                DemoItem(
                    title = "Pure live (scte35)",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/scte35.isml/.m3u8",
                    imageUrl = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png"
                ),
                DemoItem(
                    title = "fMP4, clear",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-fmp4.ism/.m3u8",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "fMP4, HEVC 4K",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hevc.ism/.m3u8",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                )
            )
        )
        private val unifiedStreamingDash = Playlist(
            title = "Unified Streaming - Dash",
            items = listOf(
                DemoItem(
                    title = "MP4",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4/.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Fragmented MP4",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Trickplay",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-trickplay.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Tiled thumbnails (live/timeline)",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-tiled-thumbnails-timeline.ism/.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Single - fragmented TTML",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-en.ism/.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Multiple - fragmented TTML",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-ttml.ism/.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Multiple - RFC 5646 language tags",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-rfc5646.ism/.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Accessibility - hard of hearing",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-hoh-subs.ism/.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Pure live",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd",
                    imageUrl = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png"
                ),
                DemoItem(
                    title = "Timeshift (5 minutes)",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live.isml/.mpd?time_shift=300",
                    imageUrl = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png"
                ),
                DemoItem(
                    title = "DVB DASH low latency",
                    uri = "https://demo.unified-streaming.com/k8s/live/stable/live-low-latency.isml/.mpd",
                    imageUrl = "https://website-storage.unified-streaming.com/images/_1200x630_crop_center-center_none/default-facebook.png"
                ),
                DemoItem(
                    title = "Audio only",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd?filter=(type!=%22video%22)",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Alternate audio language",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-lang.ism/.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Multiple audio codecs",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-multi-codec.ism/.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                ),
                DemoItem(
                    title = "Accessibility - audio description",
                    uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-desc-aud.ism/.mpd",
                    imageUrl = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg"
                )
            )
        )
        private val aspectRatios = Playlist(
            title = "Aspect ratios",
            items = listOf(
                DemoItem(
                    title = "Horizontal video",
                    uri = "urn:rts:video:14827306",
                    imageUrl = "https://www.rts.ch/2024/04/10/19/23/14827621.image/16x9"
                ),
                DemoItem(
                    title = "Square video",
                    uri = "urn:rts:video:8393241",
                    imageUrl = "https://www.rts.ch/2017/02/16/07/08/8393235.image/16x9"
                ),
                DemoItem(
                    title = "Vertical video",
                    uri = "urn:rts:video:13444390",
                    imageUrl = "https://www.rts.ch/2022/10/06/17/32/13444380.image/4x5"
                )
            )
        )
        private val unbufferedStreams = Playlist(
            title = "Unbuffered streams",
            items = listOf(
                DemoItem(
                    title = "Couleur 3 en direct",
                    uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0",
                    description = "Live video (unbuffered)",
                    imageUrl = "https://www.rts.ch/2020/05/18/14/20/11333286.image/16x9"
                ),
                DemoItem(
                    title = "Couleur 3 en direct",
                    uri = "https://stream.srg-ssr.ch/m/couleur3/mp3_128",
                    description = "Audio livestream (unbuffered)",
                    imageUrl = "https://img.rts.ch/articles/2017/image/cxsqgp-25867841.image?w=320&h=320"
                )
            )
        )
        private val cornerCases = Playlist(
            title = "Corner cases",
            items = listOf(
                DemoItem(
                    title = "Expired URN",
                    uri = "urn:rts:video:13382911",
                    description = "Content that is not available anymore",
                    imageUrl = "https://www.rts.ch/2022/09/20/09/57/13365589.image/16x9"
                ),
                DemoItem(
                    title = "Unknown URN",
                    uri = "urn:srf:video:unknown",
                    description = "Content that does not exist"
                ),
                DemoItem(
                    title = "Custom MediaSource",
                    uri = "https://custom-media.ch/fondue",
                    description = "Using a custom CustomMediaSource"
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

        val EmptyPlaylist = Playlist(
            title = "Empty",
            items = emptyList(),
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
                DemoItem.Unknown,
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
