package com.example.mealfix.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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
    primary = Green500,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,

    secondary = Lime400,
    onSecondary = Green900,
    secondaryContainer = Lime100,
    onSecondaryContainer = Green900,

    tertiary = Green200,
    onTertiary = Green900,
    tertiaryContainer = Green50,
    onTertiaryContainer = Green800,

    background = Green50,
    onBackground = Green900,
    surface = Green50,
    onSurface = Green900,
    surfaceVariant = Green100,
    onSurfaceVariant = Green800,

    outline = Green600,
)

private val DarkColors = darkColorScheme(
    primary = Green300,
    onPrimary = Green900,
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = Green100,

    secondary = Lime400,
    onSecondary = Green900,
    secondaryContainer = Green700,
    onSecondaryContainer = Green50,

    tertiary = Green200,
    onTertiary = Green900,
    tertiaryContainer = DarkGreenContainer,
    onTertiaryContainer = Green100,

    background = DarkGreenBackground,
    onBackground = Green50,
    surface = DarkGreenSurface,
    onSurface = Green50,
    surfaceVariant = DarkGreenContainer,
    onSurfaceVariant = Green100,

    outline = Green400,
)

@Composable
fun MealFixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
