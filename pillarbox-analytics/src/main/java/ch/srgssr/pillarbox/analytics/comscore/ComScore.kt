/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.BuildConfig
import com.comscore.Analytics
import com.comscore.PublisherConfiguration
import com.comscore.UsagePropertiesAutoUpdateMode
import com.comscore.util.log.LogLevel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ComScore
 *
 * Initialize ComScore before using page view by calling [ComScore.init] in your Application.create
 *
 * SRGSSR doc : https://confluence.srg.beecollaboration.com/pages/viewpage.action?pageId=13188965
 *
 * @constructor Create empty Com score
 */
internal object ComScore {
    private var config: AnalyticsConfig? = null
    private const val publisherId = "6036016"
    private val started = AtomicBoolean(false)

    /**
     * Init ComScore have to be called inside your Application.onCreate
     *
     * @param config Common analytics configuration
     * @param appContext Application context
     */
    fun init(config: AnalyticsConfig, appContext: Context): ComScore {
        if (this.config != null) {
            require(this.config == config) { "Already init with this config ${this.config}" }
            return this
        }
        this.config = config
        val persistentLabels = HashMap<String, String>()
        val versionName: String = try {
            // When unit testing from library packageInfo.versionName is null!
            appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName ?: BuildConfig.VERSION_NAME
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("COMSCORE", "Cannot find package", e)
            BuildConfig.VERSION_NAME
        }
        persistentLabels[ComScoreLabel.MP_V.label] = versionName
        persistentLabels[ComScoreLabel.MP_BRAND.label] = config.distributor.toString()
        val publisher = PublisherConfiguration.Builder()
            .publisherId(publisherId)
            .persistentLabels(persistentLabels)
            .secureTransmission(true)
            .httpRedirectCaching(false) // as described page 16 of Coding instructions extended tv SMDH version 0.7
            .build()

        Analytics.getConfiguration().addClient(publisher)
        Analytics.getConfiguration().setUsagePropertiesAutoUpdateMode(UsagePropertiesAutoUpdateMode.FOREGROUND_ONLY)
        if (!config.nonLocalizedApplicationName.isNullOrBlank()) {
            Analytics.getConfiguration().setApplicationName(config.nonLocalizedApplicationName)
        }

        Analytics.setLogLevel(if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR)
        if (BuildConfig.DEBUG) {
            Analytics.getConfiguration().enableImplementationValidationMode()
        }
        startWhenAtLeastOneActivityIsCreated(appContext)
        return this
    }

    private fun startWhenAtLeastOneActivityIsCreated(appContext: Context) {
        ComScoreStarter.startTrackingActivity(appContext.applicationContext as Application)
    }

    internal fun start(appContext: Context) {
        if (!started.getAndSet(true)) {
            checkInitialized()
            Log.i("COMSCORE", "Start")
            Analytics.start(appContext)
        }
    }

    /**
     * Send page view to ComScore
     * @param title The title of the page stored in c8.
     */
    fun sendPageView(title: String) {
        checkInitialized()
        if (!started.get()) return
        Analytics.notifyViewEvent(title.toComScoreLabel())
    }

    private fun checkInitialized() {
        requireNotNull(config) { "ComScore init has to be called before start." }
    }

    internal fun String.toComScoreLabel(): HashMap<String, String> {
        val labels = HashMap<String, String>()
        require(isNotBlank()) { "Title cannot be blank!" }
        labels[ComScoreLabel.C8.label] = this
        return labels
    }
}
