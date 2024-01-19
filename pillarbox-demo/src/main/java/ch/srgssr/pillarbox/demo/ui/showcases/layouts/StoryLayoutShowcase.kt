/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.layouts

import androidx.compose.runtime.Composable

/**
 * Story home
 *
 * Two version available
 * - Simple which create and release for each pages.
 * - Optimized with 3 players, 1 for the 3 visible pages.
 *
 * @param optimizedStory `true` to use an optimized implementation, `false` otherwise.
 */
@Composable
fun StoryLayoutShowcase(optimizedStory: Boolean = true) {
    if (optimizedStory) {
        OptimizedStory()
    } else {
        SimpleStory()
    }
}
