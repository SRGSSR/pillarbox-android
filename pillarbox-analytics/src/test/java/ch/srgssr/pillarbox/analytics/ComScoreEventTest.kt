/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.comscore.ComScoreLabel
import ch.srgssr.pillarbox.analytics.comscore.ComScorePageView
import org.junit.Assert
import org.junit.Test

class ComScoreEventTest {

    @Test
    fun testPageView() {
        val title = "title 1"
        val pageView = ComScorePageView(title)
        val actual = pageView.toLabels()
        val expected = HashMap<String, String>().apply {
            this[ComScoreLabel.C8.label] = "title 1"
        }
        Assert.assertEquals(actual, expected)
    }


    @Test(expected = IllegalArgumentException::class)
    fun testPageViewEmptyTitle() {
        ComScorePageView("")
        Assert.assertTrue(false)
    }

    @Test
    fun testCustomLabels() {
        val title = "title 1"
        val customLabels = mapOf(Pair("key1", "value1"), Pair("key2", ""))
        val pageView = ComScorePageView(title, customLabels)
        val actual = pageView.toLabels()
        val expected = HashMap<String, String>().apply {
            this[ComScoreLabel.C8.label] = "title 1"
            this["key1"] = "value1"
        }
        Assert.assertEquals(actual, expected)
    }
}
