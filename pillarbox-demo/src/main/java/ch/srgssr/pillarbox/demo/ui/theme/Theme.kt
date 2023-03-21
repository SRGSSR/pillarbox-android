/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Gray16,
    primaryVariant = Gray33,
    secondary = RedSrg,
    secondaryVariant = RedSrgDark,
    background = Gray16,
    surface = Gray33,
    error = RedSrgLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

private val LightColorPalette = lightColors(
    primary = Gray16,
    primaryVariant = Gray33,
    secondary = RedSrg,
    secondaryVariant = RedSrgDark,
    background = Color.White,
    surface = Color.White,
    error = RedSrgLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Gray16,
    onSurface = Gray16,
    onError = Color.White

    /* Other default colors to override
background = Color.White,
surface = Color.White,
onPrimary = Color.White,
onSecondary = Color.Black,
onBackground = Color.Black,
onSurface = Color.Black,
*/
)

/**
 * Pillarbox demo theme
 *
 * @param darkTheme true to have [DarkColorPalette]
 * @param content
 */
@Composable
fun PillarboxTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
