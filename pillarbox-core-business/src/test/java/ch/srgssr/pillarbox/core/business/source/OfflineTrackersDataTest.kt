/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import ch.srgssr.pillarbox.analytics.commandersact.CommandersActSource
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OfflineTrackersDataTest {
    @Test
    fun `encode and decode all data`() {
        val comScoreData = ComScoreTracker.Data(mapOf("ns_st_ci" to "123"))
        val commandersActData = CommandersActTracker.Data(
            assets = mapOf("media_urn" to "urn:rts:video:123"),
            source = CommandersActSource(
                pageId = "page",
                pageVersion = "1",
                sectionId = "section",
                sectionVersion = "2",
                sectionPosition = 3,
                itemPositionInSection = 4,
                labels = mapOf("key" to "value"),
            ),
        )
        val data = OfflineTrackersData.decode(OfflineTrackersData.from(comScoreData, commandersActData).encode())

        assertNotNull(data)
        assertEquals(comScoreData, data.comScoreData)
        assertEquals(commandersActData, data.commandersActData)
    }

    @Test
    fun `encode and decode without source`() {
        val commandersActData = CommandersActTracker.Data(assets = mapOf("media_urn" to "urn:rts:video:123"))
        val data = OfflineTrackersData.decode(OfflineTrackersData.from(null, commandersActData).encode())

        assertNotNull(data)
        assertNull(data.comScoreData)
        assertEquals(commandersActData, data.commandersActData)
    }

    @Test
    fun `encode and decode without data`() {
        val data = OfflineTrackersData.decode(OfflineTrackersData.from(null, null).encode())

        assertNotNull(data)
        assertNull(data.comScoreData)
        assertNull(data.commandersActData)
    }

    @Test
    fun `decode invalid data`() {
        assertNull(OfflineTrackersData.decode("invalid".encodeToByteArray()))
        assertNull(OfflineTrackersData.decode("{\"comScore\":1}".encodeToByteArray()))
    }

    @Test
    fun `decode ignores unknown keys`() {
        val data = OfflineTrackersData.decode("{\"comScore\":{\"a\":\"b\"},\"unknown\":true}".encodeToByteArray())

        assertEquals(ComScoreTracker.Data(mapOf("a" to "b")), data?.comScoreData)
    }
}
