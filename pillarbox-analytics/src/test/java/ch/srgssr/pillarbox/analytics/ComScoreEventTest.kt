/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.comscore.ComScore.toComScoreLabel
import ch.srgssr.pillarbox.analytics.comscore.ComScoreLabel
import org.junit.Assert
import org.junit.Test

class ComScoreEventTest {

    @Test
    fun testPageView() {
        val pageView = PageView(
            "title 1", arrayOf("level1", "level2"),
        )
        val actual = pageView.toComScoreLabel()
        val expected = HashMap<String, String>().apply {
            this[ComScoreLabel.C8.label] = "title 1"
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
            this[ComScoreLabel.C8.label] = "title 1"
        }
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun testPageViewFromPushNotification() {
        val pageView = PageView(
            "title 1", fromPushNotification = true
        )
        val actual = pageView.toComScoreLabel()
        val expected = HashMap<String, String>().apply {
            this[ComScoreLabel.C8.label] = "title 1"
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
