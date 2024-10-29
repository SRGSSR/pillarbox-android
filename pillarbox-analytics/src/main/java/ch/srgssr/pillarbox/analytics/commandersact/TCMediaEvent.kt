/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.BuildConfig
import com.tagcommander.lib.serverside.events.TCCustomEvent
import org.json.JSONObject
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Represents a media event to be sent to Commanders Act. This class extends `TCCustomEvent` and adds specific properties for media tracking.
 * @property eventType The type of media event, defined by the Analytics team using the [MediaEventType] enum.
 * @property assets A map representing additional data associated with the event.
 * @property sourceId An optional identifier for the source of the event.
 */
class TCMediaEvent(
    val eventType: MediaEventType,
    val assets: Map<String, String>,
    val sourceId: String? = null
) : TCCustomEvent(eventType.toString()) {
    /**
     * Represents the current playback position.
     */
    var mediaPosition: Duration = Duration.ZERO

    /**
     * Represents the time shift applied if it is a live stream, `null` otherwise.
     */
    var timeShift: Duration? = null

    /**
     * Represents the device's volume level as a percentage.
     */
    var deviceVolume: Float? = null

    /**
     * Indicates whether subtitles are enabled.
     */
    var isSubtitlesOn: Boolean = false

    /**
     * Represents the language of the currently selected subtitle track.
     */
    var subtitleSelectionLanguage: String? = null

    /**
     * Represents the language of the currently selected audio track.
     */
    var audioTrackLanguage: String? = null

    /**
     * Indicates whether the current audio track has an associated audio description.
     */
    var audioTrackHasAudioDescription: Boolean = false

    override fun getJsonObject(): JSONObject {
        val jsonObject = super.getJsonObject()
        for (asset in assets) {
            jsonObject.putIfValid(asset.key, asset.value)
        }
        jsonObject.putIfValid(KEY_SOURCE_ID, sourceId)
        jsonObject.putIfValid(MEDIA_POSITION, toSeconds(mediaPosition).toString())
        timeShift?.let {
            jsonObject.putIfValid(MEDIA_TIMESHIFT, toSeconds(it).toString())
        }
        deviceVolume?.let {
            jsonObject.putIfValid(MEDIA_VOLUME, it.toString())
        }
        jsonObject.putIfValid(MEDIA_PLAYER_VERSION, BuildConfig.VERSION_NAME)
        jsonObject.putIfValid(MEDIA_PLAYER_DISPLAY, PLAYER_DISPLAY_NAME)
        jsonObject.putIfValid(MEDIA_SUBTITLES_ON, isSubtitlesOn.toString())
        subtitleSelectionLanguage?.let {
            jsonObject.putIfValid(MEDIA_SUBTITLE_SELECTION, it.uppercase())
        }
        audioTrackLanguage?.let {
            jsonObject.putIfValid(MEDIA_AUDIO_TRACK, it.uppercase())
        }
        jsonObject.putIfValid(MEDIA_AUDIO_DESCRIPTION_ON, audioTrackHasAudioDescription.toString())

        return jsonObject
    }

    private fun JSONObject.putIfValid(key: String, value: String?) {
        if (value.isNullOrBlank()) return
        put(key, value)
    }

    private companion object {
        private const val PLAYER_DISPLAY_NAME = "Pillarbox"
        private const val MEDIA_PLAYER_VERSION = "media_player_version"
        private const val MEDIA_VOLUME = "media_volume"
        private const val MEDIA_POSITION = "media_position"
        private const val MEDIA_PLAYER_DISPLAY = "media_player_display"
        private const val MEDIA_TIMESHIFT = "media_timeshift"
        private const val MEDIA_SUBTITLES_ON = "media_subtitles_on"
        private const val MEDIA_AUDIO_TRACK = "media_audio_track"
        private const val MEDIA_SUBTITLE_SELECTION = "media_subtitle_selection"
        private const val MEDIA_AUDIO_DESCRIPTION_ON = "media_audiodescription_on"
        private const val KEY_SOURCE_ID = "source_id"

        private fun toSeconds(duration: Duration): Long {
            return duration.toDouble(DurationUnit.SECONDS).roundToLong()
        }
    }
}
