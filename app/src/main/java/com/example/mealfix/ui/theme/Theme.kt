package com.example.mealfix.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Every "on*" color below is chosen for readable contrast against the color it
// sits on top of — that pairing is what Material3 uses throughout the app
// (button text, card text, etc.), so getting it right matters more than the
// base colors themselves.

private val LightColors = lightColorScheme(
    primary = Teal600,
    onPrimary = Color.White,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal700,

    secondary = Amber600,
    onSecondary = Color.White,
    secondaryContainer = Amber100,
    onSecondaryContainer = Color(0xFF7C4A03),

    tertiary = Teal500,
    onTertiary = Color.White,
    tertiaryContainer = Teal100,
    onTertiaryContainer = Teal700,

    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,

    outline = Slate400,
)

private val DarkColors = darkColorScheme(
    primary = Teal300,
    onPrimary = Color(0xFF00201C),
    primaryContainer = TealContainerDark,
    onPrimaryContainer = Teal100,

    secondary = Amber400,
    onSecondary = Color(0xFF2A1800),
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = Amber100,

    tertiary = Teal300,
    onTertiary = Color(0xFF00201C),
    tertiaryContainer = TealContainerDark,
    onTertiaryContainer = Teal100,

    background = Slate900,
    onBackground = Slate100,
    surface = SlateSurfaceDark,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate200,

    outline = Slate600,
)

/**
 * MealFix's Material3 theme, in exactly two modes — light and dark — selected explicitly by
 * [darkTheme] rather than inferred, so the in-app theme switch (see ThemePreferences /
 * MainActivity) always wins over the system setting once the user has picked one.
 */
@Composable
fun MealFixTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
