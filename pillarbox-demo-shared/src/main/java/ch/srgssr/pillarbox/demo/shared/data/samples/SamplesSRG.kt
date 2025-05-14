/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist

/**
 * Samples from SRG SSR.
 */
@Suppress("StringLiteralDuplication", "MaximumLineLength", "MaxLineLength", "UndocumentedPublicProperty")
object SamplesSRG {
    val STXT_DashOriginL1 = DemoItem.URL(
        title = "DRM Widevine (Origin, L1)",
        description = "DRM, licensed Android",
        uri = "https://15595674c0604ed6.mediapackage.eu-central-1.amazonaws.com/out/v1/9a17611e28b54641b169ae4735ee408d/index.mpd",
        licenseUri = "https://srg.stage.ott.irdeto.com/licenseServer/widevine/v1/SRG/license?contentId=FULLHD",
        multiSession = true,
        imageUri = "https://www.rts.ch/2024/06/13/11/34/14970435.image/16x9",
        languageTag = "fr-CH",
    )

    val STXT_DashOriginSDOnly = DemoItem.URL(
        title = "DRM Widevine SD (Origin, L1)",
        description = "DRM, all other Android, only SD quality",
        uri = "https://15595674c0604ed6.mediapackage.eu-central-1.amazonaws.com/out/v1/9a17611e28b54641b169ae4735ee408d/index.mpd?aws.manifestfilter=video_height:180-540",
        licenseUri = "https://srg.stage.ott.irdeto.com/licenseServer/widevine/v1/SRG/license?contentId=FULLHD",
        multiSession = true,
        imageUri = "https://www.rts.ch/2024/06/13/11/34/14970435.image/16x9",
        languageTag = "fr-CH",
    )

    val STXT_DashCDNL1 = DemoItem.URL(
        title = "DRM Widevine (CDN, L1)",
        description = "DRM, licensed Android",
        uri = "https://rsila1-lsvs-1080p.akamaized-staging.net/out/v1/9a17611e28b54641b169ae4735ee408d/index.mpd",
        licenseUri = "https://srg.stage.ott.irdeto.com/licenseServer/widevine/v1/SRG/license?contentId=FULLHD",
        multiSession = true,
        imageUri = "https://www.rts.ch/2024/06/13/11/34/14970435.image/16x9",
        languageTag = "fr-CH",
    )

    val STXT_DashCDNSDOnly = DemoItem.URL(
        title = "DRM Widevine SD (CDN, L1)",
        description = "DRM, all other Android, only SD quality",
        uri = "https://rsila1-lsvs-1080p.akamaized-staging.net/out/v1/9a17611e28b54641b169ae4735ee408d/index.mpd?aws.manifestfilter=video_height:180-540",
        licenseUri = "https://srg.stage.ott.irdeto.com/licenseServer/widevine/v1/SRG/license?contentId=FULLHD",
        multiSession = true,
        imageUri = "https://www.rts.ch/2024/06/13/11/34/14970435.image/16x9",
        languageTag = "fr-CH",
    )

    val STXT_DashOriginL3 = DemoItem.URL(
        title = "DRM Widevine (Origin, L3)",
        description = "DRM, licensed Android",
        uri = "https://15595674c0604ed6.mediapackage.eu-central-1.amazonaws.com/out/v1/15aa07daf6c34e969b164b6df4a815aa/index.mpd",
        licenseUri = "https://srg.stage.ott.irdeto.com/licenseServer/widevine/v1/SRG/license?contentId=FULLHDL3",
        multiSession = true,
        imageUri = "https://www.rts.ch/2024/06/13/11/34/14970435.image/16x9",
        languageTag = "fr-CH",
    )

    val STXT_DashCDNL3 = DemoItem.URL(
        title = "DRM Widevine (CDN, L3)",
        description = "DRM, licensed Android",
        uri = "https://rsila1-lsvs-1080p.akamaized-staging.net/out/v1/15aa07daf6c34e969b164b6df4a815aa/index.mpd",
        licenseUri = "https://srg.stage.ott.irdeto.com/licenseServer/widevine/v1/SRG/license?contentId=FULLHDL3",
        multiSession = true,
        imageUri = "https://www.rts.ch/2024/06/13/11/34/14970435.image/16x9",
        languageTag = "fr-CH",
    )

    val OnDemandHLS = DemoItem.URL(
        title = "Sacha part à la rencontre d'univers atypiques",
        uri = "https://rts-vod-amd.akamaized.net/ww/14970442/da2b38fb-ca9f-3c76-80c6-e6fa7f3c2699/master.m3u8",
        description = "VOD - HLS",
        imageUri = "https://www.rts.ch/2024/06/13/11/34/14970435.image/16x9",
        languageTag = "fr-CH",
    )

    val ShortOnDemandVideoHLS = DemoItem.URL(
        title = "Des violents orages ont touché Ajaccio, chef-lieu de la Corse, jeudi",
        uri = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8",
        description = "VOD - HLS (short)",
        imageUri = "https://www.rts.ch/2022/08/18/12/38/13317144.image/16x9",
        languageTag = "fr-CH",
    )

    // urn:swi:video:48498670
    val OnDemandVideoMP4 = DemoItem.URL(
        title = "Swiss wheelchair athlete wins top award",
        uri = "https://cdn.prod.swi-services.ch/video-projects/94f5f5d1-5d53-4336-afda-9198462c45d9/localised-videos/ENG/renditions/ENG.mp4",
        description = "VOD - MP4",
        imageUri = "https://cdn.prod.swi-services.ch/video-delivery/images/94f5f5d1-5d53-4336-afda-9198462c45d9/_.1hAGinujJ.yERGrrGNzBGCNSxmhKZT/16x9",
        languageTag = "en-CH",
    )

    val LiveVideoHLS = DemoItem.URL(
        title = "Couleur 3 en vidéo (live)",
        uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0",
        description = "Video livestream - HLS",
        imageUri = "https://img.rts.ch/audio/2010/image/924h3y-25865853.image?w=640&h=640",
        languageTag = "fr-CH",
    )

    val DvrVideoHLS = DemoItem.URL(
        title = "Couleur 3 en vidéo (DVR)",
        uri = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8",
        description = "Video livestream with DVR - HLS",
        imageUri = "https://il.srgssr.ch/images/?imageUrl=https%3A%2F%2Fwww.rts.ch%2F2020%2F05%2F18%2F14%2F20%2F11333286.image%2F16x9&format=jpg&width=960",
        languageTag = "fr-CH",
    )

    val OnDemandAudioMP3 = DemoItem.URL(
        title = "On en parle",
        uri = "https://rts-aod-dd.akamaized.net/ww/13306839/63cc2653-8305-3894-a448-108810b553ef.mp3",
        description = "AOD - MP3",
        imageUri = "https://www.rts.ch/2023/09/28/17/49/11872957.image?w=624&h=351",
        languageTag = "fr-CH",
    )

    val LiveAudioMP3 = DemoItem.URL(
        title = "Couleur 3 (live)",
        uri = "https://stream.srg-ssr.ch/m/couleur3/mp3_128",
        description = "Audio livestream - MP3",
        imageUri = "https://www.rts.ch/2020/05/18/14/20/11333286.image/16x9",
        languageTag = "fr-CH",
    )

    val DvrAudioHLS = DemoItem.URL(
        title = "Couleur 3 (DVR)",
        uri = "https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8",
        description = "Audio livestream - HLS",
        imageUri = "https://www.rts.ch/2020/05/18/14/20/11333286.image/16x9",
        languageTag = "fr-CH",
    )

    val OnDemandHorizontalVideo = DemoItem.URN(
        title = "Horizontal video",
        urn = "urn:rts:video:14827306",
        imageUri = "https://www.rts.ch/2024/04/10/19/23/14827621.image/16x9",
        languageTag = "en-CH",
    )

    val OnDemandSquareVideo = DemoItem.URN(
        title = "Square video",
        urn = "urn:rts:video:8393241",
        imageUri = "https://www.rts.ch/2017/02/16/07/08/8393235.image/16x9",
        languageTag = "en-CH",
    )

    val OnDemandVerticalVideo = DemoItem.URN(
        title = "Vertical video",
        urn = "urn:rts:video:13444390",
        imageUri = "https://www.rts.ch/2022/10/06/17/32/13444380.image/4x5",
        languageTag = "en-CH",
    )

    val DvrVideo = DemoItem.URN(
        title = "RTS 1",
        urn = "urn:rts:video:3608506",
        description = "DVR video livestream",
        imageUri = "https://www.rts.ch/2023/09/06/14/43/14253742.image/16x9",
        languageTag = "fr-CH",
    )

    val DvrAudio = DemoItem.URN(
        title = "Couleur 3 (DVR)",
        urn = "urn:rts:audio:3262363",
        description = "DVR audio livestream",
        imageUri = "https://www.rts.ch/2020/05/18/14/20/11333286.image/16x9",
        languageTag = "fr-CH",
    )

    val SuperfluouslyTokenProtectedVideo = DemoItem.URN(
        title = "Telegiornale flash",
        urn = "urn:rsi:video:2660088",
        description = "Superfluously token-protected video",
        imageUri = "https://il.rsi.ch/rsi-api/resize/image/v2/EPISODE_IMAGE/2693192",
        languageTag = "it-CH",
    )

    val LiveVideo = DemoItem.URN(
        title = "SRF 1",
        urn = "urn:srf:video:c4927fcf-e1a0-0001-7edd-1ef01d441651",
        description = "Live video",
        imageUri = "https://ws.srf.ch/asset/image/audio/d91bbe14-55dd-458c-bc88-963462972687/EPISODE_IMAGE",
        languageTag = "de-CH",
    )

    val OnDemandAudio = DemoItem.URN(
        title = "Nachrichten von 08:00 Uhr - 08.03.2024",
        urn = "urn:srf:audio:b9706015-632f-4e24-9128-5de074d98eda",
        description = "On-demand audio stream",
        languageTag = "de-CH",
    )

    val Expired = DemoItem.URN(
        title = "Expired URN",
        urn = "urn:rts:video:13382911",
        description = "Content that is not available anymore",
        languageTag = "en-CH",
    )

    val Unknown = DemoItem.URN(
        title = "Unknown URN",
        urn = "urn:srf:video:unknown",
        description = "Content that does not exist",
        languageTag = "en-CH",
    )

    val BlockedSegment = DemoItem.URN(
        title = "Blocked segment at 29:26",
        urn = "urn:srf:video:40ca0277-0e53-4312-83e2-4710354ff53e",
        imageUri = "https://ws.srf.ch/asset/image/audio/f1a1ab5d-c009-4ba1-aae0-a2be5b89edd9/EPISODE_IMAGE/1465482801.png",
        languageTag = "en-CH",
    )

    val OverlapinglockedSegments = DemoItem.URN(
        title = "Overlapping segments",
        urn = "urn:srf:video:d57f5c1c-080f-49a2-864e-4a1a83e41ae1",
        imageUri = "https://ws.srf.ch/asset/image/audio/75c3d4a4-4357-4703-b407-2d076aa15fd7/EPISODE_IMAGE/1384985072.png",
        languageTag = "en-CH",
    )

    val MultiAudioWithAccessibility = DemoItem.URN(
        title = "Bonjour la Suisse (5/5) - Que du bonheur?",
        description = "Multi audio with AD track",
        urn = "urn:rts:video:8806923",
        imageUri = "https://www.rts.ch/2017/07/28/21/11/8806915.image/16x9",
        languageTag = "en-CH",
    )

    val Tataki_1 = DemoItem.URN(
        title = "Mario vs Sonic",
        description = "Tataki 1",
        urn = "urn:rts:video:13950405",
        languageTag = "fr-CH",
    )
    val Tataki_2 = DemoItem.URN(
        title = "Pourquoi Beyoncé fait de la country",
        description = "Tataki 2",
        urn = "urn:rts:video:14815579",
        languageTag = "fr-CH",
    )
    val Tataki_3 = DemoItem.URN(
        title = "L'île North Sentinel",
        description = "Tataki 3",
        urn = "urn:rts:video:13795051",
        languageTag = "fr-CH",
    )
    val Tataki_4 = DemoItem.URN(
        title = "Mourir pour ressembler à une idole",
        description = "Tataki 4",
        urn = "urn:rts:video:14020134",
        languageTag = "fr-CH",
    )
    val Tataki_5 = DemoItem.URN(
        title = "Pourquoi les gens mangent des insectes ?",
        description = "Tataki 5",
        urn = "urn:rts:video:12631996",
        languageTag = "fr-CH",
    )
    val Tataki_6 = DemoItem.URN(
        title = "Le concert de Beyoncé à Dubai",
        description = "Tataki 6",
        urn = "urn:rts:video:13752646",
        languageTag = "fr-CH",
    )

    val StoryVideoUrls = Playlist(
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

    val StoryVideoUrns = Playlist(
        title = "Video urns",
        items = listOf(
            Tataki_1,
            Tataki_2,
            Tataki_3,
            Tataki_4,
            Tataki_5,
            Tataki_6,
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

    val StreamUrls = Playlist(
        title = "SRG SSR streams (URLs)",
        languageTag = "en-CH",
        items = listOf(
            STXT_DashOriginL1,
            STXT_DashOriginSDOnly,
            STXT_DashOriginL3,
            STXT_DashCDNL1,
            STXT_DashCDNSDOnly,
            STXT_DashCDNL3,
            OnDemandHLS,
            ShortOnDemandVideoHLS,
            OnDemandVideoMP4,
            LiveVideoHLS,
            DvrVideoHLS,
            OnDemandAudioMP3,
            LiveAudioMP3,
            DvrAudioHLS,
        ) + StoryVideoUrls.items
    )

    val StreamUrns = Playlist(
        title = "SRG SSR streams (URNs)",
        languageTag = "en-CH",
        items = listOf(
            OnDemandHorizontalVideo,
            OnDemandSquareVideo,
            OnDemandVerticalVideo,
            DvrVideo,
            DvrAudio,
            SuperfluouslyTokenProtectedVideo,
            LiveVideo,
            OnDemandAudio,
            Expired,
            Unknown,
            BlockedSegment,
            OverlapinglockedSegments,
            MultiAudioWithAccessibility,
        ) + StoryVideoUrns.items,
    )
}
