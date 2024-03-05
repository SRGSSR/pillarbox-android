/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Timeline
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ForwardingTimeline
import androidx.media3.exoplayer.source.MediaSource
import ch.srgssr.pillarbox.core.business.HttpResultException
import ch.srgssr.pillarbox.core.business.akamai.AkamaiTokenDataSource
import ch.srgssr.pillarbox.core.business.exception.BlockReasonException
import ch.srgssr.pillarbox.core.business.exception.DataParsingException
import ch.srgssr.pillarbox.core.business.exception.ResourceNotFoundException
import ch.srgssr.pillarbox.core.business.integrationlayer.ImageScalingService
import ch.srgssr.pillarbox.core.business.integrationlayer.ResourceSelector
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Chapter
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Drm
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.Resource
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.core.business.tracker.SRGEventLoggerTracker
import ch.srgssr.pillarbox.core.business.tracker.commandersact.CommandersActTracker
import ch.srgssr.pillarbox.core.business.tracker.comscore.ComScoreTracker
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerData
import ch.srgssr.pillarbox.player.extension.setTrackerData
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import ch.srgssr.pillarbox.player.source.SuspendMediaSource
import ch.srgssr.pillarbox.player.utils.DebugLogger
import io.ktor.client.plugins.ClientRequestException
import kotlinx.serialization.SerializationException
import java.io.IOException

/**
 * Mime Type for representing SRG SSR content
 */
const val MimeTypeSrg = "${MimeTypes.BASE_TYPE_APPLICATION}/srg-ssr"

/**
 * MediaSource that handle SRG SSR content.
 *
 * @param mediaItem The [MediaItem] set by the user.
 * @param mediaCompositionService The [MediaCompositionService] to load SRG SSR data.
 * @param mediaSourceFactory The [MediaSource.Factory] to create the media [MediaSource], for example HlsMediaSource or DashMediaSource.
 * @param minLiveDvrDurationMs The minimal live duration to be considered DVR.
 */
class SRGMediaSource private constructor(
    mediaItem: MediaItem,
    private val mediaCompositionService: MediaCompositionService,
    private val mediaSourceFactory: MediaSource.Factory,
    private val minLiveDvrDurationMs: Long,
) : SuspendMediaSource(mediaItem) {

    private val resourceSelector = ResourceSelector()
    private val imageScalingService = ImageScalingService()

    override suspend fun loadMediaSource(mediaItem: MediaItem): MediaSource {
        checkNotNull(mediaItem.localConfiguration)
        val result = mediaCompositionService.fetchMediaComposition(mediaItem.localConfiguration!!.uri).getOrElse {
            when (it) {
                is ClientRequestException -> {
                    throw HttpResultException(it)
                }

                is SerializationException -> {
                    throw DataParsingException(it)
                }

                else -> {
                    throw IOException(it.message)
                }
            }
        }

        val chapter = result.mainChapter
        chapter.blockReason?.let {
            throw BlockReasonException(it)
        }
        chapter.listSegment?.firstNotNullOfOrNull { it.blockReason }?.let {
            throw BlockReasonException(it)
        }

        val resource = resourceSelector.selectResourceFromChapter(chapter) ?: throw ResourceNotFoundException()
        var uri = Uri.parse(resource.url)
        if (resource.tokenType == Resource.TokenType.AKAMAI) {
            uri = AkamaiTokenDataSource.appendTokenQueryToUri(uri)
        }
        val trackerData = mediaItem.getMediaItemTrackerData().buildUpon().apply {
            putData(SRGEventLoggerTracker::class.java)
            getComScoreData(result, chapter, resource)?.let {
                putData(ComScoreTracker::class.java, it)
            }
            getCommandersActData(result, chapter, resource)?.let {
                putData(CommandersActTracker::class.java, it)
            }
        }.build()
        val loadingMediaItem = MediaItem.Builder()
            .setDrmConfiguration(fillDrmConfiguration(resource))
            .setUri(uri)
            .build()

        updateMediaItem(
            mediaItem.buildUpon()
                .setMediaMetadata(fillMetaData(mediaItem.mediaMetadata, chapter))
                .setTrackerData(trackerData)
                .build()
        )
        return mediaSourceFactory.createMediaSource(loadingMediaItem)
    }

    override fun onChildSourceInfoRefreshed(
        childSourceId: String?,
        mediaSource: MediaSource,
        newTimeline: Timeline
    ) {
        DebugLogger.debug(TAG, "onChildSourceInfoRefreshed: $childSourceId")
        super.onChildSourceInfoRefreshed(childSourceId, mediaSource, SRGTimeline(minLiveDvrDurationMs, newTimeline))
    }

    private fun fillMetaData(metadata: MediaMetadata, chapter: Chapter): MediaMetadata {
        val builder = metadata.buildUpon()
        metadata.title ?: builder.setTitle(chapter.title)
        metadata.subtitle ?: builder.setSubtitle(chapter.lead)
        metadata.description ?: builder.setDescription(chapter.description)
        metadata.artworkUri ?: run {
            val artworkUri = imageScalingService.getScaledImageUrl(
                imageUrl = chapter.imageUrl
            ).toUri()

            builder.setArtworkUri(artworkUri)
        }
        // Extras are forwarded to MediaController, but not involve in the equality checks
        // builder.setExtras(extras)
        return builder.build()
    }

    private fun fillDrmConfiguration(resource: Resource): MediaItem.DrmConfiguration? {
        val drm = resource.drmList.orEmpty().find { it.type == Drm.Type.WIDEVINE }
        return drm?.let {
            MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setLicenseUri(it.licenseUrl)
                .build()
        }
    }

    /**
     * ComScore (MediaPulse) don't want to track audio. Integration layer doesn't fill analytics labels for audio content,
     * but only in [chapter] and [resource]. MediaComposition will still have analytics content.
     */
    private fun getComScoreData(
        mediaComposition: MediaComposition,
        chapter: Chapter,
        resource: Resource
    ): ComScoreTracker.Data? {
        val comScoreData = HashMap<String, String>().apply {
            chapter.comScoreAnalyticsLabels?.let {
                mediaComposition.comScoreAnalyticsLabels?.let { mediaComposition -> putAll(mediaComposition) }
                putAll(it)
            }
            resource.comScoreAnalyticsLabels?.let { putAll(it) }
        }
        return if (comScoreData.isNotEmpty()) {
            ComScoreTracker.Data(comScoreData)
        } else {
            null
        }
    }

    /**
     * ComScore (MediaPulse) don't want to track audio. Integration layer doesn't fill analytics labels for audio content,
     * but only in [chapter] and [resource]. MediaComposition will still have analytics content.
     */
    private fun getCommandersActData(
        mediaComposition: MediaComposition,
        chapter: Chapter,
        resource: Resource
    ): CommandersActTracker.Data? {
        val commandersActData = HashMap<String, String>().apply {
            mediaComposition.analyticsLabels?.let { mediaComposition -> putAll(mediaComposition) }
            chapter.analyticsLabels?.let { putAll(it) }
            resource.analyticsLabels?.let { putAll(it) }
        }
        return if (commandersActData.isNotEmpty()) {
            // TODO : sourceId can be store inside MediaItem.metadata.extras["source_key"]
            CommandersActTracker.Data(assets = commandersActData, sourceId = null)
        } else {
            null
        }
    }

    /**
     * Pillarbox timeline wrap the underlying Timeline to suite SRGSSR needs.
     *  - Live stream with a window duration <= [minLiveDvrDurationMs] cannot seek.
     */
    private class SRGTimeline(val minLiveDvrDurationMs: Long, timeline: Timeline) : ForwardingTimeline(timeline) {

        override fun getWindow(windowIndex: Int, window: Window, defaultPositionProjectionUs: Long): Window {
            val internalWindow = timeline.getWindow(windowIndex, window, defaultPositionProjectionUs)
            if (internalWindow.isLive()) {
                internalWindow.isSeekable = internalWindow.durationMs >= minLiveDvrDurationMs
            }
            return internalWindow
        }
    }

    /**
     * Factory create a [SRGMediaSource].
     *
     * This factory handles [MediaItem] that are created from [SRGMediaItemBuilder].
     * The item must contains at least
     * - The correct mime type [MimeTypeSrg].
     * - An uri to the integration layer with the urn for example (https://il.srgssr.ch/.../byUrn/urn:...:1234).
     *
     * @param mediaSourceFactory The [MediaSource.Factory] to create the internal [MediaSource]. By default [DefaultMediaSourceFactory].
     * @param mediaCompositionService The [MediaCompositionService] to load SRG SSR data.
     */
    class Factory(
        mediaSourceFactory: DefaultMediaSourceFactory,
        private val mediaCompositionService: MediaCompositionService = HttpMediaCompositionService()
    ) :
        PillarboxMediaSourceFactory.DelegateFactory(mediaSourceFactory) {
        /**
         * Minimal duration in milliseconds to consider a live with seek capabilities.
         */
        var minLiveDvrDurationMs = LIVE_DVR_MIN_DURATION_MS

        constructor(dataSource: DataSource.Factory, mediaCompositionMediaItemSource: MediaCompositionService = HttpMediaCompositionService()) : this(
            DefaultMediaSourceFactory(dataSource),
            mediaCompositionMediaItemSource
        )

        constructor(
            context: Context,
        ) : this(
            dataSource = AkamaiTokenDataSource.Factory(defaultDataSourceFactory = DefaultDataSource.Factory(context)),
        )

        override fun handleMediaItem(mediaItem: MediaItem): Boolean {
            return mediaItem.localConfiguration?.mimeType == MimeTypeSrg || mediaItem.localConfiguration?.uri?.lastPathSegment.isValidMediaUrn()
        }

        override fun createMediaSourceInternal(mediaItem: MediaItem, mediaSourceFactory: MediaSource.Factory): MediaSource {
            return SRGMediaSource(mediaItem, mediaCompositionService, mediaSourceFactory, minLiveDvrDurationMs)
        }
    }

    companion object {
        private const val TAG = "SRGMediaSource"
        private const val LIVE_DVR_MIN_DURATION_MS = 60000L // 60s
    }
}
