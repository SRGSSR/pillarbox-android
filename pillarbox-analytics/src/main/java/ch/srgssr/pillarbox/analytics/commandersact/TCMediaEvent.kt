/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import ch.srgssr.pillarbox.analytics.BuildConfig
import com.tagcommander.lib.serverside.events.TCCustomEvent
import com.tagcommander.lib.serverside.schemas.TCEventPropertiesNames
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
     * Bandwidth
     */
    var bandwidth: Long? = null

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
        val properties = jsonObject.getJSONObject(TCEventPropertiesNames.TCE_PROPERTIES)
        for (asset in assets) {
            properties.putIfValid(asset.key, asset.value)
        }
        properties.putIfValid(KEY_SOURCE_ID, sourceId)
        properties.putIfValid(MEDIA_POSITION, toSeconds(mediaPosition).toString())
        timeShift?.let {
            properties.putIfValid(MEDIA_TIMESHIFT, toSeconds(it).toString())
        }
        bandwidth?.let {
            properties.putIfValid(MEDIA_BANDWIDTH, it.toString())
        }
        deviceVolume?.let {
            properties.putIfValid(MEDIA_VOLUME, it.toString())
        }
        properties.putIfValid(MEDIA_PLAYER_VERSION, BuildConfig.VERSION_NAME)
        properties.putIfValid(MEDIA_PLAYER_DISPLAY, PLAYER_DISPLAY_NAME)
        properties.putIfValid(MEDIA_SUBTITLES_ON, isSubtitlesOn.toString())
        subtitleSelectionLanguage?.let {
            properties.putIfValid(MEDIA_SUBTITLE_SELECTION, it)
        }
        audioTrackLanguage?.let {
            properties.putIfValid(MEDIA_AUDIO_TRACK, it)
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
        private const val MEDIA_BANDWIDTH = "media_bandwidth"
        private const val MEDIA_SUBTITLES_ON = "media_subtitles_on"
        private const val MEDIA_AUDIO_TRACK = "media_audio_track"
        private const val MEDIA_SUBTITLE_SELECTION = "media_subtitle_selection"
        private const val KEY_SOURCE_ID = "source_id"

        private fun toSeconds(duration: Duration): Long {
            return duration.toDouble(DurationUnit.SECONDS).roundToLong()
        }
    }
}
