/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * The showcase viewmodel
 */
class AudioVideoLiveSwitchViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * The player.
     */
    val player: PillarboxPlayer = PlayerModule.provideDefaultPlayer(application)

    /**
     * The list of the [LiveContent].
     */
    val contents = Companion.contents

    /**
     * The current selected [ContentType].
     */
    var currentContentType by mutableStateOf(ContentType.Video)
        private set

    /**
     * The current [LiveContent] to play.
     */
    var currentContent: LiveContent? by mutableStateOf(null)

    init {
        load(contents.first())
        player.prepare()
        player.play()
    }

    override fun onCleared() {
        player.release()
    }

    /**
     * Load the given [content].
     */
    fun load(content: LiveContent) {
        if (content != currentContent) {
            player.setMediaItem(content.toMediaItem(currentContentType))
            currentContent = content
        }
    }

    /**
     * Toggle [currentContentType].
     */
    fun toggleContentType() {
        currentContentType = when (currentContentType) {
            ContentType.Video -> ContentType.Audio
            ContentType.Audio -> ContentType.Video
        }
        currentContent?.let {
            val mediaItem = it.toMediaItem(currentContentType)
            val startPositionMs = getStartPosition()
            Log.d("Coucou", "change to $currentContentType at $startPositionMs")
            player.setMediaItem(mediaItem, startPositionMs)
        }
    }

    private fun getStartPosition(): Long {
        return player.currentPosition
    }

    private companion object {
        private val contents = listOf(
            LiveContent(
                label = "SRF1",
                audioUrn = "urn:srf:audio:69e8ac16-4327-4af4-b873-fd5cd6e895a7",
                videoUrn = "urn:srf:video:5b90d1fb-477b-4d98-86a6-82921a4bb0ea"
            ),
            LiveContent(
                label = "SRF3",
                audioUrn = "urn:srf:audio:dd0fa1ba-4ff6-4e1a-ab74-d7e49057d96f",
                videoUrn = "urn:srf:video:972b2dbd-3958-43b7-8c15-e92f56c8d734"
            ),
            LiveContent(
                label = "SRF Musikwelle",
                audioUrn = "urn:srf:audio:a9c5c070-8899-46c7-ac27-f04f1be902fd",
                videoUrn = "urn:srf:video:973440d3-60a5-4ddf-ae83-2c77815a32a1"
            ),
            LiveContent(
                label = "SRF Virus",
                audioUrn = "urn:srf:audio:66815fe2-9008-4853-80a5-f9caaffdf3a9",
                videoUrn = "urn:srf:video:2a60b590-8a28-4540-bce8-fc4e52b3b5d8"
            ),
            LiveContent(label = "RTS Couleur3", audioUrn = "urn:rts:audio:3262363", videoUrn = "urn:rts:video:8841634"),
        )
    }

    /**
     * The content type
     */
    @Suppress("UndocumentedPublicProperty")
    enum class ContentType {
        Video,
        Audio,
    }

    /**
     * The live content
     * @property label The label to display.
     * @property audioUrn The urn to play when the content type is Audio.
     * @property videoUrn The urn to play when the content type is Video.
     */
    data class LiveContent(val label: String, val audioUrn: String, val videoUrn: String) {

        internal fun getUrnToPlay(contentType: ContentType): String = when (contentType) {
            ContentType.Video -> videoUrn
            ContentType.Audio -> audioUrn
        }

        internal fun toMediaItem(contentType: ContentType): MediaItem = SRGMediaItem(urn = getUrnToPlay(contentType))
    }
}
