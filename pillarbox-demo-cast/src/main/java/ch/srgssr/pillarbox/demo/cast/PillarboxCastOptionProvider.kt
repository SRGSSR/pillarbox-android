/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

/**
 * Pillarbox cast option provider
 * We choose to stop cast session on the receiver when leaving the application.
 */
class PillarboxCastOptionProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
            // .setReceiverApplicationId(DefaultCastOptionsProvider.APP_ID_DEFAULT_RECEIVER_WITH_DRM) // Media3
            // .setReceiverApplicationId("CC1AD845") // Default by Google
            .setReceiverApplicationId("1AC2931D") // Letterbox
            .setResumeSavedSession(true)
            .setEnableReconnectionService(false)
            .setStopReceiverApplicationWhenEndingSession(true)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): MutableList<SessionProvider>? {
        return null
    }
}
