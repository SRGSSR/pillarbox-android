/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo

import android.content.Context
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettingsRepository
import com.google.android.gms.cast.LaunchOptions
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Pillarbox cast option provider
 * We choose to stop cast session on the receiver when leaving the application.
 */
class DemoCastOptionProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        val settings = runBlocking {
            AppSettingsRepository(context).getAppSettings().first()
        }
        val launchOptions: LaunchOptions = LaunchOptions.Builder()
            .setAndroidReceiverCompatible(settings.receiverApplicationId == AppSettings.ReceiverId.Tv)
            .build()
        return CastOptions.Builder()
            .setReceiverApplicationId(settings.receiverApplicationId)
            .setLaunchOptions(launchOptions)
            .setResumeSavedSession(false)
            .setEnableReconnectionService(false)
            .setStopReceiverApplicationWhenEndingSession(true)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): MutableList<SessionProvider>? {
        return null
    }
}
