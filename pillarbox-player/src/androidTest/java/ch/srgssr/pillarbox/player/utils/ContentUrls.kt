/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

object ContentUrls {
    const val VOD_HLS = "https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8"
    // From urn:swi:video:48940210
    const val VOD_MP4 = "https://cdn.prod.swi-services.ch/video-projects/141b30ce-3850-424b-9063-a20d5619d342/localised-videos/ENG/renditions/ENG.mp4"
    const val VOD_DASH_H264 = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"
    const val VOD_DASH_H265 = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd"
    const val LIVE_HLS = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0"
    const val LIVE_DVR_HLS = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8"
    const val AOD_MP3 = "https://srfaudio-a.akamaihd.net/delivery/world/af671f12-6f17-415a-9dd8-b8aee24cce8b.mp3"
    const val AUDIO_LIVE_MP3 = "https://stream.srg-ssr.ch/m/la-1ere/mp3_128"
    const val AUDIO_LIVE_DVR_HLS = "https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8"
}
