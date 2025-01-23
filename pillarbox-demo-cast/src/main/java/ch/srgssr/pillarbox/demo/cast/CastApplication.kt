/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.app.Application
import ch.srgssr.pillarbox.cast.getCastContext
import com.google.android.gms.cast.framework.CastContext

/**
 * [CastApplication] initializes Google Cast functionality by setting up the [CastContext].
 */
class CastApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        getCastContext()
    }
}
