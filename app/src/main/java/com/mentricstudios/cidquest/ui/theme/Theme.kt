package com.mentricstudios.cidquest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CidQuestColorScheme = darkColorScheme(
    background = BackgroundTop,
    surface = BackgroundBottom,
    primary = AccentGold,
    secondary = AccentAmber,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun CidQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CidQuestColorScheme,
        typography = AppTypography,
        content = content
    )
}
