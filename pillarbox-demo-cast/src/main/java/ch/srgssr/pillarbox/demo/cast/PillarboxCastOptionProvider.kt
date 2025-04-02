/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.content.Context
import androidx.media3.cast.DefaultCastOptionsProvider
import com.google.android.gms.cast.CastMediaControlIntent
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
            .setReceiverApplicationId(ReceiverId.Letterbox)
            .setResumeSavedSession(true)
            .setEnableReconnectionService(true)
            .setRemoteToLocalEnabled(true)
            .setStopReceiverApplicationWhenEndingSession(false)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): MutableList<SessionProvider>? {
        return null
    }

    @Suppress("ConstPropertyName", "unused")
    private object ReceiverId {
        const val Cast = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
        const val Letterbox = "1AC2931D"
        const val Media3 = DefaultCastOptionsProvider.APP_ID_DEFAULT_RECEIVER_WITH_DRM
    }
}
