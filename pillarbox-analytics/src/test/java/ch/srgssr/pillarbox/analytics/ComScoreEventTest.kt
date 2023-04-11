/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.comscore.ComScore.toComScoreLabel
import org.junit.Assert
import org.junit.Test

class ComScoreEventTest {

    @Test
    fun testPageView() {
        val pageView = PageView(
            "title 1", arrayOf("level1", "level2"),
            customLabels = CustomLabels.Builder()
                .putComScoreLabel("A", "a")
                .putComScoreLabel("B", "b")
                .putCommandersActLabel("C", "c")
                .build()
        )
        val actual = pageView.toComScoreLabel()
        val expected = HashMap<String, String>().apply {
            this["A"] = "a"
            this["B"] = "b"
            this["srg_title"] = "title 1"
            this["name"] = "level1.level2.title 1"
            this["ns_category"] = "level1.level2"
            this["srg_n1"] = "level1"
            this["srg_n2"] = "level2"
            this["srg_ap_push"] = "false"
        }
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun testPageVieEmptyLevels() {
        val pageView = PageView(
            "title 1"
        )
        val actual = pageView.toComScoreLabel()
        val expected = HashMap<String, String>().apply {
            this["srg_title"] = "title 1"
            this["name"] = "app.title 1"
            this["ns_category"] = "app"
            this["srg_n1"] = "app"
            this["srg_ap_push"] = "false"
        }
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun testPageViewFromPushNotification() {
        val pageView = PageView(
            "title 1",
            fromPushNotification = true
        )
        val actual = pageView.toComScoreLabel()
        val expected = HashMap<String, String>().apply {
            this["srg_title"] = "title 1"
            this["name"] = "app.title 1"
            this["ns_category"] = "app"
            this["srg_n1"] = "app"
            this["srg_ap_push"] = "true"
        }
        Assert.assertEquals(actual, expected)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPageViewEmptyTitle() {
        val pageView = PageView(
            " "
        )
        pageView.toComScoreLabel()
        Assert.assertTrue(false)
    }
}
