/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

/**
 * Playlist
 *
 * @property title
 * @property items
 * @property description optional
 */
@Suppress("UndocumentedPublicProperty")
data class Playlist(val title: String, val items: List<DemoItem>, val description: String? = null) : java.io.Serializable {
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
                DemoItem.ShortOnDemandVideoHLS
            )
        )

        val StreamUrls = Playlist(
            title = "Media with urls",
            items = listOf(
                DemoItem.OnDemandHLS,
                DemoItem.ShortOnDemandVideoHLS,
                DemoItem.OnDemandVideoMP4,
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
                DemoItem.TokenProtectedVideo,
                DemoItem.SuperfluouslyTokenProtectedVideo,
                DemoItem.DrmProtectedVideo,
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
            )
        )

        val StreamGoogles = Playlist(
            title = "Google samples",
            items = listOf(
                DemoItem.GoogleDashH264,
                DemoItem.GoogleDashH265,
            )
        )

        val All = Playlist(
            title = "Standart items",
            items = StreamUrls.items + StreamUrns.items + VideoUrns.items + StreamGoogles.items + StreamApples.items
        )
    }
}
