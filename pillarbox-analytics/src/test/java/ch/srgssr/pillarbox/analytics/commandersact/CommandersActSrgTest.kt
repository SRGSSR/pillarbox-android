/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tagcommander.lib.core.TCUser
import com.tagcommander.lib.serverside.TCPredefinedVariables
import com.tagcommander.lib.serverside.TCServerSide
import com.tagcommander.lib.serverside.events.base.TCEvent
import com.tagcommander.lib.serverside.schemas.TCDevice
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class CommandersActSrgTest {

    private val analyticsConfig = TestUtils.analyticsConfig

    private val commandersAct: CommandersActSrg by lazy {
        CommandersActSrg(config = analyticsConfig, appContext = ApplicationProvider.getApplicationContext())
    }

    @Test
    @Config(qualifiers = "television")
    fun `navigation device is tvbox`() {
        val actual = commandersAct.getPermanentDataLabel("navigation_device")
        Assert.assertEquals("tvbox", actual)
    }

    @Test
    @Config
    fun `navigation device is phone`() {
        val actual = commandersAct.getPermanentDataLabel("navigation_device")
        Assert.assertEquals("phone", actual)
    }

    @Test
    @Config(qualifiers = "sw600dp")
    fun `test navigation device is tablet`() {
        val actual = commandersAct.getPermanentDataLabel("navigation_device")
        Assert.assertEquals("tablet", actual)
    }

    @Test
    @Config(qualifiers = "car")
    fun `navigation device is auto`() {
        val actual = commandersAct.getPermanentDataLabel("navigation_device")
        Assert.assertEquals("auto", actual)
    }

    @Test
    fun `sendEvent()  with CommandersActEvent`() {
        val serverSide = mockk<TCServerSide>(relaxed = true)
        val commandersAct = CommandersActSrg(tcServerSide = serverSide, config = analyticsConfig, "tests")
        val eventSlot = slot<TCEvent>()

        commandersAct.sendEvent(CommandersActEvent(name = "dummy"))
        verify(exactly = 1) {
            serverSide.execute(capture(eventSlot))
        }

        Assert.assertTrue(eventSlot.isCaptured)
        Assert.assertEquals(eventSlot.captured.name, "dummy")
    }

    @Test
    fun `sendPageView() with CommandersActPageView`() {
        val serverSide = mockk<TCServerSide>(relaxed = true)
        val commandersAct = CommandersActSrg(tcServerSide = serverSide, config = analyticsConfig, "tests")
        val eventSlot = slot<TCEvent>()

        commandersAct.sendPageView(
            CommandersActPageView(
                name = "PageTitle1",
                type = "UnitTest",
                levels = listOf("pillarbox", "unit-test")
            )
        )

        verify(exactly = 1) {
            serverSide.execute(capture(eventSlot))
        }
        Assert.assertTrue(eventSlot.isCaptured)
        val capturedEvent = eventSlot.captured
        Assert.assertEquals("page_view", capturedEvent.name)
        Assert.assertEquals("PageTitle1", capturedEvent.pageName)
        Assert.assertEquals("UnitTest", capturedEvent.pageType)
        Assert.assertEquals("pillarbox", capturedEvent.additionalProperties["navigation_level_1"])
        Assert.assertEquals("unit-test", capturedEvent.additionalProperties["navigation_level_2"])
        Assert.assertNull(capturedEvent.additionalProperties["navigation_level_3"])
    }

    @Test
    fun `sendTcMediaEvent()  with TCMediaEvent`() {
        val serverSide = mockk<TCServerSide>(relaxed = true)
        val commandersAct = CommandersActSrg(tcServerSide = serverSide, config = analyticsConfig, "tests")
        val eventSlot = slot<TCEvent>()

        commandersAct.sendTcMediaEvent(TCMediaEvent(eventType = MediaEventType.Eof, assets = emptyMap()))
        verify(exactly = 1) {
            serverSide.execute(capture(eventSlot))
        }

        Assert.assertTrue(eventSlot.isCaptured)
        Assert.assertEquals("eof", eventSlot.captured.name)
    }

    @Test
    fun `initial consent services`() {
        Assert.assertNull(commandersAct.getPermanentDataLabel(CommandersActLabels.CONSENT_SERVICES.label))
    }

    @Test
    fun `set consent services`() {
        val services = listOf("service1", "service2")
        val expected = "service1,service2"
        commandersAct.setConsentServices(services)
        Assert.assertEquals(expected, commandersAct.getPermanentDataLabel(CommandersActLabels.CONSENT_SERVICES.label))
    }

    @Test
    fun `legacyUniqueID is used for sdkID and anonymous_id`() {
        val legacyUniqueId: String = TCPredefinedVariables.getInstance().uniqueIdentifier
        Assert.assertEquals(legacyUniqueId, TCDevice.getInstance().sdkID)
        Assert.assertEquals(legacyUniqueId, TCUser.getInstance().anonymous_id)
    }
}
