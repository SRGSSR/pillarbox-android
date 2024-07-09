/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.BuildConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.minutes

class TCMediaEventTest {
    @Test
    fun `convert event to JSONObject`() {
        val tcEvent = TCMediaEvent(
            eventType = MediaEventType.Play,
            assets = emptyMap(),
        )
        val json = tcEvent.jsonObject

        assertEquals(7, json.length())

        // Properties set by TCEvent
        assertEquals("play", json.getString("event_name"))
        assertNotNull(json.optJSONObject("context"))
        assertNotNull(json.getJSONObject("context").getString("event_id"))
        assertFalse(json.has("page_type"))
        assertFalse(json.has("page_name"))

        // Properties set by TCMediaEvent
        assertEquals("0", json.getString("media_position"))
        assertEquals(BuildConfig.VERSION_NAME, json.getString("media_player_version"))
        assertEquals("Pillarbox", json.getString("media_player_display"))
        assertEquals("false", json.getString("media_subtitles_on"))
        assertEquals("false", json.getString("media_audiodescription_on"))
    }

    @Test
    fun `convert event with properties set to JSONObject`() {
        val tcEvent = TCMediaEvent(
            eventType = MediaEventType.Play,
            assets = mapOf(
                "key1" to "value1",
                "key2" to "",
                "key3" to "value3",
                "key4" to " ",
            ),
            sourceId = "sourceId",
        ).apply {
            mediaPosition = 2.5.minutes
            timeShift = 1.minutes
            deviceVolume = 0.5f
            isSubtitlesOn = true
            subtitleSelectionLanguage = "German"
            audioTrackLanguage = "French"
            audioTrackHasAudioDescription = true
        }
        val json = tcEvent.jsonObject

        assertEquals(14, json.length())

        // Properties set by TCEvent
        assertEquals("play", json.getString("event_name"))
        assertNotNull(json.optJSONObject("context"))
        assertNotNull(json.getJSONObject("context").getString("event_id"))
        assertFalse(json.has("page_type"))
        assertFalse(json.has("page_name"))

        // Properties set by TCMediaEvent
        assertEquals("value1", json.getString("key1"))
        assertEquals("value3", json.getString("key3"))
        assertEquals("sourceId", json.getString("source_id"))
        assertEquals("150", json.getString("media_position"))
        assertEquals("60", json.getString("media_timeshift"))
        assertEquals("0.5", json.getString("media_volume"))
        assertEquals(BuildConfig.VERSION_NAME, json.getString("media_player_version"))
        assertEquals("Pillarbox", json.getString("media_player_display"))
        assertEquals("true", json.getString("media_subtitles_on"))
        assertEquals("GERMAN", json.getString("media_subtitle_selection"))
        assertEquals("FRENCH", json.getString("media_audio_track"))
        assertEquals("true", json.getString("media_audiodescription_on"))
    }
}
