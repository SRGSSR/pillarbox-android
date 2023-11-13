/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.commandersact.CommandersActEvent
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActPageView
import org.junit.Assert
import org.junit.Test

class CommandersActEventTest {

    @Test
    fun testPageEvent() {
        val pageView = CommandersActPageView(
            name = "title",
            type = "type",
            levels = listOf("level1", "level2")
        )
        val tcEvent = pageView.toTCPageViewEvent(AnalyticsConfig.Vendor.RTS)
        val expected = hashMapOf(
            Pair("navigation_level_1", "level1"),
            Pair("navigation_level_2", "level2"),
            Pair("navigation_bu_distributer", "RTS")
        )
        val actual = tcEvent.additionalProperties
        Assert.assertEquals(expected, actual)
        Assert.assertEquals("title", tcEvent.pageName)
        Assert.assertEquals("type", tcEvent.pageType)
    }

    @Test
    fun testPageEventEmptyLevels() {
        val pageView = CommandersActPageView(
            name = "title", type = "type"
        )
        val tcEvent = pageView.toTCPageViewEvent(AnalyticsConfig.Vendor.RTS)
        val expected = hashMapOf(
            Pair("navigation_bu_distributer", "RTS")
        )
        val actual = tcEvent.additionalProperties
        Assert.assertEquals(expected, actual)
        Assert.assertEquals("title", tcEvent.pageName)
        Assert.assertEquals("type", tcEvent.pageType)
    }

    @Test
    fun testPageEventCustomLabels() {
        val pageView = CommandersActPageView(
            name = "title",
            type = "type",
            labels = mapOf(Pair("Key1", "value1"), Pair("Key2", " "))
        )
        val tcEvent = pageView.toTCPageViewEvent(AnalyticsConfig.Vendor.RTS)
        val expected = hashMapOf(
            Pair("navigation_bu_distributer", "RTS"),
            Pair("Key1", "value1")
        )
        val actual = tcEvent.additionalProperties
        Assert.assertEquals(expected, actual)
        Assert.assertEquals("title", tcEvent.pageName)
        Assert.assertEquals("type", tcEvent.pageType)
    }

    @Test
    fun testEvent() {
        val event = CommandersActEvent("name")
        val tcEvent = event.toTCCustomEvent()
        Assert.assertEquals(tcEvent.name, "name")
        Assert.assertNull(tcEvent.pageType)
        Assert.assertNull(tcEvent.pageName)
    }

    @Test
    fun testEventWithLabels() {
        val expectedLabels = hashMapOf(
            Pair("event_value_3", "extra3"),
            Pair("event_source", "source"),
            Pair("event_value", "value"),
            Pair("event_type", "type"),
        )
        val event = CommandersActEvent("name", labels = expectedLabels)
        val tcEvent = event.toTCCustomEvent()
        val actualLabels = tcEvent.additionalProperties
        Assert.assertEquals(expectedLabels, actualLabels)
        Assert.assertEquals(tcEvent.name, "name")
        Assert.assertNull(tcEvent.pageType)
        Assert.assertNull(tcEvent.pageName)
    }

    @Test
    fun testEventWithLabelsBlank() {
        val labels = hashMapOf(
            Pair("event_value_3", ""),
            Pair("event_source", " "),
            Pair("event_value", "value"),
            Pair("event_type", "type"),
        )
        val event = CommandersActEvent("name", labels = labels)
        val expectedLabels = hashMapOf(
            Pair("event_value", "value"),
            Pair("event_type", "type"),
        )
        val tcEvent = event.toTCCustomEvent()
        val actualLabels = tcEvent.additionalProperties
        Assert.assertEquals(expectedLabels, actualLabels)
        Assert.assertEquals(tcEvent.name, "name")
        Assert.assertNull(tcEvent.pageType)
        Assert.assertNull(tcEvent.pageName)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBlankEventName() {
        CommandersActEvent(name = " ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBlankPageViewTitle() {
        CommandersActPageView(name = " ", type = "type")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBlankPageViewType() {
        CommandersActPageView(name = "Title", type = " ")
    }
}
