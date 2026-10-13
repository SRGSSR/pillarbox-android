/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.extension

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActSource
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CommandersActSourceTest {

    @Test
    fun `toBundle with mandatory values`() {
        val bundle = CommandersActSource(pageId = "page_id").toBundle()

        assertEquals(setOf("pageId"), bundle.keySet())
        assertEquals("page_id", bundle.getString("pageId"))
    }

    @Test
    fun `toBundle with all values`() {
        val bundle = fullSource.toBundle()

        assertEquals(
            setOf("pageId", "pageVersion", "sectionId", "sectionVersion", "sectionPosition", "itemPositionInSection", "labels"),
            bundle.keySet(),
        )
        assertEquals("page_id", bundle.getString("pageId"))
        assertEquals("page_version", bundle.getString("pageVersion"))
        assertEquals("section_id", bundle.getString("sectionId"))
        assertEquals("section_version", bundle.getString("sectionVersion"))
        assertEquals(0, bundle.getInt("sectionPosition", -1))
        assertEquals(3, bundle.getInt("itemPositionInSection", -1))

        val labels = assertNotNull(bundle.getBundle("labels"))
        assertEquals(setOf("label1", "label2"), labels.keySet())
        assertEquals("value1", labels.getString("label1"))
        assertEquals("value2", labels.getString("label2"))
    }

    @Test
    fun `toBundle with empty labels`() {
        val bundle = CommandersActSource(pageId = "page_id", labels = emptyMap()).toBundle()

        val labels = assertNotNull(bundle.getBundle("labels"))
        assertTrue(labels.isEmpty)
    }

    @Test
    fun `toCommandersActSource with empty bundle`() {
        assertNull(Bundle().toCommandersActSource())
    }

    @Test
    fun `toCommandersActSource without page id`() {
        val bundle = Bundle().apply {
            putString("pageVersion", "page_version")
            putString("sectionId", "section_id")
            putInt("sectionPosition", 1)
        }

        assertNull(bundle.toCommandersActSource())
    }

    @Test
    fun `toCommandersActSource with mandatory values`() {
        val bundle = Bundle().apply {
            putString("pageId", "page_id")
        }

        assertEquals(CommandersActSource(pageId = "page_id"), bundle.toCommandersActSource())
    }

    @Test
    fun `toCommandersActSource with all values`() {
        val bundle = Bundle().apply {
            putString("pageId", "page_id")
            putString("pageVersion", "page_version")
            putString("sectionId", "section_id")
            putString("sectionVersion", "section_version")
            putInt("sectionPosition", 0)
            putInt("itemPositionInSection", 3)
            putBundle(
                "labels",
                Bundle().apply {
                    putString("label1", "value1")
                    putString("label2", "value2")
                },
            )
        }

        assertEquals(fullSource, bundle.toCommandersActSource())
    }

    @Test
    fun `toCommandersActSource ignores non string labels`() {
        val bundle = Bundle().apply {
            putString("pageId", "page_id")
            putBundle(
                "labels",
                Bundle().apply {
                    putString("label1", "value1")
                    putInt("label2", 2)
                },
            )
        }

        assertEquals(
            CommandersActSource(pageId = "page_id", labels = mapOf("label1" to "value1")),
            bundle.toCommandersActSource(),
        )
    }

    @Test
    fun `toCommandersActSource ignores unknown keys`() {
        val bundle = Bundle().apply {
            putString("pageId", "page_id")
            putString("unknown", "value")
        }

        assertEquals(CommandersActSource(pageId = "page_id"), bundle.toCommandersActSource())
    }

    @Test
    fun `round trip with mandatory values`() {
        val source = CommandersActSource(pageId = "page_id")

        assertEquals(source, source.toBundle().toCommandersActSource())
    }

    @Test
    fun `round trip with all values`() {
        assertEquals(fullSource, fullSource.toBundle().toCommandersActSource())
    }

    @Test
    fun `round trip with empty labels`() {
        val source = CommandersActSource(pageId = "page_id", labels = emptyMap())

        assertEquals(source, source.toBundle().toCommandersActSource())
    }

    @Test
    fun `round trip with negative positions`() {
        val source = CommandersActSource(pageId = "page_id", sectionPosition = -1, itemPositionInSection = Int.MIN_VALUE)

        assertEquals(source, source.toBundle().toCommandersActSource())
    }

    private companion object {
        private val fullSource = CommandersActSource(
            pageId = "page_id",
            pageVersion = "page_version",
            sectionId = "section_id",
            sectionVersion = "section_version",
            sectionPosition = 0,
            itemPositionInSection = 3,
            labels = mapOf("label1" to "value1", "label2" to "value2"),
        )
    }
}
