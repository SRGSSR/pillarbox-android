/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import ch.srgssr.pillarbox.analytics.commandersact.CommandersActSource
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * The analytics data of an SRG SSR media, stored with its download so that its playback can be tracked offline.
 *
 * Only the data is stored. The tracker factories are provided again by [SRGAssetLoader] when the downloaded media is played.
 */
@Serializable
internal data class OfflineTrackersData(
    @SerialName("comScore") val comScore: Map<String, String>? = null,
    @SerialName("commandersAct") val commandersAct: CommandersAct? = null,
) {
    @Serializable
    data class CommandersAct(
        @SerialName("assets") val assets: Map<String, String>,
        @SerialName("source") val source: Source? = null,
    )

    @Serializable
    data class Source(
        @SerialName("pageId") val pageId: String,
        @SerialName("pageVersion") val pageVersion: String? = null,
        @SerialName("sectionId") val sectionId: String? = null,
        @SerialName("sectionVersion") val sectionVersion: String? = null,
        @SerialName("sectionPosition") val sectionPosition: Int? = null,
        @SerialName("itemPositionInSection") val itemPositionInSection: Int? = null,
        @SerialName("labels") val labels: Map<String, String>? = null,
    )

    val comScoreData: ComScoreTracker.Data?
        get() = comScore?.let(ComScoreTracker::Data)

    val commandersActData: CommandersActTracker.Data?
        get() = commandersAct?.let { CommandersActTracker.Data(assets = it.assets, source = it.source?.toCommandersActSource()) }

    fun encode(): ByteArray {
        return json.encodeToString(this).encodeToByteArray()
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun from(comScoreData: ComScoreTracker.Data?, commandersActData: CommandersActTracker.Data?): OfflineTrackersData {
            return OfflineTrackersData(
                comScore = comScoreData?.assets,
                commandersAct = commandersActData?.let { CommandersAct(assets = it.assets, source = it.source?.toSource()) },
            )
        }

        /**
         * Decodes the data encoded with [encode], or returns `null` if it is invalid.
         */
        fun decode(data: ByteArray): OfflineTrackersData? {
            return try {
                json.decodeFromString<OfflineTrackersData>(data.decodeToString())
            } catch (_: SerializationException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
        }

        private fun CommandersActSource.toSource(): Source {
            return Source(
                pageId = pageId,
                pageVersion = pageVersion,
                sectionId = sectionId,
                sectionVersion = sectionVersion,
                sectionPosition = sectionPosition,
                itemPositionInSection = itemPositionInSection,
                labels = labels,
            )
        }

        private fun Source.toCommandersActSource(): CommandersActSource {
            return CommandersActSource(
                pageId = pageId,
                pageVersion = pageVersion,
                sectionId = sectionId,
                sectionVersion = sectionVersion,
                sectionPosition = sectionPosition,
                itemPositionInSection = itemPositionInSection,
                labels = labels,
            )
        }
    }
}
