/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import androidx.test.platform.app.InstrumentationRegistry
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActEvent
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActPageView
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActSrg
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
}
