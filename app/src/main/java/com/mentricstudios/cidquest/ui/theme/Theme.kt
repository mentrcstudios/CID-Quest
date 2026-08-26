package com.mentricstudios.cidquest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CIDQuestColorScheme = darkColorScheme(
    background = BackgroundTop,
    surface = BackgroundBottom,
    primary = AccentTeal,
    secondary = AccentOrange,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun CIDQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CIDQuestColorScheme,
        typography = AppTypography,
        content = content
    )
}
