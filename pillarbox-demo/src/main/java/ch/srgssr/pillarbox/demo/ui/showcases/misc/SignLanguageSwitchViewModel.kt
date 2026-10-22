/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.demo.shared.di.PlayerModule
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * The sign language switch showcase viewmodel
 */
class SignLanguageSwitchViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * The player.
     */
    val player: PillarboxPlayer = PlayerModule.provideDefaultPlayer(application)

    /**
     * Whether the signed version of [content] is currently played.
     */
    var isSigned by mutableStateOf(false)
        private set

    init {
        player.setMediaItem(content.toMediaItem(isSigned))
        player.prepare()
        player.play()
    }

    override fun onCleared() {
        player.release()
    }

    /**
     * Toggle between the normal and the signed version of [content], keeping the current position.
     */
    fun toggleSigned() {
        isSigned = !isSigned
        player.setMediaItem(content.toMediaItem(isSigned), player.currentPosition)
    }

    companion object {
        /**
         * The content to play.
         */
        val content = SignedContent(
            label = "19h30 - 20.10.2026",
            urn = "urn:rts:video:cc02e588-f0d2-3c28-bc58-c6f338b06785",
            signedUrn = "urn:rts:video:0fabbd87-102e-3c48-8bb9-7730679f2f96",
        )
    }

    /**
     * A content available with a sign language interpreter.
     * @property label The label to display.
     * @property urn The urn to play without sign language interpreter.
     * @property signedUrn The urn to play with sign language interpreter.
     */
    data class SignedContent(val label: String, val urn: String, val signedUrn: String) {

        internal fun toMediaItem(signed: Boolean): MediaItem = SRGMediaItem(urn = if (signed) signedUrn else urn)
    }
}
