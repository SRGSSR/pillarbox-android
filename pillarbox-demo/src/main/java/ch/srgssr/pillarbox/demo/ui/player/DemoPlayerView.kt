/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.player

import android.util.Pair
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.ui.PlayerView
import ch.srg.pillarbox.core.business.integrationlayer.data.BlockReasonException
import ch.srg.pillarbox.core.business.integrationlayer.data.ResourceNotFoundException
import retrofit2.HttpException

/**
 * Demo player view demonstrate how to integrate PlayerView with Compose
 *
 * doc : https://developer.android.com/jetpack/compose/interop/interop-apis#fragments-in-compose
 *
 * @param playerViewModel
 */
@Composable
fun DemoPlayerView(playerViewModel: SimplePlayerViewModel) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PlayerView(context).also { view ->
                view.controllerAutoShow = true
                view.useController = true
                view.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                view.setErrorMessageProvider { throwable ->
                    when (val cause = throwable.cause) {
                        is BlockReasonException -> {
                            Pair.create(0, cause.blockReason)
                        }
                        is HttpException -> {
                            Pair.create(cause.code(), cause.message)
                        }
                        is ResourceNotFoundException -> {
                            Pair.create(0, "Can't find Resource to play")
                        }
                        else -> {
                            Pair.create(throwable.errorCode, "${throwable.localizedMessage} (${throwable.errorCodeName})")
                        }
                    }
                }
            }
        },
        update = { view ->
            view.player = playerViewModel.player
        }
    )
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                playerViewModel.resumePlayback()
            } else if (event == Lifecycle.Event.ON_STOP) {
                playerViewModel.pausePlayback()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
