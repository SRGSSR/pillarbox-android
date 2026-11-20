/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.misc

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import ch.srgssr.pillarbox.core.business.PillarboxExoPlayer
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaCompositionResponse
import ch.srgssr.pillarbox.core.business.integrationlayer.service.HttpMediaCompositionService
import ch.srgssr.pillarbox.core.business.integrationlayer.service.MediaCompositionService
import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * The live to VOD transition showcase view model.
 * It reloads the content when it switches from live to VOD.
 */
class LiveToVodViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * The player.
     */
    val player: PillarboxPlayer = PillarboxExoPlayer(application) {
        srgAssetLoader(application) {
            mediaCompositionService(LocalMediaCompositionService())
        }
    }

    private val content = DemoItem.URN(
        urn = URN_TEST,
        title = "Live to VOD transition",
    )

    init {
        restart()
    }

    /**
     * Play the content from the start.
     */
    fun restart() {
        player.setMediaItem(content.toMediaItem())
        player.prepare()
        player.play()
    }

    override fun onCleared() {
        player.release()
    }

    /**
     * A [MediaCompositionService] that loads the media composition from a local test server instead of the integration layer,
     * by replacing the host of the integration layer URL with [METADATA_HOST].
     */
    private class LocalMediaCompositionService(
        private val httpMediaCompositionService: MediaCompositionService = HttpMediaCompositionService(),
    ) : MediaCompositionService {
        override suspend fun fetchMediaComposition(uri: Uri): Result<MediaCompositionResponse> {
            val host = METADATA_HOST.toUri()
            val localUri = uri.buildUpon()
                .scheme(host.scheme)
                .encodedAuthority(host.encodedAuthority)
                .build()
            return httpMediaCompositionService.fetchMediaComposition(localUri)
        }
    }

    companion object {
        /**
         * The URN of the content to play.
         */
        const val URN_TEST = "urn:rts:video:1967124"

        /**
         * The host of the local test server serving the media composition.
         */
        const val METADATA_HOST = "http://10.0.2.2:8080"
        // const val URL_TEST = "http://10.0.2.2:8080/metadata"
    }
}
