/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data.samples

import ch.srgssr.pillarbox.demo.shared.data.DemoItem
import ch.srgssr.pillarbox.demo.shared.data.Playlist

object SampleStandard {
    @Suppress("UndocumentedPublicProperty")
    val playlist = Playlist(
        title = "Standard connector",
        items = listOf(
            DemoItem.Standard(id = "pillarbox:video:1", title = "Video1"),
            DemoItem.Standard(id = "pillarbox:video:2", title = "Video1 with blocked interval"),
            DemoItem.Standard(id = "pillarbox:video:blocked", title = "Blocked"),
            DemoItem.Standard(id = "pillarbox:no_source", title = "No source"),
            DemoItem.Standard(id = "pillarbox:not_found", title = "Not found"),
        )
    )
}
