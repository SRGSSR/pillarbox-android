/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.utils

object ContentUrls {
    const val VOD_HLS = "https://swi-vod.akamaized.net/videoJson/47603186/master.m3u8"
    const val VOD_MP4 = "https://media.swissinfo.ch/media/video/dddaff93-c2cd-4b6e-bdad-55f75a519480/rendition/154a844b-de1d-4854-93c1-5c61cd07e98c.mp4"
    const val VOD_DASH_H264 = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"
    const val VOD_DASH_H265 = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd"
    const val LIVE_HLS = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8?dw=0"
    const val LIVE_DVR_HLS = "https://rtsc3video.akamaized.net/hls/live/2042837/c3video/3/playlist.m3u8"
    const val AOD_MP3 = "https://srfaudio-a.akamaihd.net/delivery/world/6adee39a-e56e-4b22-9e38-2620ef5556ec.mp3"
    const val AUDIO_LIVE_MP3 = "https://stream.srg-ssr.ch/m/la-1ere/mp3_128"
    const val AUDIO_LIVE_DVR_HLS = "https://lsaplus.swisstxt.ch/audio/couleur3_96.stream/playlist.m3u8"
}
