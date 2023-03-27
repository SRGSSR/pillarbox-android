/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

import android.content.Context
import android.content.pm.PackageManager
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.AnalyticsDelegate
import ch.srgssr.pillarbox.analytics.BuildConfig
import ch.srgssr.pillarbox.analytics.Event
import ch.srgssr.pillarbox.analytics.PageEvent
import com.comscore.Analytics
import com.comscore.PublisherConfiguration
import com.comscore.UsagePropertiesAutoUpdateMode
import com.comscore.util.log.LogLevel

object ComScore : AnalyticsDelegate {
    private var config: AnalyticsConfig? = null
    private const val NS_AP_AN = "ns_ap_an"
    private const val MP_V = "mp_v"
    private const val MP_BRAND = "mp_brand"
    private const val PAGE_NAME = "name"
    private const val PAGE_TITLE = "srg_title"
    private const val PAGE_CATEGORY = "ns_category"
    private const val PAGE_LEVEL_PREFIX = "srg_n"
    private const val DEFAULT_LEVEL_1 = "app"

    fun init(config: AnalyticsConfig, appContext: Context): ComScore {
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
            e.printStackTrace()
        }
        persistentLabels[MP_BRAND] = config.distributor.toString()
        val publisher = PublisherConfiguration.Builder()
            .publisherId("10001") // appContext.getString(R.string.comscore_customer_c2)
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
        var safeTitle = SRGPageId.normalizeTitle(pageEvent.title)
        val labels = HashMap<String, String>()
        labels[PAGE_TITLE] = safeTitle
        safeTitle = SRGPageId.normalize(safeTitle)
        fillLevel(labels, pageEvent.levels)
        val category = SRGPageId.getCategory(pageEvent.levels).toString()
        labels[PAGE_CATEGORY] = category
        labels[PAGE_NAME] = "$category.$safeTitle"

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
     * Fill the first 10 levels of [levels]
     *
     * @param labels
     * @param levels
     */
    private fun fillLevel(labels: HashMap<String, String>, levels: Array<String>) {
        if (levels.isEmpty()) {
            labels[PAGE_LEVEL_PREFIX + "1"] = DEFAULT_LEVEL_1
            return
        }
        // Take only first 10 levels
        val first10Level = levels.filterIndexed { index, _ -> index <= 10 }
        for (i in first10Level.indices) {
            val normalizedLeveli = SRGPageId.normalize(levels[i])
            if (normalizedLeveli.isNotBlank()) {
                labels[PAGE_LEVEL_PREFIX + i] = normalizedLeveli
            }
        }
    }
}
