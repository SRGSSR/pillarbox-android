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
import androidx.media3.common.MimeTypes
import ch.srgssr.pillarbox.cast.PillarboxCastPlayer
import ch.srgssr.pillarbox.cast.getCastContext
import ch.srgssr.pillarbox.cast.widget.CastButton
import ch.srgssr.pillarbox.demo.cast.ui.theme.PillarboxTheme
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
                    @Suppress("MaximumLineLength", "MaxLineLength")
                    val mediaItems = listOf(
                        "https://cdn.prod.swi-services.ch/video-projects/94f5f5d1-5d53-4336-afda-9198462c45d9/localised-videos/ENG/renditions/ENG.mp4",
                        "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/MI201109210084_mpeg-4_hd_high_1080p25_10mbits.mp4",
                        "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4/.mpd",
                    ).map {
                        MediaItem.Builder()
                            .setMimeType(MimeTypes.VIDEO_MP4)
                            .setUri(it)
                            .build()
                    }

                    setSessionAvailabilityListener(object : SessionAvailabilityListener {
                        override fun onCastSessionAvailable() {
                            setMediaItems(mediaItems)
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
                        showShuffleButton = true,
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
