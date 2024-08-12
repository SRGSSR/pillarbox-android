/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.pillarbox.demo.shared.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.NumberFormat
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Display a line chart from the provided set of values.
 *
 * @param data The list of values to draw.
 * @param modifier The [Modifier] to apply to the composable. You must use this [Modifier] to specify the size of this chart.
 * Either with exact values (for example, with `Modifier.size()`), or relative to its parent (for example, with `Modifier.fillMaxSize()`).
 * @param lineColor The color of the line.
 * @param lineWidth The width of the line.
 * @param lineCornerRadius The radius of the line corners.
 * @param stretchChartToPointsCount The number of points to display on the chart.
 * If `null`, all the provided points are drawn.
 * If [data] has fewer points than [stretchChartToPointsCount], an empty space will be reserved for the missing points.
 * Otherwise, the last [stretchChartToPointsCount] values from [data] will be drawn, filling the whole width.
 * @param scaleItemsCount The number of values to display on the vertical axis.
 * @param scaleTextFormatter The formatter used to format each value of the vertical axis.
 * @param scaleTextStyle The text style to apply on each value of the vertical axis.
 * @param scaleTextHorizontalPadding The horizontal padding to apply on each value of the vertical axis.
 * @param scaleLineColor The color of the horizontal line next to each value of the vertical axis.
 */
@Composable
fun LineChart(
    data: List<Float>,
    modifier: Modifier,
    lineColor: Color = Color.Red,
    lineWidth: Dp = 2.dp,
    lineCornerRadius: Dp = 6.dp,
    stretchChartToPointsCount: Int? = null,
    scaleItemsCount: Int = 5,
    scaleTextFormatter: NumberFormat = NumberFormat.getIntegerInstance(),
    scaleTextStyle: TextStyle = TextStyle.Default,
    scaleTextHorizontalPadding: Dp = 8.dp,
    scaleLineColor: Color = Color.LightGray,
) {
    Chart(
        data = data,
        modifier = modifier,
        scaleItemsCount = scaleItemsCount,
        scaleTextFormatter = scaleTextFormatter,
        scaleTextStyle = scaleTextStyle,
        scaleTextHorizontalPadding = scaleTextHorizontalPadding,
        scaleLineColor = scaleLineColor,
        drawChart = { maxValue, bounds ->
            drawLineChart(
                points = data,
                bounds = bounds,
                maxValue = maxValue,
                lineColor = lineColor,
                lineWidth = lineWidth,
                lineCornerRadius = lineCornerRadius,
                maxPoints = stretchChartToPointsCount ?: data.size,
            )
        },
    )
}

/**
 * Display a bar chart from the provided set of values.
 *
 * @param data The list of values to draw.
 * @param modifier The [Modifier] to apply to the composable. You must use this [Modifier] to specify the size of this chart.
 * Either with exact values (for example, with `Modifier.size()`), or relative to its parent (for example, with `Modifier.fillMaxSize()`).
 * @param barColor The color of each bar.
 * @param barSpacing The spacing between two bars.
 * @param stretchChartToPointsCount The number of points to display on the chart.
 * If `null`, all the provided points are drawn.
 * If [data] has fewer points than [stretchChartToPointsCount], an empty space will be reserved for the missing points.
 * Otherwise, the last [stretchChartToPointsCount] values from [data] will be drawn, filling the whole width.
 * @param scaleItemsCount The number of values to display on the vertical axis.
 * @param scaleTextFormatter The formatter used to format each value of the vertical axis.
 * @param scaleTextStyle The text style to apply on each value of the vertical axis.
 * @param scaleTextHorizontalPadding The horizontal padding to apply on each value of the vertical axis.
 * @param scaleLineColor The color of the horizontal line next to each value of the vertical axis.
 */
@Composable
fun BarChart(
    data: List<Float>,
    modifier: Modifier,
    barColor: Color = Color.Blue,
    barSpacing: Dp = 1.dp,
    stretchChartToPointsCount: Int? = null,
    scaleItemsCount: Int = 5,
    scaleTextFormatter: NumberFormat = NumberFormat.getIntegerInstance(),
    scaleTextStyle: TextStyle = TextStyle.Default,
    scaleTextHorizontalPadding: Dp = 8.dp,
    scaleLineColor: Color = Color.LightGray,
) {
    Chart(
        data = data,
        modifier = modifier,
        scaleItemsCount = scaleItemsCount,
        scaleTextFormatter = scaleTextFormatter,
        scaleTextStyle = scaleTextStyle,
        scaleTextHorizontalPadding = scaleTextHorizontalPadding,
        scaleLineColor = scaleLineColor,
        drawChart = { maxValue, bounds ->
            drawBarChart(
                points = data,
                bounds = bounds,
                maxValue = maxValue,
                barColor = barColor,
                barSpacing = barSpacing,
                maxPoints = stretchChartToPointsCount ?: data.size,
            )
        },
    )
}

@Composable
private fun Chart(
    data: List<Float>,
    modifier: Modifier,
    scaleItemsCount: Int,
    scaleTextFormatter: NumberFormat,
    scaleTextStyle: TextStyle,
    scaleTextHorizontalPadding: Dp,
    scaleLineColor: Color,
    drawChart: DrawScope.(maxValue: Int, bounds: Rect) -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()

    val maxValue = data.max()
    val numberOfDigitsInMaxValue = log10(abs(maxValue.toDouble())).toInt()
    val increment = 10.0.pow(numberOfDigitsInMaxValue).toInt()

    var nextMaxMultipleOfScales = (increment * (floor(maxValue / increment) + 1)).toInt()
    while (nextMaxMultipleOfScales % (scaleItemsCount - 1) != 0) {
        nextMaxMultipleOfScales += increment
    }

    Canvas(modifier = modifier) {
        val maxScaleWidth = textMeasurer.measure(scaleTextFormatter.format(nextMaxMultipleOfScales), scaleTextStyle).size.width
        val chartBounds = Rect(
            offset = Offset.Zero,
            size = Size(
                width = size.width - maxScaleWidth - scaleTextHorizontalPadding.toPx() * 2f,
                height = size.height,
            ),
        )

        drawScale(
            textMeasurer = textMeasurer,
            maxValue = nextMaxMultipleOfScales,
            scaleItemsCount = scaleItemsCount,
            scaleTextFormatter = scaleTextFormatter,
            scaleTextStyle = scaleTextStyle,
            scaleTextHorizontalPadding = scaleTextHorizontalPadding,
            scaleLineColor = scaleLineColor,
        )

        drawChart(nextMaxMultipleOfScales, chartBounds)
    }
}

private fun DrawScope.drawLineChart(
    points: List<Float>,
    bounds: Rect,
    maxValue: Int,
    lineColor: Color,
    lineWidth: Dp,
    lineCornerRadius: Dp,
    maxPoints: Int,
) {
    fun getX(index: Int): Float {
        return (index / (maxPoints - 1f)) * bounds.width
    }

    fun getY(value: Float): Float {
        return (1 - (value / maxValue.toFloat())) * bounds.height
    }

    val path = Path()
    path.moveTo(getX(0), getY(points[0]))

    for (index in 1 until points.size) {
        path.lineTo(getX(index), getY(points[index]))
    }

    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(
            width = lineWidth.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.cornerPathEffect(lineCornerRadius.toPx()),
        ),
    )
}

private fun DrawScope.drawBarChart(
    points: List<Float>,
    bounds: Rect,
    maxValue: Int,
    barColor: Color,
    barSpacing: Dp,
    maxPoints: Int,
) {
    val barSpacingPx = barSpacing.toPx()
    val barWidth = (bounds.width - barSpacingPx * (maxPoints - 1)) / maxPoints

    points.forEachIndexed { index, point ->
        val x = (index / maxPoints) * bounds.width + (barSpacingPx + barWidth) * index
        val y = (1f - (point / maxValue.toFloat())) * bounds.height

        drawRect(
            color = barColor,
            topLeft = Offset(
                x = x,
                y = y,
            ),
            size = Size(
                width = barWidth,
                height = bounds.height - y,
            ),
        )
    }
}

private fun DrawScope.drawScale(
    textMeasurer: TextMeasurer,
    maxValue: Int,
    scaleItemsCount: Int,
    scaleTextFormatter: NumberFormat,
    scaleTextStyle: TextStyle,
    scaleTextHorizontalPadding: Dp,
    scaleLineColor: Color,
) {
    val step = (maxValue / (scaleItemsCount - 1f)).toInt()

    repeat(scaleItemsCount) { index ->
        val scale = scaleTextFormatter.format(index * step)
        val textSize = textMeasurer.measure(scale, scaleTextStyle).size
        val lineXEnd = size.width - textSize.width - scaleTextHorizontalPadding.toPx() * 2f
        val lineY = ((scaleItemsCount - 1f - index) / (scaleItemsCount - 1f)) * size.height
        val textX = lineXEnd + scaleTextHorizontalPadding.toPx()
        val textY = (lineY - textSize.center.y).coerceIn(
            minimumValue = 0f,
            maximumValue = (size.height - textSize.height).coerceAtLeast(0f),
        )

        drawLine(
            color = scaleLineColor,
            start = Offset(
                x = 0f,
                y = lineY,
            ),
            end = Offset(
                x = lineXEnd,
                y = lineY,
            ),
        )

        drawText(
            textMeasurer = textMeasurer,
            text = scale,
            topLeft = Offset(
                x = textX,
                y = textY,
            ),
            style = scaleTextStyle,
        )
    }
}

@Composable
@Preview(showBackground = true, group = "Line chart")
private fun LineChartPreview() {
    LineChart(
        data = generateRandomPreviewData(
            dataSize = 90,
            initialValueRange = 0..10,
            nextItemVariation = -0.3..0.3,
        ),
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
@Preview(showBackground = true, group = "Line chart")
private fun LineChartLiveDataPreview() {
    val dataSize = 90

    LineChart(
        data = generateRandomPreviewLiveData(
            dataSize = dataSize,
            initialValueRange = 0..10,
            nextItemVariation = -0.3..0.3,
            refreshInterval = 1.seconds,
        ),
        modifier = Modifier.fillMaxSize(),
        stretchChartToPointsCount = dataSize,
    )
}

@Composable
@Preview(showBackground = true, group = "Bar chart")
private fun BarChartsPreview() {
    BarChart(
        data = generateRandomPreviewData(
            dataSize = 90,
            initialValueRange = 10..1000,
            nextItemVariation = -10.0..10.0,
        ),
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
@Preview(showBackground = true, group = "Bar chart")
private fun BarChartsLiveDataPreview() {
    val dataSize = 90

    BarChart(
        data = generateRandomPreviewLiveData(
            dataSize = dataSize,
            initialValueRange = 10..1000,
            nextItemVariation = -10.0..10.0,
            refreshInterval = 1.seconds,
        ),
        modifier = Modifier.fillMaxSize(),
        stretchChartToPointsCount = dataSize,
    )
}

@Composable
@Preview(showBackground = true, group = "Combined chars")
private fun CombinedChartsPreview() {
    val dataSize = 90

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LineChart(
            data = generateRandomPreviewData(
                dataSize = dataSize,
                initialValueRange = 0..10,
                nextItemVariation = -0.3..0.3,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        BarChart(
            data = generateRandomPreviewData(
                dataSize = dataSize,
                initialValueRange = 100..1000,
                nextItemVariation = -10.0..10.0,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
@Preview(showBackground = true, group = "Combined chars")
private fun CombinedChartsLiveDataPreview() {
    val dataSize = 90

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LineChart(
            data = generateRandomPreviewLiveData(
                dataSize = dataSize,
                initialValueRange = 0..10,
                nextItemVariation = -0.3..0.3,
                refreshInterval = 1.seconds,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            stretchChartToPointsCount = dataSize,
        )

        BarChart(
            data = generateRandomPreviewLiveData(
                dataSize = dataSize,
                initialValueRange = 100..1000,
                nextItemVariation = -10.0..10.0,
                refreshInterval = 1.seconds,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            stretchChartToPointsCount = dataSize,
        )
    }
}

@Suppress("MagicNumber")
private fun generateRandomPreviewData(
    @Suppress("SameParameterValue") dataSize: Int,
    initialValueRange: IntRange,
    nextItemVariation: ClosedRange<Double>,
): List<Float> {
    return (0 until dataSize).runningFold(Random.nextInt(initialValueRange.first, initialValueRange.last).toFloat()) { acc, _ ->
        if (Random.nextInt(5) < 2) {
            acc
        } else {
            (acc + Random.nextDouble(nextItemVariation.start, nextItemVariation.endInclusive).toFloat()).coerceAtLeast(0f)
        }
    }.drop(1)
}

@Composable
private fun generateRandomPreviewLiveData(
    @Suppress("SameParameterValue") dataSize: Int,
    initialValueRange: IntRange,
    nextItemVariation: ClosedRange<Double>,
    refreshInterval: Duration,
): List<Float> {
    val data = remember {
        mutableStateListOf(Random.nextInt(initialValueRange.first, initialValueRange.last).toFloat())
    }

    LaunchedEffect(Unit) {
        while (true) {
            val newValue = if (Random.nextInt(5) < 2) {
                data.last()
            } else {
                (data.last() + Random.nextDouble(nextItemVariation.start, nextItemVariation.endInclusive).toFloat()).coerceAtLeast(0f)
            }

            val newData = (data + newValue).takeLast(dataSize)

            data.clear()
            data.addAll(newData)

            delay(refreshInterval)
        }
    }

    return data
}
