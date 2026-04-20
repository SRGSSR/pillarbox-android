/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.analytics.UserConsent
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class CommandersActInitialValuesTest {

    @Test
    fun `initial values with initial user consent`() {
        val analyticsConfig = DEFAULT.copy(
            userConsent = UserConsent(commandersActConsentServices = listOf("service1", "service2")),
            profileIdentifier = "ProfileIdentifierValue"
        )
        val commandersAct = CommandersActSrg(config = analyticsConfig, appContext = ApplicationProvider.getApplicationContext())

        assertEquals("service1,service2", commandersAct.getPermanentDataLabel(CommandersActLabels.CONSENT_SERVICES.label))
        assertEquals("ProfileIdentifierValue", commandersAct.getPermanentDataLabel(CommandersActLabels.PROFILE_ID.label))
    }

    @Test
    fun `initial values with default AnalyticsConfig`() {
        val analyticsConfig = DEFAULT
        val commandersAct = CommandersActSrg(config = analyticsConfig, appContext = ApplicationProvider.getApplicationContext())

        assertEquals("", commandersAct.getPermanentDataLabel(CommandersActLabels.CONSENT_SERVICES.label))
        assertNull(commandersAct.getPermanentDataLabel(CommandersActLabels.PROFILE_ID.label))
    }

    private companion object {

        val DEFAULT = TestUtils.analyticsConfig
    }
}
