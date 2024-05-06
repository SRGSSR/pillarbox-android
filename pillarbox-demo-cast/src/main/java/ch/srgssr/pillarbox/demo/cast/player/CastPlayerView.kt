/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import ch.srgssr.pillarbox.demo.cast.R
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerView
import com.google.android.gms.cast.framework.CastContext

/**
 * A player that display controls when a cast session is available.
 * The demo add a playlist of content playable by the default cast receiver.
 */
@Composable
fun CastPlayerView(
    castContext: CastContext,
    modifier: Modifier = Modifier
) {
    val castPlayer = remember {
        CastPlayer(castContext, DefaultItemConverterWithoutMimeType()).apply {
            setSessionAvailabilityListener(object : SessionAvailabilityListener {
                override fun onCastSessionAvailable() {
                    setMediaItems(
                        listOf(
                            DemoItem.OnDemandHLS.toMediaItem(),
                            DemoItem.AppleBasic_16_9_TS_HLS.toMediaItem(),
                            DemoItem.UnifiedStreamingOnDemand_Dash_FragmentedMP4.toMediaItem(),
                            DemoItem.UnifiedStreamingOnDemand_Dash_AudioOnly.toMediaItem(),
                            // Fragmented Mp4 hls doesn't work, we have to set MediaInfo..setHlsVideoSegmentFormat(HlsVideoSegmentFormat.FMP4)
                            // DemoItem.AppleAdvanced_16_9_fMP4_HLS.toMediaItem().buildUpon().setMimeType(MimeTypes.APPLICATION_M3U8).build(),
                            // For audio hls we have to specify HlsSegment format, and a mime type audio if we want the music ui.
                            // DemoItem.UnifiedStreamingOnDemandAudioOnly.toMediaItem(),
                            DemoItem.UnifiedStreamingOnDemand_Dash_PureLive.toMediaItem(),
                            DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_TTML.toMediaItem(),
                            DemoItem.UnifiedStreamingOnDemand_Dash_Timeshift.toMediaItem(),
                        )
                    )
                    prepare()
                    play()
                }

                override fun onCastSessionUnavailable() {
                    release()
                }
            })
        }
    }
    ExoPlayerView(
        modifier = modifier,
        player = castPlayer,
        defaultArtWork = R.drawable.ic_baseline_cast_connected_400,
    )
}
