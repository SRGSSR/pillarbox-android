/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data

import ch.srg.dataProvider.integrationlayer.data.ImageUrl
import ch.srg.dataProvider.integrationlayer.data.remote.Media
import ch.srg.dataProvider.integrationlayer.data.remote.MediaType
import ch.srg.dataProvider.integrationlayer.data.remote.Type
import ch.srg.dataProvider.integrationlayer.data.remote.Vendor
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ContentTest {
    @Test
    fun getMediaDate() {
        Locale.setDefault(Locale("fr", "CH"))

        val media = Media(
            id = "id",
            mediaType = MediaType.VIDEO,
            vendor = Vendor.RTS,
            urn = "urn:media:id",
            title = "Media title",
            type = Type.CLIP,
            date = Instant.fromEpochMilliseconds(1703493045000L),
            duration = 30.seconds.inWholeMilliseconds,
            imageUrl = ImageUrl("https://image2.png"),
            playableAbroad = true
        )
        val content = Content.Media(media)

        assertEquals("25.12.23", content.date)
    }

    @Test
    fun getMediaDurationLessThanAMinute() {
        val media = Media(
            id = "id",
            mediaType = MediaType.VIDEO,
            vendor = Vendor.RTS,
            urn = "urn:media:id",
            title = "Media title",
            type = Type.CLIP,
            date = Instant.fromEpochMilliseconds(1703493045L),
            duration = 30.seconds.inWholeMilliseconds,
            imageUrl = ImageUrl("https://image2.png"),
            playableAbroad = true
        )
        val content = Content.Media(media)

        assertEquals(1L, content.duration)
    }

    @Test
    fun getMediaDurationLessThanAnHour() {
        val media = Media(
            id = "id",
            mediaType = MediaType.VIDEO,
            vendor = Vendor.RTS,
            urn = "urn:media:id",
            title = "Media title",
            type = Type.CLIP,
            date = Instant.fromEpochMilliseconds(1703493045L),
            duration = 45.5.minutes.inWholeMilliseconds,
            imageUrl = ImageUrl("https://image2.png"),
            playableAbroad = true
        )
        val content = Content.Media(media)

        assertEquals(45L, content.duration)
    }

    @Test
    fun getMediaDurationMoreThanAnHour() {
        val media = Media(
            id = "id",
            mediaType = MediaType.VIDEO,
            vendor = Vendor.RTS,
            urn = "urn:media:id",
            title = "Media title",
            type = Type.CLIP,
            date = Instant.fromEpochMilliseconds(1703493045L),
            duration = 2.3.hours.inWholeMilliseconds,
            imageUrl = ImageUrl("https://image2.png"),
            playableAbroad = true
        )
        val content = Content.Media(media)

        assertEquals(138L, content.duration)
    }
}
