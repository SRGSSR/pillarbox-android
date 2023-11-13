/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.story

import androidx.compose.runtime.Composable

/**
 * Story home
 *
 * Two version available
 * - Simple which create and release for each pages.
 * - Optimized with 3 players, 1 for the 3 visible pages.
 */
@Composable
fun StoryHome() {
    // SimpleStory()
    OptimizedStory()
}
