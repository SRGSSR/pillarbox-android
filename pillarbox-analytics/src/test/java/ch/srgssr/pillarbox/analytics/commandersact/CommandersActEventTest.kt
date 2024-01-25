/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CommandersActEventTest {
    @Test(expected = IllegalArgumentException::class)
    fun `empty event name is invalid`() {
        CommandersActEvent("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank event name is invalid`() {
        CommandersActEvent("  ")
    }

    @Test
    fun `convert event with no labels to TCCustomEvent`() {
        val event = CommandersActEvent("name")
        val tcEvent = event.toTCCustomEvent()

        assertEquals(emptyMap<String, Any>(), tcEvent.additionalProperties)
        assertEquals(event.name, tcEvent.name)
        assertNull(tcEvent.pageName)
        assertNull(tcEvent.pageType)
    }

    @Test
    fun `convert event with labels to TCCustomEvent`() {
        val event = CommandersActEvent(
            name = "name",
            labels = mapOf(
                "event_value_3" to "extra3",
                "event_source" to "source",
                "event_value" to "value",
                "event_type" to "type",
            )
        )
        val tcEvent = event.toTCCustomEvent()

        assertEquals<Map<String, Any>>(event.labels, tcEvent.additionalProperties)
        assertEquals(event.name, tcEvent.name)
        assertNull(tcEvent.pageName)
        assertNull(tcEvent.pageType)
    }

    @Test
    fun `convert event with some blank labels to TCCustomEvent`() {
        val event = CommandersActEvent(
            name = "name",
            labels = mapOf(
                "event_value_3" to "",
                "event_source" to " ",
                "event_value" to "value",
                "event_type" to "type",
            )
        )
        val tcEvent = event.toTCCustomEvent()
        val expectedProperties = mapOf(
            "event_value" to "value",
            "event_type" to "type",
        )

        assertEquals<Map<String, Any>>(expectedProperties, tcEvent.additionalProperties)
        assertEquals(event.name, tcEvent.name)
        assertNull(tcEvent.pageName)
        assertNull(tcEvent.pageType)
    }
}
