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
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import ch.srgssr.pillarbox.demo.shared.extension.onDpadEvent
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_inverseSurface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onSurface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_primary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_surfaceVariant
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_inverseSurface
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
 * @param secondaryValue An optional second value to display on the slider.
 * @param enabled Whether this slider is enabled.
 * @param thumbColorEnabled The thumb color when the component is enabled.
 * @param thumbColorDisabled The thumb color when the component is disabled.
 * @param activeTrackColorEnabled The active track color when the component is enabled.
 * @param activeTrackColorDisabled The active track color when the component is disabled.
 * @param inactiveTrackColorEnabled The inactive track color when the component is enabled.
 * @param inactiveTrackColorDisabled The inactive track color when the component is disabled.
 * @param secondaryTrackColorEnabled The secondary track color when the component is enabled.
 * @param secondaryTrackColorDisabled The secondary track color when the component is disabled.
 * @param interactionSource The [MutableInteractionSource] representing the stream of [Interaction]s for this slider.
 * You can create and pass in your own `remember`ed instance to observe  [Interaction]s.
 * @param onValueChange The action to perform whenever the slider value changes.
 * @param onValueChangeFinished The action to perform when the slider value is done changing.
 * @param onSeekBack The action to perform when seeking back.
 * @param onSeekForward The action to perform when seeking forward.
 */
@Composable
fun PillarboxSlider(
    value: Long,
    range: LongRange,
    compactMode: Boolean,
    modifier: Modifier = Modifier,
    secondaryValue: Long? = null,
    enabled: Boolean = true,
    thumbColorEnabled: Color,
    thumbColorDisabled: Color,
    activeTrackColorEnabled: Color,
    activeTrackColorDisabled: Color,
    inactiveTrackColorEnabled: Color,
    inactiveTrackColorDisabled: Color,
    secondaryTrackColorEnabled: Color = Color.Unspecified,
    secondaryTrackColorDisabled: Color = Color.Unspecified,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onValueChange: (value: Long) -> Unit = {},
    onValueChangeFinished: () -> Unit = {},
    onSeekBack: () -> Unit = {},
    onSeekForward: () -> Unit = {},
) {
    PillarboxSliderInternal(
        activeTrackWeight = value / range.endInclusive.toFloat(),
        compactMode = compactMode,
        modifier = modifier,
        secondaryValueWeight = secondaryValue?.let { it / range.endInclusive.toFloat() },
        enabled = enabled,
        thumbColorEnabled = thumbColorEnabled,
        thumbColorDisabled = thumbColorDisabled,
        activeTrackColorEnabled = activeTrackColorEnabled,
        activeTrackColorDisabled = activeTrackColorDisabled,
        inactiveTrackColorEnabled = inactiveTrackColorEnabled,
        inactiveTrackColorDisabled = inactiveTrackColorDisabled,
        secondaryTrackColorEnabled = secondaryTrackColorEnabled,
        secondaryTrackColorDisabled = secondaryTrackColorDisabled,
        interactionSource = interactionSource,
        onSliderValueChange = { ratio ->
            onValueChange((ratio * (range.endInclusive - range.start)).toLong())
        },
        onSliderValueChangeFinished = onValueChangeFinished,
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
 * @param secondaryValue An optional second value to display on the slider.
 * @param enabled Whether this slider is enabled.
 * @param thumbColorEnabled The thumb color when the component is enabled.
 * @param thumbColorDisabled The thumb color when the component is disabled.
 * @param activeTrackColorEnabled The active track color when the component is enabled.
 * @param activeTrackColorDisabled The active track color when the component is disabled.
 * @param inactiveTrackColorEnabled The inactive track color when the component is enabled.
 * @param inactiveTrackColorDisabled The inactive track color when the component is disabled.
 * @param secondaryTrackColorEnabled The secondary track color when the component is enabled.
 * @param secondaryTrackColorDisabled The secondary track color when the component is disabled.
 * @param interactionSource The [MutableInteractionSource] representing the stream of [Interaction]s for this slider.
 * You can create and pass in your own `remember`ed instance to observe  [Interaction]s.
 * @param onValueChange The action to perform whenever the slider value changes.
 * @param onValueChangeFinished The action to perform when the slider value is done changing.
 * @param onSeekBack The action to perform when seeking back.
 * @param onSeekForward The action to perform when seeking forward.
 */
@Composable
fun PillarboxSlider(
    value: Float,
    range: ClosedRange<Float>,
    compactMode: Boolean,
    modifier: Modifier = Modifier,
    secondaryValue: Float? = null,
    enabled: Boolean = true,
    thumbColorEnabled: Color,
    thumbColorDisabled: Color,
    activeTrackColorEnabled: Color,
    activeTrackColorDisabled: Color,
    inactiveTrackColorEnabled: Color,
    inactiveTrackColorDisabled: Color,
    secondaryTrackColorEnabled: Color = Color.Unspecified,
    secondaryTrackColorDisabled: Color = Color.Unspecified,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onValueChange: (value: Float) -> Unit = {},
    onValueChangeFinished: () -> Unit = {},
    onSeekBack: () -> Unit = {},
    onSeekForward: () -> Unit = {},
) {
    PillarboxSliderInternal(
        activeTrackWeight = value / range.endInclusive,
        compactMode = compactMode,
        modifier = modifier,
        secondaryValueWeight = secondaryValue?.let { it / range.endInclusive },
        enabled = enabled,
        thumbColorEnabled = thumbColorEnabled,
        thumbColorDisabled = thumbColorDisabled,
        activeTrackColorEnabled = activeTrackColorEnabled,
        activeTrackColorDisabled = activeTrackColorDisabled,
        inactiveTrackColorEnabled = inactiveTrackColorEnabled,
        inactiveTrackColorDisabled = inactiveTrackColorDisabled,
        secondaryTrackColorEnabled = secondaryTrackColorEnabled,
        secondaryTrackColorDisabled = secondaryTrackColorDisabled,
        interactionSource = interactionSource,
        onSliderValueChange = { ratio ->
            onValueChange(ratio * (range.endInclusive - range.start))
        },
        onSliderValueChangeFinished = onValueChangeFinished,
        onSeekBack = onSeekBack,
        onSeekForward = onSeekForward,
    )
}

@Composable
private fun PillarboxSliderInternal(
    activeTrackWeight: Float,
    compactMode: Boolean,
    modifier: Modifier = Modifier,
    secondaryValueWeight: Float?,
    enabled: Boolean,
    thumbColorEnabled: Color,
    thumbColorDisabled: Color,
    activeTrackColorEnabled: Color,
    activeTrackColorDisabled: Color,
    inactiveTrackColorEnabled: Color,
    inactiveTrackColorDisabled: Color,
    secondaryTrackColorEnabled: Color,
    secondaryTrackColorDisabled: Color,
    interactionSource: MutableInteractionSource,
    onSliderValueChange: (ratio: Float) -> Unit,
    onSliderValueChangeFinished: () -> Unit,
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

    val secondaryTrackColor by animateColorAsState(
        targetValue = if (enabled) secondaryTrackColorEnabled else secondaryTrackColorDisabled,
        label = "secondary_track_color",
    )

    Row(
        modifier = modifier
            .height(seekBarHeight)
            .clickToSlide(
                interactionSource = interactionSource,
                onSliderValueChange = onSliderValueChange,
                onSliderValueChangeFinished = onSliderValueChangeFinished,
            )
            .dragThumb(
                interactionSource = interactionSource,
                onSliderValueChange = onSliderValueChange,
                onSliderValueChangeFinished = onSliderValueChangeFinished,
            ),
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

        if (inactiveTrackWeight > 0f) {
            Box(
                modifier = Modifier.weight(inactiveTrackWeight),
            ) {
                Track(
                    weight = 1f,
                    color = inactiveTrackColor,
                )

                if (secondaryValueWeight != null) {
                    Track(
                        weight = secondaryValueWeight / inactiveTrackWeight,
                        color = secondaryTrackColor,
                    )
                }
            }
        }
    }
}

private fun Modifier.clickToSlide(
    interactionSource: MutableInteractionSource,
    onSliderValueChange: (ratio: Float) -> Unit,
    onSliderValueChangeFinished: () -> Unit,
): Modifier {
    var pressInteraction: PressInteraction.Press? = null

    fun PointerInputScope.initPressInteraction(offset: Offset) {
        if (pressInteraction == null) {
            pressInteraction = PressInteraction.Press(offset)
                .also { interactionSource.tryEmit(it) }

            onSliderValueChange(offset.x / size.width)
        }
    }

    return this then pointerInput(Unit) {
        detectTapGestures(
            onPress = { offset ->
                initPressInteraction(offset)

                val nextInteraction = if (tryAwaitRelease()) {
                    PressInteraction::Release
                } else {
                    PressInteraction::Cancel
                }

                pressInteraction?.let {
                    interactionSource.emit(nextInteraction(it))
                    pressInteraction = null

                    onSliderValueChangeFinished()
                }
            },
            onTap = ::initPressInteraction,
        )
    }
}

private fun Modifier.dragThumb(
    interactionSource: MutableInteractionSource,
    onSliderValueChange: (ratio: Float) -> Unit,
    onSliderValueChangeFinished: () -> Unit,
): Modifier {
    var startInteraction: DragInteraction.Start? = null

    fun destroyStartInteraction(finalInteractionConstructor: (start: DragInteraction.Start) -> DragInteraction) {
        startInteraction?.let {
            interactionSource.tryEmit(finalInteractionConstructor(it))
            startInteraction = null
        }

        onSliderValueChangeFinished()
    }

    return this then Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = { offset ->
                startInteraction = DragInteraction.Start()
                    .also { interactionSource.tryEmit(it) }

                onSliderValueChange(offset.x / size.width)
            },
            onDragEnd = {
                destroyStartInteraction(DragInteraction::Stop)
            },
            onDragCancel = {
                destroyStartInteraction(DragInteraction::Cancel)
            },
            onHorizontalDrag = { change, _ ->
                onSliderValueChange(change.position.x / size.width)
            },
        )
    }
}

@Composable
private fun Track(
    weight: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (weight > 0f) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = weight)
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
        secondaryValue = previewParameters.secondaryValue,
        enabled = previewParameters.enabled,
        thumbColorEnabled = if (isDark) md_theme_dark_primary else md_theme_light_primary,
        thumbColorDisabled = (if (isDark) md_theme_dark_onSurface else md_theme_light_onSurface).copy(alpha = 0.38f),
        activeTrackColorEnabled = if (isDark) md_theme_dark_primary else md_theme_light_primary,
        activeTrackColorDisabled = (if (isDark) md_theme_dark_onSurface else md_theme_light_onSurface).copy(alpha = 0.38f),
        inactiveTrackColorEnabled = if (isDark) md_theme_dark_surfaceVariant else md_theme_light_surfaceVariant,
        inactiveTrackColorDisabled = (if (isDark) md_theme_dark_onSurface else md_theme_light_onSurface).copy(alpha = 0.12f),
        secondaryTrackColorEnabled = if (isDark) md_theme_dark_inverseSurface else md_theme_light_inverseSurface,
        secondaryTrackColorDisabled = (if (isDark) md_theme_dark_inverseSurface else md_theme_light_inverseSurface).copy(alpha = 0.12f),
        onValueChange = { progress = it },
        onSeekBack = { progress-- },
        onSeekForward = { progress++ },
    )
}

private class PreviewParameters(
    val compactMode: Boolean,
    val enabled: Boolean,
    val initialValue: Long,
    val secondaryValue: Long?,
)

private class TVSliderPreviewParameters : PreviewParameterProvider<PreviewParameters> {
    override val values = sequence {
        listOf(false, true).forEach { compactMode ->
            listOf(false, true).forEach { enabled ->
                listOf(0L, 50L, 100L).forEach { initialValue ->
                    listOf(null, 10L).forEach { secondaryValue ->
                        yield(
                            PreviewParameters(
                                compactMode = compactMode,
                                enabled = enabled,
                                initialValue = initialValue,
                                secondaryValue = secondaryValue,
                            )
                        )
                    }
                }
            }
        }
    }
}
