/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.AnalyticsConfig.Vendor
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandersActPageViewTest {
    @Test(expected = IllegalArgumentException::class)
    fun `empty name is invalid`() {
        CommandersActPageView(
            name = "",
            type = "type",
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank name is invalid`() {
        CommandersActPageView(
            name = " ",
            type = "type",
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty type is invalid`() {
        CommandersActPageView(
            name = "name",
            type = "",
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank type is invalid`() {
        CommandersActPageView(
            name = "name",
            type = " ",
        )
    }

    @Test
    fun `convert page view to TCPageViewEvent`() {
        val pageView = CommandersActPageView(
            name = "name",
            type = "type",
        )
        val tcPageView = pageView.toTCPageViewEvent(vendor = Vendor.RTS)
        val expectedProperties = mapOf<String, Any>(
            "content_bu_owner" to "RTS",
        )

        assertEquals(expectedProperties, tcPageView.additionalProperties)
        assertEquals(pageView.name, tcPageView.pageName)
        assertEquals(pageView.type, tcPageView.pageType)
    }

    @Test
    fun `convert page view with levels to TCPageViewEvent`() {
        val pageView = CommandersActPageView(
            name = "name",
            type = "type",
            levels = listOf("level1", "level2"),
        )
        val tcPageView = pageView.toTCPageViewEvent(vendor = Vendor.RSI)
        val expectedProperties = mapOf<String, Any>(
            "navigation_level_1" to "level1",
            "navigation_level_2" to "level2",
            "content_bu_owner" to "RSI",
        )

        assertEquals(expectedProperties, tcPageView.additionalProperties)
        assertEquals(pageView.name, tcPageView.pageName)
        assertEquals(pageView.type, tcPageView.pageType)
    }

    @Test
    fun `convert page view with some blank labels to TCPageViewEvent`() {
        val pageView = CommandersActPageView(
            name = "name",
            type = "type",
            labels = mapOf(
                "key1" to "value1",
                "key2" to "",
                "key3" to "value3",
                "key4" to " ",
            ),
        )
        val tcPageView = pageView.toTCPageViewEvent(vendor = Vendor.RTR)
        val expectedProperties = mapOf<String, Any>(
            "key1" to "value1",
            "key3" to "value3",
            "content_bu_owner" to "RTR",
        )

        assertEquals(expectedProperties, tcPageView.additionalProperties)
        assertEquals(pageView.name, tcPageView.pageName)
        assertEquals(pageView.type, tcPageView.pageType)
    }

    @Test
    fun `convert page view with levels and labels to TCPageViewEvent`() {
        val pageView = CommandersActPageView(
            name = "name",
            type = "type",
            levels = listOf("level1", "level2"),
            labels = mapOf(
                "key1" to "value1",
                "key2" to "",
                "key3" to "value3",
                "key4" to " ",
            ),
        )
        val tcPageView = pageView.toTCPageViewEvent(vendor = Vendor.SRF)
        val expectedProperties = mapOf<String, Any>(
            "key1" to "value1",
            "key3" to "value3",
            "navigation_level_1" to "level1",
            "navigation_level_2" to "level2",
            "content_bu_owner" to "SRF",
        )

        assertEquals(expectedProperties, tcPageView.additionalProperties)
        assertEquals(pageView.name, tcPageView.pageName)
        assertEquals(pageView.type, tcPageView.pageType)
    }
}
