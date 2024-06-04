/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import com.comscore.Analytics
import com.comscore.PublisherConfiguration
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class ComScoreSrgTest {
    private val config = AnalyticsConfig(
        vendor = AnalyticsConfig.Vendor.SRG,
        appSiteName = "pillarbox-test-android",
        sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG
    )

    private lateinit var context: Context

    @BeforeTest
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        shadowOf(context.packageManager).getInternalMutablePackageInfo(context.packageName).versionName = "1.2.3"

        mockkStatic(Analytics::class)
        ComScoreSrg.init(config = config, context = context)
        ComScoreSrg.start(context)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `init a second time with other config should throw exception`() {
        ComScoreSrg.init(config = config.copy(vendor = AnalyticsConfig.Vendor.RTS), context = context)
    }

    @Test
    fun `put persistent labels`() {
        val config = mockk<PublisherConfiguration>(relaxed = true)
        every { Analytics.getConfiguration().getPublisherConfiguration(any()) } returns config
        val labels = mapOf("a" to "b", "c" to "d")
        ComScoreSrg.putPersistentLabels(labels)
        verify(exactly = 1) {
            config.addPersistentLabels(labels)
        }
        confirmVerified(config)
    }

    @Test
    fun `set user consent`() {
        val config = mockk<PublisherConfiguration>(relaxed = true)
        every { Analytics.getConfiguration().getPublisherConfiguration(any()) } returns config

        ComScoreSrg.setUserConsent(ComScoreUserConsent.ACCEPTED)
        ComScoreSrg.setUserConsent(ComScoreUserConsent.DECLINED)
        ComScoreSrg.setUserConsent(ComScoreUserConsent.UNKNOWN)

        verify(exactly = 1) {
            config.addPersistentLabels(mapOf(ComScoreLabel.CS_UC_FR.label to "1"))
            config.addPersistentLabels(mapOf(ComScoreLabel.CS_UC_FR.label to "0"))
            config.addPersistentLabels(mapOf(ComScoreLabel.CS_UC_FR.label to ""))
        }
        confirmVerified(config)
    }

    @Test
    fun `put persistent label`() {
        val config = mockk<PublisherConfiguration>(relaxed = true)
        every { Analytics.getConfiguration().getPublisherConfiguration(any()) } returns config

        ComScoreSrg.putPersistentLabels(mapOf("label1" to "value1"))

        verify(exactly = 1) {
            config.addPersistentLabels(mapOf("label1" to "value1"))
        }
        confirmVerified(config)
    }

    @Test
    fun `get persistent label`() {
        val config = mockk<PublisherConfiguration>(relaxed = true)
        every { Analytics.getConfiguration().getPublisherConfiguration(any()) } returns config

        ComScoreSrg.getPersistentLabel("label1")
        ComScoreSrg.getPersistentLabel(ComScoreLabel.CS_UC_FR.label)

        verify(exactly = 1) {
            config.getPersistentLabel("label1")
            config.getPersistentLabel(ComScoreLabel.CS_UC_FR.label)
        }
        confirmVerified(config)
    }

    @Test
    fun `remove persistent label`() {
        val config = mockk<PublisherConfiguration>(relaxed = true)
        every { Analytics.getConfiguration().getPublisherConfiguration(any()) } returns config

        ComScoreSrg.removePersistentLabel("label1")
        ComScoreSrg.removePersistentLabel(ComScoreLabel.CS_UC_FR.label)

        verify(exactly = 1) {
            config.removePersistentLabel("label1")
            config.removePersistentLabel(ComScoreLabel.CS_UC_FR.label)
        }
        confirmVerified(config)
    }

    @Test
    fun `send page view with no labels`() {
        ComScoreSrg.sendPageView(ComScorePageView("page"))
        val expectedLabels = mapOf(ComScoreLabel.C8.label to "page")
        verify(exactly = 1) {
            Analytics.notifyViewEvent(expectedLabels)
        }
    }

    @Test
    fun `send page view with labels`() {
        val labels = mapOf("key01" to "value01", "key02" to "value02")
        ComScoreSrg.sendPageView(ComScorePageView("page", labels = labels))
        val expectedLabels = mapOf(ComScoreLabel.C8.label to "page", "key01" to "value01", "key02" to "value02")
        verify(exactly = 1) {
            Analytics.notifyViewEvent(expectedLabels)
        }
    }
}
