/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import java.util.Collections

/**
 * Default option like setup by Media3 cast library.
 */
class DemoCastOptionProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
            .setResumeSavedSession(false)
            .setEnableReconnectionService(false)
            .setReceiverApplicationId("A12D4273") // The one provided by ExoPlayer
            .setStopReceiverApplicationWhenEndingSession(true)
            .build()
    }

    override fun getAdditionalSessionProviders(p0: Context): MutableList<SessionProvider> {
        return Collections.emptyList()
    }
}
