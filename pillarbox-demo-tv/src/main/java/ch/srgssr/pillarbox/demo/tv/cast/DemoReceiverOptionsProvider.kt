/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.tv.cast

import android.content.Context
import ch.srgssr.pillarbox.demo.shared.ui.settings.AppSettings
import com.google.android.gms.cast.tv.CastReceiverOptions
import com.google.android.gms.cast.tv.ReceiverOptionsProvider

/**
 * Demo receiver options provider
 */
class DemoReceiverOptionsProvider : ReceiverOptionsProvider {
    override fun getOptions(context: Context): CastReceiverOptions {
        return CastReceiverOptions.Builder(context)
            .setCastAppId(AppSettings.Tv)
            .setVersionCode(1)
            .setStatusText("Pillarbox Cast receiver TV")
            .build()
    }
}
