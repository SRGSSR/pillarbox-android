/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.cast.widget.CastButton
import ch.srgssr.pillarbox.demo.cast.ui.theme.PillarboxTheme
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.ui.exoplayer.ExoPlayerView

/**
 * Activity showing how to use Cast with Pillarbox.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val player = remember {
                PillarboxCastPlayer(getCastContext(), this).apply {
                    setSessionAvailabilityListener(object : SessionAvailabilityListener {
                        override fun onCastSessionAvailable() {
                            setMediaItems(
                                listOf(
                                    MediaItem.Builder()
                                        .setMimeType(MimeTypes.APPLICATION_MPD)
                                        .setUri(DemoItem.UnifiedStreamingOnDemand_Dash_Multiple_TTML.uri)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle("Google DASH H265")
                                                .build()
                                        )
                                        .build(),
                                    MediaItem.Builder()
                                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                                        .setUri(DemoItem.AppleAdvanced_16_9_TS_HLS.uri)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle("Google HLS subtitles")
                                                .build()
                                        )
                                        .build(),
                                    MediaItem.Builder()
                                        .setMimeType(MimeTypes.VIDEO_MP4)
                                        .setUri(DemoItem.GoogleDashH264.uri)
                                        .setMediaMetadata(
                                            MediaMetadata.Builder()
                                                .setTitle("Google DASH H264")
                                                .build()
                                        )
                                        .build(),
                                )
                            )
                        }

                        override fun onCastSessionUnavailable() {
                            release()
                        }
                    })
                }
            }

            PillarboxTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        CastButton()
                    },
                ) { innerPadding ->
                    ExoPlayerView(
                        player = player,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        setupView = {
                            setShowShuffleButton(true)
                            setShowSubtitleButton(true)
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MainPreview() {
    PillarboxTheme {
        CastButton()
    }
}
