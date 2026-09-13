package com.hermes.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Open Minis-inspired Theme with Hermes identity:
 * Deep Obsidian Void background, Emerald & Electric Cyan accents with Amber highlights.
 */
object HermesTokens {
    val Emerald = Color(0xFF10B981)
    val EmeraldDark = Color(0xFF047857)
    val Cyan = Color(0xFF06B6D4)
    val Amber = Color(0xFFF59E0B)
    val Gold = Color(0xFFEAB308)

    // Obsidian background surfaces
    val BgDark = Color(0xFF080C14)
    val SurfaceDark = Color(0xFF0F172A)
    val CardDark = Color(0xFF162033)
    val CardElevated = Color(0xFF1E293B)
    val BorderSubtle = Color(0xFF243247)

    // Bubbles
    val UserBubble = Color(0xFF1E3A5F)
    val AssistantBubble = Color(0xFF131C2E)
    val CodeBlockBg = Color(0xFF060911)

    // Text & status
    val TextPrimary = Color(0xFFF1F5F9)
    val TextMuted = Color(0xFF94A3B8)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF38BDF8)

    // Layout
    val RadiusS = 8.dp
    val RadiusM = 14.dp
    val RadiusL = 20.dp
    val RadiusPill = 999.dp
    val TitleSize = 20.sp
    val BodySize = 14.sp
    val MonoSize = 12.sp
}

private val DarkScheme = darkColorScheme(
    primary = HermesTokens.Emerald,
    onPrimary = Color.Black,
    primaryContainer = HermesTokens.UserBubble,
    onPrimaryContainer = HermesTokens.TextPrimary,
    secondary = HermesTokens.Cyan,
    onSecondary = Color.Black,
    tertiary = HermesTokens.Amber,
    background = HermesTokens.BgDark,
    onBackground = HermesTokens.TextPrimary,
    surface = HermesTokens.SurfaceDark,
    onSurface = HermesTokens.TextPrimary,
    surfaceVariant = HermesTokens.CardDark,
    onSurfaceVariant = HermesTokens.TextMuted,
    outline = HermesTokens.BorderSubtle,
    error = HermesTokens.Error
)

private val LightScheme = lightColorScheme(
    primary = HermesTokens.EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun HermesTheme(
    dark: Boolean = isSystemInDarkTheme(),
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = when {
        dark && amoled -> DarkScheme.copy(background = Color.Black, surface = Color(0xFF080C14))
        dark -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
