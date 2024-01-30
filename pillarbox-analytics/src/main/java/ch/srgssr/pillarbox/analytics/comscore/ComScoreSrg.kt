/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

import android.app.Activity
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
 * ComScore for SRG SSR
 *
 * Initialize ComScore before using page view by calling [ComScoreSrg.init] in your Application.create
 *
 * SRGSSR doc : https://confluence.srg.beecollaboration.com/pages/viewpage.action?pageId=13188965
 */
internal object ComScoreSrg : ComScore {
    private var config: AnalyticsConfig? = null
    private const val publisherId = "6036016"
    private val started = AtomicBoolean(false)

    /**
     * Init ComScore if [context] is an [Activity] we init ComScpre directly otherwise we start it when an [Activity] as been created.
     *
     * @param config Common analytics configuration
     * @param context Context context
     */
    fun init(config: AnalyticsConfig, context: Context): ComScoreSrg {
        if (this.config != null) {
            require(this.config == config) { "Already init with this config ${this.config}" }
            return this
        }
        this.config = config

        val persistentLabels = HashMap<String, String>()
        config.comScorePersistentLabels?.let { labels ->
            persistentLabels.putAll(labels)
        }

        val userConsentLabel = getUserConsentPair(config.userConsent.comScore)
        persistentLabels[userConsentLabel.first] = userConsentLabel.second

        val versionName: String = try {
            // When unit testing from library packageInfo.versionName is null!
            context.applicationContext.packageManager.getPackageInfo(context.applicationContext.packageName, 0).versionName
                ?: BuildConfig.VERSION_NAME
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("COMSCORE", "Cannot find package", e)
            BuildConfig.VERSION_NAME
        }
        persistentLabels[ComScoreLabel.MP_V.label] = versionName
        persistentLabels[ComScoreLabel.MP_BRAND.label] = config.vendor.toString()
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
        start(context.applicationContext)
        return this
    }

    internal fun start(appContext: Context) {
        if (!started.getAndSet(true)) {
            checkInitialized()
            Log.i("COMSCORE", "Start")
            Analytics.start(appContext)
        }
    }

    override fun sendPageView(pageView: ComScorePageView) {
        checkInitialized()
        if (!started.get()) return
        with(pageView.toLabels()) {
            Analytics.notifyViewEvent(this)
        }
    }

    override fun putPersistentLabels(labels: Map<String, String>) {
        val configuration = Analytics.getConfiguration().getPublisherConfiguration(publisherId)
        configuration.addPersistentLabels(labels)
    }

    override fun removePersistentLabel(label: String) {
        val configuration = Analytics.getConfiguration().getPublisherConfiguration(publisherId)
        configuration.removePersistentLabel(label)
    }

    override fun getPersistentLabel(label: String): String? {
        val configuration = Analytics.getConfiguration().getPublisherConfiguration(publisherId)
        return configuration.getPersistentLabel(label)
    }

    override fun setUserConsent(userConsent: ComScoreUserConsent) {
        putPersistentLabels(mapOf(getUserConsentPair(userConsent)))
    }

    /**
     * Values from ComScore documentation section 2.5.2
     */
    private fun getUserConsentPair(userConsent: ComScoreUserConsent): Pair<String, String> {
        val value = when (userConsent) {
            ComScoreUserConsent.ACCEPTED -> "1"
            ComScoreUserConsent.DECLINED -> "0"
            ComScoreUserConsent.UNKNOWN -> ""
        }
        return Pair(ComScoreLabel.CS_UC_FR.label, value)
    }

    private fun checkInitialized() {
        requireNotNull(config) { "ComScore init has to be called before start." }
    }
}
