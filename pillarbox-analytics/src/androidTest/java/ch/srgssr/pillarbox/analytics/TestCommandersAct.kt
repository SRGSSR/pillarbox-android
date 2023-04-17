/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import androidx.test.platform.app.InstrumentationRegistry
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestCommandersAct {

    private lateinit var commandersAct: CommandersAct

    @Before
    fun setup() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val config = CommandersAct.Config(virtualSite = "pillarbox-test-android", sourceKey = CommandersAct.Config.SOURCE_KEY_SRG_DEBUG)
        commandersAct = CommandersAct(config = TestUtils.analyticsConfig, commandersActConfig = config, appContext = appContext)
    }

    @Test
    fun testSendEvent() {
        commandersAct.sendEvent(Event("Event1"))
        Assert.assertTrue(true)
    }

    @Test
    fun testSendPageViewEvent() {
        commandersAct.sendPageView(PageView("PageTitle1", arrayOf("pillarbox", "unit-test")))
        Assert.assertTrue(true)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSendPageViewNoTitle() {
        commandersAct.sendPageView(PageView("", arrayOf("pillarbox", "unit-test")))
    }
}
