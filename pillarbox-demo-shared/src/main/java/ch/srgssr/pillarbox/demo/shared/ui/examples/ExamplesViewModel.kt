/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.examples

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.srg.dataProvider.integrationlayer.request.parameters.Bu
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesApple
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesBitmovin
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesDASHIF
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesGoogle
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesOther
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesUnifiedStreaming
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.demo.shared.source.BlockedTimeRangeAssetLoader
import ch.srgssr.pillarbox.demo.shared.ui.integrationLayer.data.ILRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * Examples view model
 *
 * @param application Android Application to create [ILRepository]
 */
@Suppress("StringLiteralDuplication")
class ExamplesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ILRepository = PlayerModule.createIlRepository(application)

    /**
     * Contents to display
     */
    val contents: StateFlow<List<Playlist>> = flow {
        val listDrmContent = repository.getLatestMediaByShowUrn(SHOW_URN, PROTECTED_CONTENT_PAGE_SIZE).getOrDefault(emptyList())
            .map { item ->
                val showTitle = item.show?.title.orEmpty()

                DemoItem.URN(
                    title = if (showTitle.isNotBlank()) {
                        "$showTitle (${item.title})"
                    } else {
                        item.title
                    },
                    urn = item.urn,
                    description = "DRM-protected video",
                    imageUri = item.imageUrl.rawUrl,
                    languageTag = "fr-CH",
                )
            }
        val listTokenProtectedContent = repository.getTvLiveCenter(Bu.RTS, PROTECTED_CONTENT_PAGE_SIZE).getOrDefault(emptyList())
            .map { item ->
                DemoItem.URN(
                    title = item.title,
                    urn = item.urn,
                    description = "Token-protected video",
                    imageUri = item.imageUrl.rawUrl,
                    languageTag = "fr-CH",
                )
            }
        val allProtectedContent = listDrmContent + listTokenProtectedContent

        if (allProtectedContent.isEmpty()) {
            emit(examplesPlaylists)
        } else {
            val protectedPlaylist = Playlist(
                title = "Protected streams (URNs)",
                items = allProtectedContent,
                languageTag = "en-CH",
            )
            val updatedPlaylists = examplesPlaylists.toMutableList()
                .apply {
                    add(PROTECTED_STREAMS_PLAYLIST_INDEX, protectedPlaylist)
                }

            emit(updatedPlaylists)
        }
    }.stateIn(viewModelScope, started = SharingStarted.Eagerly, examplesPlaylists)

    private companion object {
        private const val PROTECTED_CONTENT_PAGE_SIZE = 2
        private const val PROTECTED_STREAMS_PLAYLIST_INDEX = 2
        private const val SHOW_URN = "urn:rts:show:tv:532539"

        private val examplesPlaylists = listOf(
            Playlist(
                title = "SRG SSR streams (URLs)",
                languageTag = "en-CH",
                items = listOf(
                    SamplesSRG.OnDemandHLS,
                    SamplesSRG.ShortOnDemandVideoHLS,
                    SamplesSRG.OnDemandVideoMP4,
                    SamplesSRG.LiveVideoHLS,
                    SamplesSRG.DvrVideoHLS,
                    SamplesOther.LiveTimestampVideoHLS,
                    SamplesSRG.OnDemandAudioMP3,
                    SamplesSRG.LiveAudioMP3,
                    SamplesSRG.DvrAudioHLS,
                )
            ),
            Playlist(
                title = "SRG SSR streams (URNs)",
                languageTag = "en-CH",
                items = listOf(
                    SamplesSRG.STXT_DashOriginL1,
                    SamplesSRG.STXT_DashOriginSDOnly,
                    SamplesSRG.STXT_DashOriginL3,
                    SamplesSRG.DvrVideo,
                    SamplesSRG.DvrAudio,
                    SamplesSRG.SuperfluouslyTokenProtectedVideo,
                    SamplesSRG.LiveVideo,
                    SamplesSRG.OnDemandAudio,
                    SamplesSRG.MultiAudioWithAccessibility,
                    SamplesSRG.BlockedSegment,
                    SamplesSRG.OverlapinglockedSegments,
                ),
            ),
            SamplesGoogle.All,
            SamplesApple.All,
            Playlist(
                title = "Third-party streams",
                items = listOf(
                    SamplesOther.OnDemandVideoUHD,
                ),
                languageTag = "en-CH",
            ),
            SamplesBitmovin.All,
            SamplesUnifiedStreaming.HLS,
            SamplesUnifiedStreaming.DASH,
            Playlist(
                title = "Aspect ratios",
                items = listOf(
                    SamplesSRG.OnDemandHorizontalVideo,
                    SamplesSRG.OnDemandSquareVideo,
                    SamplesSRG.OnDemandVerticalVideo,
                ),
                languageTag = "en-CH",
            ),
            Playlist(
                title = "Unbuffered streams",
                items = listOf(
                    SamplesSRG.LiveVideoHLS,
                    SamplesSRG.LiveAudioMP3,
                ),
                languageTag = "en-CH",
            ),
            SamplesDASHIF.All,
            Playlist(
                title = "Corner cases",
                items = listOf(
                    SamplesSRG.Expired,
                    SamplesSRG.Unknown,
                    DemoItem.URL(
                        title = "Custom MediaSource",
                        uri = "https://custom-media.ch/fondue",
                        description = "Using a custom CustomMediaSource",
                        languageTag = "en-CH",
                    ),
                    BlockedTimeRangeAssetLoader.DemoItemBlockedTimeRangeAtStartAndEnd,
                    BlockedTimeRangeAssetLoader.DemoItemBlockedTimeRangeOverlaps,
                    BlockedTimeRangeAssetLoader.DemoItemBlockedTimeRangeIncluded,
                ),
                languageTag = "en-CH",
            ),
        )
    }
}
