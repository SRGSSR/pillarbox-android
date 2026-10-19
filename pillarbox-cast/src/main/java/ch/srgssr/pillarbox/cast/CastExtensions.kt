/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import com.google.common.util.concurrent.MoreExecutors

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
