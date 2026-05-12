package com.domagic.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.domagic.R

// Custom Colors
val Purple800 = Color(0xFF120E16)
val Purple900 = Color(0xFF0D0A0F)
val Purple700 = Color(0xFF1D1724)
val Violet500 = Color(0xFF8B5CF6)
val Violet400 = Color(0xFFA78BFA)
val Violet300 = Color(0xFFC4B5FD)
val ErrorRed = Color(0xFFEF4444)
val OnSurface = Color(0xFFE2E8F0)
val SurfaceDark = Color(0xFF1E1B24)

val GoogleSansFlex = FontFamily(
    Font(R.font.google_sans_flex)
)

val DoMagicTypography = Typography(
    displayLarge = Typography().displayLarge.copy(fontFamily = GoogleSansFlex),
    displayMedium = Typography().displayMedium.copy(fontFamily = GoogleSansFlex),
    displaySmall = Typography().displaySmall.copy(fontFamily = GoogleSansFlex),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = GoogleSansFlex),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = GoogleSansFlex),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = GoogleSansFlex),
    titleLarge = Typography().titleLarge.copy(fontFamily = GoogleSansFlex),
    titleMedium = Typography().titleMedium.copy(fontFamily = GoogleSansFlex),
    titleSmall = Typography().titleSmall.copy(fontFamily = GoogleSansFlex),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = GoogleSansFlex),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = GoogleSansFlex),
    bodySmall = Typography().bodySmall.copy(fontFamily = GoogleSansFlex),
    labelLarge = Typography().labelLarge.copy(fontFamily = GoogleSansFlex),
    labelMedium = Typography().labelMedium.copy(fontFamily = GoogleSansFlex),
    labelSmall = Typography().labelSmall.copy(fontFamily = GoogleSansFlex),
)

private val DarkColorScheme = darkColorScheme(
    primary        = Color(0xFFD0BCFF),
    onPrimary      = Color(0xFF381E72),
    primaryContainer    = Color(0xFF4F378B),
    onPrimaryContainer  = Color(0xFFEADDFF),
    secondary      = Color(0xFFCCC2DC),
    onSecondary    = Color(0xFF332D41),
    background     = Color(0xFF1C1B1F),
    onBackground   = Color(0xFFE6E1E5),
    surface        = Color(0xFF1C1B1F),
    onSurface      = Color(0xFFE6E1E5),
)

@Composable
fun DoMagicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = DoMagicTypography,
        content     = content
    )
}
