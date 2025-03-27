/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.content.Context
import androidx.media3.cast.SessionAvailabilityListener
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Retrieves the shared instance of [CastContext].
 *
 * This function attempts to retrieve the existing shared instance of [CastContext]. If it doesn't exist, it initializes a new instance and returns
 * it.
 *
 * Call this method inside your [Activity][android.app.Activity] or [Application][android.app.Application] for early initialization. The earlier this
 * is called, the better the user experience.
 *
 * @return The shared instance of [CastContext].
 */
fun Context.getCastContext(): CastContext {
    return CastContext.getSharedInstance() ?: CastContext.getSharedInstance(this, MoreExecutors.directExecutor()).result
}

/**
 * @return if the Cast is connected.
 */
fun CastContext.isConnected(): Boolean {
    return castState == CastState.CONNECTED || castState == CastState.CONNECTING
}

/**
 * Observe the availability of a Cast session as a [Flow].
 *
 * The flow emits `true` when a Cast session is available, and `false` otherwise.
 *
 * @return A [Flow] emitting whether a Cast session is available.
 */
fun PillarboxCastPlayer.isCastSessionAvailableAsFlow(): Flow<Boolean> {
    return callbackFlow {
        val listener = object : SessionAvailabilityListener {
            override fun onCastSessionAvailable() {
                trySend(true)
            }

            override fun onCastSessionUnavailable() {
                trySend(false)
            }
        }

        send(isCastSessionAvailable())

        setSessionAvailabilityListener(listener)

        awaitClose { setSessionAvailabilityListener(null) }
    }
}
