/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.io.Serializable

/**
 * Demo item
 *
 * @property title
 * @property uri
 * @property description
 * @property imageUrl
 */
@Suppress("UndocumentedPublicProperty")
data class DemoItem(val title: String, val uri: String, val description: String? = null, val imageUrl: String? = null) : Serializable {
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

        val GoogleDashH264 = DemoItem(
            title = "VoD - Dash (H264)",
            description = "Dash sample from Exoplayer",
            uri = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"
        )

        val GoogleDashH265 = DemoItem(
            title = "VoD - Dash (H265)",
            description = "Dash sample from Exoplayer",
            uri = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd"
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
    }
}
