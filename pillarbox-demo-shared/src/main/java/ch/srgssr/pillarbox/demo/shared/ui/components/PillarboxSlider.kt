/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.shared.extension.onDpadEvent
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onSurface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_primary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_surfaceVariant
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onSurface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_primary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_surfaceVariant

/**
 * Custom slider component that can be used on mobile devices as well as TV.
 *
 * @param value The current value of this slider.
 * @param range The range of values supported by this slider.
 * @param compactMode If `true`, the slider will be thinner.
 * @param modifier The [Modifier] to apply to the layout.
 * @param enabled Whether this slider is enabled.
 * @param thumbColorEnabled The thumb color when the component is enabled.
 * @param thumbColorDisabled The thumb color when the component is disabled.
 * @param activeTrackColorEnabled The active track color when the component is enabled.
 * @param activeTrackColorDisabled The active track color when the component is disabled.
 * @param inactiveTrackColorEnabled The inactive track color when the component is enabled.
 * @param inactiveTrackColorDisabled The inactive track color when the component is disabled.
 * @param onValueChange The action to perform whenever the slider value changes.
 * @param onSeekBack The action to perform when seeking back.
 * @param onSeekForward The action to perform when seeking forward.
 */
@Composable
fun PillarboxSlider(
    value: Long,
    range: LongRange,
    compactMode: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    thumbColorEnabled: Color,
    thumbColorDisabled: Color,
    activeTrackColorEnabled: Color,
    activeTrackColorDisabled: Color,
    inactiveTrackColorEnabled: Color,
    inactiveTrackColorDisabled: Color,
    onValueChange: (value: Long) -> Unit = {},
    onSeekBack: () -> Unit = {},
    onSeekForward: () -> Unit = {},
) {
    PillarboxSliderInternal(
        activeTrackWeight = value / range.last.toFloat(),
        modifier = modifier,
        compactMode = compactMode,
        enabled = enabled,
        thumbColorEnabled = thumbColorEnabled,
        thumbColorDisabled = thumbColorDisabled,
        activeTrackColorEnabled = activeTrackColorEnabled,
        activeTrackColorDisabled = activeTrackColorDisabled,
        inactiveTrackColorEnabled = inactiveTrackColorEnabled,
        inactiveTrackColorDisabled = inactiveTrackColorDisabled,
        onSliderClick = { ratio ->
            onValueChange((ratio * (range.last - range.start)).toLong())
        },
        onSeekBack = onSeekBack,
        onSeekForward = onSeekForward,
    )
}

/**
 * Custom slider component that can be used on mobile devices as well as TV.
 *
 * @param value The current value of this slider.
 * @param range The range of values supported by this slider.
 * @param compactMode If `true`, the slider will be thinner.
 * @param modifier The [Modifier] to apply to the layout.
 * @param enabled Whether this slider is enabled.
 * @param thumbColorEnabled The thumb color when the component is enabled.
 * @param thumbColorDisabled The thumb color when the component is disabled.
 * @param activeTrackColorEnabled The active track color when the component is enabled.
 * @param activeTrackColorDisabled The active track color when the component is disabled.
 * @param inactiveTrackColorEnabled The inactive track color when the component is enabled.
 * @param inactiveTrackColorDisabled The inactive track color when the component is disabled.
 * @param onValueChange The action to perform whenever the slider value changes.
 * @param onSeekBack The action to perform when seeking back.
 * @param onSeekForward The action to perform when seeking forward.
 */
@Composable
fun PillarboxSlider(
    value: Float,
    range: ClosedRange<Float>,
    compactMode: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    thumbColorEnabled: Color,
    thumbColorDisabled: Color,
    activeTrackColorEnabled: Color,
    activeTrackColorDisabled: Color,
    inactiveTrackColorEnabled: Color,
    inactiveTrackColorDisabled: Color,
    onValueChange: (value: Float) -> Unit = {},
    onSeekBack: () -> Unit = {},
    onSeekForward: () -> Unit = {},
) {
    PillarboxSliderInternal(
        activeTrackWeight = value / range.endInclusive,
        modifier = modifier,
        compactMode = compactMode,
        enabled = enabled,
        thumbColorEnabled = thumbColorEnabled,
        thumbColorDisabled = thumbColorDisabled,
        activeTrackColorEnabled = activeTrackColorEnabled,
        activeTrackColorDisabled = activeTrackColorDisabled,
        inactiveTrackColorEnabled = inactiveTrackColorEnabled,
        inactiveTrackColorDisabled = inactiveTrackColorDisabled,
        onSliderClick = { ratio ->
            onValueChange(ratio * (range.endInclusive - range.start))
        },
        onSeekBack = onSeekBack,
        onSeekForward = onSeekForward,
    )
}

@Composable
private fun PillarboxSliderInternal(
    activeTrackWeight: Float,
    compactMode: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    thumbColorEnabled: Color,
    thumbColorDisabled: Color,
    activeTrackColorEnabled: Color,
    activeTrackColorDisabled: Color,
    inactiveTrackColorEnabled: Color,
    inactiveTrackColorDisabled: Color,
    onSliderClick: (ratio: Float) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
) {
    val seekBarHeight by animateDpAsState(targetValue = if (compactMode) 8.dp else 16.dp, label = "seek_bar_height")
    val thumbColor by animateColorAsState(targetValue = if (enabled) thumbColorEnabled else thumbColorDisabled, label = "thumb_color")

    val activeTrackWeight by animateFloatAsState(targetValue = activeTrackWeight, label = "active_track_weight")
    val activeTrackColor by animateColorAsState(
        targetValue = if (enabled) activeTrackColorEnabled else activeTrackColorDisabled,
        label = "active_track_color",
    )

    val inactiveTrackWeight by animateFloatAsState(targetValue = 1f - activeTrackWeight, label = "inactive_track_weight")
    val inactiveTrackColor by animateColorAsState(
        targetValue = if (enabled) inactiveTrackColorEnabled else inactiveTrackColorDisabled,
        label = "inactive_track_color",
    )

    Row(
        modifier = modifier
            .height(seekBarHeight)
            .pointerInput(Unit) {
                detectTapGestures {
                    onSliderClick(it.x / size.width)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    onSliderClick(change.position.x / size.width)
                }
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Track(
            weight = activeTrackWeight,
            color = activeTrackColor,
        )

        Thumb(
            color = thumbColor,
            enabled = enabled,
            onSeekBack = onSeekBack,
            onSeekForward = onSeekForward,
        )

        Track(
            weight = inactiveTrackWeight,
            color = inactiveTrackColor,
        )
    }
}

@Composable
private fun RowScope.Track(
    weight: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (weight > 0f) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .weight(weight)
                .background(
                    color = color,
                    shape = CircleShape,
                ),
        )
    }
}

@Composable
private fun Thumb(
    color: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(8.dp)
            .background(
                color = color,
                shape = CircleShape,
            )
            .then(
                if (enabled) {
                    Modifier
                        .focusable()
                        .onDpadEvent(
                            onLeft = {
                                onSeekBack()
                                true
                            },
                            onRight = {
                                onSeekForward()
                                true
                            },
                        )
                } else {
                    Modifier
                }
            ),
    )
}

@Composable
@PreviewLightDark
private fun TVSliderPreview(
    @PreviewParameter(TVSliderPreviewParameters::class) previewParameters: PreviewParameters,
) {
    val isDark = isSystemInDarkTheme()
    var progress by remember {
        mutableLongStateOf(previewParameters.initialValue)
    }

    PillarboxSlider(
        value = progress,
        range = 0L..100L,
        compactMode = previewParameters.compactMode,
        enabled = previewParameters.enabled,
        thumbColorEnabled = if (isDark) md_theme_dark_primary else md_theme_light_primary,
        thumbColorDisabled = (if (isDark) md_theme_dark_onSurface else md_theme_light_onSurface).copy(alpha = 0.38f),
        activeTrackColorEnabled = if (isDark) md_theme_dark_primary else md_theme_light_primary,
        activeTrackColorDisabled = (if (isDark) md_theme_dark_onSurface else md_theme_light_onSurface).copy(alpha = 0.38f),
        inactiveTrackColorEnabled = if (isDark) md_theme_dark_surfaceVariant else md_theme_light_surfaceVariant,
        inactiveTrackColorDisabled = (if (isDark) md_theme_dark_onSurface else md_theme_light_onSurface).copy(alpha = 0.12f),
        onValueChange = { progress = it },
        onSeekBack = { progress-- },
        onSeekForward = { progress++ },
    )
}

private class PreviewParameters(
    val compactMode: Boolean,
    val enabled: Boolean,
    val initialValue: Long,
)

private class TVSliderPreviewParameters : PreviewParameterProvider<PreviewParameters> {
    override val values = sequence {
        listOf(false, true).forEach { compactMode ->
            listOf(false, true).forEach { enabled ->
                listOf(0L, 50L, 100L).forEach { initialValue ->
                    yield(
                        PreviewParameters(
                            compactMode = compactMode,
                            enabled = enabled,
                            initialValue = initialValue,
                        )
                    )
                }
            }
        }
    }
}
