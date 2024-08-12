/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.player.metrics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.Format
import ch.srgssr.pillarbox.demo.shared.ui.settings.MetricsOverlayOptions
import ch.srgssr.pillarbox.player.analytics.metrics.PlaybackMetrics
import ch.srgssr.pillarbox.player.utils.BitrateUtil.toByteRate

/**
 * Display [playbackMetrics] as overlay.
 *
 * @param playbackMetrics The [PlaybackMetrics] to display.
 * @param overlayOptions The [MetricsOverlayOptions] the options.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun MetricsOverlay(
    playbackMetrics: PlaybackMetrics,
    overlayOptions: MetricsOverlayOptions,
    modifier: Modifier = Modifier,
) {
    val currentVideoFormat = playbackMetrics.videoFormat
    val currentAudioFormat = playbackMetrics.audioFormat
    Column(modifier = modifier) {
        currentVideoFormat?.let {
            OverlayText(
                overlayOptions = overlayOptions,
                text = "video format codecs:${it.codecs} ${it.bitrate.toByteRate()}Bps frame-rate:${it.frameRate}"
            )
        }
        currentAudioFormat?.let {
            OverlayText(
                overlayOptions = overlayOptions,
                text = "audio format codes:${it.codecs} ${it.bitrate.toByteRate()}Bps channels=${it.channelCount} sample-rate:${it.sampleRate}Hz"
            )
        }

        val averageBitRateString = StringBuilder("average bitrate ")
        currentVideoFormat?.getAverageBitrateOrNull()?.let {
            averageBitRateString.append("video:${it.toByteRate()}Bps ")
        }
        currentAudioFormat?.getAverageBitrateOrNull()?.let {
            averageBitRateString.append("audio:${it.toByteRate()}Bps")
        }
        OverlayText(text = averageBitRateString.toString(), overlayOptions = overlayOptions)

        val peekBitrateString = StringBuilder("peek bitrate ")
        currentVideoFormat?.getPeekBitrateOrNull()?.let {
            peekBitrateString.append("video:${it.toByteRate()}Bps ")
        }
        currentAudioFormat?.getPeekBitrateOrNull()?.let {
            peekBitrateString.append("audio:${it.toByteRate()}Bps")
        }
        OverlayText(text = peekBitrateString.toString(), overlayOptions = overlayOptions)

        OverlayText(
            overlayOptions = overlayOptions,
            text = "indicated bitrate: ${playbackMetrics.indicatedBitrate.toByteRate()}Bps"
        )
        OverlayText(
            overlayOptions = overlayOptions,
            text = "bandwidth ${playbackMetrics.bandwidth.toByteRate()}Bps"
        )
        OverlayText(
            overlayOptions = overlayOptions,
            text = "asset: ${playbackMetrics.loadDuration.asset}"
        )
        OverlayText(
            overlayOptions = overlayOptions,
            text = "drm: ${playbackMetrics.loadDuration.drm}"
        )
        OverlayText(
            overlayOptions = overlayOptions,
            text = "manifest: ${playbackMetrics.loadDuration.manifest}"
        )
        OverlayText(
            overlayOptions = overlayOptions,
            text = "source: ${playbackMetrics.loadDuration.source}"
        )
        OverlayText(
            overlayOptions = overlayOptions,
            text = "timeToReady: ${playbackMetrics.loadDuration.timeToReady}"
        )

        OverlayText(
            overlayOptions = overlayOptions,
            text = "playtime: ${playbackMetrics.playbackDuration}"
        )
    }
}

@Composable
private fun OverlayText(
    text: String,
    overlayOptions: MetricsOverlayOptions,
    modifier: Modifier = Modifier
) {
    BasicText(
        modifier = modifier,
        style = overlayOptions.textStyle.copy(
            shadow = Shadow(
                color = Color.Black,
                blurRadius = 4f,
            ),
        ),
        color = { overlayOptions.textColor },
        text = text,
    )
}

@Preview
@Composable
private fun OverlayTextPreview() {
    val overlayOptions = MetricsOverlayOptions()
    OverlayText(text = "Text; 12 ac1.mp3 channels:4 colors:4", overlayOptions = overlayOptions)
}

private fun Format.getPeekBitrateOrNull(): Int? {
    return if (peakBitrate == Format.NO_VALUE) null else peakBitrate
}

private fun Format.getAverageBitrateOrNull(): Int? {
    return if (averageBitrate == Format.NO_VALUE) null else averageBitrate
}
