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
        val pageView = PageEvent(
            "title 1", arrayOf("level1", "level2"),
            customLabels = CustomLabels.Builder()
                .putComScoreLabel("A", "a")
                .putComScoreLabel("B", "b")
                .putCommandersActLabel("C", "c")
                .build()
        )
        val actual = pageView.toComScoreLabel()
        Assert.assertEquals("a", actual["A"])
        Assert.assertEquals("b", actual["B"])
        Assert.assertFalse(actual.containsKey("C"))
        Assert.assertEquals("title 1", actual["srg_title"])
        Assert.assertEquals("level1.level2.title 1", actual["name"])
        Assert.assertEquals("level1", actual["srg_n1"])
        Assert.assertEquals("level2", actual["srg_n2"])
        Assert.assertFalse(actual.containsKey("srg_n3"))
        Assert.assertFalse(actual["srg_ap_push"].toBoolean())
    }

    @Test
    fun testPageVieEmptyLevels() {
        val pageView = PageEvent(
            "title 1"
        )
        val actual = pageView.toComScoreLabel()
        Assert.assertEquals("title 1", actual["srg_title"])
        Assert.assertEquals("app.title 1", actual["name"])
        Assert.assertEquals("app", actual["srg_n1"])
        Assert.assertFalse(actual["srg_ap_push"].toBoolean())
    }

    @Test
    fun testPageViewFromPushNotification() {
        val pageView = PageEvent(
            "title 1",
            fromPushNotification = true
        )
        val actual = pageView.toComScoreLabel()
        Assert.assertEquals("title 1", actual["srg_title"])
        Assert.assertEquals("app.title 1", actual["name"])
        Assert.assertEquals("app", actual["srg_n1"])
        Assert.assertTrue(actual["srg_ap_push"].toBoolean())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPageViewEmptyTitle() {
        val pageView = PageEvent(
            " "
        )
        pageView.toComScoreLabel()
        Assert.assertTrue(false)
    }
}
