/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_background
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_error
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_errorContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_inverseOnSurface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_inversePrimary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_inverseSurface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onBackground
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onError
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onErrorContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onPrimary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onPrimaryContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onSecondary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onSecondaryContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onSurface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onSurfaceVariant
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onTertiary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_onTertiaryContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_outline
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_outlineVariant
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_primary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_primaryContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_scrim
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_secondary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_secondaryContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_surface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_surfaceTint
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_surfaceVariant
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_tertiary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_dark_tertiaryContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_background
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_error
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_errorContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_inverseOnSurface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_inversePrimary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_inverseSurface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onBackground
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onError
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onErrorContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onPrimary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onPrimaryContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onSecondary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onSecondaryContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onSurface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onSurfaceVariant
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onTertiary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_onTertiaryContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_outline
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_outlineVariant
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_primary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_primaryContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_scrim
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_secondary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_secondaryContainer
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_surface
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_surfaceTint
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_surfaceVariant
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_tertiary
import ch.srgssr.pillarbox.demo.shared.ui.theme.md_theme_light_tertiaryContainer

private val darkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

private val lightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

/**
 * Pillarbox theme.
 *
 * @param darkTheme `true` if the app uses a dark theme, `false` otherwise.
 * @param useDynamicColors `true` to use a dynamic color scheme, `false` otherwise.
 * @param paddings The paddings to use inside `PillarboxTheme`.
 * @param content The content to display on the screen.
 */
@Composable
fun PillarboxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColors: Boolean = true,
    paddings: Paddings = MaterialTheme.paddings,
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current

        if (darkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        if (darkTheme) {
            darkColorScheme
        } else {
            lightColorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme) {
        CompositionLocalProvider(LocalPaddings provides paddings) {
            content()
        }
    }
}
