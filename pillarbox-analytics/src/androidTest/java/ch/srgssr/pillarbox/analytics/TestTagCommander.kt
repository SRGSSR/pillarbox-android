/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import androidx.test.platform.app.InstrumentationRegistry
import ch.srgssr.pillarbox.analytics.commandersact.TagCommander
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TestTagCommander {

    private lateinit var tagCommander: TagCommander

    @Before
    fun setup() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val config = TagCommander.Config.SRG_DEBUG
        tagCommander = TagCommander(config = TestUtils.analyticsConfig, commandersActConfig = config, appContext = appContext)
    }

    @Test
    fun testSendEvent() {
        tagCommander.sendEvent(Event("Event1"))
        Assert.assertTrue(true)
    }

    @Test
    fun testSendPageViewEvent() {
        tagCommander.sendPageViewEvent(PageEvent("PageTitle1", arrayOf("pillarbox", "unit-test")))
        Assert.assertTrue(true)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSendPageViewNoTitle() {
        tagCommander.sendPageViewEvent(PageEvent("", arrayOf("pillarbox", "unit-test")))
    }
}
