package com.example.whatsappcleaner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = PremiumPrimary,
    onPrimary = PremiumBackground,
    primaryContainer = PremiumPrimary.copy(alpha = 0.20f),
    onPrimaryContainer = PremiumTextPrimary,
    secondary = PremiumSecondary,
    onSecondary = PremiumTextPrimary,
    secondaryContainer = PremiumSecondary.copy(alpha = 0.20f),
    onSecondaryContainer = PremiumTextPrimary,
    tertiary = PremiumAccent,
    onTertiary = PremiumBackground,
    tertiaryContainer = PremiumAccent.copy(alpha = 0.20f),
    onTertiaryContainer = PremiumTextPrimary,
    error = PremiumDanger,
    onError = PremiumTextPrimary,
    background = PremiumBackground,
    onBackground = PremiumTextPrimary,
    surface = PremiumSurface,
    onSurface = PremiumTextPrimary,
    surfaceVariant = PremiumSurfaceVariant,
    onSurfaceVariant = PremiumTextSecondary,
    outline = DividerColor,
    outlineVariant = DividerColor.copy(alpha = 0.75f)
)

private val DarkColorScheme = darkColorScheme(
    primary = PremiumPrimary,
    onPrimary = PremiumBackground,
    primaryContainer = PremiumPrimary.copy(alpha = 0.22f),
    onPrimaryContainer = PremiumTextPrimary,
    secondary = PremiumSecondary,
    onSecondary = PremiumTextPrimary,
    secondaryContainer = PremiumSecondary.copy(alpha = 0.22f),
    onSecondaryContainer = PremiumTextPrimary,
    tertiary = PremiumAccent,
    onTertiary = PremiumBackground,
    tertiaryContainer = PremiumAccent.copy(alpha = 0.24f),
    onTertiaryContainer = PremiumTextPrimary,
    error = PremiumDanger,
    onError = PremiumTextPrimary,
    errorContainer = PremiumDanger.copy(alpha = 0.24f),
    onErrorContainer = PremiumTextPrimary,
    background = PremiumBackground,
    onBackground = PremiumTextPrimary,
    surface = PremiumSurface,
    onSurface = PremiumTextPrimary,
    surfaceVariant = PremiumSurfaceVariant,
    onSurfaceVariant = PremiumTextSecondary,
    outline = DividerColor,
    outlineVariant = DividerColor.copy(alpha = 0.75f)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, shapes = AppShapes, content = content)
}
