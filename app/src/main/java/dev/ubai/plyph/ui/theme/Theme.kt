package dev.ubai.plyph.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

private val LightColors = lightColorScheme(
    primary = Color(0xFF171717),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF0F0F0),
    onPrimaryContainer = Color(0xFF171717),
    secondary = Color(0xFF525252),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5F5F5),
    onSecondaryContainer = Color(0xFF262626),
    tertiary = Color(0xFF404040),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFAFAFA),
    onTertiaryContainer = Color(0xFF262626),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF171717),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171717),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainer = Color(0xFFF5F5F5),
    surfaceContainerHigh = Color(0xFFEDEDED),
    surfaceContainerHighest = Color(0xFFE5E5E5),
    outline = Color(0xFFA3A3A3),
    outlineVariant = Color(0xFFE5E5E5),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEF2F2),
    onErrorContainer = Color(0xFF991B1B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5B564C),
    onPrimary = Color(0xFFF5F3DD),
    primaryContainer = Color(0xFF524D44),
    onPrimaryContainer = Color(0xFFF5F3DD),
    secondary = Color(0xFFBCAD95),
    onSecondary = Color(0xFF3B3831),
    secondaryContainer = Color(0xFF524D44),
    onSecondaryContainer = Color(0xFFF5F3DD),
    tertiary = Color(0xFFD8CCB1),
    onTertiary = Color(0xFF3B3831),
    tertiaryContainer = Color(0xFF48433C),
    onTertiaryContainer = Color(0xFFF5F3DD),
    background = Color(0xFF423E37),
    onBackground = Color(0xFFF5F3DD),
    surface = Color(0xFF423E37),
    onSurface = Color(0xFFF5F3DD),
    surfaceVariant = Color(0xFF48433C),
    onSurfaceVariant = Color(0xFFD8CCB1),
    surfaceContainerLowest = Color(0xFF35322C),
    surfaceContainerLow = Color(0xFF3B3831),
    surfaceContainer = Color(0xFF48433C),
    surfaceContainerHigh = Color(0xFF524D44),
    surfaceContainerHighest = Color(0xFF5B564C),
    outline = Color(0xFFBCAD95),
    outlineVariant = Color(0xFF6D675B),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF2B1212),
    onErrorContainer = Color(0xFFFCA5A5),
)

private val PlyphTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3f).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2f).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    ),
)

private val PlyphShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

@Composable
fun PlyphTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = PlyphTypography,
        shapes = PlyphShapes,
        content = content,
    )
}
