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
 * CommandersAct media event
 *
 * @property eventType Media event type defined by Analytics team.
 * @property assets Assets to populate with key,values.
 * @property sourceId an optional sourceId.
 */
class TCMediaEvent(
    val eventType: MediaEventType,
    val assets: Map<String, String>,
    val sourceId: String? = null
) : TCCustomEvent(eventType.toString()) {
    /**
     * Current Media position
     */
    var mediaPosition: Duration = Duration.ZERO

    /**
     * Time shift if applicable
     */
    var timeShift: Duration? = null

    /**
     * Device volume in percentage
     */
    var deviceVolume: Float? = null

    /**
     * Is subtitles on
     */
    var isSubtitlesOn: Boolean = false

    /**
     * Selected subtitle language if any
     */
    var subtitleSelectionLanguage: String? = null

    /**
     * Selected audio language if any
     */
    var audioTrackLanguage: String? = null

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

        return jsonObject
    }

    private fun JSONObject.putIfValid(key: String, value: String?) {
        if (value.isNullOrBlank()) return
        put(key, value)
    }

    companion object {
        private const val PLAYER_DISPLAY_NAME = "Pillarbox"
        private const val MEDIA_PLAYER_VERSION = "media_player_version"
        private const val MEDIA_VOLUME = "media_volume"
        private const val MEDIA_POSITION = "media_position"
        private const val MEDIA_PLAYER_DISPLAY = "media_player_display"
        private const val MEDIA_TIMESHIFT = "media_timeshift"
        private const val MEDIA_SUBTITLES_ON = "media_subtitles_on"
        private const val MEDIA_AUDIO_TRACK = "media_audio_track"
        private const val MEDIA_SUBTITLE_SELECTION = "media_subtitle_selection"
        private const val KEY_SOURCE_ID = "source_id"

        private fun toSeconds(duration: Duration): Long {
            return duration.toDouble(DurationUnit.SECONDS).roundToLong()
        }
    }
}
