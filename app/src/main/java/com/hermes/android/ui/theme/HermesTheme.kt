package com.hermes.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Hermes brand: dark-first, gold accent. 4dp spacing grid, coherent radii. */
object HermesTokens {
    val Gold = Color(0xFFD4A017)
    val GoldSoft = Color(0xFF8A6D1B)
    val BgDark = Color(0xFF0B0D12)
    val SurfaceDark = Color(0xFF14171F)
    val CardDark = Color(0xFF1A1E29)
    val TextDark = Color(0xFFE8EAF0)
    val MutedDark = Color(0xFF9AA0B4)
    val Success = Color(0xFF34C77B)
    val Warning = Color(0xFFF0A020)
    val Error = Color(0xFFE5484D)
    val Info = Color(0xFF4C9AFF)
    val RadiusS = 8.dp
    val RadiusM = 12.dp
    val RadiusL = 16.dp
    val TitleSize = 20.sp
    val BodySize = 14.sp
    val MonoSize = 12.sp
}

private val DarkScheme = darkColorScheme(
    primary = HermesTokens.Gold,
    onPrimary = Color.Black,
    background = HermesTokens.BgDark,
    onBackground = HermesTokens.TextDark,
    surface = HermesTokens.SurfaceDark,
    onSurface = HermesTokens.TextDark,
    surfaceVariant = HermesTokens.CardDark,
    error = HermesTokens.Error,
    tertiary = HermesTokens.Info
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF9A7210),
    onPrimary = Color.White,
    background = Color(0xFFF6F4EC),
    surface = Color.White
)

@Composable
fun HermesTheme(
    dark: Boolean = isSystemInDarkTheme(),
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = when {
        dark && amoled -> DarkScheme.copy(background = Color.Black, surface = Color.Black)
        dark -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
