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
import ch.srgssr.pillarbox.analytics.PageView
import ch.srgssr.pillarbox.analytics.PageViewAnalytics
import com.comscore.Analytics
import com.comscore.PublisherConfiguration
import com.comscore.UsagePropertiesAutoUpdateMode
import com.comscore.util.log.LogLevel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ComScore
 *
 * SRGSSR doc : https://confluence.srg.beecollaboration.com/pages/viewpage.action?pageId=13188965
 *
 * @constructor Create empty Com score
 */
internal object ComScore : PageViewAnalytics {
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
    private const val publisherId = "6036016"

    /**
     * Custom label Key for push notification source
     */
    private const val KEY_FROM_PUSH_NOTIFICATION = "srg_ap_push"

    private val started = AtomicBoolean(false)

    /**
     * Init ComScore have to be called inside Application.onCreate
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
        if (!config.nonLocalizedApplicationName.isNullOrBlank()) {
            persistentLabels[NS_AP_AN] = config.nonLocalizedApplicationName
        }
        val versionName: String = try {
            // When unit testing from library packageInfo.versionName is null!
            appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName ?: BuildConfig.VERSION_NAME
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("COMSCORE", "Cannot find package", e)
            BuildConfig.VERSION_NAME
        }
        persistentLabels[MP_V] = versionName
        persistentLabels[MP_BRAND] = config.distributor.toString()
        val publisher = PublisherConfiguration.Builder()
            .publisherId(publisherId)
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
        ComScoreStarter.startTrackingActivity(appContext as Application)
        if (ComScoreStarter.isUiExperienceStarted) {
            start(appContext)
        }
        return this
    }

    internal fun start(appContext: Context) {
        if (!started.getAndSet(true)) {
            checkInitialized()
            Log.i("COMSCORE", "Start")
            Analytics.start(appContext)
        }
    }

    override fun sendPageView(pageView: PageView) {
        checkInitialized()
        if (!started.get()) return
        Analytics.notifyViewEvent(pageView.toComScoreLabel())
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

    internal fun PageView.toComScoreLabel(): HashMap<String, String> {
        val labels = HashMap<String, String>()
        require(title.isNotBlank()) { "Title cannot be blank!" }
        labels[PAGE_TITLE] = title

        fillLevel(labels, levels)

        val category = getCategory(levels)
        labels[PAGE_CATEGORY] = category
        labels[PAGE_NAME] = "$category.$title"
        labels[KEY_FROM_PUSH_NOTIFICATION] = fromPushNotification.toString()
        return labels
    }
}
