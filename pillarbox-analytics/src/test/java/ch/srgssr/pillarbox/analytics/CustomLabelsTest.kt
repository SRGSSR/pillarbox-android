/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import org.junit.Assert
import org.junit.Test

class CustomLabelsTest {

    @Test
    fun testEmptyBuilder() {
        val expectedCommanderActLabels = HashMap<String, String>().apply {
            this[CommandersAct.KEY_USER_IS_LOGGED] = "false"
        }
        val customLabels = CustomLabels.Builder().build()
        Assert.assertNull(customLabels.comScoreLabels)
        Assert.assertEquals(expectedCommanderActLabels, customLabels.commandersActLabels)
    }

    @Test
    fun testBuilderBoth() {
        val expectedLabels = HashMap<String, String>().apply {
            this["A"] = "a"
            this["B"] = "b"
        }
        val expectedCommandersActLabels = HashMap<String, String>().apply {
            this["A"] = "a"
            this["B"] = "b"
            this[CommandersAct.KEY_USER_IS_LOGGED] = "false"
        }
        val customLabelsBuilder = CustomLabels.Builder()
        for (entry in expectedLabels.entries) {
            customLabelsBuilder.putBothLabel(entry.key, entry.value)
        }
        val expected = CustomLabels(commandersActLabels = expectedCommandersActLabels, comScoreLabels = expectedLabels)
        val actual = customLabelsBuilder.build()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testBuilderPutEach() {
        val expectedComScoreLabels = HashMap<String, String>().apply {
            this["A"] = "a"
        }
        val expectedCommanderActLabels = HashMap<String, String>().apply {
            this["B"] = "b"
            this[CommandersAct.KEY_USER_IS_LOGGED] = "false"
        }
        val customLabelsBuilder = CustomLabels.Builder()
            .putComScoreLabel("A", "a")
            .putCommandersActLabel("B", "b")
        val expected = CustomLabels(commandersActLabels = expectedCommanderActLabels, comScoreLabels = expectedComScoreLabels)
        val actual = customLabelsBuilder.build()
        Assert.assertEquals(expected.comScoreLabels, actual.comScoreLabels)
    }

    @Test
    fun testBuilderWithUserId() {
        val userId = "UserId"
        val expectedComScoreLabels = HashMap<String, String>().apply {
            this["A"] = "a"
        }
        val expectedCommanderActLabels = HashMap<String, String>().apply {
            this["B"] = "b"
            this[CommandersAct.KEY_USER_IS_LOGGED] = "true"
            this[CommandersAct.KEY_USER_ID] = userId
        }
        val customLabelsBuilder = CustomLabels.Builder()
            .putComScoreLabel("A", "a")
            .putCommandersActLabel("B", "b")
            .setUserId(userId)
        val expected = CustomLabels(commandersActLabels = expectedCommanderActLabels, comScoreLabels = expectedComScoreLabels)
        val actual = customLabelsBuilder.build()
        Assert.assertEquals(expected, actual)
    }
}
