/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist

/**
 * Samples that can be queried from https://pillarbox-backend-apple.onrender.com/media
 */
object SampleStandard {
    @Suppress("UndocumentedPublicProperty")
    val playlist = Playlist(
        title = "Vapor standard server",
        items = listOf(
            DemoItem.Standard(id = "pillarbox:apple-basic-16-9", title = "Apple basic 16:9"),
            DemoItem.Standard(id = "pillarbox:bip", title = "Bip bop"),
            DemoItem.Standard(id = "pillarbox:abe", title = "ABE"),
            DemoItem.Standard(id = "pillarbox:topmodels", title = "Top models", description = "Content with DRM (faireplay)"),
        )
    )
}
