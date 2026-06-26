/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import com.tagcommander.lib.serverside.events.TCCustomEvent
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandersActSourceTest {

    @Test
    fun `test setCommandersActSource with mandatory values`() {
        val source = CommandersActSource(
            pageId = "page_id_value",
            sectionId = "section_id_value"
        )
        val event = TCCustomEvent("test_event")

        with(source) {
            event.setCommandersActSource()
        }

        val expectedProperties: Map<String, Any> = mapOf(
            "page_id" to "page_id_value",
            "section_id" to "section_id_value"
        )
        assertEquals(expectedProperties, event.additionalProperties)
    }

    @Test
    fun `test setCommandersActSource with all values`() {
        val source = CommandersActSource(
            pageId = "page_id_value",
            pageVersion = "page_version_value",
            sectionId = "section_id_value",
            sectionVersion = "section_version_value",
            sectionPosition = 1,
            itemPositionInSection = 2,
            labels = mapOf("custom_label" to "custom_value")
        )
        val event = TCCustomEvent("test_event")

        with(source) {
            event.setCommandersActSource()
        }

        val expectedProperties: Map<String, Any> = mapOf(
            "page_id" to "page_id_value",
            "page_version" to "page_version_value",
            "section_id" to "section_id_value",
            "section_version" to "section_version_value",
            "section_position_in_page" to 1,
            "item_position_in section" to 2,
            "custom_label" to "custom_value"
        )
        assertEquals(expectedProperties, event.additionalProperties)
    }

    @Test
    fun `test setCommandersActSource with blank values`() {
        val source = CommandersActSource(
            pageId = " ",
            sectionId = ""
        )
        val event = TCCustomEvent("test_event")

        with(source) {
            event.setCommandersActSource()
        }

        assertEquals(emptyMap<String, Any>(), event.additionalProperties)
    }

    @Test
    fun `test setCommandersActSource with custom labels overwriting mandatory values`() {
        val source = CommandersActSource(
            pageId = "page_id_value",
            sectionId = "section_id_value",
            labels = mapOf("page_id" to "custom_page_id")
        )
        val event = TCCustomEvent("test_event")

        with(source) {
            event.setCommandersActSource()
        }

        // Standard values are added after labels, so they overwrite them.
        val expectedProperties: Map<String, Any> = mapOf(
            "page_id" to "page_id_value",
            "section_id" to "section_id_value"
        )
        assertEquals(expectedProperties, event.additionalProperties)
    }
}
