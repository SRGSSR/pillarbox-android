/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.AnalyticsDelegate
import ch.srgssr.pillarbox.analytics.BuildConfig
import ch.srgssr.pillarbox.analytics.Event
import ch.srgssr.pillarbox.analytics.PageEvent
import com.comscore.Analytics
import com.comscore.PublisherConfiguration
import com.comscore.UsagePropertiesAutoUpdateMode
import com.comscore.util.log.LogLevel

/**
 * ComSscore
 *
 * SRGSSR doc : https://confluence.srg.beecollaboration.com/pages/viewpage.action?pageId=13188965
 *
 * @constructor Create empty Com score
 */
object ComScore : AnalyticsDelegate {
    private var config: AnalyticsConfig? = null
    private const val NS_AP_AN = "ns_ap_an"
    private const val MP_V = "mp_v"
    private const val MP_BRAND = "mp_brand"
    private const val PAGE_NAME = "name"
    private const val PAGE_TITLE = "srg_title"
    private const val PAGE_CATEGORY = "ns_category"
    private const val PAGE_LEVEL_PREFIX = "srg_n%s"
    private const val DEFAULT_LEVEL_1 = "app"
    private const val MAX_LEVEL: Int = 10
    private const val CATEGORY_SEPARATOR = "."

    /**
     * Configuration for ComScore
     */
    class Config {
        /**
         * Publisher id received from MediaPulse for the SRG SSR.
         */
        val publisherId = "6036016"
    }

    internal fun init(config: AnalyticsConfig, comScoreConfig: Config, appContext: Context): ComScore {
        if (this.config != null) {
            return this
        }
        this.config = config
        val persistentLabels = HashMap<String, String>()
        if (!config.nonLocalizedApplicationName.isNullOrBlank()) {
            persistentLabels[NS_AP_AN] = config.nonLocalizedApplicationName
        }
        try {
            persistentLabels[MP_V] = appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("COMSCORE", "Cannot find package", e)
        }
        persistentLabels[MP_BRAND] = config.distributor.toString()
        val publisher = PublisherConfiguration.Builder()
            .publisherId(comScoreConfig.publisherId)
            .persistentLabels(persistentLabels)
            .secureTransmission(true)
            .httpRedirectCaching(false) // as described page 16 of Coding instructions extended tv SMDH version 0.7
            .build()

        Analytics.getConfiguration().addClient(publisher)
        Analytics.getConfiguration().setUsagePropertiesAutoUpdateMode(UsagePropertiesAutoUpdateMode.FOREGROUND_AND_BACKGROUND)
        Analytics.setLogLevel(if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR)
        if (BuildConfig.DEBUG) {
            Analytics.getConfiguration().enableImplementationValidationMode()
        }
        start(appContext)
        return this
    }

    private fun start(appContext: Context): ComScore {
        checkInitialized()
        Analytics.start(appContext)
        return this
    }

    override fun sendPageViewEvent(pageEvent: PageEvent) {
        checkInitialized()
        val labels = HashMap<String, String>()
        labels[PAGE_TITLE] = pageEvent.title

        fillLevel(labels, pageEvent.levels)

        val category = getCategory(pageEvent.levels)
        labels[PAGE_CATEGORY] = category
        labels[PAGE_NAME] = "$category.${pageEvent.title}"

        pageEvent.customLabels?.let {
            labels.putAll(it)
        }
        Analytics.notifyViewEvent(labels)
    }

    override fun sendEvent(event: Event) {
        assert(false) { "Not implemented" }
    }

    private fun checkInitialized() {
        requireNotNull(config) { "ComScore init has to be called before start." }
    }

    /**
     * Fill the first 10 levels of [levels] into [labels]
     *
     * @param labels where to put result
     * @param levels levels to fill. Only the 10 first are filled.
     */
    fun fillLevel(labels: HashMap<String, String>, levels: Array<String>) {
        val filteredLevels = levels.filter { it.isNotBlank() }
        if (filteredLevels.isEmpty()) {
            labels[PAGE_LEVEL_PREFIX.format(1)] = DEFAULT_LEVEL_1
            return
        }
        val count = filteredLevels.size.coerceAtMost(MAX_LEVEL)
        for (i in 0 until count) {
            labels[PAGE_LEVEL_PREFIX.format(i + 1)] = filteredLevels[i]
        }
    }

    /**
     * Get category
     *
     * @param levels levels to format.
     * @return levels separated with a .
     */
    fun getCategory(levels: Array<String>): String {
        val filteredLevels = levels.filter { it.isNotBlank() }
        if (filteredLevels.isEmpty()) {
            return DEFAULT_LEVEL_1
        }
        return filteredLevels.joinToString(separator = CATEGORY_SEPARATOR)
    }
}
