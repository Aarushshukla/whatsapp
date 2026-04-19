package com.example.whatsappcleaner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = AccentGreen,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = AccentGreen.copy(alpha = 0.16f),
    onSecondaryContainer = BrandNavy,
    tertiary = AccentPurple,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = AccentPurple.copy(alpha = 0.14f),
    onTertiaryContainer = BrandNavy,
    error = AccentError,
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color(0xFF06210A),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = AccentGreen,
    onSecondary = Color(0xFF06210A),
    secondaryContainer = AccentGreen.copy(alpha = 0.18f),
    onSecondaryContainer = Color(0xFFE8F5E9),
    tertiary = AccentPurple,
    onTertiary = Color(0xFF100620),
    tertiaryContainer = AccentPurple.copy(alpha = 0.18f),
    onTertiaryContainer = Color(0xFFEDE7F6),
    error = AccentError,
    onError = Color(0xFF220404),
    errorContainer = AccentError.copy(alpha = 0.18f),
    onErrorContainer = Color(0xFFFFE7E7),
    background = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE7EDF7),
    surface = Color(0xFF2A2A2A),
    onSurface = Color(0xFFE7EDF7),
    surfaceVariant = Color(0xFF1C2A41),
    onSurfaceVariant = Color(0xFFAAB8CD),
    outline = Color(0xFF31435E),
    outlineVariant = Color(0xFF27354C)
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)

@Composable
fun WhatsCleanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = remember(darkTheme) {
        if (darkTheme) darkColorScheme(
            primary = DarkColorScheme.primary,
            onPrimary = DarkColorScheme.onPrimary,
            primaryContainer = DarkColorScheme.primaryContainer,
            onPrimaryContainer = DarkColorScheme.onPrimaryContainer,
            secondary = DarkColorScheme.secondary,
            onSecondary = DarkColorScheme.onSecondary,
            secondaryContainer = DarkColorScheme.secondaryContainer,
            onSecondaryContainer = DarkColorScheme.onSecondaryContainer,
            tertiary = DarkColorScheme.tertiary,
            onTertiary = DarkColorScheme.onTertiary,
            tertiaryContainer = DarkColorScheme.tertiaryContainer,
            onTertiaryContainer = DarkColorScheme.onTertiaryContainer,
            background = DarkColorScheme.background,
            onBackground = DarkColorScheme.onBackground,
            surface = DarkColorScheme.surface,
            onSurface = DarkColorScheme.onSurface,
            surfaceVariant = DarkColorScheme.surfaceVariant,
            onSurfaceVariant = DarkColorScheme.onSurfaceVariant,
            outline = DarkColorScheme.outline,
            outlineVariant = DarkColorScheme.outlineVariant,
            error = DarkColorScheme.error,
            onError = DarkColorScheme.onError
        ) else lightColorScheme(
            primary = LightColorScheme.primary,
            onPrimary = LightColorScheme.onPrimary,
            primaryContainer = LightColorScheme.primaryContainer,
            onPrimaryContainer = LightColorScheme.onPrimaryContainer,
            secondary = LightColorScheme.secondary,
            onSecondary = LightColorScheme.onSecondary,
            secondaryContainer = LightColorScheme.secondaryContainer,
            onSecondaryContainer = LightColorScheme.onSecondaryContainer,
            tertiary = LightColorScheme.tertiary,
            onTertiary = LightColorScheme.onTertiary,
            tertiaryContainer = LightColorScheme.tertiaryContainer,
            onTertiaryContainer = LightColorScheme.onTertiaryContainer,
            background = LightColorScheme.background,
            onBackground = LightColorScheme.onBackground,
            surface = LightColorScheme.surface,
            onSurface = LightColorScheme.onSurface,
            surfaceVariant = LightColorScheme.surfaceVariant,
            onSurfaceVariant = LightColorScheme.onSurfaceVariant,
            outline = LightColorScheme.outline,
            outlineVariant = LightColorScheme.outlineVariant,
            error = LightColorScheme.error,
            onError = LightColorScheme.onError
        )
    }
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
}
