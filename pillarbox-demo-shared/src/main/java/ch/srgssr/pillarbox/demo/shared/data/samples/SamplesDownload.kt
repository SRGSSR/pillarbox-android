/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist

/**
 * Samples that are interesting to test downloads with: AES-128 encrypted, DRM-protected and Apple's content.
 */
@Suppress("StringLiteralDuplication", "MaximumLineLength", "MaxLineLength", "UndocumentedPublicProperty")
object SamplesDownload {
    val AES128EncryptedUrn = DemoItem.URN(
        title = "AES-128 encrypted video (URN)",
        urn = "urn:rsi:video:4195500",
        description = "AES-128 encrypted content",
        languageTag = "it-CH",
    )

    val AES128EncryptedTearsOfSteel = DemoItem.URL(
        title = "Tears of Steel",
        uri = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel-aes.ism/.m3u8",
        description = "AES-128 encrypted content",
        imageUri = "https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg",
        languageTag = "en-CH",
    )

    val AES128EncryptedArtOfMotion = DemoItem.URL(
        title = "Art of Motion",
        uri = "https://cdn.bitmovin.com/content/internal/assets/art-of-motion_drm/m3u8s/11331.m3u8",
        description = "AES-128 encrypted content",
        imageUri = "https://img.redbull.com/images/c_crop,w_3840,h_1920,x_0,y_0,f_auto,q_auto/c_scale,w_1200/redbullcom/tv/FO-1MR39KNMH2111/fo-1mr39knmh2111-featuremedia",
        languageTag = "en-CH",
    )

    val AES128EncryptedOceans = DemoItem.URL(
        title = "Oceans",
        uri = "https://playertest.longtailvideo.com/adaptive/oceans_aes/oceans_aes.m3u8",
        description = "AES-128 encrypted content",
        languageTag = "en-CH",
    )

    val AES128EncryptedWithTracks = DemoItem.URL(
        title = "AES-128 with tracks",
        uri = "https://playertest.longtailvideo.com/adaptive/aes-with-tracks/master.m3u8",
        description = "AES-128 encrypted content with multiple tracks",
        languageTag = "en-CH",
    )

    val DrmProtected = DemoItem.URN(
        title = "DRM-protected video (URN)",
        urn = "urn:rts:video:c980c05e-9161-35a6-852f-b9ac039e1a5c",
        description = "DRM-protected content",
        languageTag = "fr-CH",
    )

    val All = Playlist(
        title = "Download test cases",
        languageTag = "en-CH",
        items = listOf(
            AES128EncryptedUrn,
            AES128EncryptedTearsOfSteel,
            AES128EncryptedArtOfMotion,
            AES128EncryptedOceans,
            AES128EncryptedWithTracks,
            DrmProtected,
            // Apple's content
            SamplesApple.MorningShowSeason1,
        ),
    )
}
