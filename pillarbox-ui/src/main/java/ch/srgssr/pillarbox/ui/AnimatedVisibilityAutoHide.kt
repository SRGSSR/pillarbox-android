/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Animated visibility auto hide
 *
 * @param visible defines whether the content should be visible.
 * @param modifier modifier for the [Layout] created to contain the [content].
 * @param autoHideEnabled enable auto hide after [autoHideDelay].
 * @param autoHideDelay duration delay after automatically hide the view if [autoHideEnabled] is true.
 * @param content content to hide or show.
 */
@Composable
fun AnimatedVisibilityAutoHide(
    visible: Boolean,
    modifier: Modifier = Modifier,
    autoHideEnabled: Boolean = true,
    autoHideDelay: Duration = DEFAULT_DELAY,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var visibleState by remember(visible) {
        mutableStateOf(visible)
    }
    LaunchedEffect(autoHideEnabled, visibleState, autoHideDelay) {
        if (autoHideEnabled && visibleState && autoHideDelay > Duration.ZERO) {
            delay(autoHideDelay)
            visibleState = false
        }
    }
    AnimatedVisibility(
        visible = visibleState,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
        content = content
    )
}

private val DEFAULT_DELAY = 4_000.toDuration(DurationUnit.MILLISECONDS)
