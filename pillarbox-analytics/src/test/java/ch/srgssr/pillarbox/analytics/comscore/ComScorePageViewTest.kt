/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.comscore

import kotlin.test.Test
import kotlin.test.assertEquals

class ComScorePageViewTest {
    @Test(expected = IllegalArgumentException::class)
    fun `empty page name is invalid`() {
        ComScorePageView("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank page name is invalid`() {
        ComScorePageView("  ")
    }

    @Test
    fun `get labels from page view`() {
        val pageView = ComScorePageView("name")
        val labels = pageView.toLabels()
        val expectedLabels = mapOf(
            "c8" to pageView.name,
        )

        assertEquals(expectedLabels, labels)
    }

    @Test
    fun `get labels from page view with some blank labels`() {
        val pageView = ComScorePageView(
            name = "name",
            labels = mapOf(
                "key1" to "value1",
                "key2" to "",
                "key3" to "value3",
                "key4" to " ",
            ),
        )
        val labels = pageView.toLabels()
        val expectedLabels = mapOf(
            "key1" to "value1",
            "key3" to "value3",
            "c8" to pageView.name,
        )

        assertEquals(expectedLabels, labels)
    }
}
