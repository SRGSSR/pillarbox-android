/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.ui.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * Toggle view
 *
 * @param visibilityState A state that holds the current visibility and auto hide delay mode.
 * @param toggleableContent Content to show or hide based on the value of [ToggleVisibilityState.isDisplayed].
 * @param modifier modifier for the Layout created to contain the content.
 * @param toggleable content is toggleable.
 * @param contentAlignment - The default alignment inside the Box.
 * @param propagateMinConstraints - Whether the incoming min constraints should be passed to content.
 * @param enter EnterTransition(s) used for the appearing animation, fading in while expanding by default.
 * @param exit ExitTransition(s) used for the disappearing animation, fading out while shrinking by default.
 * @param content Content displayed under toggleableContent.
 */
@Composable
fun ToggleableBox(
    visibilityState: DelayedVisibilityState,
    toggleableContent: @Composable AnimatedVisibilityScope.() -> Unit,
    modifier: Modifier = Modifier,
    toggleable: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    enter: EnterTransition = expandVertically { it },
    exit: ExitTransition = shrinkVertically { it },
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .toggleable(
                enabled = toggleable,
                delayedVisibilityState = visibilityState
            ),
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints
    ) {
        content(this)
        val animatedModifier = if (toggleable) {
            Modifier
                .matchParentSize()
                .maintainVisibleOnFocus(delayedVisibilityState = visibilityState)
        } else {
            Modifier.matchParentSize()
        }
        AnimatedVisibility(
            modifier = animatedModifier,
            visible = visibilityState.isVisible,
            enter = enter,
            exit = exit,
            content = toggleableContent,
        )
    }
}

@Preview
@Composable
private fun TogglePreview() {
    var delay by remember {
        mutableStateOf(DelayedVisibilityState.DefaultDuration)
    }
    var toggleable by remember {
        mutableStateOf(true)
    }
    val visibilityState = rememberDelayedVisibilityState(duration = delay)
    val coroutineScope = rememberCoroutineScope()
    Column {
        ToggleableBox(
            visibilityState = visibilityState,
            modifier = Modifier.aspectRatio(16 / 9f),
            toggleable = toggleable,
            toggleableContent = {
                BasicText(text = "Text to hide", color = { Color.Red })
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                text = "Show",
                modifier = Modifier.clickable {
                    visibilityState.show()
                }
            )
            BasicText(
                text = "Toggle",
                modifier = Modifier.clickable {
                    visibilityState.toggle()
                }
            )
            BasicText(
                text = "Hide",
                modifier = Modifier.clickable {
                    visibilityState.hide()
                }
            )
            BasicText(
                text = "Disable",
                modifier = Modifier.clickable {
                    delay = Duration.ZERO
                }
            )
            BasicText(
                text = "Enable",
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        delay = DelayedVisibilityState.DefaultDuration
                    }
                }
            )
        }
        Row {
            BasicText(
                text = "Toggleable",
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        toggleable = !toggleable
                    }
                }
            )
        }
    }
}
