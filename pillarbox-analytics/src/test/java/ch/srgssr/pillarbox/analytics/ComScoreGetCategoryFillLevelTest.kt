/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.comscore.ComScore
import org.junit.Assert
import org.junit.Test

class ComScoreGetCategoryFillLevelTest {

    @Test
    fun testEmptyLevels() {
        val levels = emptyArray<String>()
        val expectedCategory = "app"
        val actual = ComScore.getCategory(levels)
        Assert.assertEquals(expectedCategory, actual)

        val expectedLevels = mapOf(Pair(LEVEL.format(1), "app"))
        val actualLevels = HashMap<String, String>().apply {
            ComScore.fillLevel(this, levels)
        }
        Assert.assertEquals(expectedLevels, actualLevels)
    }

    @Test
    fun testSomeLevels() {
        val levels = arrayOf("level 1", "level 2")
        val expectedCategory = "level 1.level 2"
        val actual = ComScore.getCategory(levels)
        Assert.assertEquals(expectedCategory, actual)

        val expectedLevels = mapOf(Pair(LEVEL.format(1), "level 1"), Pair(LEVEL.format(2), "level 2"))
        val actualLevels = HashMap<String, String>().apply {
            ComScore.fillLevel(this, levels)
        }
        Assert.assertEquals(expectedLevels, actualLevels)
    }

    @Test
    fun testSomeLevelsEmptyAndBlankOnly() {
        val levels = arrayOf("", " ", "   ")
        val expectedCategory = "app"
        val actual = ComScore.getCategory(levels)
        Assert.assertEquals(expectedCategory, actual)

        val expectedLevels = mapOf(
            Pair(LEVEL.format(1), "app"),
        )
        val actualLevels = HashMap<String, String>().apply {
            ComScore.fillLevel(this, levels)
        }
        Assert.assertEquals(expectedLevels, actualLevels)
    }

    @Test
    fun testSomeLevelsWithSomeEmptyAndBlank() {
        val levels = arrayOf("", "level1", "   ", "level2"," ","level3")
        val expectedCategory = "level1.level2.level3"
        val actual = ComScore.getCategory(levels)
        Assert.assertEquals(expectedCategory, actual)

        val expectedLevels = mapOf(
            Pair(LEVEL.format(1), "level1"),
            Pair(LEVEL.format(2), "level2"),
            Pair(LEVEL.format(3), "level3"),
        )
        val actualLevels = HashMap<String, String>().apply {
            ComScore.fillLevel(this, levels)
        }
        Assert.assertEquals(expectedLevels, actualLevels)
    }

    @Test
    fun testMoreExact10Levels() {
        val levels = arrayOf("level1", "level2", "level3", "level4", "level5", "level6", "level7", "level8", "level9", "level10")
        val expectedCategory = "level1.level2.level3.level4.level5.level6.level7.level8.level9.level10"
        val actual = ComScore.getCategory(levels)
        Assert.assertEquals(expectedCategory, actual)

        val expectedLevels = mapOf(
            Pair(LEVEL.format(1), "level1"),
            Pair(LEVEL.format(2), "level2"),
            Pair(LEVEL.format(3), "level3"),
            Pair(LEVEL.format(4), "level4"),
            Pair(LEVEL.format(5), "level5"),
            Pair(LEVEL.format(6), "level6"),
            Pair(LEVEL.format(7), "level7"),
            Pair(LEVEL.format(8), "level8"),
            Pair(LEVEL.format(9), "level9"),
            Pair(LEVEL.format(10), "level10"),
        )
        val actualLevels = HashMap<String, String>().apply {
            ComScore.fillLevel(this, levels)
        }
        Assert.assertEquals(expectedLevels, actualLevels)
    }

    @Test
    fun testMoreThan10Levels() {
        val levels = arrayOf("level1", "level2", "level3", "level4", "level5", "level6", "level7", "level8", "level9", "level10", "level11")
        val expectedCategory = "level1.level2.level3.level4.level5.level6.level7.level8.level9.level10.level11"
        val actual = ComScore.getCategory(levels)
        Assert.assertEquals(expectedCategory, actual)

        val expectedLevels = mapOf(
            Pair(LEVEL.format(1), "level1"),
            Pair(LEVEL.format(2), "level2"),
            Pair(LEVEL.format(3), "level3"),
            Pair(LEVEL.format(4), "level4"),
            Pair(LEVEL.format(5), "level5"),
            Pair(LEVEL.format(6), "level6"),
            Pair(LEVEL.format(7), "level7"),
            Pair(LEVEL.format(8), "level8"),
            Pair(LEVEL.format(9), "level9"),
            Pair(LEVEL.format(10), "level10"),
        )
        val actualLevels = HashMap<String, String>().apply {
            ComScore.fillLevel(this, levels)
        }
        Assert.assertEquals(expectedLevels, actualLevels)
    }

    companion object {
        private const val LEVEL = "srg_n%s"
    }

}
