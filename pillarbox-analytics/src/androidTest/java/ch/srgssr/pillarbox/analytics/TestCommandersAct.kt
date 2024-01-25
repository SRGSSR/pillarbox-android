/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import androidx.test.platform.app.InstrumentationRegistry
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActEvent
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActLabels
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActPageView
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActSrg
import com.tagcommander.lib.core.TCUser
import com.tagcommander.lib.serverside.TCPredefinedVariables
import com.tagcommander.lib.serverside.schemas.TCDevice
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestCommandersAct {

    private lateinit var commandersAct: CommandersActSrg

    @Before
    fun setup() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        commandersAct = CommandersActSrg(config = TestUtils.analyticsConfig, appContext = appContext)
    }

    @Test
    fun testSendEvent() {
        commandersAct.sendEvent(CommandersActEvent(name = "Event1"))
        Assert.assertTrue(true)
    }

    @Test
    fun testSendPageViewEvent() {
        commandersAct.sendPageView(
            CommandersActPageView(
                name = "PageTitle1",
                type = "UnitTest",
                levels = listOf("pillarbox", "unit-test")
            )
        )
        Assert.assertTrue(true)
    }

    @Test
    fun testConsentServices() {
        val services = listOf("service1", "service2")
        val expected = "service1,service2"
        commandersAct.setConsentServices(services)
        Assert.assertEquals(expected, commandersAct.getPermanentDataLabel(CommandersActLabels.CONSENT_SERVICES.label))
    }

    @Test
    fun testLegacyUniqueId() {
        val legacyUniqueId: String = TCPredefinedVariables.getInstance().uniqueIdentifier
        Assert.assertEquals(legacyUniqueId, TCDevice.getInstance().sdkID)
        Assert.assertEquals(legacyUniqueId, TCUser.getInstance().anonymous_id)
    }
}
