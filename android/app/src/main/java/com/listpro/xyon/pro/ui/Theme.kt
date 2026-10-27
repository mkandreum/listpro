package com.listpro.xyon.pro.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.listpro.xyon.pro.R
import com.listpro.xyon.pro.data.ThemePref

@Immutable
data class ListProColors(
    val ground: Color,
    val surface: Color,
    val surface2: Color,
    val ink: Color,
    val ink2: Color,
    val line: Color,
    val shadow: Color,
    val glass: Color,
    val lime: Color = Color(0xFFC8FF3D),
    val onLime: Color = Color(0xFF0E0E12),
    val down: Color = Color(0xFF13C46B),
    val up: Color = Color(0xFFFF4D3A),
    val focus: Color = Color(0xFF5C7CFF),
    val isDark: Boolean,
)

private val Light = ListProColors(
    ground = Color(0xFFF3F4F1), surface = Color.White, surface2 = Color(0xFFE9EAE5),
    ink = Color(0xFF0E0E12), ink2 = Color(0xFF4B4D55), line = Color(0xFF0E0E12), shadow = Color(0xFF0E0E12),
    glass = Color(0xD6FFFFFF), isDark = false,
)

private val Dark = ListProColors(
    ground = Color(0xFF111116), surface = Color(0xFF1C1C23), surface2 = Color(0xFF26262F),
    ink = Color(0xFFF4F4EE), ink2 = Color(0xFFB4B5BE), line = Color(0xFFF4F4EE), shadow = Color.Black,
    glass = Color(0xD91C1C23), isDark = true,
)

val LocalColors = staticCompositionLocalOf { Light }

/** Trazo y sombra dura del sistema neo-brutalista. */
object Brutal {
    val stroke = 2.5.dp
    val thin = 2.dp
    val lift = 4.dp
    val radius = 18.dp
    val radiusL = 26.dp
    val radiusS = 12.dp
}

@OptIn(ExperimentalTextApi::class)
private fun variable(res: Int, weight: Int) =
    Font(res, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))

val Display = FontFamily(
    variable(R.font.bricolage, 600), variable(R.font.bricolage, 700), variable(R.font.bricolage, 800),
)
val Body = FontFamily(
    variable(R.font.figtree, 400), variable(R.font.figtree, 500), variable(R.font.figtree, 600),
    variable(R.font.figtree, 700), variable(R.font.figtree, 800), variable(R.font.figtree, 900),
)

private val ListProTypography = Typography(
    displayLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 52.sp, lineHeight = 50.sp, letterSpacing = (-0.04).em),
    displayMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 40.sp, letterSpacing = (-0.035).em),
    headlineLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 32.sp, letterSpacing = (-0.03).em),
    headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 28.sp, letterSpacing = (-0.025).em),
    titleLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, lineHeight = 23.sp, letterSpacing = (-0.02).em),
    titleMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, lineHeight = 20.sp, letterSpacing = (-0.015).em),
    bodyLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 14.sp),
)

fun hueColor(hex: String): Color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color(0xFFB9BCC4))

/** Tinta legible sobre el color de un pasillo. */
fun Color.ink(): Color = if (luminance() > 0.18f) Color(0xFF0E0E12) else Color.White

@Composable
fun ListProTheme(pref: ThemePref, content: @Composable () -> Unit) {
    val dark = when (pref) {
        ThemePref.System -> isSystemInDarkTheme()
        ThemePref.Light -> false
        ThemePref.Dark -> true
    }
    val c = if (dark) Dark else Light
    val scheme = if (dark) {
        darkColorScheme(primary = c.lime, onPrimary = c.onLime, background = c.ground, onBackground = c.ink, surface = c.surface, onSurface = c.ink, surfaceVariant = c.surface2, onSurfaceVariant = c.ink2, outline = c.line, error = c.up)
    } else {
        lightColorScheme(primary = c.lime, onPrimary = c.onLime, background = c.ground, onBackground = c.ink, surface = c.surface, onSurface = c.ink, surfaceVariant = c.surface2, onSurfaceVariant = c.ink2, outline = c.line, error = c.up)
    }
    CompositionLocalProvider(LocalColors provides c) {
        MaterialTheme(colorScheme = scheme, typography = ListProTypography, content = content)
    }
}
