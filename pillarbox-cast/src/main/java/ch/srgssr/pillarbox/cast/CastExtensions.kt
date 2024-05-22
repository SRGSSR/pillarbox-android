/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastContext
import com.google.common.util.concurrent.MoreExecutors

/**
 * Get the shared instance of [CastContext] if available, or initialize a new one.
 * Call this method inside your [Activity][android.app.Activity] or [Application][android.app.Application].
 * The sooner, the better.
 */
fun Context.getCastContext(): CastContext {
    return CastContext.getSharedInstance() ?: CastContext.getSharedInstance(this, MoreExecutors.directExecutor()).result
}
