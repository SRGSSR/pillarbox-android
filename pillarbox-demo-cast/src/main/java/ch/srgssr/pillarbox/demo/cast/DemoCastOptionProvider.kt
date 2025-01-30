/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.cast

import android.content.Context
import com.google.android.gms.cast.LaunchOptions
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

class DemoCastOptionProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        val launchOptions: LaunchOptions = LaunchOptions.Builder()
            .setAndroidReceiverCompatible(true)
            .build()
        return CastOptions.Builder()
            .setReceiverApplicationId("16509351")
            .setLaunchOptions(launchOptions)
            .build()
    }

    override fun getAdditionalSessionProviders(p0: Context): List<SessionProvider?>? {
        return null
    }
}
