/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.Playlist

/**
 * All sample merged.
 */
@Suppress("StringLiteralDuplication", "MaximumLineLength", "MaxLineLength", "UndocumentedPublicProperty")
object SamplesAll {
    val playlist = Playlist(
        title = "All samples",
        items =
        SamplesSRG.StreamUrls.items +
            SamplesSRG.StreamUrns.items +
            SamplesGoogle.All.items +
            SamplesApple.All.items +
            SamplesBitmovin.All.items +
            SamplesUnifiedStreaming.All.items +
            SamplesOther.All.items

    )
}
