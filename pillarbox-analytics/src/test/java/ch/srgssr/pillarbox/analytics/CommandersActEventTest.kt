/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.commandersact.TCEventUtils.toTCCustomEvent
import org.junit.Assert
import org.junit.Test

/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
class CommandersActEventTest {

    @Test
    fun testPageEvent() {
        val pageView = PageView(
            "title", arrayOf("level1", "level2")
        )
        val tcEvent = pageView.toTCCustomEvent("RTS")
        val expected = hashMapOf(
            Pair(accessed_after_push_notification,"false"),
            Pair("navigation_level_1","level1"),
            Pair("navigation_level_2","level2"),
            Pair("navigation_bu_distributer","RTS")
        )
        val actual = tcEvent.additionalProperties
        Assert.assertEquals(actual,expected)
        Assert.assertEquals("title", tcEvent.pageType)
    }

    @Test
    fun testPageEventEmptyLevels() {
        val pageView = PageView(
            "title"
        )
        val tcEvent = pageView.toTCCustomEvent("RTS")
        val expected = hashMapOf(
            Pair(accessed_after_push_notification,"false"),
            Pair("navigation_bu_distributer","RTS")
        )
        val actual = tcEvent.additionalProperties
        Assert.assertEquals(actual,expected)
        Assert.assertEquals("title", tcEvent.pageType)
    }

    @Test
    fun testPageEventFromPushNotification() {
        val pageView = PageView(
            "title",
            fromPushNotification = true
        )
        val tcEvent = pageView.toTCCustomEvent("RTS")
        val expected = hashMapOf(
            Pair(accessed_after_push_notification,"true"),
            Pair("navigation_bu_distributer","RTS")
        )
        val actual = tcEvent.additionalProperties
        Assert.assertEquals(actual,expected)
        Assert.assertEquals("title", tcEvent.pageType)
    }

    @Test
    fun testEvent() {
        val event = Event(
            "name", type = "type", value = "value", source = "source", extra1 = "extra1", extra2 = "extra2", extra3 = "extra3", extra4 =
            "extra4", extra5 = "extra5"
        )
        val tcEvent = event.toTCCustomEvent()
        val expected = hashMapOf(
            Pair("event_value_1","extra1"),
            Pair("event_value_2","extra2"),
            Pair("event_value_3","extra3"),
            Pair("event_value_4","extra4"),
            Pair("event_value_5","extra5"),
            Pair("event_source","source"),
            Pair("event_value","value"),
            Pair("event_type","type"),
        )
        val actual = tcEvent.additionalProperties
        Assert.assertEquals(actual,expected)
        Assert.assertEquals(tcEvent.name,"name")
        Assert.assertNull(tcEvent.pageType)
        Assert.assertNull(tcEvent.pageName)
    }

    @Test
    fun testEventBlank() {
        val event = Event(
            "name", type = "type", value = "value", source = "source", extra1 = "", extra2 = " ", extra3 = "extra3"
        )
        val tcEvent = event.toTCCustomEvent()
        val expected = hashMapOf(
            Pair("event_value_3","extra3"),
            Pair("event_source","source"),
            Pair("event_value","value"),
            Pair("event_type","type"),
        )
        val actual = tcEvent.additionalProperties
        Assert.assertEquals(actual,expected)
        Assert.assertEquals(tcEvent.name,"name")
        Assert.assertNull(tcEvent.pageType)
        Assert.assertNull(tcEvent.pageName)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBlankEventName() {
        Event(name = " ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBlankPageViewTitle() {
        PageView(title = " ")
    }

    companion object {
        private const val accessed_after_push_notification = "accessed_after_push_notification"
    }
}
