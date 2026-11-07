package com.henrylumis.rvhgrader.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** RVH Edu's shared visual system: calm academic blue, restrained teal accents and consistent geometry. */
private val BrandBlue = Color(0xFF2457C5)
private val BrandBlueDark = Color(0xFF9DBAFF)
private val BrandTeal = Color(0xFF087F8C)
private val ErrorRed = Color(0xFFBA1A1A)

private val DarkColors = darkColorScheme(
    primary = BrandBlueDark,
    onPrimary = Color(0xFF08275F),
    primaryContainer = Color(0xFF173A78),
    onPrimaryContainer = Color(0xFFE0E8FF),
    secondary = Color(0xFF72D5D5),
    onSecondary = Color(0xFF003738),
    background = Color(0xFF10141C),
    surface = Color(0xFF171C25),
    surfaceVariant = Color(0xFF242B37),
    onBackground = Color(0xFFE8ECF4),
    onSurface = Color(0xFFE8ECF4),
    onSurfaceVariant = Color(0xFFB7C0D0),
    outline = Color(0xFF465165),
    outlineVariant = Color(0xFF303949),
    error = Color(0xFFFFB4AB)
)

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2EAFE),
    onPrimaryContainer = Color(0xFF102F70),
    secondary = BrandTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1F0F0),
    onSecondaryContainer = Color(0xFF002F33),
    background = Color(0xFFF5F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEBEFF6),
    onBackground = Color(0xFF171C27),
    onSurface = Color(0xFF171C27),
    onSurfaceVariant = Color(0xFF566174),
    outline = Color(0xFFBBC4D3),
    outlineVariant = Color(0xFFDCE2EB),
    error = ErrorRed
)

private val RvhShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = RvhShapes,
        content = content
    )
}
