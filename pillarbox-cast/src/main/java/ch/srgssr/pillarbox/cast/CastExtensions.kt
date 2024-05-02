/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastContext
import com.google.common.util.concurrent.MoreExecutors

/**
 * Get the SharedInstance of [CastContext].
 * Call this method inside Activity or Application.
 * The more sooner, the better.
 */
fun Context.getCastContext(): CastContext {
    return CastContext.getSharedInstance() ?: CastContext.getSharedInstance(this, MoreExecutors.directExecutor()).result
}
