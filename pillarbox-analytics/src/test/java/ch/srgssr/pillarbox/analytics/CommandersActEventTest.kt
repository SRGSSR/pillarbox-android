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
            "title", arrayOf("level1", "level2"),
            customLabels = CustomLabels.Builder()
                .putCommandersActLabel("A", "a")
                .putCommandersActLabel("B", "b")
                .build()
        )
        val tcEvent = pageView.toTCCustomEvent("RTS")

        Assert.assertEquals("a", tcEvent.additionalParameters.getData("A"))
        Assert.assertEquals("b", tcEvent.additionalParameters.getData("B"))
        Assert.assertEquals("false", tcEvent.additionalParameters.getData(accessed_after_push_notification))
        Assert.assertEquals("level1", tcEvent.additionalParameters.getData("navigation_level_1"))
        Assert.assertEquals("level2", tcEvent.additionalParameters.getData("navigation_level_2"))
        Assert.assertEquals("title", tcEvent.pageType)
    }

    @Test
    fun testPageEventEmptyLevels() {
        val pageView = PageView(
            "title"
        )
        val tcEvent = pageView.toTCCustomEvent("RTS")

        Assert.assertNull(tcEvent.additionalParameters.getData("navigation_level_1"))
        Assert.assertEquals("false", tcEvent.additionalParameters.getData(accessed_after_push_notification))
        Assert.assertEquals("title", tcEvent.pageType)
    }

    @Test
    fun testPageEventFromPushNotification() {
        val pageView = PageView(
            "title",
            fromPushNotification = true
        )
        val tcEvent = pageView.toTCCustomEvent("RTS")

        Assert.assertNull(tcEvent.additionalParameters.getData("navigation_level_1"))
        Assert.assertEquals("true", tcEvent.additionalParameters.getData(accessed_after_push_notification))
        Assert.assertEquals("title", tcEvent.pageType)
    }

    @Test
    fun testEvent() {
        val event = Event(
            "name", type = "type", value = "value", source = "source", extra1 = "extra1", extra2 = "extra2", extra3 = "extra3", extra4 =
            "extra4", extra5 = "extra5", customLabels = CustomLabels.Builder().putCommandersActLabel("A", "a").build()
        )
        val tcEvent = event.toTCCustomEvent()
        Assert.assertEquals("hidden_event", tcEvent.name)
        Assert.assertEquals("name", tcEvent.additionalParameters.getData("event_name"))
        Assert.assertEquals("value", tcEvent.additionalParameters.getData("event_value"))
        Assert.assertEquals("type", tcEvent.additionalParameters.getData("event_type"))
        Assert.assertEquals("source", tcEvent.additionalParameters.getData("event_source"))
        Assert.assertEquals("extra5", tcEvent.additionalParameters.getData("event_value_5"))
        Assert.assertEquals("extra4", tcEvent.additionalParameters.getData("event_value_4"))
        Assert.assertEquals("extra3", tcEvent.additionalParameters.getData("event_value_3"))
        Assert.assertEquals("extra2", tcEvent.additionalParameters.getData("event_value_2"))
        Assert.assertEquals("extra1", tcEvent.additionalParameters.getData("event_value_1"))
        Assert.assertEquals(null, tcEvent.additionalParameters.getData(accessed_after_push_notification))
        Assert.assertEquals("a", tcEvent.additionalParameters.getData("A"))
    }

    @Test
    fun testEventWithUserId() {
        val userId = "UserIdKey"
        val event = Event(
            "name", type = "type", value = "value", source = "source", extra1 = "extra1", extra2 = "extra2", extra3 = "extra3", extra4 =
            "extra4", extra5 = "extra5", customLabels = CustomLabels.Builder()
                .setUserId(userId)
                .putCommandersActLabel("A", "a")
                .build()
        )
        val tcEvent = event.toTCCustomEvent()
        Assert.assertEquals("hidden_event", tcEvent.name)
        Assert.assertEquals("name", tcEvent.additionalParameters.getData("event_name"))
        Assert.assertEquals("value", tcEvent.additionalParameters.getData("event_value"))
        Assert.assertEquals("type", tcEvent.additionalParameters.getData("event_type"))
        Assert.assertEquals("source", tcEvent.additionalParameters.getData("event_source"))
        Assert.assertEquals("extra5", tcEvent.additionalParameters.getData("event_value_5"))
        Assert.assertEquals("extra4", tcEvent.additionalParameters.getData("event_value_4"))
        Assert.assertEquals("extra3", tcEvent.additionalParameters.getData("event_value_3"))
        Assert.assertEquals("extra2", tcEvent.additionalParameters.getData("event_value_2"))
        Assert.assertEquals("extra1", tcEvent.additionalParameters.getData("event_value_1"))
        Assert.assertEquals(null, tcEvent.additionalParameters.getData(accessed_after_push_notification))
        Assert.assertEquals("a", tcEvent.additionalParameters.getData("A"))
    }

    companion object {
        private const val accessed_after_push_notification = "accessed_after_push_notification"
    }
}
